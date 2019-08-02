package weinze.akka.bookstore

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.{AsyncWordSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar

trait BaseSuite extends AsyncWordSpec with Matchers with MockitoSugar {

  implicit val system: ActorSystem = ActorSystem("akka-bookstore-test")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

}