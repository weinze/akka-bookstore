package weinze.akka.bookstore.exceptions

class BookstoreException(message: String, cause: Option[Throwable]) extends RuntimeException {

  override def getMessage: String = cause.map(c => s"$message | Cause: ${c.getMessage}").getOrElse(message)

}

object BookstoreException {
  private val DEFAULT_MESSAGE = "Internal error"

  def apply(): BookstoreException = new BookstoreException(DEFAULT_MESSAGE, None)
  def apply(message: String): BookstoreException = new BookstoreException(message, None)
  def apply(message: String, cause: Throwable): BookstoreException = new BookstoreException(message, Some(cause))
}
