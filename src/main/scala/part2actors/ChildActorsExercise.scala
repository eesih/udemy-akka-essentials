package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActorsExercise.WordCounterMaster.{Initialize, WorkCountReply, WorkCountTask}

object ChildActorsExercise extends App {

  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WorkCountTask(text: String)
    case class WorkCountReply(count: Int)
  }
  class WordCounterMaster extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(n) => {
        val workers = (0 until n).map(x => context.actorOf(Props[WordCounterWorker], "wordCounterWorker" + x)).toList
        println("amount " + workers.size)
        context.become(countWords(workers, 0))
      }
    }

    def countWords(workers: List[ActorRef], index: Int): Receive = {
      case text: String => {
        if (index < workers.size) {
          workers(index) ! WorkCountTask(text)
          context.become(countWords(workers, index + 1))
        } else {
          println("context.become " + workers.size + " " )
          context.become(countWords(workers, 0))
        }
      }
      case WorkCountReply(n) => context.actorSelection("/user/requester") ! n
    }

  }

  class WordCounterWorker extends Actor {
    override def receive: Receive = {
      case WorkCountTask(text) => {
        println(s"${self.path}")
        val words = text.split(" ").length
        sender() ! WorkCountReply(words)
      }
    }
  }

  class Requester extends Actor {
    import WordCounterMaster._
    override def receive: Receive = {
      case Initialize(n) => {
        val master = context.actorOf(Props[WordCounterMaster])
        master ! Initialize(n)
        context.become(initialized(master))
      }
    }
    def initialized(master: ActorRef): Receive = {
      case text: String => {
        master ! text
      }
      case n: Int => println(s"Amount of words: $n")
    }
  }

  val system = ActorSystem("wordCounter")
  val requester = system.actorOf(Props[Requester], "requester")
  requester ! Initialize(5)
  requester ! "hello my name is machine and I like to receive messages"
  requester ! "hello my name is machine and I like to receive"
  requester ! "hello my name is machine and I like "
  requester ! "hello my name is machine and"
  requester ! "hello my name is machine and I like to receive messages"
  requester ! "hello my name is machine and I like to receive messages"
  requester ! "hello my name is machine and I like to receive messages"
  requester ! "hello my name is machine and I like to receive messages"


}
