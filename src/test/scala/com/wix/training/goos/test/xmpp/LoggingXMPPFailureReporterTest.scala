package com.wix.training.goos.test.xmpp

import java.util.logging.{LogManager, Logger}

import com.wix.training.goos.xmpp.LoggingXMPPFailureReporter
import com.wixpress.common.specs2.JMock
import org.specs2.mutable.{After, Specification}
import org.specs2.specification.Scope

/**
 * Created by Nimrod Argov on 7/24/14.
 *
 */
class LoggingXMPPFailureReporterTest extends Specification with JMock {

  useClassImposterizer()

  val logger = mock[Logger]
  val reporter = new LoggingXMPPFailureReporter(logger)

  trait Ctx extends Scope with After {
    def after = resetLogging()

    def resetLogging() = {
      LogManager.getLogManager.reset()
    }
  }

  "LoggingXMPPFailureReporter" should {
    "write message translation failure to log" in new Ctx {

      checking {
        oneOf(logger).severe("<auction id> Could not translate message \"bad message\" because \"java.lang.Exception: bad\"")
      }
      reporter.cannotTranslateMessage("auction id", "bad message", new Exception("bad"))
    }
  }
}
