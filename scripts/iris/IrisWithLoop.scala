package iris

import ocs.framework.ScriptImports._

class IrisWithLoop(cs: CswServices) extends Script(cs) {

  val publisherStream = cs.publish(10.seconds) {
    SystemEvent(Prefix("iris-test"), EventName("system"))
  }

  val subscriptionStream = cs.subscribe(Set(EventKey("iris-test.system"))) { _ =>
    println(s"------------------> received-event for ocs on key-------------------->")
    Done
  }

  var flag = true
  def setupAssemblyLoop = loop {
    spawn {
      println("************ loop **************")
      val command = Setup(Prefix("test"), CommandName("test-command"), None)
      cs.submit("Sample1Assembly", command).await
      stopWhen(flag)
    }
  }

  handleSetupCommand("setup-start-loop") { command =>
    spawn {
      println(s"[Iris] Received command: ${command.commandName}")
      flag = false
      setupAssemblyLoop
      AggregateResponse(CommandResponse.Completed(command.runId))
    }
  }

  handleSetupCommand("setup-stop-loop") { command =>
    spawn {
      println(s"[Iris] Received command: ${command.commandName}")
      flag = true
      AggregateResponse(CommandResponse.Completed(command.runId))
    }
  }

  handleSetupCommand("setup-iris") { command =>
    spawn {
      println(s"[Iris] Received command: ${command.commandName}")
      var firstAssemblyResponse: CommandResponse = null
      var counter                                = 0
      loop {
        spawn {
          counter += 1
          firstAssemblyResponse = cs.submit("Sample1Assembly", command).await
          println(counter)
          stopWhen(counter > 2)
        }
      }.await

      val response = AggregateResponse(Completed(command.runId))

      println(s"[Iris] Received response: $response")
      cs.sendResult(s"$response")
      response
    }
  }

  override def onShutdown(): Future[Done] = spawn {
    println("shutdown iris")
    Done
  }
}
