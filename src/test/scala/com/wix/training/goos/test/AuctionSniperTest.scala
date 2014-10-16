package com.wix.training.goos.test

import com.wix.training.goos.SniperState._
import com.wix.training.goos._
import com.wix.training.goos.xmpp.PriceSource
import com.wixpress.common.specs2.JMock
import org.specs2.matcher.Matcher
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

/**
 * Created by Nimrod Argov on 7/6/14.
 *
 */
class AuctionSniperTest extends Specification with JMock {
  val ITEM_ID = "auction-12345"
  val ITEM_ID2 = "auction-23456"
  val sniperListener = mock[SniperListener]
  val auction = mock[Auction]
  val sniper = new AuctionSniper(Item(ITEM_ID, Int.MaxValue), auction)
  val sniper2 = new AuctionSniper(Item(ITEM_ID2, 1000), auction)
  sniper.addSniperListener(sniperListener)
  sniper2.addSniperListener(sniperListener)

  "Auction Sniper" should {
    "report Lost if auction closes immediately" in new TestScope {
      checking {
        atLeast(1).of(sniperListener).sniperStateChanged(`with`(aSniperThatIs(LOST)))
      }
      sniper.auctionClosed()
    }

    "report Lost if auction closes when bidding" in new TestScope {
      checking {
        ignoring(auction)
        allowing(sniperListener).sniperStateChanged(`with`(aSniperThatIs(BIDDING))); `then`(sniperState.is("bidding"))
        atLeast(1).of(sniperListener).sniperStateChanged(`with`(aSniperThatIs(LOST))); when(sniperState.is("bidding"))
      }
      sniper.currentPrice(123, 45, PriceSource.FromOtherBidder)
      sniper.auctionClosed()
    }

    "bid higher and report bidding when new price arrives" in {
      val price = 1001
      val increment = 25
      val bid = price + increment
      checking {
        oneOf(auction).bid(bid)
        atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID, price, bid, BIDDING))
      }

      sniper.currentPrice(price, increment, PriceSource.FromOtherBidder)
    }

    "report is winning when current price comes from sniper" in new TestScope{
      checking {
        ignoring(auction)
        allowing(sniperListener).sniperStateChanged(`with`(aSniperThatIs(BIDDING))); `then`(sniperState.is("bidding"))
        atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 135, 135, WINNING)); when(sniperState.is("bidding"))
      }
      sniper.currentPrice(123, 12, PriceSource.FromOtherBidder)
      sniper.currentPrice(135, 45, PriceSource.FromSniper)
    }

    "report Won if auction closes when winning" in new TestScope {
      checking {
        ignoring(auction)
        allowing(sniperListener).sniperStateChanged(`with`(aSniperThatIs(WINNING))); `then`(sniperState.is("winning"))
        atLeast(1).of(sniperListener).sniperStateChanged(`with`(aSniperThatIs(WON))); when(sniperState.is("winning"))
      }
      sniper.currentPrice(123, 45, PriceSource.FromSniper)
      sniper.auctionClosed()
    }

    "not bid and report losing if subsequent price is above stop price" in new TestScope {
      allowingSniperBidding()
      checking{
        val bid = 123 + 45
        allowing(auction).bid(bid)
        atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID2, 2345, bid, LOSING)); when(sniperState.is("bidding"))
      }
      sniper2.currentPrice(123, 45, PriceSource.FromOtherBidder)
      sniper2.currentPrice(2345, 25, PriceSource.FromOtherBidder)
    }

    "not bid and report losing if first price is above stop price" in new TestScope {
      checking{
        atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID2, 2345, 0, LOSING))
      }
      sniper2.currentPrice(2345, 25, PriceSource.FromOtherBidder)
    }

    "report lost if auction closes when losing" in new TestScope {
      allowingSniperBidding()
      checking{
        ignoring(auction)
        allowing(sniperListener).sniperStateChanged(`with`(aSniperThatIs(LOSING)));`then`(sniperState.is("losing"))
        atLeast(1).of(sniperListener).sniperStateChanged(`with`(aSniperThatIs(LOST))); when(sniperState.is("losing"))
      }
      sniper2.currentPrice(2000, 50, PriceSource.FromOtherBidder)
      sniper2.auctionClosed()
    }

    "not bid and report losing if price after winning is above stop price" in new TestScope {
      checking{
        allowing(sniperListener).sniperStateChanged(`with`(aSniperThatIs(WINNING))); `then`(sniperState.is("winning"))
        atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID2, 2345, 0, LOSING)); when(sniperState.is("winning"))
      }
      sniper2.currentPrice(168, 32, PriceSource.FromSniper)
      sniper2.currentPrice(2345, 50, PriceSource.FromOtherBidder)
    }

    "report failed if auction fails when bidding" in new TestScope {
      ignoringAuction()
      allowingSniperBidding()

      checking{
        atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID, 0, 0, SniperState.FAILED))
      }

      sniper.currentPrice(123, 45, PriceSource.FromOtherBidder)
      sniper.auctionFailed()
    }
  }


  trait TestScope extends Scope {
    val sniperState = states("sniper")

    def aSniperThatIs(state: SniperState.Value): Matcher[SniperSnapshot] = ((_:SniperSnapshot).state == state, s"Sniper is not $state")

    def allowingSniperBidding() = {
      checking{
        allowing(sniperListener).sniperStateChanged(`with`(aSniperThatIs(BIDDING))); then(sniperState.is("bidding"))
      }
    }

    def ignoringAuction() = {
      checking{
        ignoring(auction)
      }
    }
  }
}
