/**
 * Created by Shilpika on 1/30/16.
 */
package edu.luc.cs.metrics.dashboard
package common

import org.apache.spark.{ SparkConf, SparkContext }

import scala.sys.process._

object KlocSysScala extends IO with gitbash.GitBashExec {

  case class Config(dim: Option[Int] = None, nodes: Option[Int] = None, partitions: Option[Int] = None, workload: Option[Int] = None, outputDir: Option[String] = None, cacheRdd: Boolean = false, cloc: Option[String] = None)

  val DEFAULT_DIMENSION = 2048
  val DEFAULT_NODES = 4
  val DEFAULT_PARTITIONS = 48
  val DEFAULT_WORKLOAD = DEFAULT_NODES * DEFAULT_PARTITIONS
  val DEFAULT_OUTPUT_DIR = "."
  val DEFAULT_CACHE_POLICY = false
  val DEFAULT_CLOC_DIR = "/home/thiruvat/code/cloc"

  def main2(args: Array[String]) {
    //case class Data(array: Array[DenseMatrix[Double]], time: Time, space: Space, hostname: String)

    val t1 = System.currentTimeMillis()
    val lines = scala.io.Source.stdin.getLines
    val header = lines.next()
    val cloneCommand = parseLine(header)
    //val cloneCommand = gitCloneCommand()
    val gitCloneExitCode = gitCloneExec(cloneCommand)
    //println(gitCloneExitCode+"gitCloneExitCode")

    val gitLogCode = gitCommitsListExec(gitCommitsList("literalinclude-scala"))
    println("GIT LOG " + gitLogCode)
    println("DELETINF")
    //val deleteCode = Seq("/bin/sh","-c","rm -rf literalinclude-scala").!!
    //println(deleteCode+" DCODE")

    val exitCode = Seq("/bin/sh", "-c", "cd literalinclude-scala && git log --shortstat --pretty=format:\"metrics@cs.luc.edu %H %cI metrics-dash %an - %ad - %cn - %cd - %s\" | grep \"metrics@cs.luc.edu \" | " +
      "awk '{gsub(\"metrics@cs.luc.edu\",\"\");print}' > log1.txt").!!

    val conf = new SparkConf().setMaster("local").setAppName("Spark Git Checkout")
    val sc = new SparkContext(conf)
    val gitLogSpark = sc.textFile("literalinclude-scala/log1.txt")
    println("XXXXX")

    val result = gitLogSpark.foreach(x => {
      println("This is X!!!!!!" + x)
      val y = x.split(" ")
      val sha = y(1)
      val date = y(2)
      println("This is the SHA code:::" + sha)
      println("This is the the commit date:::" + date)

      //val checkout = Seq("/bin/sh", "-c", "cd literalinclude-scala && git checkout "+y).!
      Seq("/bin/sh", "-c", "cd literalinclude-scala && mkdir literalinclude-scala-" + x + " && cd literalinclude-scala-" + x +
        " && mkdir .git/ && rsync -r ../.git/ .git/ && git checkout " + sha + " && cloc --by-file --report_file=res2.txt . && grep \"../\" res2.txt >> ~/log100.txt && echo " + date + " >> ~/log100.txt").! //&& " +
      //"awk '{gsub(\"SUM \",\"\");print}' >> log100.txt").!!
      //Seq("/bin/sh", "-c", "cd literalinclude-scala && echo "+date+" >>~/log100.txt").!
      //val gitLogStore = sc.textFile("~/log100.txt")
      //println("THE ZZZZZ "+z)
    })
    val deleteCode = Seq("/bin/sh", "-c", "rm -rf literalinclude-scala").!!
    // result
    println("THE TOTAL COMPUTATION TIME IS :" + (System.currentTimeMillis() - t1))

  }

  def parseCommandLine(args: Array[String]): Option[Config] = {
    val parser = new scopt.OptionParser[Config]("scopt") {
      head("breeze-spark-demo", "1.0")
      opt[Int]('d', "dim") action { (x, c) =>
        c.copy(dim = Some(x))
      } text (s"dim is the matrix dimension (default = $DEFAULT_NODES)")

      opt[Int]('p', "partitions") action { (x, c) =>
        c.copy(partitions = Some(x))
      } text (s"partitions is RDD partition size (default = $DEFAULT_PARTITIONS)")

      opt[Int]('n', "nodes") action { (x, c) =>
        c.copy(nodes = Some(x))
      } text (s"nodes is the number of cluster nodes being used (default $DEFAULT_NODES)")

      opt[Int]('w', "workload") action { (x, c) =>
        c.copy(workload = Some(x))
      } text (s"workload is number of matrix operations to do (default $DEFAULT_NODES * $DEFAULT_PARTITIONS)")

      opt[String]('o', "outputdir") action { (x, c) =>
        c.copy(outputDir = Some(x))
      } text (s"outputDir is where to write the benchmark results (default $DEFAULT_OUTPUT_DIR)")

      opt[Boolean]('r', "cacherdd") action { (x, c) =>
        c.copy(cacheRdd = true)
      } text (s"cache the RDD (default is $DEFAULT_CACHE_POLICY)")

      opt[String]('c', "cloc") action { (x, c) =>
        c.copy(cloc = Some(x))
      } text (s"outputDir is where to write the benchmark results (default $DEFAULT_CLOC_DIR)")

      help("help") text ("prints this usage text")

    }
    // parser.parse returns Option[C]
    parser.parse(args, Config())
  }

}
