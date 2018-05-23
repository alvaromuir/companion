package com.verizon.itanalytics.dataengineering.companion

import org.scalatest.FlatSpec

import spray.json._
import ViewedJsonSupport._

/*
* companion v.1
* Alvaro Muir, Verizon IT Analytics: Data Engineering
* 05 21, 2018
*/



class ViewedJsonSupportSpec extends FlatSpec with Utils {

  "'Viewed' devices" should "be cased into classes" in {

    val browsedSku = "200101112"
    val cartSkus = List("77-5319", "200101141")
    val compatibleDevice = "3D169LL/A"
    val viewed = Viewed(browsedSku, Option(cartSkus), compatibleDevice)

    assert(Some(viewed.browsedSku).isDefined)
    assert(Some(viewed.cartSkus).isDefined)
    assert(Some(viewed.compatibleDevice).isDefined)

    assert(Some(viewed.browsedSku).contains(browsedSku))
    assert(Some(viewed.cartSkus).get.get.size.equals(2))
    assert(Some(viewed.cartSkus).get.contains(cartSkus))
    assert(Some(viewed.compatibleDevice).contains(compatibleDevice))

  }

  it should "accept classes with 3 fields and return a proerly formed JSON response" in {
    val browsedSku = "200101112"
    val cartSkus = List("77-5319", "200101141")
    val compatibleDevice = "3D169LL/A"
    val viewed = Viewed(browsedSku, Option(cartSkus), compatibleDevice)

    val viewedJson = viewed.toJson

    val expectedResults = """{"browsedSku":"200101112","cartSkus":["77-5319","200101141"],"compatibleDevice":"3D169LL/A"}"""
    println(viewedJson.compactPrint.contains(expectedResults))
  }

}
