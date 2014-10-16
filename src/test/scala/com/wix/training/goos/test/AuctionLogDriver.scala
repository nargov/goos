package com.wix.training.goos.test

import java.io.File
import java.util.logging.LogManager

import com.wix.training.goos.xmpp.XMPPAuctionHouse
import org.apache.commons.io.FileUtils
import org.specs2.matcher.{MustMatchers, Matcher}

/**
 * Created by Nimrod Argov on 7/24/14.
 *
 */
class AuctionLogDriver extends MustMatchers{

  private val logFile = new File(XMPPAuctionHouse.logFileName)

  def hasEntry(matcher: Matcher[String]) = FileUtils.readFileToString(logFile) must matcher

  def clearLog() = {
    logFile.delete()
    LogManager.getLogManager.reset()
  }
}