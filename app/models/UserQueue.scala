// The MIT License (MIT)
// Copyright (c) 2016 Paul Lavery
//
// See the LICENCE.txt file distributed with this work for additional information regarding copyright ownership.

package models

import java.util.UUID
import play.api.libs.functional.syntax._
import play.api.libs.json._
import com.github.paulporu.PriorityQueue

class UserQueue(val _id: UUID, var queue: PriorityQueue[User]) {

  def enqueue(user: User): Unit =
    if (!queue.exists(_._id == user._id)) queue += user

  def remove(user: User): Unit =
    queue = queue.filter(_._id != user._id).asInstanceOf[PriorityQueue[User]]

  // If your queue has several millions of users in it you would want
  // to check that the priority does not go below Integer.MIN_VALUE
  def moveToTop(user: User): Unit = {
    queue
      .zipWithIndex
      .find(_._1._id == user._id )
      .collect { 
        case (usr, index) if (index != 0) =>
          queue.remove(index)
          val updatedUser = usr.copy(priority = queue.head.priority - 1)
          queue += updatedUser
      }
  }

}

object UserQueue {

  /** deserializes a UserQueue from a (String, List[User]) */
  implicit val UserQueueReads: Reads[UserQueue] = (
    (JsPath \ "_id").read[String] and
    (JsPath \ "queue").readNullable[List[User]]
  )((id: String, queue: Option[List[User]]) =>
      new UserQueue(
        UUID.fromString(id),
        new PriorityQueue[User] ++= queue.getOrElse(List[User]())
      ))

  /** serializes a UserQueue to a (String, List[User]) */
  implicit val UserQueueWrites: OWrites[UserQueue] = (
    (JsPath \ "_id").write[String] and
    (JsPath \ "queue").write[List[User]]
  )((userQueue: UserQueue) =>
      (userQueue._id.toString, userQueue.queue.getAllByPriority))
}
