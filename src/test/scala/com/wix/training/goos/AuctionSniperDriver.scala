package com.wix.training.goos

import javax.swing.{JButton, JTextField}
import javax.swing.table.JTableHeader

import com.objogate.wl.swing.AWTEventQueueProber
import com.objogate.wl.swing.driver.ComponentDriver._
import com.objogate.wl.swing.driver._
import com.objogate.wl.swing.gesture.GesturePerformer
import com.objogate.wl.swing.matcher.IterableComponentsMatcher._
import com.objogate.wl.swing.matcher.JLabelTextMatcher._

/**
 *
 * Created by Nimrod Argov on 6/26/14.
 */
class AuctionSniperDriver(timeoutMillis: Int) extends JFrameDriver(
  new GesturePerformer(),
  JFrameDriver.topLevelFrame(named(MainWindow.MAIN_WINDOW_NAME), showingOnScreen()),
  new AWTEventQueueProber(timeoutMillis, 100)) {

  def showsSniperStatus(itemId: String, lastPrice: Int, lastBid: Int, statusText: String) {
    new JTableDriver(this).hasRow(matching(
      withLabelText(itemId),
      withLabelText(lastPrice.toString),
      withLabelText(lastBid.toString),
      withLabelText(statusText)))
  }

  def hasColumnTitles() = {
    val headers = new JTableHeaderDriver(this, classOf[JTableHeader])
    headers.hasHeaders(matching(withLabelText("Item"), withLabelText("Last Price"), withLabelText("Last Bid"), withLabelText("State")))
  }

  def startBiddingFor(item: Item): Unit = {
    textField(MainWindow.NEW_ITEM_ID_NAME).replaceAllText(item.identifier)
    textField(MainWindow.NEW_ITEM_STOP_PRICE_NAME).replaceAllText(item.stopPrice.toString)
    bidButton().click()
  }

  private def textField(name: String): JTextFieldDriver = {
    val newItemId = new JTextFieldDriver(this, classOf[JTextField], named(name))
    newItemId.focusWithMouse()
    newItemId
  }

  private def bidButton(): JButtonDriver = new JButtonDriver(this, classOf[JButton], named(MainWindow.JOIN_BUTTON_NAME))
}
