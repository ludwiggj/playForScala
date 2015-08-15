package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema

object DatabaseStateless extends Schema {
  val productsTable = table[ProductStateless]("products")
  val warehousesTable = table[WarehouseStateless]("warehouses")
  val stockItemsTable = table[StockItemStateless]("stock_items")

  on(this.productsTable) { p =>
    declare(
      p.id is (autoIncremented),
      p.ean is (unique)
    )
  }

  on(this.warehousesTable) { w =>
    declare(
      w.id is (autoIncremented),
      w.name is (unique)
    )
  }

  on(this.stockItemsTable) { s =>
    declare(
      s.id is (autoIncremented),
      columns(s.product_id, s.warehouse_id) are (unique)
    )
  }

  val productToStockItems =
    oneToManyRelation(productsTable, stockItemsTable).via((p, s) =>
      p.id === s.product_id
    )

  val warehouseToStockItems =
    oneToManyRelation(warehousesTable, stockItemsTable).via((w, s) =>
      w.id === s.warehouse_id
    )
}