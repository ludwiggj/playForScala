package models

import models.Database._
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

case class StockItem(product_id: Long, warehouse_id: Long, quantity: Long) extends KeyedEntity[Long] {
  override val id = 0L
}

object StockItem {

  /**
   * The stock item with the given name.
   */
  def findByProductId(productId: Long) = inTransaction {
    from(stockItemsTable)(si =>
      where(si.product_id === productId)
        select (si)
    ).headOption
  }

  /**
   * Adds a stock item to the catalog.
   */
  def insert(item: StockItem) = inTransaction {
    stockItemsTable.insert(item.copy())
    findByProductId(item.product_id)
  }
}