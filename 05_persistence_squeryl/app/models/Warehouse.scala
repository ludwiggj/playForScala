package models

import models.Database._
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

case class Warehouse(name: String) extends KeyedEntity[Long] {
  override val id = 0L
}

object Warehouse {

  /**
   * Adds a warehouse to the catalog.
   */
  def insert(warehouse: Warehouse) = inTransaction {
    val defensiveCopy = warehouse.copy()
    warehousesTable.insert(defensiveCopy)
    defensiveCopy
  }
}