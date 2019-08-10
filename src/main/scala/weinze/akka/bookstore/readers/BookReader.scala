package weinze.akka.bookstore.readers

import akka.NotUsed
import akka.stream.scaladsl.Flow
import weinze.akka.bookstore.exceptions.{BookstoreException, ReaderException}
import weinze.akka.bookstore.models.{Book, BookSeries, GoodreadsBook}
import weinze.akka.bookstore.readers.BookReader._
import weinze.akka.bookstore.utils.Environment

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class BookReader()(implicit val executor: ExecutionContext) extends CsvReader[Book] {

  protected override val FILE_PATH: String = Environment.bookPath

  private[readers] override def mapToEntity: Flow[Map[String, String], Future[Book], NotUsed] =
    Flow[Map[String, String]].map { data =>
      try {

        // TODO The field 'isbn13' can be used if 'isbn' is empty
        val isbn = data.get(ISBN_FIELD).map(_.trim).getOrElse("")

        val completeTitle = parseTitle(data.get(TITLE_FIELD))
        val title = completeTitle._1
        val series = parseSeries(completeTitle._2)

        val authors = data.get(AUTHORS_FIELD)
          .map(_.split(AUTHORS_REGEX).map(_.trim).filter(_.nonEmpty).toSeq)
          .getOrElse(Seq.empty)

        val goodreads = GoodreadsBook(
          toLong(data, GOODREADS_ID_FIELD),
          data.get(GOODREADS_COUNT_FIELD).map(_.trim).filter(_.nonEmpty).map(_.toInt),
          data.get(GOODREADS_AVERAGE_FIELD).map(_.trim).filter(_.nonEmpty).map(_.toDouble),
          data.get(GOODREADS_RATINGS_FIELD).map(_.trim).filter(_.nonEmpty).map(_.toLong)
        )

        val year = parseYear(data)

        val imageUrl = data.get(IMAGE_FIELD).map(_.trim).filter(_.nonEmpty).orElse(None)

        Future.successful {
          Book(
            isbn,
            title,
            series,
            authors,
            year,
            goodreads,
            imageUrl
          )
        }
      } catch {
        case re: ReaderException => Future.failed(re)
        case NonFatal(e) => Future.failed(BookstoreException(e, data))
      }
    }

  private def toLong(data: Map[String, String], key: String): Long = {
    data.get(key).map(_.trim).filter(_.nonEmpty).map(_.toLong).getOrElse(throw ReaderException(key, data))
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
      if (cp.length == 2) Some(BookSeries(cp(0).trim, cp(1).replaceAll(NOT_DIGIT_REGEX, "").toInt))
      else None
    })
  }

  private def parseYear(data: Map[String, String]): Int = {
    data.get(PUBLICATION_YEAR_FIELD)
      .filter(_.nonEmpty)
      .map(_.split(YEAR_REGEX).head.trim.toInt)
      .getOrElse(throw ReaderException(PUBLICATION_YEAR_FIELD, data))
  }
}

object BookReader {

  private[readers] val TITLE_FIELD = "title"
  private[readers] val ISBN_FIELD = "isbn"
  private[readers] val AUTHORS_FIELD = "authors"
  private[readers] val PUBLICATION_YEAR_FIELD = "original_publication_year"
  private[readers] val IMAGE_FIELD = "image_url"
  private[readers] val GOODREADS_ID_FIELD = "goodreads_book_id"
  private[readers] val GOODREADS_COUNT_FIELD = "books_count"
  private[readers] val GOODREADS_AVERAGE_FIELD = "average_rating"
  private[readers] val GOODREADS_RATINGS_FIELD = "ratings_count"

  private val TITLE_SEPARATOR = '('
  private val SERIES_REGEX = "(,? ?)#"
  private val AUTHORS_REGEX = ","
  private val YEAR_REGEX = "\\."
  private val NOT_DIGIT_REGEX = "\\D*"


  def apply()(implicit executor: ExecutionContext): BookReader = new BookReader()
}