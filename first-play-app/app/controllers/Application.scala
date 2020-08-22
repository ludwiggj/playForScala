package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Hello world; Your new application is ready!!"))
  }

  def helloBare(name: String) = Action {
    Ok(s"Hello $name")
  }

  def helloDefault(name: String) = Action {
    Ok(views.html.index(s"Hello $name"))
  }

  def hello(name: String) = Action {
    Ok(views.html.hello(name))
  }

}