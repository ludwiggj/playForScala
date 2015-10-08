import models.Product
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
  var products = 0 to 10 map { i => Product(84928173l + i, "P" + i, "paperclip " + i) }

  trait Schema extends Around {

    val sqlFiles = List("1.sql")

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

  object SeveralProducts extends Schema {
    override def around[T: AsResult](test: => T) = super.around {
      products = products.map {
        Product.insert(_)
      }

      test
    }
  }
}