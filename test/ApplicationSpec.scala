import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._


@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    // "send 404 on a bad request" in new WithApplication{
    //   route(FakeRequest(GET, "/boum")) must beSome.which (status(_) == NOT_FOUND)
    // }

    "Say Hello" in new WithApplication {
      val greeting = controllers.Application.sayHello()(FakeRequest(GET, "/hello"))
      status(greeting) must equalTo(OK)
      contentAsString(greeting) must beEqualTo (""""Hello there!"""" ) 
    }
  }
}
