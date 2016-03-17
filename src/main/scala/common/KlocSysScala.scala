/**
  * Created by Shilpika on 1/30/16.
  */
package edu.luc.cs.metrics.dashboard.common


import org.apache.spark.{SparkConf, SparkContext}
import gitbash._
import scala.sys.process._

object KlocSysScala extends IO with GitBashExec{

  def main(args:Array[String]): Unit ={
    val t1 = System.currentTimeMillis()
    val lines = scala.io.Source.stdin.getLines
    val header = lines.next()
    val cloneCommand = parseLine(header)
    //val cloneCommand = gitCloneCommand()
    val gitCloneExitCode = gitCloneExec(cloneCommand)
    //println(gitCloneExitCode+"gitCloneExitCode")

    val gitLogCode = gitCommitsListExec(gitCommitsList("linux"))
    println("GIT LOG "+gitLogCode)
    println("DELETINF")
    //val deleteCode = Seq("/bin/sh","-c","rm -rf linux").!!
    //println(deleteCode+" DCODE")


    val exitCode = Seq("/bin/sh", "-c", "cd linux && git log --shortstat --pretty=format:\"metrics@cs.luc.edu %H %cI metrics-dash %an - %ad - %cn - %cd - %s\" | grep \"metrics@cs.luc.edu \" | " +
      "awk '{gsub(\"metrics@cs.luc.edu\",\"\");print}' > log1.txt").!!

    val conf = new SparkConf().setMaster("local").setAppName("Spark Git Checkout")
    val sc = new SparkContext(conf)
    val gitLogSpark = sc.textFile("linux/log1.txt")
    println("XXXXX")

    val result = gitLogSpark.foreach(x =>{
      println("This is X!!!!!!"+x)
      val y = x.split(" ")
      val sha = y(1)
      val date = y(2)
      println("This is the SHA code:::"+sha)
      println("This is the the commit date:::"+date)

      //val checkout = Seq("/bin/sh", "-c", "cd linux && git checkout "+y).!
      Seq("/bin/sh", "-c", "cd linux && git checkout "+sha+ " && cloc --by-file --report_file=res2.txt ../linux/ && grep \"../\" res2.txt >> ~/log100.txt && echo "+date+" >> ~/log100.txt" ).! //&& " +
        //"awk '{gsub(\"SUM \",\"\");print}' >> log100.txt").!!
      //Seq("/bin/sh", "-c", "cd linux && echo "+date+" >>~/log100.txt").!
      //val gitLogStore = sc.textFile("~/log100.txt")
      //println("THE ZZZZZ "+z)
    })
    val deleteCode = Seq("/bin/sh","-c","rm -rf linux").!!
   // result
    println("THE TOTAL COMPUTATION TIME IS :"+(System.currentTimeMillis()-t1))


  }

}
