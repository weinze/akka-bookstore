package weinze.akka.bookstore.readers

import java.nio.file.{Path, Paths}

import akka.NotUsed
import akka.stream.IOResult
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{FileIO, Flow, Source}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import weinze.akka.bookstore.utils.Environment

import scala.concurrent.{ExecutionContext, Future}

trait CsvReader[T] extends LazyLogging {

  protected implicit val executor: ExecutionContext

  private val ROOT_PATH: String = Environment.csvRootPath
  protected val FILE_PATH: String
  protected val CSV_DELIMITER: Byte = Environment.csvSeparator

  // TODO Set a supervisor strategy to Stream
  def runnable: Source[Future[(T, Boolean)], Future[IOResult]] = readFile via parseCsv via csvToMap via mapToEntity via persist

  private def getUri = getClass.getClassLoader.getResource(ROOT_PATH.concat(FILE_PATH)).toURI

  private def getPath: Path = Paths.get(getUri)

  private def readFile: Source[ByteString, Future[IOResult]] = FileIO.fromPath(getPath)

  private def parseCsv: Flow[ByteString, List[ByteString], NotUsed] = CsvParsing.lineScanner(CSV_DELIMITER)

  private def csvToMap: Flow[List[ByteString], Map[String, String], NotUsed] = CsvToMap.toMapAsStrings()

  private[readers] def mapToEntity: Flow[Map[String, String], Future[T], NotUsed]

  private def persist: Flow[Future[T], Future[(T, Boolean)], NotUsed] = Flow[Future[T]].map(_.map { book =>
    // TODO Implement me!
    (book, true)
  })
}
