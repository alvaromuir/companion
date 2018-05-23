package com.verizon.itanalytics.dataengineering.companion

import java.io.File

import com.google.common.io.Files._
import com.typesafe.config.{Config, ConfigFactory}

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._

import org.scalatest.{Matchers, WordSpec}

import slick.jdbc.H2Profile.api._

import com.verizon.itanalytics.dataengineering.companion.Tables._

import Directives._


/*
* companion
* Alvaro Muir, Verizon IT Analytics: Data Engineering
* 05 22, 2018
*/


class ServiceRoutesSpec extends WordSpec with Matchers with ScalatestRouteTest with ServiceRoutes {

  private val config: Config = ConfigFactory.load()
  private val dataUploadPath: String = config.getString("http.dataUploadPath")
  private val srcDataFileName: String = config.getString("db.srcDataFileName")
  private val filePath = s"$dataUploadPath/$srcDataFileName"

  val heartbeatTestRoute =
    get {
      pathSingleSlash {
        complete {
          "well, hello 😉"
        }
      } ~
        path("hello") {
          complete("World!")
        }
    }


  "The microservice" should {

    "return a greeting for GET requests to the root path" in {
      // tests:
      Get() ~> heartbeatTestRoute ~> check {
        responseAs[String] shouldEqual "well, hello 😉"
      }
    }

    "return a 'World!' response for GET requests to /ping" in {
      Get("/hello") ~> heartbeatTestRoute ~> check {
        responseAs[String] shouldEqual "World!"
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/foo") ~> heartbeatTestRoute ~> check {
        handled shouldBe false
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> Route.seal(heartbeatTestRoute) ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
      }
    }
  }

  "The API" should {
    "serve the system's data set as requested" in {
      Get("/api/v1/dataset") ~> serviceRoutes ~> check {
        assert(responseAs[String].split("\n").head.split(",").head.contains("ELECTION_SKU_0"))
      }
    }

    "accept new data sets posted to the appropriate endpoint" in {

      val testDataLine = "3D169LL/A,IPHONE 7 JET BLACK 32GB USA SO,Device,,,,77-54500,CAS OB SYMMETRY IPHONE7 BLACK,Case,3.567875182,0.039621424\n"
      val uploadForm =
        Multipart.FormData(
          Multipart.FormData.BodyPart.Strict(
            "file",
            HttpEntity(ContentTypes.`text/plain(UTF-8)`, testDataLine),
            Map("filename" -> "test_data.csv")))


      Post("/api/v1/dataset", uploadForm) ~> serviceRoutes ~> check {
        status shouldEqual StatusCodes.OK
      }

      val srcFile = new File(s"$dataUploadPath/orig_data.csv")
      val destFile = new File(s"$dataUploadPath/$srcDataFileName")
      copy(srcFile, destFile)

      lazy val affinities = TableQuery[AffinityTable]
      val totalLines = io.Source.fromFile(filePath).getLines.size
      val rowCount = exec(affinities.length.result)

      assert(rowCount.equals(totalLines - 1))

    }

    "return a MethodNotAllowed error for GET requests to the'recommend' endpoint" in {
      Get("/api/v1/recommend") ~> serviceRoutes ~> check {
        handled shouldBe false
      }
    }

    "return a response to a POST request to the'recommend' endpoint" in {
      val jsonReq = """{"browsedSku": "3D169LL/A", "cartSkus": ["3D169LL/A"], "compatibleDevice": "3D169LL/A"}"""

      Post("/api/v1/recommend", HttpEntity(ContentTypes.`application/json`, jsonReq)) ~> serviceRoutes ~> check {

        // just checking a 200 response, as the results can change depending on data set
       assert(responseAs[String].contains("status\":200"))
      }
    }
  }
}