// The MIT License (MIT)
// Copyright (c) 2016 Paul Lavery
//
// See the LICENCE.txt file distributed with this work for additional information regarding copyright ownership.

package models

import java.util.UUID
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._
import com.github.paulporu.PriorityQueue

case class UserQueue(_id: UUID, queue: PriorityQueue[User])

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
