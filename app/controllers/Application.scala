package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json

class Application extends Controller {

  def index = Action {
  	val message = "Go to /hello for a greeting."
  	Ok(message)
  }

  def sayHello = Action {
  	val greeting = "Hello there!"
  	Ok(Json.toJson(greeting))
  }
}