package test 

import play.api.test._
import play.api.test.Helpers._


class ApplicationSpec extends PlayWithDBSpec {
    
  "Application" must {

    "send 404 on a bad request" in {
      val nowhere = route(FakeRequest(GET, "/nowhere")).get
      status(nowhere) mustBe (NOT_FOUND)
    }

    "render the hello page" in {
      val hello = route(FakeRequest(GET, "/hello")).get

      status(hello) mustBe (OK)
      contentType(hello) mustBe Some("application/json")
      contentAsString(hello) must include ("Hello there!")
    }

    "connect to test DB" in {
      reactiveMongoApi.db mustNot be (null)
    }
  }
}
