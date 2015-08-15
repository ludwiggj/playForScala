import models.{ProductStateless, WarehouseStateless, StockItemStateless}
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
  val product = ProductStateless(342545645l, "P1", "simple paperclip")
  var products = 0 to 10 map { i => ProductStateless(84928173l + i, "P" + i, "paperclip " + i) }

  var warehouseForbiddenPlanet = WarehouseStateless("Forbidden Planet")
  var warehouseGosh = WarehouseStateless("Gosh")

  var productOpticNerve = ProductStateless(1l, "Optic Nerve", "A depressing comic")
  var productSandman = ProductStateless(2l, "Sandman", "A classic")
  var productStarman = ProductStateless(3l, "Starman", "An revisionist take on a minor character from the 50s")

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
      ProductStateless.insert(product)

      test
    }
  }

  object WarehousesStockItemsAndProducts extends Schema {

    override def around[T: AsResult](test: => T) = super.around {

      productOpticNerve = ProductStateless.insert(productOpticNerve)
      productSandman = ProductStateless.insert(productSandman)
      productStarman = ProductStateless.insert(productStarman)

      warehouseForbiddenPlanet = WarehouseStateless.insert(warehouseForbiddenPlanet)
      warehouseGosh = WarehouseStateless.insert(warehouseGosh)

      val warehouseCentralCityComics = WarehouseStateless.insert(WarehouseStateless("Central City Comics"))

      StockItemStateless.insert(StockItemStateless(1), productOpticNerve, warehouseForbiddenPlanet)
      StockItemStateless.insert(StockItemStateless(3), productSandman, warehouseForbiddenPlanet)
      StockItemStateless.insert(StockItemStateless(5), productStarman, warehouseForbiddenPlanet)

      StockItemStateless.insert(StockItemStateless(7), productSandman, warehouseCentralCityComics)

      StockItemStateless.insert(StockItemStateless(2), productOpticNerve, warehouseGosh)
      StockItemStateless.insert(StockItemStateless(4), productStarman, warehouseGosh)

      test
    }
  }

  object SeveralProducts extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      products = products.map {
        ProductStateless.insert(_)
      }

      test
    }
  }
}