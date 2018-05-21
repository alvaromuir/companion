package com.verizon.itanalytics.dataengineering.companion

import akka.http.scaladsl.model.StatusCodes
import com.verizon.itanalytics.dataengineering.companion.Tables.Recommendation
import spray.json._

/*
 * companion v.1
 * Alvaro Muir, Verizon IT Analytics: Data Engineering
 * 05 20, 2018
 */



object JsonProtocol extends DefaultJsonProtocol with NullOptions {
  case class RecommendedSku(
                             id: String,
                             recNum: Int
                           )

  case class Response(
                       status: Int,
                       msg: String,
                       recommendedSku: Option[Seq[RecommendedSku]] = None
                     )



  implicit object RecommendedSkuFormat extends RootJsonFormat[RecommendedSku] {
    def write(recommendedSku: RecommendedSku) = JsObject(
      "id" -> JsString(recommendedSku.id),
      "recNum" -> JsNumber(recommendedSku.recNum)
    )

    override def read(json: JsValue): RecommendedSku = ???
  }

  implicit object ResponseJsonFormat extends RootJsonFormat[Response] {
    def write(response: Response) =
      JsObject(
        Map(
          "status" -> JsNumber(response.status),
          "msg" -> JsString(response.msg),
          "recommendedSku" -> JsArray(response.recommendedSku.get.map(_.toJson).toList)
        )
      )

    override def read(json: JsValue): Response = ???
  }

  def jsonize (rslts: Seq[Recommendation]): String = Response(
    status = StatusCodes.OK.intValue,
    msg = StatusCodes.OK.defaultMessage,
    recommendedSku = Option(rslts.zipWithIndex.map {
      case (rslt, index) => RecommendedSku(id = rslt.RCMND_ITEM_CD, recNum = index + 1)
    })
  ).toJson.compactPrint

}

