package com.wix.training.goos

import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{BorderLayout, Color, FlowLayout}
import java.util.EventListener
import javax.swing._
import javax.swing.border.LineBorder

import com.wix.training.goos.MainWindow._
import org.jmock.example.announcer.Announcer

/**
 * Created by Nimrod Argov on 6/29/14.
 *
 */
class MainWindow(portfolio: SniperPortfolio) extends JFrame(APP_TITLE) {

  val userRequests = Announcer.to(classOf[UserRequestListener])

  def createLabel(initialText: String): JLabel = {
    val result = new JLabel(initialText)
    result.setName(SNIPER_STATUS_NAME)
    result.setBorder(new LineBorder(Color.BLACK))
    result
  }

  def fillContentPane(snipersTable: JTable, controls: JPanel) {
    val contentPane = getContentPane
    contentPane.setLayout(new BorderLayout())
    contentPane.add(controls, BorderLayout.PAGE_START)
    contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER)
  }

  def makeSnipersTable(portfolio: SniperPortfolio): JTable = {
    val model = new SnipersTableModel()
    portfolio.addPortfolioListener(model)
    val snipersTable = new JTable(model)
    snipersTable.setName(SNIPERS_TABLE_NAME)
    snipersTable
  }

  def makeControls(): JPanel = {
    val controls = new JPanel(new FlowLayout())

    val itemIdLabel = new JLabel("Item:")
    controls.add(itemIdLabel)

    val itemIdField = new JTextField()
    itemIdField.setColumns(12)
    itemIdField.setName(NEW_ITEM_ID_NAME)
    controls.add(itemIdField)

    val stopPriceLabel = new JLabel("Stop price:")
    controls.add(stopPriceLabel)

    val stopPriceField = new JFormattedTextField()
    stopPriceField.setInputVerifier(new InputVerifier {
      override def verify(input: JComponent): Boolean = input.asInstanceOf[JFormattedTextField].getText.forall(Character.isDigit)
    })
    stopPriceField.setColumns(12)
    stopPriceField.setName(NEW_ITEM_STOP_PRICE_NAME)
    controls.add(stopPriceField)

    val joinAuctionButton = new JButton("Join Auction")
    joinAuctionButton.setName(JOIN_BUTTON_NAME)
    joinAuctionButton.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = userRequests.announce().joinAuction(Item(itemIdField.getText, stopPriceField.getText.toInt))
    })
    controls.add(joinAuctionButton)

    controls
  }

  def addUserRequestListener(listener: UserRequestListener): Unit = userRequests.addListener(listener)

  setName(MAIN_WINDOW_NAME)
  fillContentPane(makeSnipersTable(portfolio), makeControls())
  pack()
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

  setVisible(true)
}

object MainWindow {
  val SNIPERS_TABLE_NAME = "Snipers Table"
  val APP_TITLE = "Auction Sniper"
  val NEW_ITEM_ID_NAME = "new-item-id"
  val NEW_ITEM_STOP_PRICE_NAME = "new-item-stop-price"
  val JOIN_BUTTON_NAME = "join-button"
  val MAIN_WINDOW_NAME = "Auction Sniper"
  val SNIPER_STATUS_NAME = "sniper-status"
}

trait UserRequestListener extends EventListener {
  def joinAuction(item: Item): Unit
}