import java.sql.ResultSet

import org.specs2.mutable.Specification
import models.{StockItemStateful, ProductStateful}
import play.api.db.DB
import play.api.Play.current

class ProductSpec extends Specification with DatabaseHelpers {
  "Products" should {

    "be able to be inserted" in EmptySchema {
      val product = ProductStateful(128193849l, "P3000", "fancy paperclip")
      val inserted = ProductStateful.insert(product)
      var products = List[ProductStateful]()
      DB.withConnection { connection =>
        val results: ResultSet = connection.createStatement().executeQuery("select * from products")
        results.first()
        while (!results.isAfterLast()) {
          val id = results.getLong("id")
          val ean = results.getLong("ean")
          val name = results.getString("name")
          val description = results.getString("description")
          products = ProductStateful(ean, name, description) :: products
          results.next()
        }
      }

      products must contain(exactly(product))
    }

    "be able to be retrieved" in SingleProduct {
      ProductStateful.findAll must contain(exactly(this.product))
    }

    "be retrievable in bulk" in SeveralProducts {
      // Must contain same elements, but in any order... (superfluous, really)
      ProductStateful.findAll must containTheSameElementsAs(this.products)
    }

    "be retrievable in bulk sorted" in SeveralProducts {
      ProductStateful.findAll must contain(allOf(this.products.sortBy(_.ean): _*).inOrder)
    }

    "be queryable on warehouse" in WarehousesStockItemsAndProducts {

      ProductStateful.productsInWarehouse(warehouseForbiddenPlanet) must
        containTheSameElementsAs(List(productOpticNerve, productSandman, productStarman))
    }

    "be queryable on warehouse by product name" in WarehousesStockItemsAndProducts {
      ProductStateful.productsInWarehouseByName(productStarman.name, warehouseGosh) must
        containTheSameElementsAs(List(productStarman))
    }

    "be able to retrieve stock items" in WarehousesStockItemsAndProducts {
      ProductStateful.getStockItems(productOpticNerve) must
        containTheSameElementsAs(List(
          StockItemStateful(1, productOpticNerve.id, warehouseForbiddenPlanet.id),
          StockItemStateful(2, productOpticNerve.id, warehouseGosh.id)))
    }

    "be able to retrieve stock items exceeding or eqial to stated quantity" in WarehousesStockItemsAndProducts {
      ProductStateful.getStockItemsWithQuantityExceeding(productOpticNerve, 1) must
        containTheSameElementsAs(List(
          StockItemStateful(2, productOpticNerve.id, warehouseGosh.id)))
    }

    "be retrievable by ean" in SeveralProducts {
      ProductStateful.findByEan(84928173l) must beSome(this.products(0))
      ProductStateful.findByEan(40) must beNone
    }

    "be retrievable by name" in SeveralProducts {
      ProductStateful.findByName("paperclip 0") must containTheSameElementsAs(this.products.take(0))
      ProductStateful.findByName("paperclip -1") must beEmpty
    }

    "be able to be removed" in SeveralProducts {
      val deletedProduct = this.products(7)
      ProductStateful.remove(deletedProduct)
      val remainingProducts = ProductStateful.findAll

      remainingProducts must contain(deletedProduct).not
      remainingProducts must have length (this.products.size - 1)
    }

    "be able to be updated" in SeveralProducts {
      val Some(product) = ProductStateful.findByEan(84928181l)

      val productCopy = new ProductStateful(product.ean, "Paperclip NG", product.description) {
        override val id = product.id
      }

      ProductStateful.update(productCopy)

      val Some(updatedProduct) = ProductStateful.findByEan(84928181l)

      updatedProduct must equalTo(productCopy)
      ProductStateful.findByEan(84928182l).get must equalTo("Paperclip NG").not
    }
  }
}