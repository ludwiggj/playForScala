package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema

object Database extends Schema {
	val productsTable = table[Product]("products")

	on(this.productsTable) { p =>
		declare (
      p.id is(autoIncremented),
      p.ean is(unique)
    )
	}
}