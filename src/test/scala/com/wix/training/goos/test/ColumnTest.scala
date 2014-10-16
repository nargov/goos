package com.wix.training.goos.test

import com.wix.training.goos.SniperState.BIDDING
import com.wix.training.goos.SnipersTableModel.textFor
import com.wix.training.goos._
import org.specs2.mutable.Specification

/**
 * Created by Nimrod Argov on 7/13/14.
 *
 */
class ColumnTest extends Specification{
  "A Column" should {
    "return the correct fields" in {
      val snapshot = new SniperSnapshot("12345", 123, 45, BIDDING)
      ItemIdentifier.valueIn(snapshot) must be equalTo "12345"
      LastPrice.valueIn(snapshot).asInstanceOf[Int] must be equalTo 123
      LastBid.valueIn(snapshot).asInstanceOf[Int] must be equalTo 45
      State.valueIn(snapshot) must be equalTo textFor(BIDDING)
    }
  }
}
