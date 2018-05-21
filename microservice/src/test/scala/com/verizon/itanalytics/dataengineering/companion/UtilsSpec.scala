package com.verizon.itanalytics.dataengineering.companion

import org.scalatest.FlatSpec

/*
 * companion v.1
 * Alvaro Muir, Verizon IT Analytics: Data Engineering
 * 05 16, 2018
 */

class UtilsSpec extends FlatSpec with Utils {

  "the utils package" should "listify JSON strings as required" in {
    import spray.json._

    val testJson =
      """
        |{
        |   "Accept-Language": "en-US,en;q=0.8",
        |   "Host": "headers.jsontest.com",
        |   "Accept-Charset": "ISO-8859-1,utf-8;q=0.7,*;q=0.3",
        |   "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
        |}
      """.stripMargin.parseJson
    val parsedJson = Listify(testJson)
    assert(parsedJson.size.equals(4))
    assert(Some(parsedJson.head).get.toString.contains("en-US,en;q=0.8"))
  }

  it should "slugify strings as required" in {
    val testString = "Hello, World!!! \n" +
      "This is a TEST STRING"

    assert(Slugify(testString).contains("hello-world-this-is-a-test-string"))
  }

  it should "safely throw exceptions when necessary" in {

    intercept[ArithmeticException] {
      try {
        print(s"${1 / 0}")
      } catch Safely { case ex: ArithmeticException => throw ex }
    }
  }

}
