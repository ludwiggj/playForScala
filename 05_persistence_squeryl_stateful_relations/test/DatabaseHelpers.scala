import models.{ProductStateful, WarehouseStateful, StockItemStateful}
import org.specs2.execute.AsResult
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Around
import org.specs2.specification.mutable.SpecificationFeatures
import play.api.db.DB
import play.api.test.FakeApplication
import play.api.test.Helpers._
import play.api.Play.current

import scala.io.Source

trait DatabaseHelpers {
  this: SpecificationFeatures =>
  val product = ProductStateful(342545645l, "P1", "simple paperclip")
  var products = 0 to 10 map { i => ProductStateful(84928173l + i, "P" + i, "paperclip " + i) }

  var warehouseForbiddenPlanet = WarehouseStateful("Forbidden Planet")
  var warehouseGosh = WarehouseStateful("Gosh")

  var productOpticNerve = ProductStateful(1l, "Optic Nerve", "A depressing comic")
  var productSandman = ProductStateful(2l, "Sandman", "A classic")
  var productStarman = ProductStateful(3l, "Starman", "An revisionist take on a minor character from the 50s")

  trait Schema extends Around {

    val sqlFiles = List("1.sql", "2.sql", "3.sql")

    val ddls = for {
      sqlFile <- sqlFiles
      evolutionContent = Source.fromFile(s"conf/evolutions/paperclips/$sqlFile").getLines.mkString("\n")
      splitEvolutionContent = evolutionContent.split("# --- !Ups")
      upsDowns = splitEvolutionContent(1).split("# --- !Downs")
    } yield (upsDowns(1), upsDowns(0))

    val dropDdls = (ddls map { _._1 }).reverse
    val createDdls = ddls map { _._2 }

    def dropCreateDb() = {
      DB.withConnection { implicit connection =>

        for (ddl <- dropDdls ++ createDdls) {
          connection.createStatement.execute(ddl)
        }
      }
    }

    def around[T: AsResult](test: => T) =
      running(FakeApplication()) {
        dropCreateDb()

        test.asInstanceOf[MatchResult[T]].toResult
      }
  }

  object EmptySchema extends Schema {
  }

  object SingleProduct extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      ProductStateful.insert(product)

      test
    }
  }

  object WarehousesStockItemsAndProducts extends Schema {

    override def around[T: AsResult](test: => T) = super.around {

      productOpticNerve = ProductStateful.insert(productOpticNerve)
      productSandman = ProductStateful.insert(productSandman)
      productStarman = ProductStateful.insert(productStarman)

      warehouseForbiddenPlanet = WarehouseStateful.insert(warehouseForbiddenPlanet)
      warehouseGosh = WarehouseStateful.insert(warehouseGosh)

      val warehouseCentralCityComics = WarehouseStateful.insert(WarehouseStateful("Central City Comics"))

      StockItemStateful.insert(StockItemStateful(1), productOpticNerve, warehouseForbiddenPlanet)
      StockItemStateful.insert(StockItemStateful(3), productSandman, warehouseForbiddenPlanet)
      StockItemStateful.insert(StockItemStateful(5), productStarman, warehouseForbiddenPlanet)

      StockItemStateful.insert(StockItemStateful(7), productSandman, warehouseCentralCityComics)

      StockItemStateful.insert(StockItemStateful(2), productOpticNerve, warehouseGosh)
      StockItemStateful.insert(StockItemStateful(4), productStarman, warehouseGosh)

      test
    }
  }

  object SeveralProducts extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      products = products.map {
        ProductStateful.insert(_)
      }

      test
    }
  }
}