package utils

import scala.concurrent.duration.Duration
import scala.concurrent.duration
import play.api.cache.Cache
import play.api.Play.current
import play.api.Logger

/**
 * Cache helper
 */
object HCache {

  import duration._

  private val logger = Logger("HCache").logger

  val default = 30 minutes
  val long = 2 hours
  val short = 5 minutes
  val veryShort = 1 minute

  def set(k: String, v: Any): Unit = {
    set(k, v, default)
  }

  def remove(k: String): Unit = try {
    Cache.remove(k)
  } catch {
    case e: IllegalStateException =>
    case e: Throwable => logger.error(s"Error at deleting: $k\n ${e.getClass} ${e.getStackTraceString}")
  }

  def set(k: String, v: Any, d: Duration): Unit = try {
    Cache.set(k, v, d)
  } catch {
    case e: IllegalStateException =>
    case e: Throwable => logger.error(s"Error at saving: $k\n ${e.getClass} ${e.getStackTraceString}")
  }

}
