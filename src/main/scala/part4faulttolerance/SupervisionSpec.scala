package part4faulttolerance

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props, Terminated}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class SupervisionSpec extends TestKit(ActorSystem("SupervisionSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll  {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
  import SupervisionSpec._

  "A supervisor" should {
    "resume it's child in case of a minor fault" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      child ! "I love akka"
      child ! Report
      expectMsg(3)

      child ! "Akka is awesome because I'm learning to think a whole other way"
      child ! Report
      expectMsg(3)
    }
    "restart it's child in case an empty sentence" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      child ! "I love akka"
      child ! Report
      expectMsg(3)

      child ! ""
      child ! Report
      expectMsg(0)
    }
    "terminate it's child in case of a major error" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      watch(child)
      child ! "akka is nice"
      val terminatedMsg = expectMsgType[Terminated]
      assert(terminatedMsg.actor == child)

    }
    "escalate an error when it doesn't know what to do" in {
      val supervisor = system.actorOf(Props[Supervisor], "supervisor")
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      watch(child)
      child ! 43
      val terminatedMsg = expectMsgType[Terminated]
      assert(terminatedMsg.actor == child)
    }

  }
 /* "A kinder supervisor" should {
    "not kill children in case it's restarted or escalates failure" in {
      val supervisor = system.actorOf(Props[NoDeath], "supervisor")
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]
    }
  }*/

}
object SupervisionSpec {

  class Supervisor extends Actor {

    override val supervisorStrategy = OneForOneStrategy() {
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }

    override def receive: Receive = {
      case props: Props =>
        val childRef = context.actorOf(props)
        sender() ! childRef
    }
  }

  case object Report {
    def apply: Any = ???
  }
  class FussyWordCounter extends Actor {
    var words = 0

    override def receive: Receive = {
      case Report => sender() ! words
      case "" => throw new NullPointerException("sentence is empty")
      case sentence: String =>
          if (sentence.length > 20) throw new RuntimeException("Sentence too long")
          else if (!Character.isUpperCase(sentence.charAt(0))) throw new IllegalArgumentException("sentence must start with uppercase")
          else words += sentence.split(" ").length
      case _ => throw new Exception("Can only receive strings")
    }
  }
}