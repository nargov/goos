package com.wix.training.goos

import javax.swing.SwingUtilities

/**
 * Created by Nimrod Argov on 7/15/14.
 *
 */
class SniperLauncher(val auctionHouse: AuctionHouse, val collector: SniperCollector) extends UserRequestListener{
  var notToBeGCd: List[Auction] = Nil

  def joinAuction(item: Item): Unit = {
    val auction = auctionHouse.auctionFor(item)
    val sniper = new AuctionSniper(item, auction)
    auction.addAuctionEventListener(sniper)
    collector.addSniper(sniper)
    auction.join()
  }
}

class SwingThreadSniperListener(snipers: SnipersTableModel) extends SniperListener {
  override def sniperStateChanged(sniperState: SniperSnapshot): Unit = SwingUtilities.invokeLater(new Runnable {
    override def run(): Unit = snipers.sniperStateChanged(sniperState)
  })
}
