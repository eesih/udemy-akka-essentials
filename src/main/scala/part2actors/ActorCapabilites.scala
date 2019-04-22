package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
     def receive: Receive = {
       case "hi" => context.sender() ! "Hello there"
       case message:String => println(s"[${self.path}] I have received $message")
       case number: Int => println(s"[simple actor] I have received a number $number")
       case SpecialMessage(msg) => println(s"[simple actor] I have received something special $msg")
       case SendMessageToYourself(msg) => self ! msg
       case SayHiTo(ref) => ref ! "hi"
       case WirelessPhoneMessage(msg, ref) => ref forward (msg + "s")
    }
  }

  val system = ActorSystem("ActorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "hello, actor"

  simpleActor ! 42


  case class SpecialMessage(contents: String)

  simpleActor ! SpecialMessage("some special message")

  case class SendMessageToYourself(contents: String)

  simpleActor ! SendMessageToYourself("Message to myself")

  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)

  alice ! SayHiTo(bob)

  case class WirelessPhoneMessage(contents: String, ref: ActorRef)

  alice ! WirelessPhoneMessage("hi", bob)

  case class Increment()
  case class Decrement()
  case class Print()


  class CounterActor extends Actor {
     var counter = 0
     def receive: Receive = {
       case Increment => counter += 1
       case Decrement => counter -= 1
       case Print => println(s"[counter actor] current count is $counter")
     }
  }

  val counterActor = system.actorOf(Props[CounterActor], "counterActor")
  counterActor ! Increment
  counterActor ! Increment
  counterActor ! Increment
  counterActor ! Decrement
  counterActor ! Print

  case class Deposit(amount: Int, actorRef: ActorRef)
  case class Withdraw(amount: Int, actorRef: ActorRef)
  case class Statement()
  case class Success(msg: String)
  case class Failure(msg: String)

  class BankAccountActor extends Actor {
    var amount = 0
    def receive: Receive = {
      case Deposit(a, ref) => {
        amount += a
        sender() ! Success(s"Amount of $a has been successfully deposited")
      }
      case Withdraw(a, ref) => {
        if (a > amount) sender() ! Failure(s"Withdraw of $a can't be taken from the account of $amount amount")
        else {
          amount -= a
          sender() ! Success(s"Withdraw of $a has been successfully taken from the account")
        }
      }
      case Statement => println(s"Bank account amount is $amount")
    }
  }

  class BankAccountHelper extends Actor {
    def receive: Receive = {
      case Deposit(a, ref) => ref ! Deposit(a, ref)
      case Withdraw(a, ref) => ref ! Withdraw(a, ref)
      case Failure(message) => println(message)
      case Success(message) => println(message)
    }
  }

  val bankAccountActor = system.actorOf(Props[BankAccountActor], "bankAccountActor")
  val helper = system.actorOf(Props[BankAccountHelper], "helper")

  helper ! Deposit(1000, bankAccountActor)
  helper ! Deposit(2000, bankAccountActor)
  helper ! Withdraw(500, bankAccountActor)
  bankAccountActor ! Statement
  helper ! Deposit(2000, bankAccountActor)
  helper ! Withdraw(10000, bankAccountActor)




}
