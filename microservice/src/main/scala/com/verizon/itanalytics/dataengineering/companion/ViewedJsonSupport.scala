package com.verizon.itanalytics.dataengineering.companion

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/*
* companion
* Alvaro Muir, Verizon IT Analytics: Data Engineering
* 05 20, 2018
*/


case class Viewed(browsedSku: String, cartSkus: Option[Seq[String]] = None, compatibleDevice: String)

object ViewedJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val PortofolioFormats = jsonFormat3(Viewed)
}



