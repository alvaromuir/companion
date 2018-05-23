package com.verizon.itanalytics.dataengineering.companion

import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

import org.scalatest.FlatSpec

import slick.jdbc.H2Profile.api._

import Companion.system
import Tables._

import scala.concurrent.ExecutionContextExecutor

import scala.io.Source


/*
* companion v.1
* Alvaro Muir, Verizon IT Analytics: Data Engineering
* 05 20, 2018
*/


class TablesSpec extends FlatSpec with Utils {

  private val config: Config = ConfigFactory.load()
  private val dataUploadPath: String = config.getString("http.dataUploadPath")
  private val srcDataFileName: String = config.getString("db.srcDataFileName")
  private val filePath = s"$dataUploadPath/$srcDataFileName"

  "The Tables object" should
  "hold a Affinity case class" in {

    val r = new scala.util.Random
    val lineNumber =  0 + r. nextInt(Source.fromFile(filePath).getLines.size)
    val line = Source.fromFile(filePath).getLines.toList.slice(lineNumber - 1, lineNumber).head.toString.split(",")
    val affinity = Affinity(line(0), line(1), line(2), Option(line(3)), Option(line(4)), Option(line(5)),
                line(6), line(7), line(8), line(9).toDouble, line(10).toDouble)

    assert(affinity.SELECTION_SKU_0.equals(line(0)))
    assert(affinity.SELECTION_ITEM_DESC_0.equals(line(1)))
    assert(affinity.SELECTION_CATEGORY_0.equals(line(2)))
    assert(affinity.SELECTION_SKU_1.contains(line(3)))
    assert(affinity.SELECTION_ITEM_DESC_1.contains(line(4)))
    assert(affinity.SELECTION_CATEGORY_1.contains(line(5)))
    assert(affinity.RCMND_ITEM_CD.equals(line(6)))
    assert(affinity.RCMND_ITEM_DESC.equals(line(7)))
    assert(affinity.RCMND_EQP_CTGRY_CD.equals(line(8)))
    assert(affinity.LIFT.equals(line(9).toDouble))
    assert(affinity.CONFIDENCE.equals(line(10).toDouble))
  }


  it should "insert records into the database (via seedTable)" in {
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    lazy val affinities = TableQuery[AffinityTable]


    // Tables.seed() is run on start up
    val totalLines = io.Source.fromFile(filePath).getLines.size
    val rowCount = exec(affinities.length.result)

    assert(rowCount.equals(totalLines - 1))

  }

}
