package weinze.akka.bookstore.models

case class GoodreadsBook(id: Long, count: Option[Int], averageRating: Option[Double], ratingsCount: Option[Long])
