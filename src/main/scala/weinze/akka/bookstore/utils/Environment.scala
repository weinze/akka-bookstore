package weinze.akka.bookstore.utils

import com.typesafe.config.ConfigFactory

object Environment {

  private lazy val config = ConfigFactory.load()

//  private lazy val mongoConfig = config.getConfig("mongo")
//  lazy val mongoUri: String = mongoConfig.getString("uri")
//  lazy val mongoDatabase: String = mongoConfig.getString("database")
//  private lazy val mongoExpirationConfig = mongoConfig.getConfig("expiration")
//  lazy val mongoExpirationEnabled: Boolean = mongoExpirationConfig.getBoolean("enabled")
//  lazy val mongoExpirationAt: Int = mongoExpirationConfig.getInt("at")
}
