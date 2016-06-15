package test 

import org.scalatest._
import org.scalatestplus.play._
import play.api.Application
import play.api.Play.current
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import play.api.inject.guice.GuiceApplicationBuilder
import play.modules.reactivemongo.ReactiveMongoApi


class PlayWithDBSpec 
    extends PlaySpec
    with OneAppPerSuite
    with Results {

   // Use the test DB
  implicit override lazy val app: Application = 
    new GuiceApplicationBuilder()
      .configure(Map("mongodb.uri" -> "mongodb://localhost:27017/user-queue-test"))
      .build()

  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]
}