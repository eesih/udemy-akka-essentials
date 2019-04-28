package part5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, PoisonPill, Props, Timers}
import akka.routing.FromConfig

import scala.concurrent.duration._

object TimesAndSchedulers extends App {

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("timesAndSchedulersDemo")
  val simpleActor = system.actorOf(Props[SimpleActor])

  system.log.info("Scheduling reminder for simple actor")
  //implicit val executionContext = system.dispatcher
  import system.dispatcher

 /* system.scheduler.scheduleOnce(1 second) {
    simpleActor ! "reminder"
  }*/

/*  val routine: Cancellable = system.scheduler.schedule(1 second, 2 seconds) {
    simpleActor ! "heartbeat"
  }

  system.scheduler.scheduleOnce(5 seconds) {
    routine.cancel()
  }*/

  class MySelfClosingActor extends Actor {

    var millis: Long = 0

    override def receive: Receive = {
      case message =>
        if (millis == 0) millis = System.currentTimeMillis()
        if ((System.currentTimeMillis() - millis) > 1000) {
          println("Closing the actor...")
          self ! PoisonPill
        } else {
          millis = System.currentTimeMillis()
          println("Still alive " + millis)
        }
    }
  }

/*  val selfClosingActor = system.actorOf(Props[MySelfClosingActor])

  val routine2: Cancellable = system.scheduler.schedule(1 millisecond, 100 millisecond) {
    selfClosingActor ! "heartbeat"
  }

  system.scheduler.scheduleOnce(5 seconds) {
    routine2.cancel()
  }
  Thread.sleep(7000)
  println("ready to close")
  selfClosingActor ! "last heartbeat"*/
  //Thread.sleep(2000)
  //selfClosingActor ! "last heartbeat"


  class SelfClosingActor extends Actor with ActorLogging {

    var schedule = createTimeoutWindow()

    override def receive: Receive = {
      case "timeout" =>
        log.info("Stopping myself")
        context.stop(self)
      case message =>
        log.info(s"Received $message, staying alive")
        schedule.cancel()
        schedule = createTimeoutWindow()
    }

    def createTimeoutWindow(): Cancellable = {
      context.system.scheduler.scheduleOnce(1 second) {
        self ! "timeout"
      }
    }
  }

/*  val selfClosingActor = system.actorOf(Props[SelfClosingActor], "selfClosingActor")
  system.scheduler.scheduleOnce(250 millis) {
    selfClosingActor ! "ping"
  }

  system.scheduler.scheduleOnce(2 seconds) {
    system.log.info("Sending pong to to the self closing actor")
    selfClosingActor ! "pong"
  }*/

  case object TimeKey
  case object Start
  case object Reminder
  case object Stop
  class TimeBasedHeartbeatActor extends Actor with ActorLogging with Timers {

    timers.startSingleTimer(TimeKey, Start, 500 millis)

    override def receive: Receive = {
      case Start =>
        log.info("Bootstrapping")
        timers.startPeriodicTimer(TimeKey, Reminder, 1 second)
      case Reminder =>
        log.info("I'm alive")
      case Stop =>
        log.warning("Stopping")
        timers.cancel(TimeKey)
        context.stop(self)
    }
  }

  val timer = system.actorOf(Props[TimeBasedHeartbeatActor], "timeActor")
  system.scheduler.scheduleOnce(5 seconds) {
    timer ! Stop
  }



}
