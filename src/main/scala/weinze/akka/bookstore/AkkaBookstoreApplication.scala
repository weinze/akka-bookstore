package weinze.akka.bookstore

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.scalalogging.LazyLogging
import weinze.akka.bookstore.exceptions.{BookstoreException, ReaderException}
import weinze.akka.bookstore.readers.BookReader

import scala.concurrent.Future
import scala.util.{Failure, Success}

object AkkaBookstoreApplication extends App with LazyLogging {

  implicit val system: ActorSystem = ActorSystem("akka-bookstore-system")
  import system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  BookReader().runnable.runWith(writeResult)

  // Test purpose, remove then
  private def writeResult[T]: Sink[Future[(T, Boolean)], Future[Done]] = Sink.foreach { future =>
    future onComplete {
      case Success((line, result)) => logger.debug("Line: {}, saved: {}", line, result)
      case Failure(e) => e match {
        case be: BookstoreException => logger.warn(be.getMessage)
        case re: ReaderException    => logger.warn(re.getMessage)
        case ex: Exception           => logger.error("Unhandled exception", ex)
      }
    }
  }
}
