package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActors.Parent.{CreateChild, TellChild}

object ChildActors extends App {

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(msg: String)
  }
  class Parent extends Actor {
    import Parent._

    override def receive: Receive = {
      case CreateChild(name) => println(s"${self.path} creating child")
        //create a child right here
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
    }

    def withChild(child: ActorRef): Receive =  {
      case TellChild(msg) => child forward msg
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} I got: $message")
    }
  }

  val system = ActorSystem("childParentDemo")

  val parent = system.actorOf(Props[Parent], "parent")

  parent ! CreateChild("child")
  parent ! TellChild("Mustaa makkaraa naamaan!")


  val childSelection = system.actorSelection("/user/parent/child")
  childSelection ! "I found you"

  /**
    * Danger!
    *
    * NEVER PASS MUTABLE ACTOR STATE, OR THE 'THIS' REFERENCE TO CHILD ACTORS
    *
    */

}
