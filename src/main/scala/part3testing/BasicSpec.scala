package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll  {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import BasicSpec._

  "A simple actor" should {
    "sent back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "hello, test"
      echoActor ! message

      expectMsg(message)
    }
  }
  "A blackhole actor" should {
    "send back some message" in {
      val blackHole = system.actorOf(Props[BlackHole])
      val message = "hello, test"
      blackHole ! message

      expectNoMessage(1 second)
    }
  }
  "A lab test actor" should {
    val labtTestActor = system.actorOf(Props[LabTestActor])

    "turn string to upper case" in {
      labtTestActor ! "I love akka"
      val reply = expectMsgType[String]
      assert(reply == "I LOVE AKKA")
    }
    "reply a greeting" in {
      labtTestActor ! "greeting"
      expectMsgAnyOf("hi", "hello")
    }
    "reply with favoriteTech" in {
      labtTestActor ! "favoriteTech"
      expectMsgAllOf("Scala", "Akka")
    }
    "reply with cool tech in different way" in {
      labtTestActor ! "favoriteTech"
      val messages = receiveN(2) //Seq[Any]
    }
    "reply with cool tech in a fancy way" in {
      labtTestActor ! "favoriteTech"

      expectMsgPF() {
        case "Scala" =>
        case "Akka" =>
      }
    }
  }
}

object BasicSpec {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }

  class BlackHole extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {
    val random = new Random()
    override def receive: Receive = {
      case "greeting" => if (random.nextBoolean()) sender() ! "hi" else sender() ! "hello"
      case "favoriteTech" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message: String => sender() ! message.toUpperCase
    }
  }
}
