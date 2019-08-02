package weinze.akka.bookstore.models

case class Book(isbn: String, title: String, series: Option[BookSeries], authors: Seq[String], year: Int, goodreads: GoodreadsBook, imageUrl: Option[String]) {
  require(isbn.nonEmpty, "isbn cannot be empty")
  require(title.nonEmpty, "title cannot be empty")
  require(authors.nonEmpty, "authors cannot be empty")
  require(authors.forall(_.nonEmpty), "authors cannot be empty")
  require(imageUrl.forall(_.nonEmpty), "imageUrl cannot be empty")
}
