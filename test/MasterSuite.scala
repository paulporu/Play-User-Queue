package test 

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.DurationInt
import play.api.Application
import play.api.Play.current
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import org.scalatest._
import org.scalatestplus.play._


class MasterSuite 
    extends Suites(
      new ApplicationSpec,
      new UsersControllerSpec)
    with Results
    with BeforeAndAfterAll
    with OneAppPerSuite
     {

   // Use the test DB
  implicit override lazy val app: Application = 
    new GuiceApplicationBuilder()
      .configure(Map("mongodb.uri" -> "mongodb://localhost:27017/user-queue-test"))
      .build()

  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]
  def userCollection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("users")
  def queueCollection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("userQueues")

  override def afterAll() {
    val f = cleanDB()
    Await.ready(f, 10 seconds)
  }
  override def beforeAll() {
    val f = cleanDB()
    Await.ready(f, 10 seconds)
  }

  private def cleanDB(): Future[String] = {
    val deleteUsers = userCollection.drop(failIfNotFound=false)
    val deleteQueues = queueCollection.drop(failIfNotFound=false)
    for {
      x <- deleteUsers
      y <- deleteQueues
    } yield ("DB was successfully reset")
  }
}