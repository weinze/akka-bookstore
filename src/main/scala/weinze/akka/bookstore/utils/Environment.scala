package weinze.akka.bookstore.utils

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

object Environment extends LazyLogging {

  private lazy val config = ConfigFactory.load()

  private lazy val csvConfig = config.getConfig("csv")
  lazy val csvRootPath: String = csvConfig.getString("path")
  lazy val csvSeparator: Byte = csvConfig.getString("default-delimiter").toByte

  private lazy val csvBookConfig = csvConfig.getConfig("books")
  lazy val bookPath: String = csvBookConfig.getString("path")

}
