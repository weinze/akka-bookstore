package weinze.akka.bookstore.models

case class Book(isbn: String, title: String, series: Option[BookSeries], author: Seq[String], year: Int, goodreads: GoodreadsBook, imageUrl: Option[String])
