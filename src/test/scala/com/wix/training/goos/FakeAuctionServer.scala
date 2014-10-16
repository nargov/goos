package com.wix.training.goos

import java.util.concurrent.{ArrayBlockingQueue, TimeUnit}

import com.wix.training.goos.xmpp.XMPPAuction
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack._
import org.specs2.matcher.{Matcher, MustMatchers}
import XMPPAuction._

/**
 *
 * Created by Nimrod Argov on 6/29/14.
 */
class FakeAuctionServer(val itemId: String) extends MustMatchers{

  val ITEM_ID_AS_LOGIN = "auction-%s"
  val AUCTION_RESOURCE = "Auction"
  val XMPP_HOST_NAME = "localhost"
  val AUCTION_PASSWORD = "auction"

  val connection = new XMPPConnection(XMPP_HOST_NAME)
  private var currentChat: Chat = _
  private val messageListener = new SingleMessageListener

  def startSellingItem() {
    connection.connect()
    connection.login(String.format(ITEM_ID_AS_LOGIN, itemId), AUCTION_PASSWORD, AUCTION_RESOURCE)
    connection.getChatManager.addChatListener(new ChatManagerListener {
      override def chatCreated(chat: Chat, createdLocally: Boolean): Unit = {
        currentChat = chat
        chat.addMessageListener(messageListener)
      }
    })
  }

  def sendInvalidMessageContaining(msg: String) = currentChat.sendMessage(msg)

  def receivesMessageMatching(sniperId: String, messageMatcher: Matcher[String]) {
    messageListener.receivesMessage(messageMatcher)
    currentChat.getParticipant must beEqualTo(sniperId)
  }

  def hasReceivedJoinRequestFrom(sniperId: String) { receivesMessageMatching(sniperId, beEqualTo(JOIN_COMMAND_FORMAT)) }

  def announceClosed() { currentChat.sendMessage("SOLVersion: 1.1; Event: CLOSE;") }

  def stop() { connection.disconnect() }

  def reportPrice(price: Int, increment: Int, bidder:String) {
    currentChat.sendMessage(f"SOLVersion: 1.1; Event: PRICE; CurrentPrice: $price%d; Increment: $increment%d; Bidder: $bidder%s;")
  }

  def hasReceivedBid(bid: Int, sniperId: String){ receivesMessageMatching(sniperId, equalTo(BID_COMMAND_FORMAT.format(bid))) }

  class SingleMessageListener extends MessageListener {
    private val messages = new ArrayBlockingQueue[Message](1)

    def processMessage(chat: Chat, message: Message) {
      messages.add(message)
    }

    def receivesMessage(msgMatch: Matcher[String]) {
      val message = messages.poll(5, TimeUnit.SECONDS)
      message must not beNull;
      message.getBody must msgMatch
    }
  }
}
