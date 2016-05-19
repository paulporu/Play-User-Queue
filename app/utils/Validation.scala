package utils

import java.util.UUID
import scala.util.Try

object Validation {
	
	def validateUUID(ids: String*): Try[Seq[UUID]] = Try(ids.map(id => UUID.fromString(id))) 

}
