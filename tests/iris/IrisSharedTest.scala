package iris
import akka.actor.ActorSystem
import csw.params.commands.{CommandName, Setup}
import csw.params.core.models.Prefix
import ocs.framework.dsl.CswServices
import ocs.testkit.mocks.{CswServicesMock, SequencerFactory}
import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.duration.DurationDouble

class IrisSharedTest extends FunSuite {
  test("should be able to execute handleCommand for setup-iris-shared command") {
    implicit val system: ActorSystem = ActorSystem("test")
    val mockCswServices: CswServices = CswServicesMock.create(SequencerFactory.create())
    val irisDarkNight                = new IrisDarkNight(mockCswServices)

    val eventualResponse = irisDarkNight.execute(Setup(Prefix("sequencer"), CommandName("setup-iris-shared"), None))
    println(Await.result(eventualResponse, 10.seconds))

    Await.result(irisDarkNight.shutdown(), 10.seconds)
    Await.result(system.terminate(), 10.seconds)
  }
}
