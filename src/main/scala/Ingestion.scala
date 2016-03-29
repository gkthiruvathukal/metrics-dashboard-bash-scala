/**
 * Created by Shilpika on 3/28/16.
 */

package edu.luc.cs.metrics.dashboard

import com.mongodb.casbah.Imports._
import org.apache.spark._
import scala.sys.process._
import scala.io.Source
import scala.util.Try
import org.slf4j.Logger;
import org.slf4j.LoggerFactory

object Ingestion extends gitbash.GitBashExec {

  case class Config(username: Option[String] = None, repository: Option[String] = None, branchname: Option[String] = None)

  def parseCommandLine(args: Array[String]): Option[Config] = {
    val parser = new scopt.OptionParser[Config]("scopt") {
      head("Ingest", "1.0")
      opt[String]('u', "username") action { (x, c) =>
        c.copy(username = Some(x))
      } text ("username is a String property")
      opt[String]('r', "repository") action { (x, c) =>
        c.copy(repository = Some(x))
      } text ("repository is a String property")
      opt[String]('b', "branchname") action { (x, c) =>
        c.copy(branchname = Some(x))
      } text ("branchname is an Int property")
      help("help") text ("Usage: -u [username] -r [reponame] -b [branchname]")

    }
    // parser.parse returns Option[C]
    parser.parse(args, Config())
  }

  def main(args: Array[String]) {
    val log = LoggerFactory.getLogger(Ingestion.getClass)
    val conf = new SparkConf().setAppName("LineCount File I/O")
    val spark = new SparkContext(conf)

    // getting the values for ingestion using scopt
    val appConfig = parseCommandLine(args).getOrElse(Config())
    val username = appConfig.username.getOrElse("")
    val reponame = appConfig.repository.getOrElse("")
    val branchname = appConfig.branchname.getOrElse("master")

    // clone the repository locally
    val (cloneTime, cloneSpace, cloneRepo) = performance {
      gitCloneExec("git clone https://github.com/" + username + "/" + reponame + ".git")
      gitCommitsListExec("cd " + reponame + " && git checkout " + branchname)
    }

    // get commit count and commit hash
    val (shaTime, shaSpace, commitSHAList) = performance {
      gitExec("cd " + reponame + " && git log --pretty=format:'%H' > logSHA.txt")
    }

    // create RDD to make copies of commit objects locally -- one folder per commit object -- abort this if memory is insufficient
    val (rddTime, rddSpace, rdd) = performance {
      val inputRDD = spark.textFile(reponame + "/logSHA.txt")
      log.info(inputRDD.first())
      inputRDD.foreach(sha => {
        log.info("THIS IS SHA " + sha)
        //create sha dir in commits/ inside the cloned repository
        gitExec("cd " + reponame + " && mkdir -p commits/" + sha)
        // git checkout into sha directory
        val cdCommand = "cd " + reponame + "/commits/" + sha + " &&"
        gitExec(cdCommand + " git init")
        gitExec(cdCommand + " git remote add parentNode ../../") // remote add to repo being tracked
        gitExec(cdCommand + " git pull parentNode " + branchname)
        gitExec(cdCommand + " git reset --hard " + sha)
        // perform distributed line counting using cloc per file and print all information obtained
        gitExec(cdCommand + " cloc --by-file --report_file=clocByFile.txt .")
      })
    }

    // parse the cloc result and store in MongoDB
    val (storeTime, storeSpace, storerdd) = performance {
      val inputRDDforStore = spark.textFile(reponame + "/logSHA.txt")

      inputRDDforStore.foreach(sha => {

        //for each sha folder - create another RDD to read the cloc result and store in the DB
        val clocResultRDD = Source.fromFile(reponame + "/commits/" + sha + "/clocByFile.txt") getLines ()
        val cdCommand = "cd " + reponame + "/commits/" + sha + " &&"

        clocResultRDD.filter(_.startsWith("./")).foreach(clocs => {
          val data = Try(clocs.split(" +")) getOrElse (Array(""))
          data match {
            case Array(filepath, blank, comment, code) =>

              // store the results in MongoDB here
              val filename = filepath.replaceAll("\\.", "")
              val collectionName = filename.replaceFirst("/", "").replaceAll("/", "_")
              val mongoClient = MongoClient("localhost", 27017)
              val db = mongoClient(username + "_" + reponame + "_" + branchname)
              val collection = db(collectionName)
              val commitDate = gitCommitsListExec(cdCommand + " git log -1 --pretty=format:'%ci'")
              //collection.insert(MongoDBObject("date" -> commitDate))
              collection.update(MongoDBObject("date" -> commitDate), $set(
                "commitSha" -> sha,
                "loc" -> (blank.toInt + comment.toInt + code.toInt).toString, "filename" -> filepath, "sorted" -> false
              ), true, true)
              log.info(commitDate)
            case _ => "This is malformed cloc result"
          }
        })
      })
    }

    /*val (rddTime, rddSpace, rdd) = performance {
      spark.parallelize(fileList, slices).map {
        fileName => countLinesInFile(fileName)
      }
    }



    val (computeTimeDetails, computeSpaceDetails, text) = performance {
      rdd.map { fileInfo => fileInfo.toString + "\n" } reduce (_ + _)
    }

    // prepare data to be saved in MongoDB


    // save information in MongoDB



    // perform distributed line counting and sum up all individual times to get
    // an idea of the actual workload of reading all files serially

    val (computeIndividualTime, computeIndividualSpace, sumIndividualTime) = performance {
      rdd map { _.time } reduce (_ + _)
    }

    // This shows how to count the number of unique hosts involved
    // in the computation.

    val pairs = rdd.map(lc => (lc.hostname, 1))
    val counts = pairs.reduceByKey((a, b) => a + b)

    // perform distributed line counting but only project the total line count

    val (computeTime, computeSpace, sumLineCount) = performance {
      rdd map { _.lineCount } reduce (_ + _)
    }

    // TODO: Get this into CSV form or something better for analysis

    log.info("Nodes Used")
    log.info(counts.count())
    counts.collect() foreach log.info

    log.info("File Line Counts")
    log.info(text)

    log.info("Results")
    log.info(s"fileList.length=${fileList.length}")
    log.info(s"sumLineCount=$sumLineCount")

    log.info("Statistics")
    log.info(s"rddTime=${rddTime.time in Milliseconds}")
    log.info(s"lsTime=${lsTime.time in Milliseconds}")
    log.info(s"computeTime=${computeTime.time in Milliseconds}")
    log.info(s"sumIndividualTime=${sumIndividualTime.time in Milliseconds}")

    log.info("Quick look at memory on each Spark node")
    log.info(rddSpace.free)*/
    // spark.stop()
  }

}
