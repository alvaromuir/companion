package com.verizon.itanalytics.dataengineering.companion

import akka.http.scaladsl.model.StatusCodes
import com.verizon.itanalytics.dataengineering.companion.Tables.Affinity

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

  case class Recommendation(
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

  implicit object RecommendationJsonFormat extends RootJsonFormat[Recommendation] {

    def write(recommendation: Recommendation) = recommendation.recommendedSku match {
      case Some(recommendedSkus) => JsObject(
        Map(
          "status" -> JsNumber(recommendation.status),
          "msg" -> JsString(recommendation.msg),
          "recommendedSku" -> JsArray(recommendation.recommendedSku.get.map(_.toJson).toVector)
        )
      )

      case None => JsObject(Map("status" -> JsNumber(recommendation.status), "msg" -> JsString(recommendation.msg)))
    }

    override def read(json: JsValue): Recommendation = ???
  }

  def jsonize(rslts: AnyRef): String = {
    rslts match {
      case s: String =>
        Recommendation(
          status = StatusCodes.OK.intValue,
          msg = s
        ).toJson.compactPrint

      case r: Seq[Affinity] =>
        Recommendation(
          status = StatusCodes.OK.intValue,
          msg = StatusCodes.OK.defaultMessage,
          recommendedSku = Option(r.zipWithIndex.map {
            case (rslt, index) =>
              RecommendedSku(id = rslt.RCMND_ITEM_CD, recNum = index + 1)
          })
        ).toJson.compactPrint

      case _: Throwable =>
        Recommendation(
          status = StatusCodes.InternalServerError.intValue,
          msg = StatusCodes.InternalServerError.defaultMessage
        ).toJson.compactPrint

    }
  }

}
