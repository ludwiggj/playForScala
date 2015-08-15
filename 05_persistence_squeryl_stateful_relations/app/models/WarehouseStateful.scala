package models

import models.DatabaseStateful._
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

case class WarehouseStateful(name: String) extends KeyedEntity[Long] {
  override val id = 0L

  override def toString = "%s - %s".format(id, name)

  lazy val stockItems = DatabaseStateful.warehouseToStockItems.leftStateful(this)
}

object WarehouseStateful {

  /**
   * Adds a warehouse to the catalog.
   */
  def insert(warehouse: WarehouseStateful) = inTransaction {
    val defensiveCopy = warehouse.copy()
    warehousesTable.insert(defensiveCopy)
    defensiveCopy
  }
}