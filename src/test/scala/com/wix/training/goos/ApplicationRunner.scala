package com.wix.training.goos

import com.wix.training.goos.ApplicationRunner.arguments
import com.wix.training.goos.SniperState._
import com.wix.training.goos.SnipersTableModel.textFor
import com.wix.training.goos.test.AuctionLogDriver
import org.specs2.matcher.MustMatchers

/**
 *
 * Created by Nimrod Argov on 6/26/14.
 */
class ApplicationRunner extends MustMatchers{

  private var driver: AuctionSniperDriver = _
  private val logDriver = new AuctionLogDriver

  def startBiddingIn(auctions: FakeAuctionServer*) {
    startSniper()
    auctions.map(auction => {
      val item: Item = Item(auction.itemId, Int.MaxValue)
      driver.startBiddingFor(item)
      driver.showsSniperStatus(item.identifier, 0, 0, textFor(JOINING))
    })
  }

  def startBiddingWithStopPrice(auction: FakeAuctionServer, stopPrice: Int) = {
    startSniper()
    val item = Item(auction.itemId, stopPrice)
    driver.startBiddingFor(item)
    driver.showsSniperStatus(item.identifier, 0, 0, textFor(JOINING))
  }

  def startSniper() {
    logDriver.clearLog()
    val thread = new Thread(new Runnable {
      override def run() {
        new Main(arguments(): _*)
      }
    })

    thread.setDaemon(true)
    thread.start()

    driver = new AuctionSniperDriver(1000)
    driver.hasTitle(MainWindow.APP_TITLE)
    driver.hasColumnTitles()
  }

  def hasShownSniperIsBidding(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int) = {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastBid, textFor(BIDDING))
  }

  def hasShownSniperIsWinning(auction: FakeAuctionServer, winningBid: Int) = {
    driver.showsSniperStatus(auction.itemId, winningBid, winningBid, textFor(WINNING))
  }

  def hasShownSniperIsLosing(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int) = {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastBid, textFor(LOSING))
  }

  def showSniperHasLostAuction(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int) = {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastBid, textFor(LOST))
  }

  def showSniperHasWonAuction(auction: FakeAuctionServer, lastPrice: Int) = {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastPrice, textFor(WON))
  }

  def showsSniperHasFailed(auction: FakeAuctionServer) = driver.showsSniperStatus(auction.itemId, 0, 0, textFor(FAILED))

  def reportsInvalidMessage(auction: FakeAuctionServer, brokenMsg: String) = {
    auction.sendInvalidMessageContaining(brokenMsg)
    logDriver.hasEntry(contain(brokenMsg))
  }

  def stop() = { if (driver != null) driver.dispose() }
}

object ApplicationRunner {
  val SNIPER_ID = "sniper"
  val SNIPER_PASSWORD = "sniper"
  val XMPP_HOSTNAME = "localhost"
  val SNIPER_XMPP_ID = SNIPER_ID + "@" + XMPP_HOSTNAME + "/Auction"

  protected def arguments(): List[String] = XMPP_HOSTNAME :: SNIPER_ID :: SNIPER_PASSWORD :: Nil
}
