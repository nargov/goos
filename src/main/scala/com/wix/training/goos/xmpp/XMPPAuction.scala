package com.wix.training.goos.xmpp

import com.wix.training.goos.{Item, Auction}
import com.wix.training.goos.xmpp.XMPPAuction._
import org.jivesoftware.smack.{Chat, XMPPConnection}
import org.jmock.example.announcer.Announcer

/**
 * Created by Nimrod Argov on 7/6/14.
 *
 */
class XMPPAuction(con: XMPPConnection, itemId: String, failureReporter: XMPPFailureReporter) extends Auction {
  private val auctionEventListeners = Announcer.to(classOf[AuctionEventListener])
  private val chat: Chat = con.getChatManager.createChat(auctionId(itemId, con), translatorFor(con))
  val translator = translatorFor(con)

  def chatDisconnectorFor(translator: AuctionMessageTranslator) = new AuctionEventListener {
    override def auctionFailed(): Unit = chat.removeMessageListener(translator)

    override def currentPrice(price: Int, increment: Int, source: PriceSource.Value): Unit = {}

    override def auctionClosed(): Unit = {}
  }

  def translatorFor(con: XMPPConnection) = new AuctionMessageTranslator(auctionEventListeners.announce(), con.getUser, failureReporter)

  def addAuctionEventListener(listener: AuctionEventListener) = auctionEventListeners.addListener(listener)

  def sendMessage(message: String) = chat.sendMessage(message)

  override def bid(amount: Int) = sendMessage(BID_COMMAND_FORMAT format amount)

  override def join() = sendMessage(JOIN_COMMAND_FORMAT)
}

object XMPPAuction {
  val AUCTION_RESOURCE = "Auction"
  val ITEM_ID_AS_LOGIN = "auction-%s"
  val AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE
  val JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN; "
  val BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;"

  private def auctionId(itemId: String, con: XMPPConnection): String = String.format(AUCTION_ID_FORMAT, itemId, con.getServiceName)
}
