package com.wix.training.goos

import com.wix.training.goos.xmpp.AuctionEventListener

/**
 * Created by Nimrod Argov on 7/6/14.
 *
 */
trait Auction {
  def bid(amount: Int)
  def join()
  def addAuctionEventListener(listener: AuctionEventListener)
}

trait AuctionHouse {
  def auctionFor(item: Item): Auction
}