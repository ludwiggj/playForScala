package test

import org.specs2.mutable.Specification
import models.Product
import play.api.db.DB
import play.api.Play.current
import java.sql.ResultSet

class ProductSpec extends Specification with DatabaseHelpers {
  "Products" should {

    "be able to be inserted" in EmptySchema {
      val product = Product(1, 128193849l, "P3000", "fancy paperclip")
      val inserted = Product.insert(product)
      var products = List[Product]()
      DB.withConnection { connection =>
        val results: ResultSet = connection.createStatement().executeQuery("select * from products")
        results.first()
        while (!results.isAfterLast()) {
          val id = results.getLong("id")
          val ean = results.getLong("ean")
          val name = results.getString("name")
          val description = results.getString("description")
          products = Product(id, ean, name, description) :: products
          results.next()
        }
      }

      inserted must beTrue
      products must contain(exactly(product))
    }

    "be able to be retrieved" in SingleProduct {
      Product.getAll must contain(exactly(this.product))
    }

    "be able to be retrieved (Pattern matching)" in SingleProduct {
      Product.getAllWithPatterns must contain(exactly(this.product))
    }

    "be able to be retrieved (Parser API)" in SingleProduct {
      Product.getAllWithParser must contain(exactly(this.product))
    }

    "be retrievable in bulk" in SeveralProducts {
      // Must contain same elements, but in any order... (superfluous, really)
      Product.getAll must containTheSameElementsAs(this.products)
    }

    "be retrievable in bulk sorted" in SeveralProducts {
      Product.getAll must contain(allOf(this.products.sortBy(_.name): _*).inOrder)
    }

    "be retrievable in bulk sorted (Pattern matching)" in SeveralProducts {
      Product.getAllWithPatterns must contain(allOf(this.products.sortBy(_.name): _*).inOrder)
    }

    "be retrievable in bulk sorted (Parser API)" in SeveralProducts {
      Product.getAllWithParser must contain(allOf(this.products.sortBy(_.name): _*).inOrder)
    }

    "be retrievable by id" in SeveralProducts {
      Product.findById(4) must beSome(this.products(4))
      Product.findById(40) must beNone
    }

    "be able to be deleted" in SeveralProducts {
      val deletedProduct = this.products(7)
      Product.delete(deletedProduct)
      val remainingProducts = Product.getAllWithPatterns

      remainingProducts must contain(deletedProduct).not
      remainingProducts must have length (this.products.size - 1)
    }

    "be able to be updated" in SeveralProducts {
      val Some(product) = Product.findById(8)
      val copy = product.copy(name = "Paperclip NG")
      Product.update(copy)

      val Some(updatedProduct) = Product.findById(8)

      updatedProduct must equalTo(copy)
      Product.findById(9).get must equalTo("Paperclip NG").not
    }

    "be queryable on 8" in SeveralProducts {
      val foundProducts = Product.search("8")

      foundProducts must have length (1)
      foundProducts(0).name must equalTo("P8")
    }

    "be queryable on P8" in SeveralProducts {
      val foundProducts = Product.search("P8")

      foundProducts must have length (1)
      foundProducts(0).name must equalTo("P8")
    }

    "be queryable on paperclip" in SeveralProducts {
      val foundProducts = Product.search("paperclip")

      foundProducts must have length (this.products.size)
    }
  }
}