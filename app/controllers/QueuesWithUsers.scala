// The MIT License (MIT)
// Copyright (c) 2016 Paul Lavery
//
// See the LICENCE.txt file distributed with this work for additional information regarding copyright ownership.

package controllers

import javax.inject.Inject
import scala.concurrent.Future
import scala.util.Success
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo._
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._
import models.{User, UserQueue}
import utils.Validation.validateUUID
import utils.RecoveryPolicy.defaultRecoveryPolicy


class QueuesWithUsers @Inject() (val reactiveMongoApi: ReactiveMongoApi)
    extends Controller
    with MongoController
    with ReactiveMongoComponents {


  def userCollection: JSONCollection = db.collection[JSONCollection]("users")
  def queueCollection: JSONCollection = db.collection[JSONCollection]("userQueues")

  case class QueueWithUserAction(queueID: String, userID: String)(block: (UserQueue, User) => JsObject) 
      extends Action[AnyContent] {

    def apply(request: Request[AnyContent]): Future[Result] = {
      validateUUID(queueID, userID) match {
        case Success(_) =>
          val userQueueFOption: Future[Option[UserQueue]] =
            queueCollection.find(Json.obj("_id" -> queueID)).one[UserQueue]
          val userFOption: Future[Option[User]] =
            userCollection.find(Json.obj("_id" -> userID)).one[User]
          val fOptions: Future[(Option[UserQueue], Option[User])] = for {
            userQueueOption <- userQueueFOption
            userOption <- userFOption
          } yield (userQueueOption, userOption)
          fOptions
            .flatMap {
              case (Some(userQueue), Some(user)) =>
                val result = block(userQueue, user)
                if ((result \ "success").get.as[Boolean]) {
                  queueCollection
                    .update(Json.obj("_id" -> queueID), userQueue)
                    .map(_ => Ok(Json.obj("status" ->"OK", "message" -> (result \ "message").get.as[String])))
                }
                else {
                  Future.successful(Ok(Json.obj("status" ->"KO", "message" -> (result \ "message").get.as[String])))
                }
              case (Some(userQueue), _) => Future.successful(NotFound(Json.obj("status" ->"KO", "message" -> "User could not be found")))
              case _ => Future.successful(NotFound(Json.obj("status" ->"KO", "message" -> "Queue could not be found")))
              }
            .recover(defaultRecoveryPolicy)
        case _ =>
          Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> "Invalid UUID format")))
      }
    }

    def parser: BodyParser[AnyContent] = parse.anyContent
  }

  def addToQueue(queueID: String, userID: String) = QueueWithUserAction(queueID,userID) {
    (userQueue, user) =>
      userQueue.enqueue(user)
      Json.obj(
        "success" -> true,
        "message" -> s"User $userID was added to the queue $queueID."
      )
  }

  def deleteFromQueue(queueID: String, userID: String) = QueueWithUserAction(queueID,userID) {
    (userQueue, user) =>
      userQueue.remove(user)
      Json.obj(
        "success" -> true,
        "message" -> s"User $userID was removed from the queue $queueID."
      )  
    }

  // This method is merely an artifact to conveniently bump a user to the top of a queue.
  // As such it does not change the priority of the user anywhere else but in said queue. 
  def moveToTopOFQueue(queueID: String, userID: String) = QueueWithUserAction(queueID,userID) {
    (userQueue, user) =>
      userQueue.queue
        .find(_._id == user._id)
        .map { _ =>
          userQueue.moveToTop(user)
          Json.obj(
            "success" -> true,
            "message" -> s"User $userID was bumped to the top of the queue $queueID."
          )}
        .getOrElse {
          Json.obj(
            "success" -> false,
            "message" -> s"User $userID could not be found in the queue $queueID."
          )}
  }

  def updateUser = Action.async(parse.json) {
    request =>
      request.body.validate[User] match {
        case JsSuccess(user, _) =>
          val userUpdate = 
            userCollection
              .update(Json.obj("_id" -> user._id), user, upsert = true)
          val queueUpdates = 
            queueCollection
              .update(Json.obj("_id" -> user._id), user, multi = true)

            .map(_ => Ok(Json.obj("status" ->"OK", "message" -> (s"User $user was updated."))))            
            .recover(defaultRecoveryPolicy)
        case _ => Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> "Invalid JSON request")))
      }
  }

}
