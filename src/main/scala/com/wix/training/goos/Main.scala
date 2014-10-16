package com.wix.training.goos

import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing.SwingUtilities

import com.wix.training.goos.Main._
import com.wix.training.goos.xmpp.XMPPAuctionHouse

/**
 * Created by Nimrod Argov on 6/29/14.
 *
 */
object Main {
  val ARG_HOSTNAME = 0
  val ARG_USERNAME = 1
  val ARG_PASSWORD = 2
}

class Main(args: String*) {
  var ui: MainWindow = _
  val portfolio = new SniperPortfolio

  startUserInterface()
  val auctionHouse = XMPPAuctionHouse.connect(args(ARG_HOSTNAME), args(ARG_USERNAME), args(ARG_PASSWORD))
  disconnectWhenUICloses(auctionHouse)
  addUserRequestListenerFor(auctionHouse)

  private def addUserRequestListenerFor(auctionHouse: XMPPAuctionHouse) = {
    ui.addUserRequestListener(new SniperLauncher(auctionHouse, portfolio))
  }

  private def startUserInterface() = {
    SwingUtilities.invokeAndWait(new Runnable {
      override def run() {
        ui = new MainWindow(portfolio)
      }
    })
  }

  private def disconnectWhenUICloses(auctionHouse: XMPPAuctionHouse) {
    ui.addWindowListener(new WindowAdapter {
      override def windowClosed(e: WindowEvent) = auctionHouse.disconnect()
    })
  }
}
