// The MIT License (MIT)
// Copyright (c) 2016 Paul Lavery
//
// See the LICENCE.txt file distributed with this work for additional information regarding copyright ownership.

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
