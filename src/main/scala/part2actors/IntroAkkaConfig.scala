package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {

  val configurationString =
    """
      | akka {
      |   loglevel = "ERROR"
      | }
    """.stripMargin

  val config = ConfigFactory.parseString(configurationString)
  val system = ActorSystem("configDemo", ConfigFactory.load(config))

  class LoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString + " " + log.isDebugEnabled)
    }
  }

  val loggingActor = system.actorOf(Props[LoggingActor])
  loggingActor ! "hello"


  val defaultConfigFileSystem = ActorSystem("defaultConfigDemo")
  val defaultConfigActor = defaultConfigFileSystem.actorOf(Props[LoggingActor])
  defaultConfigActor ! "remember me"


  val specialConfigFileSystem = ActorSystem("specialConfigDemo", ConfigFactory.load().getConfig("mySpecialConfig"))
  val specialConfigActor = specialConfigFileSystem.actorOf(Props[LoggingActor])
  specialConfigActor ! "remember me, I'm special"

  val factory = ConfigFactory.load("secret_folder/secretConfig.conf")
  val secretConfigFileSystem = ActorSystem("specialConfigDemo", factory)
  println(s"Separate config file ${factory.getString("akka.loglevel")}")
  val secretConfigActor = secretConfigFileSystem.actorOf(Props[LoggingActor])
  secretConfigActor ! "remember me, I'm secret"


}
