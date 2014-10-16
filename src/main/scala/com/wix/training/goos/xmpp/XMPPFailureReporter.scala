package com.wix.training.goos.xmpp

/**
 * Created by Nimrod Argov on 7/24/14.
 *
 */
trait XMPPFailureReporter {
  def cannotTranslateMessage(auctionId: String, failedMessage: String, exception: Exception)
}
