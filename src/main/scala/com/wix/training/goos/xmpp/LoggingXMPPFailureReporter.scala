package com.wix.training.goos.xmpp

import java.util.logging.Logger

/**
 * Created by Nimrod Argov on 7/24/14.
 *
 */
class LoggingXMPPFailureReporter(logger: Logger) extends XMPPFailureReporter{
  override def cannotTranslateMessage(auctionId: String, failedMessage: String, exception: Exception): Unit = {
    logger.severe("<" + auctionId + "> Could not translate message \"" + failedMessage + "\" because \"" + exception.toString + "\"")
  }
}
