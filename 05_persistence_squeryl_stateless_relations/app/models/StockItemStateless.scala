package models

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

case class StockItemStateless(quantity: Long, product_id: Long = 0L, warehouse_id: Long = 0L) extends KeyedEntity[Long] {
  override val id = 0L

  override def toString = "%s - %s - %s - %s".format(id, quantity, product_id, warehouse_id)

  override def equals(obj: Any): Boolean = obj match {
    case stockItem: StockItemStateless =>
      quantity.equals(stockItem.quantity) && product_id.equals(stockItem.product_id) && warehouse_id.equals(stockItem.warehouse_id)
    case _ => false
  }

  lazy val product = DatabaseStateless.productToStockItems.right(this)
  lazy val warehouse = DatabaseStateless.warehouseToStockItems.right(this)
}

object StockItemStateless {

  /**
   * Adds a stock item to the catalog.
   */
  def insert(item: StockItemStateless, product: ProductStateless, warehouse: WarehouseStateless) = transaction {
    val defensiveCopy = item.copy()
    product.stockItems.associate(defensiveCopy)
    warehouse.stockItems.associate(defensiveCopy)
    defensiveCopy
  }
}