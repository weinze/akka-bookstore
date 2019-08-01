package weinze.akka.bookstore.readers

import akka.NotUsed
import akka.stream.scaladsl.Flow
import weinze.akka.bookstore.exceptions.ReaderException
import weinze.akka.bookstore.models.{Book, BookSeries, GoodreadsBook}
import weinze.akka.bookstore.readers.BookReader._

import scala.concurrent.{ExecutionContext, Future}

class BookReader()(implicit val executor: ExecutionContext) extends CsvReader[Book] {

  protected override val FILE_PATH: String = ROOT_PATH.concat("books.csv")

  protected override def mapToEntity: Flow[Map[String, String], Future[Book], NotUsed] = Flow[Map[String, String]].map { data =>
    try {

      val isbn = data.get(ISBN_FIELD).filter(_.nonEmpty).getOrElse(throw ReaderException(ISBN_FIELD, data))

      val completeTitle = parseTitle(data.get(TITLE_FIELD))
      val title = completeTitle._1
      val series = parseSeries(completeTitle._2)

      val authors = data.get(AUTHORS_FIELD).map(_.split(AUTHORS_REGEX).map(_.trim).toSeq).getOrElse(Seq.empty)

      val goodreads = GoodreadsBook(
        toLong(data, GOODREADS_ID_FIELD),
        data.get(GOODREADS_COUNT_FIELD).map(_.toInt),
        data.get(GOODREADS_AVERAGE_FIELD).map(_.toDouble),
        data.get(GOODREADS_RATINGS_FIELD).map(_.toLong)
      )

      val year = parseYear(data)

      Future.successful {
        Book(
          isbn,
          title,
          series,
          authors,
          year,
          goodreads,
          data.get(IMAGE_FIELD)
        )
      }
    } catch {
      // TODO Handle custom exceptions?
      case e: Exception => Future.failed(e)
    }
  }

  private def toLong(data: Map[String, String], key: String): Long = {
    data.get(key).filter(_.nonEmpty).map(_.toLong).getOrElse(throw ReaderException(key, data))
  }

  private def parseTitle(completeTitle: Option[String]): (String, Option[String]) = {
    completeTitle
      .map(_.span(!TITLE_SEPARATOR.equals(_)))
      .map { case(t, s) =>
        (t.trim, Option(s).filter(_.nonEmpty).map(_.drop(1)))
      }.getOrElse(("", None))
  }

  private def parseSeries(completeSeries: Option[String]): Option[BookSeries] = {
    completeSeries.map(_.split(SERIES_REGEX)).flatMap(cp => {
      // TODO Improve
      if (cp.length == 2) Some(BookSeries(cp(0).trim, cp(1).head.toInt))
      else None
    })
  }

  private def parseYear(data: Map[String, String]): Int = {
    data.get(PUBLICATION_YEAR_FIELD)
      .filter(_.nonEmpty)
      .map(_.split(YEAR_REGEX).head.toInt)
      .getOrElse(throw ReaderException(PUBLICATION_YEAR_FIELD, data))
  }
}

object BookReader {

  private val TITLE_FIELD = "title"
  private val ISBN_FIELD = "isbn"
  private val AUTHORS_FIELD = "authors"
  private val PUBLICATION_YEAR_FIELD = "original_publication_year"
  private val IMAGE_FIELD = "image_url"
  private val GOODREADS_ID_FIELD = "goodreads_book_id"
  private val GOODREADS_COUNT_FIELD = "books_count"
  private val GOODREADS_AVERAGE_FIELD = "average_rating"
  private val GOODREADS_RATINGS_FIELD = "ratings_count"

  private val TITLE_SEPARATOR = '('
  private val SERIES_REGEX = "(,? ?)#"
  private val AUTHORS_REGEX = ","
  private val YEAR_REGEX = "\\."


  def apply()(implicit executor: ExecutionContext): BookReader = new BookReader()
}