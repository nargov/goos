package com.wix.training.goos.xmpp

import java.util.logging.{SimpleFormatter, FileHandler, Handler, Logger}

import com.wix.training.goos.{Item, Auction, AuctionHouse}
import org.jivesoftware.smack.XMPPConnection

import scala.util.Try

/**
 * Created by Nimrod Argov on 7/14/14.
 *
 */
class XMPPAuctionHouse private (con: XMPPConnection, username: String, password: String) extends AuctionHouse {
  con.connect()
  con.login(username, password, XMPPAuction.AUCTION_RESOURCE)

  def simpleFileHandler(): Handler = {
    Try {
      val handler = new FileHandler(XMPPAuctionHouse.logFileName)
      handler.setFormatter(new SimpleFormatter)
      handler
    } recover {
      case e: Exception => throw new XMPPAuctionException("could not create logger FileHandler", e)
    } get
  }

  private def makeLogger(): Logger = {
    val logger = Logger.getLogger(XMPPAuctionHouse.loggerName)
    logger.setUseParentHandlers(false)
    logger.addHandler(simpleFileHandler())
    logger
  }

  val failureReporter = new LoggingXMPPFailureReporter(makeLogger())

  override def auctionFor(item: Item): Auction = new XMPPAuction(con, item.identifier, failureReporter)
  def disconnect() = con.disconnect()
}
object XMPPAuctionHouse {
  val logFileName = "auction-sniper.log"
  val loggerName = "auction-sniper-log"
  def connect(hostname: String, username: String, password: String): XMPPAuctionHouse = new XMPPAuctionHouse(new XMPPConnection(hostname), username, password)
}
