package com.verizon.itanalytics.dataengineering.companion

/*
* companion v.1
* Alvaro Muir, Verizon IT Analytics: Data Engineering
* 05 16, 2018
*/


trait Utils {

  object Listify {
    import spray.json._
    def apply(json: JsValue): List[Any] = listify(json)

    /**
      * Returns a list collection from a spray.json.JsValue
      * @param json Spray JsValue
      * @return List[Any]
      */
    def listify(json: JsValue): List[Any] = {
      val observation = json.toString.stripMargin.parseJson.asJsObject
      observation.getClass.getDeclaredFields.map(_.getName).zip(observation.productIterator.to).toMap.get("fields")
        .head
        .asInstanceOf[Map[String, Any]]
        .values
        .toList
    }
  }

  //sourced from https://gist.github.com/sam/5213151
  object Slugify {
    import java.text.Normalizer
    def apply(input:String): String = slugify(input)

    /**
      * Returns a url-friendly-string from a string input
      * @param input string to modify
      * @return Modified String
      */
    def slugify(input: String): String = {

      Normalizer.normalize(input, Normalizer.Form.NFD)
        .replaceAll("[^\\w\\s-]", "")
        .replace('-', ' ')
        .replace("\n", "").replace("\r", "")
        .trim
        .replaceAll("\\s+", "-")
        .toLowerCase
    }
  }

  // courtesy of sumologic: https://www.sumologic.com/blog/code/why-you-should-never-catch-throwable-in-scala/
  object Safely {
    import scala.util.control.ControlThrowable
    def apply[T](handler: PartialFunction[Throwable, T]): PartialFunction[Throwable, T] = safely[T](handler)

    /** Returns a partial throwable function of type T to catch errors as required by cases under @directives
      *
      * @param handler desired exception handler
      * @tparam T abstract type
      * @return PartialFunction[Throwable, T]
      */
    def safely[T](handler: PartialFunction[Throwable, T]): PartialFunction[Throwable, T] = {
      case ex: ControlThrowable                     => throw ex
      case ex: Throwable if handler.isDefinedAt(ex) => handler(ex)
      case ex: Throwable                            => throw ex // un-necessary, but for clarity
    }
  }
}
