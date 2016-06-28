package test 

import java.util.UUID
import scala.concurrent.Future
import play.api.Play.current
import play.api.mvc.{Result, AnyContentAsEmpty}
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
  lazy val controller = new Users(reactiveMongoApi)

  "User API" must {

    "add a new user" in {
      val user = User(
        UUID.fromString("00000000-0000-0000-0000-000000000001"), 
        "John", 
        "Doe", 
        "j.doe@aol.com",
        3
      )
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

    "not add a user twice" in {
      val user = User(
        UUID.fromString("00000000-0000-0000-0000-000000000001"), 
        "John", 
        "Doe", 
        "j.doe@aol.com",
        3
      )
      val request = FakeRequest(
        method = "POST",
        uri = controllers.routes.Users.addUser.url,
        headers = FakeHeaders(),
        body = Json.toJson(user)
      )
      val apiResult: Future[Result] = controller.addUser.apply(request)
      val jsonResult: JsValue = contentAsJson(apiResult)
      jsonResult mustNot be (null)
      (jsonResult \ "status").as[String] mustBe ("KO")
      (jsonResult \ "message").as[String] mustBe ("An object with same ID already exists")
    }

    "get a user" in {
      val userID = "00000000-0000-0000-0000-000000000001"
      val request = FakeRequest(
        method = "GET",
        uri = s"/user/get/$userID",
        headers = FakeHeaders(),
        body = AnyContentAsEmpty
      )
      val apiResult: Future[Result] = controller.getUser(userID).apply(request)
      val jsonResult: JsValue = contentAsJson(apiResult)
      jsonResult mustNot be (null)
      val user: Option[User] = jsonResult.validate[User].asOpt 
      user mustNot be (None)
      val userFuture = userCollection
          .find(Json.obj("_id" -> user.get._id))
          .one[User]
      whenReady(userFuture) { usr => usr mustBe user }
    }

   "delete a user" in {
      val userID = "00000000-0000-0000-0000-000000000001"
      val request = FakeRequest(
        method = "POST",
        uri = controllers.routes.Users.deleteUser(userID).url,
        headers = FakeHeaders(),
        body = AnyContentAsEmpty
      )
      val apiResult: Future[Result] = controller.deleteUser(userID).apply(request)
      val jsonResult: JsValue = contentAsJson(apiResult)
      jsonResult mustNot be (null)
      (jsonResult \ "status").as[String] mustBe ("OK")
      val userFuture = userCollection
          .find(Json.obj("_id" -> userID))
          .one[User]
      whenReady(userFuture) { usr => usr mustBe None }
    }

    "reject incorrect JSON" in {
      val badJSON = Json.obj()
      val request = FakeRequest(
        method = "POST",
        uri = controllers.routes.Users.addUser.url,
        headers = FakeHeaders(),
        body = badJSON
      )
      val apiResult: Future[Result] = controller.addUser.apply(request)
      val jsonResult: JsValue = contentAsJson(apiResult)
      jsonResult mustNot be (null)
      (jsonResult \ "status").as[String] mustBe ("KO")
      (jsonResult \ "message").as[String] mustBe ("Invalid JSON request")
    }

    "reject incorrect UUID" in {
      val badUUID = "001"
      val request = FakeRequest(
        method = "GET",
        uri = s"/user/get/$badUUID",
        headers = FakeHeaders(),
        body = AnyContentAsEmpty
      )
      val apiResult: Future[Result] = controller.getUser(badUUID).apply(request)
      val jsonResult: JsValue = contentAsJson(apiResult)
      jsonResult mustNot be (null)
      (jsonResult \ "status").as[String] mustBe ("KO")
      (jsonResult \ "message").as[String] mustBe ("Invalid UUID format")
    }

  }

}
