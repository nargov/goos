package com.wix.training.goos.test.xmpp

import com.wix.training.goos.ApplicationRunner
import com.wix.training.goos.xmpp.{XMPPFailureReporter, AuctionEventListener, AuctionMessageTranslator, PriceSource}
import com.wixpress.common.specs2.JMock
import org.jivesoftware.smack.Chat
import org.jivesoftware.smack.packet.Message
import org.specs2.mutable.Specification

/**
 * Created by Nimrod Argov on 6/30/14.
 *
 */

class AuctionMessageTranslatorTest extends Specification with JMock {

  val unusedChat: Chat = null

  val listener = mock[AuctionEventListener]
  val failureReporter = mock[XMPPFailureReporter]
  val translator = new AuctionMessageTranslator(listener, ApplicationRunner.SNIPER_XMPP_ID, failureReporter)

  "ActionMessageTranslator " should {
    isolated
    "notify auction closed when close message received" in {
      checking {
        oneOf(listener).auctionClosed()
      }

      val message = new Message()
      message.setBody("SOLVersion: 1.1; Event: CLOSE;")

      translator.processMessage(unusedChat, message)
    }

    "notify bid details when current price message received from other bidder" in {
      checking {
        exactly(1).of(listener).currentPrice(192, 7, PriceSource.FromOtherBidder)
      }

      val message = new Message()
      message.setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;")
      translator.processMessage(unusedChat, message)
    }

    "notify bid details when current price message received from sniper" in {
      checking {
        exactly(1).of(listener).currentPrice(234, 5, PriceSource.FromSniper)
      }

      val message = new Message()
      message.setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 234; Increment: 5; Bidder: " + ApplicationRunner.SNIPER_XMPP_ID + ";")
      translator.processMessage(unusedChat, message)
    }

    "notify auction failed when bad message received" in {
      val badMessage: String = "a bad Message"

      checking {
        oneOf(failureReporter).cannotTranslateMessage(`with`(ApplicationRunner.SNIPER_XMPP_ID), `with`(badMessage), `with`(any[Exception]))
        exactly(1).of(listener).auctionFailed()
      }
      val msg = new Message()
      msg.setBody(badMessage)
      translator.processMessage(unusedChat, msg)
    }

    "notify auction failed when event type missing" in {
      checking {
        exactly(1).of(listener).auctionFailed()
        ignoring(failureReporter)
      }
      val message = new Message()
      message.setBody("SOLVersion: 1.1; CurrentPrice: 234; Increment: 5; Bidder: " + ApplicationRunner.SNIPER_XMPP_ID + ";")
      translator.processMessage(unusedChat, message)
    }



    trait Foo {
      def error(str: String)
    }

    val foo = mock[Foo]

    "write message translation failure to log" in {

      checking {
        //oneOf(logger).error("<auction id> Could not translate message \"bad message\" because \"java.lang.Exception: bad\"")
        //        exactly(1).of(logger).error("Boom")
        exactly(1).of(foo).error("Boom")

      }
      foo.error("Boom")
    }

  }
}