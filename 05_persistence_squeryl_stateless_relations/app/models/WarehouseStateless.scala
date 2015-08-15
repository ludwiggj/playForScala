package models

import models.DatabaseStateless._
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

case class WarehouseStateless(name: String) extends KeyedEntity[Long] {
  override val id = 0L

  override def toString = "%s - %s".format(id, name)

  lazy val stockItems = DatabaseStateless.warehouseToStockItems.left(this)
}

object WarehouseStateless {

  /**
   * Adds a warehouse to the catalog.
   */
  def insert(warehouse: WarehouseStateless) = inTransaction {
    val defensiveCopy = warehouse.copy()
    warehousesTable.insert(defensiveCopy)
    defensiveCopy
  }
}