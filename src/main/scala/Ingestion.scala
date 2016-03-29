/**
 * Created by Shilpika on 3/28/16.
 */

package edu.luc.cs.metrics.dashboard

import org.apache.spark._
import scala.sys.process._

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

    /*val (rddTime, rddSpace, rdd) = performance {
      spark.parallelize(fileList, slices).map {
        fileName => countLinesInFile(fileName)
      }
    }

    // perform distributed line counting using cloc per file and print all information obtained

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

    println("Nodes Used")
    println(counts.count())
    counts.collect() foreach println

    println("File Line Counts")
    println(text)

    println("Results")
    println(s"fileList.length=${fileList.length}")
    println(s"sumLineCount=$sumLineCount")

    println("Statistics")
    println(s"rddTime=${rddTime.time in Milliseconds}")
    println(s"lsTime=${lsTime.time in Milliseconds}")
    println(s"computeTime=${computeTime.time in Milliseconds}")
    println(s"sumIndividualTime=${sumIndividualTime.time in Milliseconds}")

    println("Quick look at memory on each Spark node")
    println(rddSpace.free)*/
    // spark.stop()
  }

}
