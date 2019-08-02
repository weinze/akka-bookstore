package weinze.akka.bookstore.models

case class BookSeries(name: String, sequence: Int) {
  require(name.nonEmpty, "name cannot be empty")
}
