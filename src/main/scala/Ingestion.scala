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
import java.io.File
import java.io.PrintWriter
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
      gitCommitsListExec("git clone https://github.com/" + username + "/" + reponame + ".git")
      gitCommitsListExec("cd " + reponame + " && git checkout " + branchname)
    }

    // get commit count and commit hash
    val (shaTime, shaSpace, commitSHAList) = performance {
      gitExec("cd " + reponame + " && git log --pretty=format:'%H' > logSHA.txt")
    }

    // create RDD to make copies of commit objects locally -- one folder per commit object -- abort this if memory is insufficient
    val (rddTime, rddSpace, rdd) = performance {
      val inputRDD = spark.textFile(reponame + "/logSHA.txt")
      //log.info(inputRDD.first())
      //gitExec("cd /home/shilpika/scratch/metrics-dashboard-bash-scala/" + reponame + " && mkdir commits")
      gitExec("mkdir -p " + reponame + "/commits")
      inputRDD.map(sha => {
        log.info("THIS IS SHA " + sha)
        gitExec(s"sh src/main/scala/scratch.sh $sha $reponame $branchname")

        val clocResultRDD = Source.fromFile("commitsMetrics/" + sha + "/clocByFile.txt") getLines ()
        //val cdCommand1 = "cd /home/shilpika/scratch/metrics-dashboard-bash-scala/" + reponame + "/commits &&"
        val cdCommand = "cd commitsMetrics/" + sha + " &&"

        val clocResult = clocResultRDD.filter(_.startsWith("./")).map(clocs => {
          val data = Try(clocs.split(" +")) getOrElse (Array(""))
          data match {
            case Array(filepath, blank, comment, code) =>

              // store the results here
              val filename = filepath.replaceAll("\\.", "")
              val collectionName = filename.replaceFirst("/", "").replaceAll("/", "_")
              val loc = blank.toInt + comment.toInt + code.toInt
              val commitDate = gitCommitsListExec(cdCommand + " git log -1 --pretty=format:'%ci'").stripLineEnd
              val output = raw"""{"date": "$commitDate" ,"commitSha": "$sha","loc": $loc,"filename": "$filepath","sorted": false}""".parseJson

              log.info(output.compactPrint)
              output
            case _ => """{"error":"This is malformed cloc result"}""".parseJson
          }
        })
        val writer = new PrintWriter(new File("/home/shilpika/scratch/metrics-dashboard-bash-scala/" + reponame + "/commits/" + sha + ".txt"))
        clocResult.foreach(x => writer.write(x.compactPrint))
        writer.close()
        gitCommitsListExec(cdCommand + " rm -rf " + sha)
        "/home/shilpika/scratch/metrics-dashboard-bash-scala/" + reponame + "/commits/" + sha + ".txt"
      })
    }
    rdd.saveAsTextFile("finalRES")
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
