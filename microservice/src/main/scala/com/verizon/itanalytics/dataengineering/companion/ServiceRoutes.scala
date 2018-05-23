package com.verizon.itanalytics.dataengineering.companion

import java.nio.file.Paths

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl._
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
  private val dataUploadPath: String = config.getString("http.dataUploadPath")
  private val srcDataFileName: String = config.getString("db.srcDataFileName")
  private val timeOut: Int = config.getInt("http.timeOut")

  private implicit val timeout: Timeout = Timeout(timeOut.seconds)

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
            path("dataset") {
              get {
                complete(HttpEntity(ContentTypes.`text/csv(UTF-8)`, FileIO.fromPath(Paths.get(s"$dataUploadPath/$srcDataFileName"))))
              } ~
              post {
                  toStrictEntity(timeOut.seconds) {
                    fileUpload("file") {
                      case (metadata, byteSource) =>
                        val sink = FileIO.toPath(
                          Paths.get(dataUploadPath) resolve srcDataFileName)
                        val uploaded = byteSource.runWith(sink)
                        onComplete(uploaded) {
                          case Success(_) =>
                            val path = s"$dataUploadPath/$srcDataFileName"
                            onComplete(reSeedTable(dataSet = path)) {
                              case Success(_) =>
                                val numRow = exec(affinities.length.result)
                                val respMsg = s"Database successfully seeded with $numRow rows from ${metadata.fileName}."
                                log.info(respMsg)
                                complete(HttpEntity(ContentTypes.`application/json`, respMsg))
                              case Failure(e) =>
                                log.error(s"ERROR: Database seeding failed with error message: $e. Now exiting")
                                complete(HttpEntity(ContentTypes.`application/json`, jsonize(e)))
                            }
                          case Failure(e) =>
                            log.error(s"ERROR: Uploading the data set failed with error message: $e. Now exiting")
                            complete(HttpEntity(ContentTypes.`application/json`, jsonize(e)))
                        }
                    }
                  }
                }
            } ~
            path("recommend") {
              post {
                    entity(as[Viewed]) {
                      viewed =>
                        onComplete(db.run(affinities.filter(_.selectionSku0 === viewed.browsedSku).result)) {
                          case Success(rslts) => complete(HttpEntity(ContentTypes.`application/json`, jsonize(rslts)))
                          case Failure(e) => complete(HttpEntity(ContentTypes.`application/json`, jsonize(e)))
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
