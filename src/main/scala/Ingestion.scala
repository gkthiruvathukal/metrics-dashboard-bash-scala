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
import spray.json._
import DefaultJsonProtocol._
import squants.time._
import java.io.{ FileOutputStream, File, PrintWriter }
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

    // projects folder cd command
    val cdProjects = "/projects/ExaHDF5/sshilpika"

    // clone the repository locally
    val (cloneTime, cloneSpace, cloneRepo) = performance {
      gitCommitsListExec("cd " + cdProjects + " && git clone https://github.com/" + username + "/" + reponame + ".git")
      gitCommitsListExec("cd " + cdProjects + "/" + reponame + " && git checkout " + branchname)
    }

    // get commit count and commit hash
    val (shaTime, shaSpace, commitSHAList) = performance {
      gitExec("cd " + cdProjects + "/" + reponame + " && git log --pretty=format:'%H' > logSHA.txt")
    }

    // create RDD to make copies of commit objects locally -- one folder per commit object -- abort this if memory is insufficient
    val (rddTime, rddSpace, rdd) = performance {
      val inputRDD = spark.textFile(reponame + "/logSHA.txt")

      gitExec("cd " + cdProjects + "/" + reponame + " && mkdir commits")
      inputRDD.map(sha => {
        log.info("THIS IS SHA " + sha)

        gitExec(s"sh src/main/scala/scratch.sh $sha $reponame $branchname")

        val clocResultFile = Source.fromFile("scratch/sshilpika/" + reponame + "/results/" + sha + "_clocByFile.txt") getLines ()
        //val cdCommand = "cd " + cdProjects +"/" + reponame + "/commits" + " &&"

        val clocResultSorted = clocResultFile.filter(_.startsWith("./")).map(clocs => {
          val data = Try(clocs.split(" +")) getOrElse (Array(""))
          data match {
            case Array(filepath, blank, comment, code) =>

              // store the results here
              val filename = filepath.replaceAll("\\.", "")
              val collectionName = filename.replaceFirst("/", "").replaceAll("/", "_")
              val loc = blank.toInt + comment.toInt + code.toInt
              val commitDate = Source.fromFile("scratch/sshilpika/" + reponame + "/results/" + sha + "_date.txt") getLines () toString () stripLineEnd
              val output = raw"""{"date": "$commitDate" ,"commitSha": "$sha","loc": $loc,"filename": "$filepath","sorted": false}""".parseJson

              log.info(output.compactPrint)
              output
            case _ => raw"""{"error":"This is malformed cloc result for $sha"}""".parseJson
          }
        })
        val writer = new PrintWriter(new FileOutputStream(new File("/projects/ExaHDF5/sshilpika/" + reponame + "/" + sha + ".txt"), true))
        clocResultSorted.foreach(x => writer.append(x.compactPrint))
        writer.close()
        gitCommitsListExec("cd /scratch/sshilpika/" + reponame + "/commitsMetrics && rm -rf " + sha)
        "/projects/ExaHDF5/sshilpika/" + reponame + "/" + sha + ".txt"
      })
    }

    log.info("Statistics")
    log.info("Time")
    log.info(s"git clone time : ${shaTime.milliseconds}")
    log.info(s"git clone time : ${shaSpace.memUsed}")
    //shaSpace.freeMemory
    log.info("DONE")

    spark.stop()
    // gitCommitsListExec(s"rm -rf $reponame")
  }

}
