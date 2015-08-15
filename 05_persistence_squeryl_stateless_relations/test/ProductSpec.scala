import java.sql.ResultSet

import org.specs2.mutable.Specification
import models.{StockItemStateless, ProductStateless}
import play.api.db.DB
import play.api.Play.current

class ProductSpec extends Specification with DatabaseHelpers {
  "Products" should {

    "be able to be inserted" in EmptySchema {
      val product = ProductStateless(128193849l, "P3000", "fancy paperclip")
      val inserted = ProductStateless.insert(product)
      var products = List[ProductStateless]()
      DB.withConnection { connection =>
        val results: ResultSet = connection.createStatement().executeQuery("select * from products")
        results.first()
        while (!results.isAfterLast()) {
          val id = results.getLong("id")
          val ean = results.getLong("ean")
          val name = results.getString("name")
          val description = results.getString("description")
          products = ProductStateless(ean, name, description) :: products
          results.next()
        }
      }

      products must contain(exactly(product))
    }

    "be able to be retrieved" in SingleProduct {
      ProductStateless.findAll must contain(exactly(this.product))
    }

    "be retrievable in bulk" in SeveralProducts {
      // Must contain same elements, but in any order... (superfluous, really)
      ProductStateless.findAll must containTheSameElementsAs(this.products)
    }

    "be retrievable in bulk sorted" in SeveralProducts {
      ProductStateless.findAll must contain(allOf(this.products.sortBy(_.ean): _*).inOrder)
    }

    "be queryable on warehouse" in WarehousesStockItemsAndProducts {

      ProductStateless.productsInWarehouse(warehouseForbiddenPlanet) must
        containTheSameElementsAs(List(productOpticNerve, productSandman, productStarman))
    }

    "be queryable on warehouse by product name" in WarehousesStockItemsAndProducts {
      ProductStateless.productsInWarehouseByName(productStarman.name, warehouseGosh) must
        containTheSameElementsAs(List(productStarman))
    }

    "be able to retrieve stock items" in WarehousesStockItemsAndProducts {
      ProductStateless.getStockItems(productOpticNerve) must
        containTheSameElementsAs(List(
          StockItemStateless(1, productOpticNerve.id, warehouseForbiddenPlanet.id),
          StockItemStateless(2, productOpticNerve.id, warehouseGosh.id)))
    }

    "be able to retrieve stock items exceeding or eqial to stated quantity" in WarehousesStockItemsAndProducts {
      ProductStateless.getStockItemsWithQuantityExceeding(productOpticNerve, 1) must
        containTheSameElementsAs(List(
          StockItemStateless(2, productOpticNerve.id, warehouseGosh.id)))
    }

    "be retrievable by ean" in SeveralProducts {
      ProductStateless.findByEan(84928173l) must beSome(this.products(0))
      ProductStateless.findByEan(40) must beNone
    }

    "be retrievable by name" in SeveralProducts {
      ProductStateless.findByName("paperclip 0") must containTheSameElementsAs(this.products.take(0))
      ProductStateless.findByName("paperclip -1") must beEmpty
    }

    "be able to be removed" in SeveralProducts {
      val deletedProduct = this.products(7)
      ProductStateless.remove(deletedProduct)
      val remainingProducts = ProductStateless.findAll

      remainingProducts must contain(deletedProduct).not
      remainingProducts must have length (this.products.size - 1)
    }

    "be able to be updated" in SeveralProducts {
      val Some(product) = ProductStateless.findByEan(84928181l)

      val productCopy = new ProductStateless(product.ean, "Paperclip NG", product.description) {
        override val id = product.id
      }

      ProductStateless.update(productCopy)

      val Some(updatedProduct) = ProductStateless.findByEan(84928181l)

      updatedProduct must equalTo(productCopy)
      ProductStateless.findByEan(84928182l).get must equalTo("Paperclip NG").not
    }
  }
}