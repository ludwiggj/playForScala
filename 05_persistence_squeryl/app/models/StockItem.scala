package models

import models.Database._
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

case class StockItem(product_id: Long, warehouse_id: Long, quantity: Long) extends KeyedEntity[Long] {
  override val id = 0L
}

object StockItem {

  /**
   * Adds a stock item to the catalog.
   */
  def insert(item: StockItem) = inTransaction {
    val defensiveCopy = item.copy()
    stockItemsTable.insert(defensiveCopy)
    defensiveCopy
  }
}