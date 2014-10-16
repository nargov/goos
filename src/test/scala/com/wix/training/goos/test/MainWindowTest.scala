package com.wix.training.goos.test

import com.objogate.wl.swing.probe.ValueMatcherProbe
import com.wix.training.goos._
import com.wixpress.common.specs2.JMock
import org.specs2.mock.MatcherAdapter
import org.specs2.mutable.{Specification, After}
import org.specs2.specification.Scope

/**
 * Created by Nimrod Argov on 7/14/14.
 *
 */
class MainWindowTest extends Specification with JMock{

  trait TestScope extends Scope with After{
    val tableModel = new SnipersTableModel
    val driver = new AuctionSniperDriver(100)
    val portfolio = new SniperPortfolio
    val mainWindow = new MainWindow(portfolio)

    def after = if (driver != null) driver.dispose()
  }

  "A MainWindow" should {
    "make a user request when the Join button is clicked" in new TestScope {
      val itemProbe = new ValueMatcherProbe[Item](MatcherAdapter(equalTo(Item("item123", 789))), "join request")

      mainWindow.addUserRequestListener(new UserRequestListener {
        override def joinAuction(item: Item): Unit = itemProbe.setReceivedValue(item)
      })

      driver.startBiddingFor(Item("item123", 789))
      driver.check(itemProbe)
    }
  }
}
