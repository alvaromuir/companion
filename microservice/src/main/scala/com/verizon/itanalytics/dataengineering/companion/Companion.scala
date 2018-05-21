package com.verizon.itanalytics.dataengineering.companion

import akka.Done
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink

import com.typesafe.config.{Config, ConfigFactory}

import de.heikoseeberger.accessus.Accessus._

import org.slf4j.{Logger, LoggerFactory}

import Tables._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}



/*
* companion v.1
* Alvaro Muir, Verizon IT Analytics: Data Engineering
* 05 16, 2018
*/


object Companion extends ServiceRoutes with Utils {
  private val config: Config = ConfigFactory.load()
  private val appId: String = config.getString("http.appId")
  private val interface: String  = config.getString("http.host")
  private val port:Int = config.getInt("http.port")
  private val logPath: String  = config.getString("logging.path")
  private val logFile: String  = config.getString("logging.file")
  private val logLevel: String = config.getString("akka.logLevel")


  System.setProperty("LOG_PATH", logPath)
  System.setProperty("LOG_FILE", logFile)
  System.setProperty("LOG_LEVEL", logLevel)

  implicit val system: ActorSystem = ActorSystem(s"${appId}RestService")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val log: Logger = LoggerFactory.getLogger(getClass.getName)

  def main(args: Array[String]): Unit = {

    seedTable().onComplete {
      case Success(_) => log.info("Database successfully seeded.")
      case Failure(e) =>
        log.error(s"ERROR: Database seeding failed with error message: $e. Now exiting")
        System.exit(1)
    }

    Http()
      .bindAndHandle(
        routes.withAccessLog(accessLog(Logging(system, appId.toUpperCase() + "_ACCESS_LOG"))),
        interface,
        port
      )
      .onComplete {
        case Success(ServerBinding(address)) => println(s"\n\nListening on $address\n\n")
        case Failure(cause)                  => println(s"Can't bind to $interface:$port: $cause")
      }
  }

  seedTable()


  /** Log HTTP method, path, status and response time in micros to the given log at info level. */
  def accessLog(log: LoggingAdapter): AccessLog[Long, Future[Done]] =
    Sink.foreach {
      case ((req, t0), res) =>
        val h = req.headers.mkString(",")
        val m = req.method.value
        val p = req.uri.path.toString
        val s = res.status.intValue()
        val t = (now() - t0) / 1000
        log.info(s"$m request to $p resulted in $s in $t ms \n $h")
    }

  lazy val routes: Route = serviceRoutes
  private def now() = System.nanoTime()

}
