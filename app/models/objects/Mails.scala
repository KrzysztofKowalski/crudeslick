package models.objects

import play.api.db.slick.Config.driver.simple._
import scala.slick.lifted.ColumnBase
import org.joda.time.DateTime
import scala.collection.immutable.StringOps._
import scala.Array
import play.api.db.slick._
import scala.Some
import play.api.Play.current
import com.github.tototoshi.slick.JodaSupport._
import play.api.libs.json.Json
import models.entities.{CRUDSuperPowers, Mail}


object Mails extends Table[Mail]("mails") with CRUDSuperPowers[Mail] {

  def uniqueLogin = index("UNI_LOGIN", login, unique = true)

  def login = column[String]("login")

  def password = column[String]("password")

  def pop3server = column[String]("pop3server")

  def * : ColumnBase[Mail] = base ~ login ~ password ~ pop3server <>(Mail.apply _, Mail.unapply _)

  def findByLogin(login: String): Option[Mail] = DB.withSession {
    implicit session: scala.slick.session.Session =>
      (for {a <- tableToQuery(this) if a.login === login} yield a).list.headOption
  }

}

