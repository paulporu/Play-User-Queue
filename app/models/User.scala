// The MIT License (MIT)
// Copyright (c) 2016 Paul Lavery
//
// See the LICENCE.txt file distributed with this work for additional information regarding copyright ownership.

package models

import java.util.UUID
import scala.math.Ordering
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import reactivemongo.play.json._


// Default parameter not needed because of workaround below but left intentionnally for clarity of intention
case class User(_id: UUID = UUID.randomUUID(),
                firstName: String,
                lastName: String,
                email: String,
                priority: Int)

object User {

  implicit def orderingByPriority: Ordering[User] = Ordering.by(user => user.priority)

  /*
  * We have to define explicitely the OFormat to handle case class default parameters:
  * see open issue https://github.com/playframework/playframework/issues/3244
  *
  * We use inmap to specify the transformation from Format[Option[UUID]] to and from Format[UUID]
  * https://www.playframework.com/documentation/2.4.x/api/scala/index.html#play.api.libs.functional.InvariantFunctor
  */
  implicit val userFormat: OFormat[User] =
    ((JsPath \ "_id").formatNullable[UUID].inmap[UUID](_.getOrElse(UUID.randomUUID()), Option(_)) ~
    (JsPath \ "firstName").format[String] ~
    (JsPath \ "lastName").format[String] ~
    (JsPath \ "email").format[String] ~
    (JsPath \ "priority").format[Int]
    )(User.apply, unlift(User.unapply))

}
