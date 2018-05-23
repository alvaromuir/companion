package com.verizon.itanalytics.dataengineering.companion

import java.nio.file.Paths

import akka.Done
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl._
import akka.stream.scaladsl._

import com.typesafe.config.{Config, ConfigFactory}

import slick.jdbc.H2Profile.api._


import org.slf4j.{Logger, LoggerFactory}

import Companion.system

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}



/*
 * companion v.1
 * Alvaro Muir, Verizon IT Analytics: Data Engineering
 * 05 19, 2018
 */

object Tables {

  private val config: Config = ConfigFactory.load()
  private val dataUploadPath: String = config.getString("http.dataUploadPath")
  private val srcDataFileName: String = config.getString("db.srcDataFileName")
  private val filePath = s"$dataUploadPath/$srcDataFileName"

  val log: Logger = LoggerFactory.getLogger(getClass.getName)

  final case class Affinity(
      SELECTION_SKU_0: String,
      SELECTION_ITEM_DESC_0: String,
      SELECTION_CATEGORY_0: String,
      SELECTION_SKU_1: Option[String] = None,
      SELECTION_ITEM_DESC_1: Option[String] = None,
      SELECTION_CATEGORY_1: Option[String] = None,
      RCMND_ITEM_CD: String,
      RCMND_ITEM_DESC: String,
      RCMND_EQP_CTGRY_CD: String,
      LIFT: Double,
      CONFIDENCE: Double,
      id: Int = 0
  )

  final class AffinityTable(tag: Tag) extends Table[Affinity](tag, "affinity") { // table name to config?
    def id =
      column[Int]("id", O.PrimaryKey, O.AutoInc) // I added this as a PKey
    def selectionSku0 = column[String]("SELECTION_SKU_0")
    def selectionItemDesc0 = column[String]("SELECTION_ITEM_DESC_0")
    def selectionCategory0 = column[String]("SELECTION_CATEGORY_0")
    def selectionSku1 = column[Option[String]]("SELECTION_SKU_1")
    def selectionItemDesc1 = column[Option[String]]("SELECTION_ITEM_DESC_1")
    def selectionCategory1 = column[Option[String]]("SELECTION_CATEGORY_1")
    def rcmdItemCd = column[String]("RCMND_ITEM_CD")
    def rcmdItemDesc = column[String]("RCMND_ITEM_DESC")
    def rcmdEqpCtgrCd = column[String]("RCMND_EQP_CTGRY_CD")
    def lift = column[Double]("LIFT")
    def confidence = column[Double]("CONFIDENCE")

    def * =
      (selectionSku0,
       selectionItemDesc0,
       selectionCategory0,
       selectionSku1,
       selectionItemDesc1,
       selectionCategory1,
       rcmdItemCd,
       rcmdItemDesc,
       rcmdEqpCtgrCd,
       lift,
       confidence,
       id)
        .mapTo[Affinity]
  }

  lazy val affinities = TableQuery[AffinityTable]

  lazy val db = Database.forConfig("db")

  /**
    * Helper function that awaits a database execution after desired time period. Defaults to 2 seconds
    * @param action database query
    * @param timeOut FiniteDuration time period
    * @tparam T Type of database call result
    * @return T Type
    */
  def exec[T](action: DBIO[T], timeOut: FiniteDuration = 2.seconds): T = Await.result(db.run(action), timeOut)

  /**
    * A Future that initializes a database and populates table with designated CSV data set
    * @param numRows row limit of insertion, defaults to None - which is equivalent to all rows in CSV data set
    * @param dataSet file path of CSV data set
    * @return Future[Unit]
    */
  def seedTable(table:TableQuery[AffinityTable] = affinities, numRows: Option[Int] = None, dataSet: String = filePath): Future[Unit] = {
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher



    db.run(table.schema.create)

    val limit = numRows match {
      case None => io.Source.fromFile(dataSet).getLines.size
      case _    => numRows.get
    }
    var tableData = new ListBuffer[Affinity]()

    Future(FileIO
      .fromPath(Paths.get(dataSet))
      .via(CsvParsing.lineScanner())
      .map(_.map(_.utf8String.trim))
      .drop(1)
      .map { x =>
        Affinity(x.head,
                       x(1),
                       x(2),
                       Option(x(3)),
                       Option(x(4)),
                       Option(x(5)),
                       x(6),
                       x(7),
                       x(8),
                       x(9).toDouble,
                       x(10).toDouble)
      }
      .take(limit)
      .runForeach { tableData += _ }
      .onComplete {
        case Success(Done) =>
          db.run(table ++= tableData)
        case Failure(e) => println(s"Stream failed with $e.")
      })
  }

  def reSeedTable(table:TableQuery[AffinityTable] = affinities, numRows: Option[Int] = None, dataSet: String = filePath): Future[Unit] = {
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    exec(table.schema.truncate, 5.seconds)
    log.info(s"${table.baseTableRow.tableName} table truncated.")

    seedTable(table, numRows, dataSet)

  }



}
