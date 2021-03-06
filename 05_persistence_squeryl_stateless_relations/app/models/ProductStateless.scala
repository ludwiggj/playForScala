package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.KeyedEntity

/**
 * An entry in the product catalogue.
 *
 * @param ean EAN-13 code - a unique product identifier
 * @param name Product name
 * @param description Product description
 */
case class ProductStateless(ean: Long, name: String, description: String) extends KeyedEntity[Long] {
  override val id = 0L

  override def toString = "%s - %s - %s - %s".format(id, ean, name, description)

  override def equals(obj: Any): Boolean = obj match {
    case product: ProductStateless =>
      ean.equals(product.ean) && name.equals(product.name) && description.equals(product.description)
    case _ => false
  }

  lazy val stockItems = DatabaseStateless.productToStockItems.left(this)
}

/**
 * Products data access
 */
object ProductStateless {

  import DatabaseStateless.{productsTable, stockItemsTable}

  /**
   * Query that finds all products
   */
  def allQ = from(productsTable)(product =>
    select(product) orderBy (product.ean)
  )

  /**
   * Products sorted by EAN code.
   */
  def findAll = inTransaction {
    allQ.toList
  }

  def productsInWarehouseQ(warehouse: WarehouseStateless) = {
    join(productsTable, stockItemsTable)((product, stockItem) =>
      where(stockItem.warehouse_id === warehouse.id)
        select (product)
        on (stockItem.product_id === product.id)
    )
  }

  def productsInWarehouse(warehouse: WarehouseStateless) = inTransaction {
    productsInWarehouseQ(warehouse).toList
  }

  def productsInWarehouseByNameQ(productName: String, warehouse: WarehouseStateless) = {
    from(productsInWarehouseQ(warehouse))(product =>
      where(product.name like productName)
        select (product)
    )
  }

  def productsInWarehouseByName(productName: String, warehouse: WarehouseStateless) = inTransaction {
    productsInWarehouseByNameQ(productName, warehouse).toList
  }

  /**
   * The product with the given EAN code.
   */
  def findByEan(ean: Long) = inTransaction {
    from(productsTable)(p =>
      where(p.ean === ean)
        select (p)
    ).headOption
  }

  /**
   * Products whose name matches the given query.
   */
  def findByName(query: String) = inTransaction {
    from(productsTable)(p =>
      where(p.name like ("%" + query + "%"))
        select (p)
    ).toList
  }

  /**
   * Deletes a product from the catalog.
   */
  def remove(product: ProductStateless) = inTransaction {
    productsTable.delete(product.id)
  }

  /**
   * Adds a product to the catalog.
   */
  def insert(product: ProductStateless) = inTransaction {
    val defensiveCopy = product.copy()
    productsTable.insert(defensiveCopy)
    defensiveCopy
  }

  /**
   * Updates a product in the catalog.
   */
  def update(product: ProductStateless) {
    inTransaction {
      productsTable.update(product)
    }
  }

  def getStockItems(product: ProductStateless) = inTransaction {
    product.stockItems.toList
  }

  def getStockItemsWithQuantityExceeding(product: ProductStateless, quantity: Long) = inTransaction {
    from(product.stockItems)(s =>
      where(s.quantity gt quantity)
        select (s)
    ).toList
  }
}