package controllers

import play.api.mvc.{Action, Controller}
import concurrent.{ExecutionContext, Future}

object Dashboard extends Controller {

  /**
   * Suspends an HTTP request while waiting for asynchronous processing.
   */
  def backlog(warehouse: String) = Action.async {

    import ExecutionContext.Implicits.global
    val backlog: Future[String] = scala.concurrent.Future {
      models.Order.backlog(warehouse)
    }

    backlog.map(value => Ok(value))
  }
}