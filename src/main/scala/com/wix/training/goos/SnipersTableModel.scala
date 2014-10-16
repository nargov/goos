package com.wix.training.goos

import javax.swing.table.AbstractTableModel

import com.wix.training.goos.SniperState.{JOINING, SniperState}
import com.wix.training.goos.SnipersTableModel._

/**
 * Created by Nimrod Argov on 7/8/14.
 *
 */
object SnipersTableModel {
  val STARTING_UP = new SniperSnapshot("", 0, 0, JOINING)
  val statusText = Seq("Joining", "Bidding", "Winning", "Losing", "Lost", "Won", "Failed")
  def textFor(state: SniperState) = statusText(state.id)
}
class SnipersTableModel extends AbstractTableModel with SniperListener with PortfolioListener{

  var snapshots = IndexedSeq[SniperSnapshot]()
  var notToBeGCd = IndexedSeq[AuctionSniper]()

  override def getColumnName(column: Int): String = Column(column).name
  override def getColumnCount: Int = Column.values.size
  override def getRowCount: Int = snapshots.length
  override def getValueAt(rowIndex: Int, columnIndex: Int): AnyRef = Column(columnIndex).valueIn(snapshots(rowIndex))

  override def sniperAdded(sniper: AuctionSniper): Unit = {
    addSniperSnapshot(sniper.snapshot)
    sniper.addSniperListener(new SwingThreadSniperListener(this))
  }

  def addSniperSnapshot(snapshot: SniperSnapshot) = {
    snapshots = snapshots :+ snapshot
    val row: Int = snapshots.size - 1
    fireTableRowsInserted(row, row)
  }

  def sniperStateChanged(snapshot: SniperSnapshot): Unit = {
    val index = rowMatching(snapshot)
    snapshots = snapshots.updated(index, snapshot)
    fireTableRowsUpdated(index, index)
  }

  private def rowMatching(snapshot: SniperSnapshot): Int = {
    val index: Int = snapshots.indexWhere(snapshot.isForSameItemAs)
    if (index > -1) index else throw new SillyDefect("No snapshot to update")
  }
}

trait SniperCollector {
  def addSniper(sniper: AuctionSniper)
}

sealed abstract case class Column(valueIn: SniperSnapshot => AnyRef, name: String){
  def id = Column.values.indexOf(this)
}
object Column {
  val values = Seq(ItemIdentifier, LastPrice, LastBid, State)
  def apply(id: Int) = values(id)
}
object ItemIdentifier extends Column(_.itemId, "Item")
object LastPrice extends Column(s => s.lastPrice.underlying(), "Last Price")
object LastBid extends Column(s => s.lastBid.underlying(), "Last Bid")
object State extends Column(s => textFor(s.state), "State")

