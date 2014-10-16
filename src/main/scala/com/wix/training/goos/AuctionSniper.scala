package com.wix.training.goos

import java.util.EventListener
import javax.swing.SwingUtilities

import com.wix.training.goos.xmpp.{AuctionEventListener, PriceSource}
import PriceSource.{FromOtherBidder, FromSniper}
import com.wix.training.goos.SniperState.whenAuctionClosed
import org.jmock.example.announcer.Announcer

/**
 * Created by Nimrod Argov on 7/6/14.
 *
 */
class AuctionSniper(val item: Item, val auction: Auction) extends AuctionEventListener{
  var snapshot: SniperSnapshot = SniperSnapshot.joining(item.identifier)
  private val sniperListeners = Announcer.to(classOf[SniperListener])


  class SwingThreadSniperListener(snipers: SnipersTableModel) extends SniperListener {
    override def sniperStateChanged(sniperState: SniperSnapshot): Unit = SwingUtilities.invokeLater(new Runnable {
      override def run(): Unit = snipers.sniperStateChanged(sniperState)
    })
  }

  override def auctionClosed() {
    snapshot = snapshot.closed()
    notifyChange()
  }

  def addSniperListener(listener: SniperListener) {
    sniperListeners.addListener(listener)
  }

  private def notifyChange() = sniperListeners.announce().sniperStateChanged(snapshot)

  override def currentPrice(price: Int, increment: Int, source: PriceSource.Value): Unit = {
    source match {
      case FromSniper => snapshot = snapshot.winning(price)
      case FromOtherBidder => {
        val bid: Int = price + increment
        if(item.allowsBid(bid)) {
          auction.bid(bid)
          snapshot = snapshot.bidding(price, bid)
        }
        else {
          snapshot = snapshot.losing(price)
        }
      }
    }
    notifyChange()
  }

  override def auctionFailed(): Unit = {
    snapshot = snapshot.failed()
    notifyChange()
  }
}

case class SniperSnapshot(itemId: String, lastPrice: Int, lastBid: Int, state: SniperState.Value) {
  def bidding(price: Int, bid: Int) = SniperSnapshot(itemId, price, bid, SniperState.BIDDING)
  def winning(price: Int) = SniperSnapshot(itemId, price, lastBid, SniperState.WINNING)
  def losing(price: Int) = SniperSnapshot(itemId, price, lastBid, SniperState.LOSING)
  def closed() = SniperSnapshot(itemId, lastPrice, lastBid, whenAuctionClosed(state))
  def failed() = SniperSnapshot(itemId, 0, 0, SniperState.FAILED)
  def isForSameItemAs(snapshot: SniperSnapshot) = this.itemId == snapshot.itemId
}
object SniperSnapshot {
  def joining(itemId: String) = SniperSnapshot(itemId, 0, 0, SniperState.JOINING)
}

object SniperState extends Enumeration {
  type SniperState = Value
  val JOINING, BIDDING, WINNING, LOSING, LOST, WON, FAILED = Value

  def whenAuctionClosed(state: SniperState) = state match {
    case JOINING | BIDDING | LOSING => LOST
    case WINNING => WON
    case _ => throw new SillyDefect("Auction is already closed.")
  }
}

class SillyDefect(msg: String = "This should never happen", cause: Throwable = null) extends RuntimeException(msg, cause)

trait SniperListener extends EventListener {
  def sniperStateChanged(snapshot: SniperSnapshot)
}

case class Item(identifier: String, stopPrice: Int) {
  def allowsBid(bid: Int): Boolean = bid <= stopPrice
}