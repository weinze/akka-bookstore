package weinze.akka.bookstore.readers

import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.scalatest.concurrent.ScalaFutures
import weinze.akka.bookstore.BaseSuite
import weinze.akka.bookstore.exceptions.{BookstoreException, ReaderException}
import weinze.akka.bookstore.models.Book
import weinze.akka.bookstore.readers.BookReader._

import scala.concurrent.Future

class BookReaderSuite extends BaseSuite with ScalaFutures {

  import system.dispatcher

  private lazy val (pub, sub) = TestSource.probe[Map[String, String]].via(BookReader().mapToEntity).toMat(TestSink.probe[Future[Book]])(Keep.both).run()

  private val CSV_BOOK = Map (
    TITLE_FIELD -> "The Name of the Wind (The Kingkiller Chronicle, #1)",
    ISBN_FIELD -> "075640407X",
    AUTHORS_FIELD -> "Patrick Rothfuss",
    PUBLICATION_YEAR_FIELD -> "2007.0",
    IMAGE_FIELD -> "https://images.gr-assets.com/books/1472068073m/186074.jpg",
    GOODREADS_ID_FIELD -> "186074",
    GOODREADS_COUNT_FIELD -> "123",
    GOODREADS_AVERAGE_FIELD -> "4.55",
    GOODREADS_RATINGS_FIELD -> "400101"
  )

  "Parse map to entity" should {

    sub.request(34) // TODO

    "incomplete csv line" in {
      pub.sendNext(Map(ISBN_FIELD -> "1111111111111"))

      sub.expectNext().failed.futureValue shouldBe an[ReaderException]
    }

    "correct csv line" in {
      val result = publishCsvBook()
      result.isbn shouldBe "075640407X"
      result.title shouldBe "The Name of the Wind"
      result.series shouldBe defined
      result.series.get.name shouldBe "The Kingkiller Chronicle"
      result.series.get.sequence shouldBe 1
      result.authors should not be empty
      result.authors should have length 1
      result.authors.head shouldBe "Patrick Rothfuss"
      result.year shouldBe 2007
      result.goodreads should not be null
      result.goodreads.id shouldBe 186074
      result.goodreads.count shouldBe Some(123)
      result.goodreads.averageRating shouldBe Some(4.55)
      result.goodreads.ratingsCount shouldBe Some(400101)
      result.imageUrl shouldBe defined
      result.imageUrl.get shouldBe "https://images.gr-assets.com/books/1472068073m/186074.jpg"
    }

    s"Check $TITLE_FIELD" in {
      assertMandatoryField(TITLE_FIELD)

      var result = publishCsvBook(TITLE_FIELD -> "  The Name of the Wind  ")
      result.title shouldBe "The Name of the Wind"
      result.series should not be defined

      result = publishCsvBook(TITLE_FIELD -> "The Name of the Wind ( The Kingkiller Chronicle)")
      result.title shouldBe "The Name of the Wind"
      result.series should not be defined

      result = publishCsvBook(TITLE_FIELD -> "The Name of the Wind ( The Kingkiller Chronicle , #1 )")
      result.series shouldBe defined
      result.series.get.name shouldBe "The Kingkiller Chronicle"
      result.series.get.sequence shouldBe 1

      result = publishCsvBook(TITLE_FIELD -> "The Name of the Wind (The Kingkiller Chronicle, # 1)")
      result.series shouldBe defined
      result.series.get.sequence shouldBe 1

      result = publishCsvBook(TITLE_FIELD -> "The Name of the Wind (The Kingkiller Chronicle, #1")
      result.series shouldBe defined
      result.series.get.sequence shouldBe 1
    }

    s"Check $ISBN_FIELD" in {
      assertMandatoryField(ISBN_FIELD)

      val isbn = "XXXXXXXX"
      publishCsvBook(ISBN_FIELD -> s" $isbn ").isbn shouldBe isbn
    }

    s"Check $AUTHORS_FIELD" in {
      assertMandatoryField(AUTHORS_FIELD)

      var result = publishCsvBook(AUTHORS_FIELD -> "Patrick Rothfuss, ")
      result.authors should have length 1
      result.authors should contain only "Patrick Rothfuss"

      result = publishCsvBook(AUTHORS_FIELD -> " Stephen King , J.R.R. Tolkien ")
      result.authors should have length 2
      result.authors should contain only ("Stephen King", "J.R.R. Tolkien")
    }

    s"Check $PUBLICATION_YEAR_FIELD" in {

      publishFailedCsvBook(PUBLICATION_YEAR_FIELD -> "") shouldBe an[ReaderException]

      assertNumberFormatException(PUBLICATION_YEAR_FIELD)

      var result = publishCsvBook(PUBLICATION_YEAR_FIELD -> "2007")
      result.year shouldBe 2007

      result = publishCsvBook(PUBLICATION_YEAR_FIELD -> " 2007.00")
      result.year shouldBe 2007
    }

    s"Check $IMAGE_FIELD" in {

      var result = publishCsvBook(IMAGE_FIELD -> "")
      result.imageUrl shouldBe empty

      result = publishCsvBook(IMAGE_FIELD -> " ")
      result.imageUrl shouldBe empty

      // TODO Testing by valid url
    }

    s"Check goodreads" in {
      publishFailedCsvBook(GOODREADS_ID_FIELD -> "") shouldBe an[ReaderException]
      publishFailedCsvBook(GOODREADS_ID_FIELD -> " ") shouldBe an[ReaderException]

      assertNumberFormatException(GOODREADS_ID_FIELD)
      assertNumberFormatException(GOODREADS_COUNT_FIELD)
      assertNumberFormatException(GOODREADS_AVERAGE_FIELD)
      assertNumberFormatException(GOODREADS_RATINGS_FIELD)

      var result = publishCsvBook(GOODREADS_COUNT_FIELD -> "")
      result.goodreads.count shouldBe empty

      result = publishCsvBook(GOODREADS_COUNT_FIELD -> " ")
      result.goodreads.count shouldBe empty

      result = publishCsvBook(GOODREADS_AVERAGE_FIELD -> "")
      result.goodreads.averageRating shouldBe empty

      result = publishCsvBook(GOODREADS_AVERAGE_FIELD -> " ")
      result.goodreads.averageRating shouldBe empty

      result = publishCsvBook(GOODREADS_RATINGS_FIELD -> "")
      result.goodreads.ratingsCount shouldBe empty

      result = publishCsvBook(GOODREADS_RATINGS_FIELD -> " ")
      result.goodreads.ratingsCount shouldBe empty
    }
  }

  private def publishCsvBook(entry: (String, String) = (null, null)): Book = {
    pub.sendNext(CSV_BOOK + entry)
    sub.expectNext().futureValue
  }

  private def publishFailedCsvBook(entry: (String, String) = (null, null)): Throwable = {
    pub.sendNext(CSV_BOOK + entry)
    sub.expectNext().failed.futureValue
  }

  private def assertMandatoryField(field: String) {
    val emptyException = publishFailedCsvBook(field -> "")
    emptyException shouldBe an[BookstoreException]
    emptyException.getCause shouldBe an[IllegalArgumentException]

    val blankException = publishFailedCsvBook(field -> " ")
    blankException shouldBe an[BookstoreException]
    blankException.getCause shouldBe an[IllegalArgumentException]
  }

  private def assertNumberFormatException(field: String) {
    val numberFormatException = publishFailedCsvBook(field -> "X")
    numberFormatException shouldBe an[BookstoreException]
    numberFormatException.getCause shouldBe an[NumberFormatException]
  }
}
