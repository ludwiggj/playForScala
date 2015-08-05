package models

// Anorm model
case class Product(
                    id: Long,
                    ean: Long,
                    name: String,
                    description: String)

// Companion object contains DAO code
object Product {

  // Anorm query

  import anorm.SQL
  import anorm.SqlQuery

  val sql: SqlQuery = SQL("select * from products order by name asc")

  // Using Anorm's stream API to process query results

  import play.api.Play.current
  import play.api.db.DB

  def getAll: List[Product] = DB.withConnection {
    implicit connection =>
      sql().map(row =>
        Product(row[Long]("id"), row[Long]("ean"),
          row[String]("name"), row[String]("description"))
      ).toList
  }

  // Query using Anorm pattern matching
  def getAllWithPatterns: List[Product] = DB.withConnection {
    implicit connection =>

      import anorm.Row

      sql().collect {
        // NOTE: Needed to change type of id and ean from Long to Int to get it to work
        case Row(Some(id: Int), Some(ean: Int),
        Some(name: String), Some(description: String)) =>
          Product(id, ean, name, description)
      }.toList
  }

  // Query using Anorm parsers

  // First a row parser to convert row into a product

  import anorm.RowParser

  val productParser: RowParser[Product] = {
    import anorm.~
    import anorm.SqlParser._

    long("id") ~ long("ean") ~ str("name") ~ str("description") map {
      case id ~ ean ~ name ~ description =>
        Product(id, ean, name, description)
    }
  }

  // But sql.as() method requires a ResultSetParser, so we need to build
  // a ResultSetParser from a RowParser

  import anorm.ResultSetParser

  val productsParser: ResultSetParser[List[Product]] = {
    // * means parse zero or more rows using productParser
    productParser *
  }

  def getAllWithParser: List[Product] = DB.withConnection {
    implicit connection =>
      sql.as(productsParser)
  }

  def findById(id: Long): Option[Product] = {
    DB.withConnection { implicit connection =>
      val sql = SQL("select * from products where id = {id}")
      sql.on("id" -> id).as(productParser.singleOpt)
    }
  }

  // Parse combination of product and stock item
  def productStockItemParser: RowParser[(Product, StockItem)] = {
    import anorm.SqlParser._

    productParser ~ StockItem.stockItemParser map (flatten)
  }

  // Used the parse combination of product and stock item

  // NOTE: This method isn't tested.
  def getAllProductsWithStockItems: Map[Product, List[StockItem]] = {
    DB.withConnection { implicit connection =>
      val sql = SQL("select p.*, s.* " +
        "from products p " +
        "inner join stock_items s on (p.id = s.product_id)")

        val results: List[(Product, StockItem)] = sql.as(productStockItemParser *)

        results.groupBy { _._1 }.mapValues { _.map { _._2 } }
      }
    }

  def search(query: String) = DB.withConnection { implicit connection =>
    SQL( s"""select *
        from products
        where name like '%${query}%'
        or description like '%${query}%'""").
      on("query" -> query).as(this.productsParser)
  }

  // Anorm, insert data
  def insert(product: Product): Boolean = {
    DB.withConnection { implicit connection =>
      SQL( """insert
      into products
      values ({id}, {ean}, {name}, {description})""").on(
          "id" -> product.id,
          "ean" -> product.ean,
          "name" -> product.name,
          "description" -> product.description
        ).executeUpdate() == 1
    }
  }

  // Anorm, update data
  def update(product: Product): Boolean = {
    DB.withConnection { implicit connection =>
      SQL( """update products
        set name = {name},
        ean = {ean},
        description = {description}
        where id = {id}
           """).on(
          "id" -> product.id,
          "name" -> product.name,
          "ean" -> product.ean,
          "description" -> product.description).
        executeUpdate() == 1
    }
  }

  // Anorm, delete data
  def delete(product: Product): Boolean = {
    DB.withConnection { implicit connection =>
      SQL("delete from products where id = {id}").
        on("id" -> product.id).executeUpdate() == 0
    }
  }
}

// Anorm model
case class StockItem(
                      id: Long,
                      productId: Long,
                      warehouseId: Long,
                      quantity: Long)

// Companion object contains DAO code
object StockItem {

  import anorm.RowParser

  val stockItemParser: RowParser[StockItem] = {
    import anorm.SqlParser._
    import anorm.~

    long("id") ~ long("product_id") ~
      long("warehouse_id") ~ long("quantity") map {
      case id ~ productId ~ warehouseId ~ quantity =>
        StockItem(id, productId, warehouseId, quantity)
    }
  }
}

case class Warehouse(id: Long, name: String)