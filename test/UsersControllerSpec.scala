package test 

import java.util.UUID
import scala.concurrent.Future
import play.api.mvc.Result
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Span, Seconds}
import play.modules.reactivemongo.json._
import reactivemongo.play.json.collection.JSONCollection
import models.User
import controllers.Users


class UsersControllerSpec extends PlayWithDBSpec with ScalaFutures {

  implicit val defaultPatience = PatienceConfig(timeout = Span(10, Seconds))

  def userCollection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("users")
  
  "User API" must {

    "add a new user" in {
      val user = User(
        UUID.fromString("00000000-0000-0000-0000-000000000001"), 
        "John", 
        "Doe", 
        "j.doe@aol.com",
        3
      )
      val controller = new Users(reactiveMongoApi)
      val request = FakeRequest(
        method = "POST",
        uri = controllers.routes.Users.addUser.url,
        headers = FakeHeaders(),
        body = Json.toJson(user)
      )
      val apiResult: Future[Result] = controller.addUser.apply(request)
      val jsonResult: JsValue = contentAsJson(apiResult)
      jsonResult mustNot be (null)
      (jsonResult \ "status").as[String] mustBe ("OK")
      val userFuture = userCollection
          .find(Json.obj("_id" -> user._id))
          .one[User]
      whenReady(userFuture) { usr => usr mustBe Some(user) }
    }


  }

}
