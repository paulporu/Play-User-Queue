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
import reactivemongo.api.Cursor
import reactivemongo.play.json._
import models.{User, UserQueue}
import utils.Validation.validateUUID
import utils.RecoveryPolicy.defaultRecoveryPolicy


class Queues @Inject() (val reactiveMongoApi: ReactiveMongoApi)
    extends Controller
    with MongoController
    with ReactiveMongoComponents {


  def queueCollection: JSONCollection = db.collection[JSONCollection]("userQueues")
  def userCollection: JSONCollection = db.collection[JSONCollection]("users")

  def getAllQueues = Action.async {
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

  def getFirstInQueue(queueID: String) = Action.async {
    validateUUID(queueID) match {
      case Success(_) =>
        queueCollection
          .find(Json.obj("_id" -> queueID))
          .one[UserQueue]
          .map {
            case Some(userQueue)=> Ok(Json.toJson(userQueue.queue.head))
            case _ => NotFound(Json.obj("status" ->"KO", "message" -> "Queue could not be found"))
          }
          .recover(defaultRecoveryPolicy)
        case _ =>
          Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> "Invalid UUID format")))
    }
  }

  def popFirstFromQueue(queueID: String) = Action.async {
    validateUUID(queueID) match {
      case Success(_) =>
        queueCollection
          .find(Json.obj("_id" -> queueID))
          .one[UserQueue]
          .flatMap {
            case Some(userQueue) =>
              val user = userQueue.queue.dequeue()
              queueCollection
                .update(Json.obj("_id" -> userQueue._id), userQueue)
                .map(_ => Ok(Json.toJson(user)))
            case _ => Future.successful(NotFound(Json.obj("status" ->"KO", "message" -> "Queue could not be found")))
          }
          .recover(defaultRecoveryPolicy)
        case _ =>
          Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> "Invalid UUID format")))
    }
  }

  def getAllInQueue(queueID: String) = Action.async {
    validateUUID(queueID) match {
      case Success(_) =>
        queueCollection
          .find(Json.obj("_id" -> queueID))
          .one[UserQueue]
          .map {
            case Some(userQueue)=> Ok(Json.toJson(userQueue.queue.getAllByPriority()))
            case _ => NotFound(Json.obj("status" ->"KO", "message" -> "Queue could not be found"))
          }
          .recover(defaultRecoveryPolicy)
        case _ =>
          Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> "Invalid UUID format")))
    }
  }

  //Removes all deleted users from the queue
  def cleanupQueue(queueID: String) = Action.async {
    validateUUID(queueID) match {
      case Success(_) =>
        queueCollection
          .find(Json.obj("_id" -> queueID))
          .one[UserQueue]
          .flatMap {
            case Some(userQueue) =>
              val futures = Future.sequence {
                userQueue.queue
                  .zipWithIndex
                  .map {
                    case (user, index) =>
                      val userFOption = userCollection.find(Json.obj("_id" -> user._id)).one[User]
                      userFOption.map(userOption => if (userOption.isEmpty) userQueue.queue.remove(index))
                  }
              }
              for {
                f <- futures
                update <- queueCollection.update(Json.obj("_id" -> userQueue._id), userQueue)
                } yield {
                Ok(Json.obj("status" ->"OK", "message" -> ("Deleted users were removed from queue")))
              }
            case _ => Future.successful(NotFound(Json.obj("status" ->"KO", "message" -> "Queue could not be found")))
          }
          .recover(defaultRecoveryPolicy)
        case _ =>
          Future.successful(BadRequest(Json.obj("status" ->"KO", "message" -> "Invalid UUID format")))
    }
  }

}
