// The MIT License (MIT)
// Copyright (c) 2016 Paul Lavery
//
// See the LICENCE.txt file distributed with this work for additional information regarding copyright ownership.

package controllers

import javax.inject.Inject
import scala.concurrent.Future
import scala.util.{Success, Failure}
import play.api.mvc.{Action, Controller}
import play.api.libs.json.{Json, JsSuccess, JsObject}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo._
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import com.github.paulporu.PriorityQueue
import models.{User, UserQueue}
import utils.Validation.validateUUID
import utils.RecoveryPolicy.defaultRecoveryPolicy


class Queues @Inject() (val reactiveMongoApi: ReactiveMongoApi)
    extends Controller
    with MongoController
    with ReactiveMongoComponents {


  def userCollection: JSONCollection = db.collection[JSONCollection]("users")
  def queueCollection: JSONCollection = db.collection[JSONCollection]("userQueues")

  def getAll = Action.async {
    val cursor: Cursor[JsObject] = queueCollection.find(Json.obj()).cursor[JsObject]()
    val userQueueList: Future[List[JsObject]] = cursor.collect[List]()
    userQueueList
      .map(userQueues => Ok(Json.toJson(userQueues)))
      .recover(defaultRecoveryPolicy)
  }

  def addQueue = Action.async(parse.json) {
    request =>
      request.body.validate[UserQueue] match {
        case JsSuccess(userQueue, _) =>
          queueCollection
            .insert(userQueue)
            .map(_ => Ok(Json.obj("status" ->"OK", "message" -> s"Queue $userQueue was saved.")))
            .recover(defaultRecoveryPolicy)
        case _ => Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> "Invalid JSON request")))
      }
  }

  def deleteQueue(queueID: String) = Action.async {
    validateUUID(queueID) match {
      case Success(_) =>
        queueCollection
          .remove(Json.obj("_id" -> queueID), firstMatchOnly = true)
          .map(_ => Ok(Json.obj("status" ->"OK", "message" -> (s"Queue with id $queueID was deleted.")))) 
          .recover(defaultRecoveryPolicy)
      case _ =>
        Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> "Invalid UUID format")))
    }
  }

  def addToQueue(userID: String, queueID: String) = Action.async {
    validateUUID(userID, queueID) match {
      case Success(_) =>
        val userFOption: Future[Option[User]] =
          userCollection.find(Json.obj("_id" -> userID)).one[User]
        val userQueueFOption: Future[Option[UserQueue]] =
          queueCollection.find(Json.obj("_id" -> queueID)).one[UserQueue]
        val fOptions: Future[(Option[User], Option[UserQueue])] = for {
          userOption <- userFOption
          userQueueOption <- userQueueFOption
        } yield (userOption, userQueueOption)
        fOptions
          .flatMap {
            case (Some(user), Some(userQueue)) =>
              userQueue.queue += user
              queueCollection
                .update(Json.obj("_id" -> queueID), userQueue)
                .map(_ => Ok(Json.obj("status" ->"OK", "message" -> s"User $user was added to the queue $queueID.")))
            case (Some(user), _) => Future.successful(NotFound(Json.obj("status" ->"KO", "message" -> "Queue could not be found")))
            case _ => Future.successful(NotFound(Json.obj("status" ->"KO", "message" -> "User could not be found")))
            }
          .recover(defaultRecoveryPolicy)
      case _ =>
        Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> "Invalid UUID format")))
    }
  }

  def deleteFromQueue = Action {
    val message = "User deleted from queue"
    Ok(Json.toJson(message))
  }

  def getFirstInQueue = Action {
    val message = "User"
    Ok(Json.toJson(message))
  }

  def popFirstFromQueue = Action {
    val message = "User added"
    Ok(Json.toJson(message))
  }

  def getAllInQueue = Action {
    val message = "User"
    Ok(Json.toJson(message))
  }

  def moveToTopOFQueue = Action {
    val message = "User"
    Ok(Json.toJson(message))
  }

}
