package weinze.akka.bookstore.readers

import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.TestSink
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.concurrent.ScalaFutures
import weinze.akka.bookstore.BaseSuite
import weinze.akka.bookstore.exceptions.{BookstoreException, ReaderException}
import weinze.akka.bookstore.models.Book

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class CsvReaderSuite extends BaseSuite with ScalaFutures with LazyLogging {

  import system.dispatcher

  private lazy val sink = BookReader().runnable.toMat(TestSink.probe[Future[(Book, Boolean)]])(Keep.right).run()

  "Read csv " should {
    sink.request(4)

    "be successfull" in {
      val (book, save) = sink.expectNext().futureValue
      book.isbn shouldBe "439023483"
      save shouldBe true

      val (book2, save2) = sink.expectNext().futureValue
      book2.isbn shouldBe "553296981"
      save2 shouldBe true
    }

    "be failed" in {
      Await.result(sink.expectNext().failed, 1.second) shouldBe an[BookstoreException]

      Await.result(sink.expectNext().failed, 1.second) shouldBe an[ReaderException]
    }
  }
}
