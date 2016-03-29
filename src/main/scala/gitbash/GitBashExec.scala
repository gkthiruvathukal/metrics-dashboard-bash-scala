package edu.luc.cs.metrics.dashboard
package gitbash

import scala.sys.process._

/**
 * Created by Shilpika on 2/2/16.
 */
trait GitBashExec {

  def gitCloneExec(command: String): Int = {
    val exitCode = Process(command).!
    //require(exitCode ==0 , "Git clone Failed: Invalid Username/Reponame")
    exitCode
  }

  def gitCommitsListExec(command: String): String = {
    val exitCode = Seq("/bin/sh", "-c", command).!!
    //require(exitCode ==0 , "Git clone Failed: Invalid Username/Reponame")
    exitCode
  }

  def gitExec(command: String): Int = {
    val exitCode = Seq("/bin/sh", "-c", command).!
    //require(exitCode ==0 , "Git clone Failed: Invalid Username/Reponame")
    exitCode
  }

}
