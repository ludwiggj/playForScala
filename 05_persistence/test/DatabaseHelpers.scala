package test

import com.avaje.ebean.Ebean
import models.Product
import org.specs2.execute.{AsResult, Result}
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Around
import org.specs2.specification.mutable.SpecificationFeatures
import play.api.test.FakeApplication
import play.api.test.Helpers._

import scala.io.Source

trait DatabaseHelpers {
  this: SpecificationFeatures =>
  val product = Product(1, 342545645l, "P1", "simple paperclip")
  val products = 0 to 10 map { i => Product(i, 84928173l + i, "P" + i, "paperclip " + i) }

  trait Schema extends Around {

    val sqlFiles = List("1.sql", "2.sql", "3.sql")

    val ddls = for {
      sqlFile <- sqlFiles
      evolutionContent = Source.fromFile(s"conf/evolutions/paperclips/$sqlFile").getLines.mkString("\n")
      splitEvolutionContent = evolutionContent.split("# --- !Ups")
      upsDowns = splitEvolutionContent(1).split("# --- !Downs")
    } yield (upsDowns(1), upsDowns(0))

    val dropDdls = (ddls map {_._1}).reverse
    val createDdls = ddls map {_._2}

    def dropCreateDb() = {
      for (dropDdl <- dropDdls) {
        Ebean.execute(Ebean.createCallableSql(dropDdl))
      }

      for (createDdl <- createDdls) {
        Ebean.execute(Ebean.createCallableSql(createDdl))
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
      products.foreach {
        Product.insert(_)
      }

      test
    }
  }
}