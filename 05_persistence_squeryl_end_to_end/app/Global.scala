import models.Product
import org.squeryl.adapters.MySQLAdapter
import org.squeryl.{Session, SessionFactory}
import play.api.db.DB
import play.api.{Application, GlobalSettings}

object Global extends GlobalSettings {
	override def onStart(app: Application) {
		SessionFactory.concreteFactory = Some( () =>
			Session.create(DB.getConnection()(app), new MySQLAdapter)
		)

		def reloadDatabase {
			Product.removeAll

			Product.insert(Product(5010255079763L, "Paperclips Large", "Large Plain Pack of 1000"))
			Product.insert(Product(5018206244666L, "Giant Paperclips", "Giant Plain 51mm 100 pack"))
			Product.insert(Product(5018306332812L, "Paperclip Giant Plain", "Giant Plain Pack of 10000"))
			Product.insert(Product(5018306312913L, "No Tear Paper Clip", "No Tear Extra Large Pack of 1000"))
			Product.insert(Product(5018206244611L, "Zebra Paperclips", "Zebra Length 28mm Assorted 150 Pack"))
		}
		
		reloadDatabase
	}
}