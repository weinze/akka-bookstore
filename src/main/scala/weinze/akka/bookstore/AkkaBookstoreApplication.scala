package weinze.akka.bookstore

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import weinze.akka.bookstore.readers.BookReader

object AkkaBookstoreApplication extends App {

  implicit val system: ActorSystem = ActorSystem("akka-bookstore-system")
  import system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  BookReader().read()
}
