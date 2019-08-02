package weinze.akka.bookstore.exceptions

class BookstoreException(cause: Throwable, data: Map[String, String]) extends RuntimeException(cause) {

  override def getMessage: String = s"Cause: ${cause.getMessage} | Data $data"

}

object BookstoreException {
  def apply(cause: Throwable, data: Map[String, String] = Map.empty): BookstoreException = new BookstoreException(cause, data)
}
