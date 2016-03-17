/**
  * Created by Shilpika on 1/30/16.
  */
package edu.luc.cs.metrics.dashboard.common


import org.apache.spark.{SparkConf, SparkContext}
import gitbash._
import scala.sys.process._

object KlocSysScala extends IO with GitBashExec{

  def main(args:Array[String]): Unit ={

    val lines = scala.io.Source.stdin.getLines
    val header = lines.next()
    val cloneCommand = parseLine(header)
    //val cloneCommand = gitCloneCommand()
    val gitCloneExitCode = gitCloneExec(cloneCommand)
    //println(gitCloneExitCode+"gitCloneExitCode")

    val gitLogCode = gitCommitsListExec(gitCommitsList("literalinclude-scala"))
    println("GIT LOG "+gitLogCode)
    println("DELETINF")
    //val deleteCode = Seq("/bin/sh","-c","rm -rf literalinclude-scala").!!
    //println(deleteCode+" DCODE")


    val exitCode = Seq("/bin/sh", "-c", "cd literalinclude-scala && git log --shortstat --pretty=format:\"metrics@cs.luc.edu %H %cI metrics-dash %an - %ad - %cn - %cd - %s\" | grep \"metrics@cs.luc.edu \" | " +
      "awk '{gsub(\"metrics@cs.luc.edu\",\"\");print}' > log1.txt").!!

    val conf = new SparkConf().setMaster("local").setAppName("Spark Git Checkout")
    val sc = new SparkContext(conf)
    val gitLogSpark = sc.textFile("literalinclude-scala/log1.txt")
    println("XXXXX")

    val result = gitLogSpark.foreach(x =>{
      println("This is X!!!!!!"+x)
      val y = x.split(" ")
      val sha = y(1)
      val date = y(2)
      println("This is the SHA code:::"+sha)
      println("This is the the commit date:::"+date)
      //val checkout = Seq("/bin/sh", "-c", "cd literalinclude-scala && git checkout "+y).!
      Seq("/bin/sh", "-c", "cd literalinclude-scala && git checkout "+sha+ " && cloc --by-file --report_file=res2.txt ../literalinclude-scala/ && grep \"../\" res2.txt >> ~/log100.txt").! //&& " +
        //"awk '{gsub(\"SUM \",\"\");print}' >> log100.txt").!!
      //val gitLogStore = sc.textFile("~/log100.txt")
      //println("THE ZZZZZ "+z)
    })
   // result


  }

}
