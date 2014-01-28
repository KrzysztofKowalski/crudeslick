package models.entities

import org.joda.time.DateTime

case class Mail(override val id: Option[Long] = None,
                override val createdAt: Option[DateTime] = utils.TimeHelper.some,
                override val updatedAt: Option[DateTime] = utils.TimeHelper.some,
                login: String,
                password: String,
                pop3server: String) extends HasId {
  def p = models.objects.Mails

  def save = p.update(gid(), this)

}
