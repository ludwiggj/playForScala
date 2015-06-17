package controllers

import play.api._
import play.api.mvc._
import play.twirl.api.Html

class Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def main = Action {
    Ok(views.html.main("This is the title")(Html("Hi there!")))
  }
}
