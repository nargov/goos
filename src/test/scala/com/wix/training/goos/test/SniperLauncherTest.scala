package com.wix.training.goos.test

import com.wix.training.goos._
import com.wixpress.common.specs2.JMock
import org.specs2.matcher.Matcher
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

/**
 * Created by Nimrod Argov on 7/15/14.
 *
 */
class SniperLauncherTest extends Specification with JMock{

  trait Context extends Scope {
    val auctionState = states("auction state").startsAs("not joined")
    val auctionHouse = mock[AuctionHouse]
    val auction = mock[Auction]
    val sniperCollector = mock[SniperCollector]
    val launcher = new SniperLauncher(auctionHouse, sniperCollector)
  }

  "A SniperLauncher" should {
    "add new sniper to collector and then join auction" in new Context {
      val item = Item("item123", Int.MaxValue)
      checking {
        allowing(auctionHouse).auctionFor(item); will(returnValue(auction))
        oneOf(auction).addAuctionEventListener(`with`(sniperForItem(item))); when(auctionState.is("not joined"))
        oneOf(sniperCollector).addSniper(`with`(sniperForItem(item))); when(auctionState.is("not joined"))
        oneOf(auction).join(); then(auctionState.is("joined"))
      }
      launcher.joinAuction(item)
    }
  }

  def sniperForItem(item: Item): Matcher[AuctionSniper] = (a: AuctionSniper) => (a.item.identifier == item.identifier, "sniper item id doesn't match")

}
