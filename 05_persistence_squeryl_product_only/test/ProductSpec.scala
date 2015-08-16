import java.sql.ResultSet

import org.specs2.mutable.Specification
import models.Product
import play.api.db.DB
import play.api.Play.current

class ProductSpec extends Specification with DatabaseHelpers {
  "Products" should {

    "be able to be inserted" in EmptySchema {
      val product = Product(128193849l, "P3000", "fancy paperclip")
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
          products = Product(ean, name, description) :: products
          results.next()
        }
      }

      products must contain(exactly(product))
    }

    "be able to be retrieved" in SingleProduct {
      Product.findAll must contain(exactly(this.product))
    }

    "be retrievable in bulk" in SeveralProducts {
      // Must contain same elements, but in any order... (superfluous, really)
      Product.findAll must containTheSameElementsAs(this.products)
    }

    "be retrievable in bulk sorted" in SeveralProducts {
      Product.findAll must contain(allOf(this.products.sortBy(_.ean): _*).inOrder)
    }

    "be retrievable by ean" in SeveralProducts {
      Product.findByEan(84928173l) must beSome(this.products(0))
      Product.findByEan(40) must beNone
    }

    "be retrievable by name" in SeveralProducts {
      Product.findByName("paperclip 0") must containTheSameElementsAs(this.products.take(0))
      Product.findByName("paperclip -1") must beEmpty
    }

    "be able to be removed" in SeveralProducts {
      val deletedProduct = this.products(7)
      Product.remove(deletedProduct)
      val remainingProducts = Product.findAll

      remainingProducts must contain(deletedProduct).not
      remainingProducts must have length (this.products.size - 1)
    }

    "be able to be updated" in SeveralProducts {
      val Some(product) = Product.findByEan(84928181l)

      val productCopy = new Product(product.ean, "Paperclip NG", product.description) {
        override val id = product.id
      }

      Product.update(productCopy)

      val Some(updatedProduct) = Product.findByEan(84928181l)

      updatedProduct must equalTo(productCopy)
      Product.findByEan(84928182l).get must equalTo("Paperclip NG").not
    }
  }
}