package com.wix.training.goos.test.xmpp

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS

import com.wix.training.goos._
import com.wix.training.goos.xmpp._
import com.wixpress.common.specs2.JMock
import org.specs2.mutable.{After, Specification}

/**
 * Created by Nimrod Argov on 7/14/14.
 *
 */
class XMPPAuctionTest extends Specification with JMock {
  val failuerReporter = mock[XMPPFailureReporter]

  trait TestScope extends After {
    val auctionServer = new FakeAuctionServer("item54321")
    auctionServer.startSellingItem()

    def after = {
      auctionServer.stop()
    }
  }

  "XMPPAuction" should {
    "receive events from auction server after joining" in new TestScope {
      val auctionWasClosed = new CountDownLatch(1)

      val auction = new XMPPAuction(auctionServer.connection, auctionServer.itemId, failuerReporter)
      auction.addAuctionEventListener(auctionClosedListener(auctionWasClosed))
      auction.join()

      auctionServer.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID)
      auctionServer.announceClosed()

      auctionWasClosed.await(2, SECONDS) must beTrue
    }
  }

  def auctionClosedListener(auctionWasClose: CountDownLatch): AuctionEventListener = new AuctionEventListener {

    override def currentPrice(price: Int, increment: Int, source: PriceSource.Value): Unit = {}

    override def auctionClosed(): Unit = auctionWasClose.countDown()

    override def auctionFailed(): Unit = ???
  }
}
