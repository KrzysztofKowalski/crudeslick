package models.entities

import org.joda.time.DateTime
import utils.TimeHelper

/**
 * HasId trait used for keyed entities.
 * Provides id, createdAt and updatedAt fields as well as save, and id getter methods for each entity
 */
trait HasId {
  val id: Option[Long] = None
  val createdAt: Option[DateTime] = now
  val updatedAt: Option[DateTime] = now

  def now: Option[DateTime] = TimeHelper.some

  /**
   * save method
   */
  def save

  /**
   * returns entity id
   * @param default
   * @return
   */
  def gid(default: Long = -1): Long = id match {
    case Some(i) => i
    case None => default
  }
}
