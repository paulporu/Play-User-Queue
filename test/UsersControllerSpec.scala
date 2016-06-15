package test 

import java.util.UUID
import scala.concurrent.Future
import play.api.Play.current
import play.api.mvc.Result
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json._
import reactivemongo.play.json.collection.JSONCollection
import models.User
import controllers.Users
import org.scalatest.DoNotDiscover
import org.scalatestplus.play._
import org.scalatest.concurrent.ScalaFutures.whenReady


@DoNotDiscover
class UsersControllerSpec extends PlaySpec with ConfiguredApp {

  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]

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
