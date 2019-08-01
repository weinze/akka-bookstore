package weinze.akka.bookstore.exceptions

class ReaderException(field: String, line: Map[String, String]) extends RuntimeException {

  override def getMessage: String = s"Cannot read '$field' from $line"
}

object ReaderException {
  def apply(field: String, line: Map[String, String] = Map.empty): ReaderException = new ReaderException(field, line)
}
