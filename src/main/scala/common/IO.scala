package edu.luc.cs.metrics.dashboard.common

import scala.sys.process.Process

/**
 * Created by Shilpika on 2/1/16.
 */
case class Creds(username: String, reponame: String, branch: String)
trait IO {

  def parseLine(header: String): String = {

    val cols = new java.util.StringTokenizer(header)
    require(cols.countTokens() > 2, "Usage: [username] [reponame] [branchname]")
    val username = cols.nextElement().toString
    val reponame = cols.nextElement().toString
    val branchname = cols.nextElement().toString
    //Creds(username,reponame, branchname)

    /*val gitCommand = "git clone https://github.com/"+username+"/"+reponame+".git"
    val process = Process(gitCommand).!
    println("PROCESS"+process)
    require(process == 0 , "Invalid username/reponame")*/

    "git clone https://github.com/" + username + "/" + reponame + ".git"

  }

  def gitCloneCommand(username: String, reponame: String): String = {
    "git clone https://github.com/" + username + "/" + reponame + ".git"
  }

  def gitCommitsList(reponame: String): String = {
    "cd " + reponame + " && git log | grep \"commit \" | awk '{gsub(\"commit \" , \"\");print}' > log.txt"
  }

}
