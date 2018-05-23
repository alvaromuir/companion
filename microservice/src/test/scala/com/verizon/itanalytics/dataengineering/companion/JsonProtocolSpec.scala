package com.verizon.itanalytics.dataengineering.companion

import Tables._
import JsonProtocol._
import org.scalatest.FlatSpec

/*
 * companion
 * Alvaro Muir, Verizon IT Analytics: Data Engineering
 * 05 22, 2018
 */

class JsonProtocolSpec extends FlatSpec {

  "A results object" should
    "be convertible to a json string" in {

    val affinity = Affinity(
      SELECTION_SKU_0 = "item-0-SKU",
      SELECTION_ITEM_DESC_0 = "item 0 description",
      SELECTION_CATEGORY_0 = "item)0_category",
      SELECTION_SKU_1 = Option("item-1-SKU"),
      SELECTION_ITEM_DESC_1 = Option("item 1 description"),
      SELECTION_CATEGORY_1 = Option("item_1_category"),
      RCMND_ITEM_CD = "recommended-item-cd",
      RCMND_ITEM_DESC = "recommended item category",
      RCMND_EQP_CTGRY_CD = "recommended_equipment_category",
      LIFT = 1.1,
      CONFIDENCE = 42.0,
      id = 0
    )

    val rslt1 = jsonize(Seq(affinity))
    assert(rslt1.contains("""{"status":200,"msg":"OK","recommendedSku":[{"id":"recommended-item-cd","recNum":1}]}"""))

    val rslt2 = jsonize("just a test msg.")
    assert(rslt2.contains("""{"status":200,"msg":"just a test msg."}"""))

  }
}
