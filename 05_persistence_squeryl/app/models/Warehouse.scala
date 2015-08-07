package models

import models.Database._
import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

case class Warehouse(name: String) extends KeyedEntity[Long] {
  override val id = 0L
}

object Warehouse {

  /**
   * The warehouse with the given name.
   */
  def findByName(name: String) = inTransaction {
    from(warehousesTable)(w =>
      where(w.name === name)
        select (w)
    ).headOption
  }

  /**
   * Adds a warehouse to the catalog.
   */
  def insert(warehouse: Warehouse) = inTransaction {
    warehousesTable.insert(warehouse.copy())
    findByName(warehouse.name).get
  }
}