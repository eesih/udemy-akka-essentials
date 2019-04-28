package part5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}
import akka.dispatch.{ControlMessage, PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.{Config, ConfigFactory}

object Mailboxes extends App {

  val system = ActorSystem("MailBoxDemo", ConfigFactory.load().getConfig("mailboxesDemo"))

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case messsage => log.info(messsage.toString)
    }
  }

  class SupportTickerPriorityMailbox(setting: ActorSystem.Settings, config: Config)
    extends UnboundedPriorityMailbox(PriorityGenerator  {
      case message: String if (message.startsWith("[P0]")) => 0
      case message: String if (message.startsWith("[P1]")) => 1
      case message: String if (message.startsWith("[P2]")) => 2
      case message: String if (message.startsWith("[P3]")) => 3
      case _ => 4
  })

  val supportTicketActor = system.actorOf(Props[SimpleActor].withDispatcher("support-ticket-dispatcher"))
  supportTicketActor ! PoisonPill
  supportTicketActor ! "[P3] this thing would be nice to have"
  supportTicketActor ! "[P0] this needs to be solved now"
  supportTicketActor ! "[P1] do this when you have time"

  case object ManagementTicket extends ControlMessage

  val controlAwareActor = system.actorOf(Props[SimpleActor].withMailbox("control-mailbox"))
  controlAwareActor ! "[P3] this thing would be nice to have"
  controlAwareActor ! "[P0] this needs to be solved now"
  controlAwareActor ! ManagementTicket

  val altControlAwareActor = system.actorOf(Props[SimpleActor], "altControlAwareActor")
  altControlAwareActor ! "[P3] this thing would be nice to have"
  altControlAwareActor ! "[P0] this needs to be solved now"
  altControlAwareActor ! ManagementTicket
}
