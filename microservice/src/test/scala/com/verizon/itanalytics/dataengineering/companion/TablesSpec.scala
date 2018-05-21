package com.verizon.itanalytics.dataengineering.companion

import com.verizon.itanalytics.dataengineering.companion.Tables._
import org.scalatest.FlatSpec

import scala.io.Source


/*
* companion v.1
* Alvaro Muir, Verizon IT Analytics: Data Engineering
* 05 20, 2018
*/


class TablesSpec extends FlatSpec with Utils {

  private val dataUploadPath: String = "src/test/resources"
  private val srcDataFileName: String = "test_data.csv"
  private val filePath = s"$dataUploadPath/$srcDataFileName"

  "The Tables object" should
  "hold a Recommendation case class" in {

    val r = new scala.util.Random
    val lineNumber =  0 + r. nextInt(Source.fromFile(filePath).getLines.size)
    val line = Source.fromFile(filePath).getLines.toList.slice(lineNumber - 1, lineNumber).head.toString.split(",")
    val recommendation = Recommendation(line(0), line(1), line(2), Option(line(3)), Option(line(4)), Option(line(5)),
                line(6), line(7), line(8), line(9).toDouble, line(10).toDouble)

    assert(recommendation.SELECTION_SKU_0.equals(line(0)))
    assert(recommendation.SELECTION_ITEM_DESC_0.equals(line(1)))
    assert(recommendation.SELECTION_CATEGORY_0.equals(line(2)))
    assert(recommendation.SELECTION_SKU_1.contains(line(3)))
    assert(recommendation.SELECTION_ITEM_DESC_1.contains(line(4)))
    assert(recommendation.SELECTION_CATEGORY_1.contains(line(5)))
    assert(recommendation.RCMND_ITEM_CD.equals(line(6)))
    assert(recommendation.RCMND_ITEM_DESC.equals(line(7)))
    assert(recommendation.RCMND_EQP_CTGRY_CD.equals(line(8)))
    assert(recommendation.LIFT.equals(line(9).toDouble))
    assert(recommendation.CONFIDENCE.equals(line(10).toDouble))
  }

  it should "insert records into the database" in {

    Tables.seedTable()
  }



}
