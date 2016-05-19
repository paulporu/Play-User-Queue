package utils

import play.api.Logger
import play.api.mvc.Result
import play.api.mvc.Results.{InternalServerError, Ok}
import play.api.libs.json.Json
import reactivemongo.api.commands.WriteResult

object RecoveryPolicy {

	val defaultRecoveryPolicy: PartialFunction[Throwable, Result] = {
		// If the result from Mongo is defined with the error code 11000 (duplicate error)
        case duplicateResult: WriteResult if (duplicateResult.code contains 11000) =>
            Ok(Json.obj("status" ->"KO", "message" -> "An object with same ID already exists"))
        case errorResult: WriteResult => 
            Logger.error(s"An error happened while trying to write to the DB: $errorResult.message")
            InternalServerError(Json.obj("status" ->"KO", "message" -> "Oops something went wrong"))
        case genericError =>
        	Logger.error(genericError.toString)
        	InternalServerError(Json.obj("status" ->"KO", "message" -> "Oops something went wrong"))
	}

	
}