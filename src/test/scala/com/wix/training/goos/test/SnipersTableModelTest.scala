package com.wix.training.goos.test

import javax.swing.event.{TableModelEvent, TableModelListener}

import com.wix.training.goos.SniperState.{JOINING, BIDDING}
import com.wix.training.goos.SnipersTableModel.textFor
import com.wix.training.goos._
import com.wixpress.common.specs2.JMock
import org.specs2.matcher.Matcher
import org.specs2.mutable.{Specification, Before}
import org.specs2.specification.Scope

/**
 * Created by Nimrod Argov on 7/8/14.
 *
 */
class SnipersTableModelTest extends Specification with JMock{
  val listener = mock[TableModelListener]
  val model = new SnipersTableModel()

  trait TestScope extends Scope with Before {
    override def before = model.addTableModelListener(listener)
  }

  "Snipers Table Model" should {

    "have enough columns" in new TestScope {
      model.getColumnCount must be equalTo Column.values.size
    }

    "set sniper values in columns" in new TestScope {
      val joining = SniperSnapshot.joining("item id")
      val bidding = joining.bidding(555, 666)
      checking {
        allowing(listener).tableChanged(`with`(anyInsertionEvent))
        oneOf(listener).tableChanged(`with`(aChangeInRow(0)))
      }
      model.addSniperSnapshot(joining)
      model.sniperStateChanged(bidding)

      assertRowMatchesSnapshot(0, bidding)
    }

    "set up column headings" in new TestScope {
      Column.values.map(col =>
        col.name must be equalTo model.getColumnName(col.id)
      )
    }

    "notify listeners when adding a sniper" in new TestScope {
      val joining = SniperSnapshot.joining("item123")
      checking {
        oneOf(listener).tableChanged(`with`(anInsertionAtRow(0)))
      }

      model.getRowCount === 0

      model.addSniperSnapshot(joining)

      model.getRowCount === 1

      assertRowMatchesSnapshot(0, joining)
    }

    "hold snipers in addition order" in new TestScope {
      checking {
        ignoring(listener)
      }

      model.addSniperSnapshot(SniperSnapshot.joining("item 0"))
      model.addSniperSnapshot(SniperSnapshot.joining("item 1"))

      model.getValueAt(0, ItemIdentifier.id) must be equalTo "item 0"
      model.getValueAt(1, ItemIdentifier.id) must be equalTo "item 1"
    }

    "update correct row for sniper" in new TestScope {
      checking {
        ignoring(listener)
      }

      val joining = SniperSnapshot.joining("item 1")
      model.addSniperSnapshot(SniperSnapshot.joining("item 0"))
      model.addSniperSnapshot(joining)

      val bidding = joining.bidding(555, 666)

      model.sniperStateChanged(bidding)

      model.getValueAt(0, LastBid.id) must be equalTo 0.underlying()
      model.getValueAt(1, LastBid.id) must be equalTo 666.underlying()

    }

    "throw defect if no sniper for update exists" in new TestScope {
      checking {
        ignoring(listener)
      }

      model.sniperStateChanged(SniperSnapshot.joining("item 0").bidding(333, 444)) must throwA[SillyDefect]("No snapshot to update")
    }
  }

  private def assertRowMatchesSnapshot(row: Int, snapshot: SniperSnapshot) = {
    model.getValueAt(row, ItemIdentifier.id) must be equalTo snapshot.itemId
    model.getValueAt(row, LastPrice.id) must be equalTo snapshot.lastPrice.underlying()
    model.getValueAt(row, LastBid.id) must be equalTo snapshot.lastBid.underlying()
    model.getValueAt(row, State.id) must be equalTo textFor(snapshot.state)
  }

  private def aChangeInRow(row: Int): Matcher[TableModelEvent] =
    (e: TableModelEvent) => (e.getSource == model && e.getFirstRow == row && e.getLastRow == row && e.getType == TableModelEvent.UPDATE, "not a change in row " + row)
  private def anInsertionAtRow(row: Int): Matcher[TableModelEvent] = (e: TableModelEvent) => (e.getType == TableModelEvent.INSERT && e.getFirstRow == row && e.getLastRow == row, "wrong event")
  private def anyInsertionEvent: Matcher[TableModelEvent] = (e: TableModelEvent) => (e.getType == TableModelEvent.INSERT, "not an insertion event")
}
