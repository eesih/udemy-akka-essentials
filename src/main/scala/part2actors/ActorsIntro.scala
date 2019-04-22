package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {

  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  class WordCountActor extends Actor {
     var totalWords = 0
     def receive: Receive = {
       case message: String =>
         println(s"[word counter] I have received a message: $message")
         totalWords += message.split(" ").length
       case msg => println(s"[word counter] I cannot understand ${msg.toString}")
     }
  }

  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherRordCounter")
  wordCounter ! "I am learning akka and it's pretty cool!"
  anotherWordCounter ! "Different message"
}
