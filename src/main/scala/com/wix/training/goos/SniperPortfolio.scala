package com.wix.training.goos

import java.util.EventListener

import org.jmock.example.announcer.Announcer

/**
 * Created by Nimrod Argov on 7/15/14.
 *
 */
trait PortfolioListener extends EventListener {
  def sniperAdded(sniper: AuctionSniper)
}



class SniperPortfolio extends SniperCollector {
  var snipers = Seq[AuctionSniper]()
  val listeners = Announcer.to(classOf[PortfolioListener])

  def addPortfolioListener(listener: PortfolioListener) = listeners.addListener(listener)

  override def addSniper(sniper: AuctionSniper): Unit = {
    snipers = snipers :+ sniper
    listeners.announce().sniperAdded(sniper)
  }
}
