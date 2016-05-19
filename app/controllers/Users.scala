package controllers

import java.util.UUID
import javax.inject.Inject
import scala.util.Success
import scala.concurrent.Future
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Json, JsSuccess}
import play.modules.reactivemongo._
import play.modules.reactivemongo.json._
import reactivemongo.play.json.collection.JSONCollection
import models.User
import utils.Validation.validateUUID
import utils.RecoveryPolicy.defaultRecoveryPolicy


class Users @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents {
  
  def collection: JSONCollection = db.collection[JSONCollection]("users")

  def addUser = Action.async(parse.json) {
    request =>
      request.body.validate[User] match {
        case JsSuccess(user, _) => 
          collection.insert(user).
            map(_ => Ok(Json.obj("status" ->"OK", "message" -> s"User $user was saved."))).
            recover(defaultRecoveryPolicy)
        case _ => Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> "Invalid JSON request")))
      }
  }

  def updateUser = Action.async(parse.json) {
    request =>
      request.body.validate[User] match {
        case JsSuccess(user, _) => 
          collection.update(Json.obj("_id" -> user._id), user, upsert = true).
            map(_ => Ok(Json.obj("status" ->"OK", "message" -> (s"User $user was updated.")))).
            recover(defaultRecoveryPolicy)
        case _ => Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> "Invalid JSON request")))
      }
  }

  def getUser(userID: String) = Action.async {
    validateUUID(userID) match {
      case Success(_) =>
        collection.find(Json.obj("_id" -> userID)).one[User].
          map {
            case Some(user)=> Ok(Json.toJson(user))
            case _ => NotFound(Json.obj("status" ->"KO", "message" -> "User could not be found"))
          }.
          recover(defaultRecoveryPolicy)
        case _ => 
          Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> "Invalid UUID format")))
    }
  }

  def deleteUser(userID: String) = Action.async {
    validateUUID(userID) match {
      case Success(_) =>
        collection.remove(Json.obj("_id" -> userID), firstMatchOnly = true).
          map(_ => Ok(Json.obj("status" ->"OK", "message" -> (s"User with id $userID was deleted.")))).
          recover(defaultRecoveryPolicy)
      case _ => 
        Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> "Invalid UUID format")))
    }
  }

}

