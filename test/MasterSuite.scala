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
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Span, Seconds}
import org.scalatestplus.play._


class MasterSuite 
    extends Suites(
      new ApplicationSpec,
      new UsersControllerSpec)
    with Results
    with ScalaFutures
    with BeforeAndAfterAll
    with OneAppPerSuite
     {

  implicit val defaultPatience = PatienceConfig(timeout = Span(10, Seconds))

   // Use the test DB
  implicit override lazy val app: Application = 
    new GuiceApplicationBuilder()
      .configure(Map("mongodb.uri" -> "mongodb://localhost:27017/user-queue-test"))
      .build()

  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]

  override def afterAll() {
    val collectionsFuture: Future[List[String]] = reactiveMongoApi.db.collectionNames
    whenReady(collectionsFuture) {
      collections => 
      	val deletions = collections.map {
	        collection =>
	          reactiveMongoApi.db[JSONCollection](collection).drop()
	      }
	    // We need to block here to make sure all drop operations end before we shut down the DB
	    val f = Future.sequence(deletions)
	    Await.ready(f, 10 seconds)
    }
  }
}