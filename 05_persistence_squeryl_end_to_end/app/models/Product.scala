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
case class Product(ean: Long, name: String, description: String) extends KeyedEntity[Long] {
  override val id = 0L

  override def toString = "%s - %s - %s - %s".format(id, ean, name, description)

  override def equals(obj: Any): Boolean = obj match {
    case product: Product =>
      ean.equals(product.ean) && name.equals(product.name) && description.equals(product.description)
    case _ => false
  }
}

/**
 * Products data access
 */
object Product {

  import Database.{productsTable}

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
  def remove(product: Product) = inTransaction {
    productsTable.delete(product.id)
  }

  /**
   * Adds a product to the catalog.
   */
  def insert(product: Product) = inTransaction {
    val defensiveCopy = product.copy()
    productsTable.insert(defensiveCopy)
    defensiveCopy
  }

  /**
   * Updates a product in the catalog.
   */
  def update(product: Product) {
    inTransaction {
      productsTable.update(product)
    }
  }

  def removeAll = {
    inTransaction {
      productsTable.deleteWhere(row => 1 === 1)
    }
  }
}