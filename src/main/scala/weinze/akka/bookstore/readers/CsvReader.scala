package weinze.akka.bookstore.readers

import java.nio.file.{Files, Path, Paths}

import akka.NotUsed
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{FileIO, Flow, Source}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import weinze.akka.bookstore.exceptions.ReaderException

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success
import scala.util.Failure

trait CsvReader[T] extends LazyLogging {

  protected implicit val executor: ExecutionContext

  // TODO Avoid 'resources' path
  protected val ROOT_PATH: String = "src/main/resources/"
  protected val FILE_PATH: String
  protected val CSV_DELIMITER: Byte = ','

  // TODO Set a supervisor strategy to Stream
  def read()(implicit materializer: ActorMaterializer) {
    if(Files.exists(getPath)) {

      readFile via parseCsv via csvToMap via mapToEntity via save runForeach { line =>
        line.onComplete {
          case Success(value)     => logger.debug("Line: {}, saved: {}", value._1, value._2)
          case Failure(e) => e match {
            case r: ReaderException => logger.warn(r.getMessage)
            case e: Exception => logger.error("error", e)
          }
        }

      }

    } else {
      logger.debug("File '{}' not exists", FILE_PATH)
    }
  }

  private def getPath: Path = Paths.get(FILE_PATH)

  private def readFile: Source[ByteString, Future[IOResult]] = FileIO.fromPath(getPath)

  private def parseCsv: Flow[ByteString, List[ByteString], NotUsed] = CsvParsing.lineScanner(CSV_DELIMITER)

  private def csvToMap: Flow[List[ByteString], Map[String, String], NotUsed] = CsvToMap.toMapAsStrings()

  private[readers] def mapToEntity: Flow[Map[String, String], Future[T], NotUsed]

  private def save: Flow[Future[T], Future[(T, Boolean)], NotUsed] = Flow[Future[T]].map(_.map { book =>
    // TODO Implement me!
    (book, false)
  })
}
