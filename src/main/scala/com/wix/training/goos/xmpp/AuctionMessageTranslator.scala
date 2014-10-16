package com.wix.training.goos.xmpp

import java.util.EventListener

import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.{Chat, MessageListener}

import scala.util.Try

/**
 * Created by Nimrod Argov on 6/30/14.
 *
 */
class AuctionMessageTranslator(listener: AuctionEventListener, sniperId: String, failureReporter: XMPPFailureReporter) extends MessageListener {
  override def processMessage(chat: Chat, message: Message) {
    Try(translate(message)).recover({
      case e:RuntimeException =>
        failureReporter.cannotTranslateMessage(sniperId, message.getBody, e)
        listener.auctionFailed()
    })
  }

  def translate(message: Message) {
    val event = AuctionEvent.from(message.getBody)
    event.eventType match {
      case "CLOSE" => listener.auctionClosed()
      case "PRICE" => listener.currentPrice(event.currentPrice, event.increment, event.isFrom(sniperId))
    }
  }

  import AuctionEvent._
  class AuctionEvent {
    private val values = scala.collection.mutable.Map[String, String]()

    def eventType = get(EVENT)
    def currentPrice: Int = getInt(CURRENT_PRICE)
    def increment: Int = getInt(INCREMENT)
    def isFrom(sniperId: String) = if(get(BIDDER) == sniperId) PriceSource.FromSniper else PriceSource.FromOtherBidder

    private def get(fieldName: String) = values(fieldName) ensuring(v => v != null && v.length > 0)
    private def getInt(fieldName: String) = get(fieldName).toInt
    private def addField(field: String) {
      val pair: Array[String] = field.split(":")
      values += pair(0).trim -> pair(1).trim
    }
  }
  object AuctionEvent {
    val EVENT = "Event"
    val CURRENT_PRICE = "CurrentPrice"
    val INCREMENT = "Increment"
    val BIDDER = "Bidder"

    def from(messageBody: String) = {
      val event = new AuctionEvent
      messageBody.split(";").foreach(field => event.addField(field))
      event
    }
  }
}

trait AuctionEventListener extends EventListener{
  def auctionClosed(): Unit
  def currentPrice(price: Int, increment: Int, source: PriceSource.Value)
  def auctionFailed()
}

object PriceSource extends Enumeration {
  type PriceSource = Value
  val FromSniper, FromOtherBidder = Value
}
