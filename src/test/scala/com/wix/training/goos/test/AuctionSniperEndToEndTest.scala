package com.wix.training.goos.test

import java.util.concurrent.CountDownLatch

import com.wix.training.goos.ApplicationRunner.SNIPER_XMPP_ID
import com.wix.training.goos.{Item, ApplicationRunner, FakeAuctionServer}
import org.jivesoftware.smack.XMPPConnection
import org.specs2.mutable.{After, Specification}
import org.specs2.specification.Scope

/**
 *
 * Created by Nimrod Argov on 6/26/14.
 */
class AuctionSniperEndToEndTest extends Specification {
  sequential

  "The sniper" should {

    "join an an auction until it closes" in new AuctionScope  {
      auction.startSellingItem()
      application.startBiddingIn(auction)
      auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)
      auction.announceClosed()
      application.showSniperHasLostAuction(auction, 0, 0)
    }

    "make a higher bid but lose" in new AuctionScope {
      auction.startSellingItem()

      application.startBiddingIn(auction)
      auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)

      auction.reportPrice(1000, 98, "other bidder")
      application.hasShownSniperIsBidding(auction, 1000, 1098)

      auction.hasReceivedBid(1098, SNIPER_XMPP_ID)

      auction.announceClosed()
      application.showSniperHasLostAuction(auction, 1000, 1098)
    }

    "win an auction by bidding higher" in new AuctionScope {
      auction.startSellingItem()

      application.startBiddingIn(auction)
      auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)

      auction.reportPrice(1000, 98, "other bidder")
      application.hasShownSniperIsBidding(auction, 1000, 1098)  //last price, last bid

      auction.hasReceivedBid(1098, SNIPER_XMPP_ID)

      auction.reportPrice(1098, 97, SNIPER_XMPP_ID)
      application.hasShownSniperIsWinning(auction, 1098) //winning bid

      auction.announceClosed()
      application.showSniperHasWonAuction(auction, 1098) //last price
    }

    "bid for multiple items" in new AuctionScope {
      auction.startSellingItem()
      auction2.startSellingItem()

      application.startBiddingIn(auction, auction2)
      auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)
      auction2.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)

      auction.reportPrice(1000, 98, "other bidder")
      auction.hasReceivedBid(1098, SNIPER_XMPP_ID)

      auction2.reportPrice(500, 21, "other bidder")
      auction2.hasReceivedBid(521, SNIPER_XMPP_ID)

      auction.reportPrice(1098, 97, SNIPER_XMPP_ID)
      auction2.reportPrice(521, 22, SNIPER_XMPP_ID)

      application.hasShownSniperIsWinning(auction, 1098)
      application.hasShownSniperIsWinning(auction2, 521)

      auction.announceClosed()
      auction2.announceClosed()

      application.showSniperHasWonAuction(auction, 1098)
      application.showSniperHasWonAuction(auction2, 521)
    }

    "lose an auction when the price is too high" in new AuctionScope {
      auction.startSellingItem()
      application.startBiddingWithStopPrice(auction, 1100)
      auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)
      auction.reportPrice(1000, 98, "other bidder")
      application.hasShownSniperIsBidding(auction, 1000, 1098)

      auction.hasReceivedBid(1098, SNIPER_XMPP_ID)

      auction.reportPrice(1197, 10, "third party")
      application.hasShownSniperIsLosing(auction, 1197, 1098)

      auction.announceClosed()
      application.showSniperHasLostAuction(auction, 1197, 1098)
    }

    "reports invalid auction message and stops responding to events" in new AuctionScope {
      val brokenMsg = "a broken message"
      auction.startSellingItem()
      auction2.startSellingItem()

      application.startBiddingIn(auction, auction2)
      auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)

      auction.reportPrice(500, 20, "other bidder")
      auction.hasReceivedBid(520, SNIPER_XMPP_ID)

      auction.sendInvalidMessageContaining(brokenMsg)
      application.showsSniperHasFailed(auction)

      auction.reportPrice(520, 21, "other bidder")
      waitForAnotherAuctionEvent()

      application.reportsInvalidMessage(auction, brokenMsg)
      application.showsSniperHasFailed(auction)

      def waitForAnotherAuctionEvent() = {
        auction2.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)
        auction2.reportPrice(600, 6, "other bidder")
        application.hasShownSniperIsBidding(auction2, 600, 606)
      }
    }
  }

  trait AuctionScope extends Scope with After {
    val auction = new FakeAuctionServer("item54321")
    val auction2 = new FakeAuctionServer("item65432")
    val application = new ApplicationRunner()

    def after = {
      auction.stop()
      auction2.stop()
      application.stop()
    }
  }
}

