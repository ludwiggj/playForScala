import models.{Warehouse, StockItem, Product}
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
  val product = Product(342545645l, "P1", "simple paperclip")
  val products = 0 to 10 map { i => Product(84928173l + i, "P" + i, "paperclip " + i) }
  val stockItems = 0 to 2 map { i => StockItem(product.id, 21645 + i, 5 + 2 * i) }

  val forbiddenPlanet = Warehouse("Forbidden Planet")
  val gosh = Warehouse("Gosh")

  val opticNerve = Product(1l, "Optic Nerve", "A depressing comic")
  val sandman = Product(2l, "Sandman", "A classic")
  val starman = Product(3l, "Starman", "An revisionist take on a minor character from the 50s")

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
      Product.insert(product)

      test
    }
  }

  //  object SingleProductWithStockItems extends Schema {
  //    override def around[T: AsResult](test: => T) = super.around {
  //      Product.insert(product)
  //
  //      stockItems.foreach {
  //        StockItem.insert(_)
  //      }
  //
  //      test
  //    }
  //  }

  object WarehousesStockItemsAndProducts extends Schema {

    override def around[T: AsResult](test: => T) = super.around {

      val opticNerveId = Product.insert(opticNerve).id
      val sandmanId = Product.insert(sandman).id
      val starmanId = Product.insert(starman).id

//      forbiddenPlanet = Warehouse.insert(forbiddenPlanet)
      Warehouse.insert(forbiddenPlanet)
      val forbiddenPlanetId = forbiddenPlanet.id

//      gosh = Warehouse.insert(gosh)
      Warehouse.insert(gosh)
      val goshId = gosh.id

      val centralCityComicsId = Warehouse.insert(Warehouse("Central City Comics")).id

      StockItem.insert(StockItem(opticNerveId, forbiddenPlanetId, 1))
      StockItem.insert(StockItem(sandmanId, forbiddenPlanetId, 3))
      StockItem.insert(StockItem(starmanId, forbiddenPlanetId, 5))

      StockItem.insert(StockItem(sandmanId, centralCityComicsId, 7))

      StockItem.insert(StockItem(opticNerveId, goshId, 2))
      StockItem.insert(StockItem(starmanId, goshId, 4))

      test
    }
  }

  object SeveralProducts extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      products.foreach {
        Product.insert(_)
      }

      test
    }
  }
}