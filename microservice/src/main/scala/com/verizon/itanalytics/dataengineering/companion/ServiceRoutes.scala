package com.verizon.itanalytics.dataengineering.companion

import java.util.logging.Logger

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.control.NonFatal

import Companion.system
import Tables._
import JsonProtocol._
import ViewedJsonSupport._

import scala.util.{Failure, Success}


/*
 * companion v.1
 * Alvaro Muir, Verizon IT Analytics: Data Engineering
 * 05 16, 2018
 */

trait ServiceRoutes {
  import akka.http.scaladsl.server.Directives._
  final case class ActionPerformed(description: String)

  private val config: Config = ConfigFactory.load()
  private val apiVersion: String = config.getString("http.apiVersion")
  private val timeOut: Int = config.getInt("http.timeOut")

  private implicit val timeout: Timeout = Timeout(timeOut.seconds)
  private val log = Logger.getLogger(this.getClass.getName)

  implicit def routesExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case NonFatal(e) =>
        log.info(s"Exception $e at\n${e.getStackTrace}")
        complete(
          HttpResponse(StatusCodes.InternalServerError,
                       entity = s"""{"error":"${e.getMessage}"}"""))
    }

  def serviceRoutes: Route = {
    import akka.http.scaladsl.server.Directives._

    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    pathPrefix("api") {
      handleExceptions(routesExceptionHandler) {
        extractRequestContext { ctx =>
          implicit val materializer: Materializer = ctx.materializer
          pathPrefix(s"$apiVersion") {
            path("recommend") {
              get {
                complete(s"Viewed will go here ...")
              } ~
              post {
                entity(as[Viewed]) { viewed =>
                  onComplete(db.run(recommendations.filter(_.selectionSku0 === viewed.browsedSku).result)) {
                    case Success(rslts) => complete(HttpEntity(ContentTypes.`application/json`, jsonize(rslts)))
                    case Failure(e) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, e.toString))
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
