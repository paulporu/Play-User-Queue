// The MIT License (MIT)
// Copyright (c) 2016 Paul Lavery
//
// See the LICENCE.txt file distributed with this work for additional information regarding copyright ownership.

package utils

import java.util.UUID
import scala.util.Try

object Validation {

  def validateUUID(ids: String*): Try[Seq[UUID]] = Try(ids.map(id => UUID.fromString(id)))

}
