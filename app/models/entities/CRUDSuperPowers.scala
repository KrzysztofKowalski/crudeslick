package models.entities

import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import org.joda.time.DateTime
import play.api.Logger

//import scala.reflect.runtime.{universe => ru}

import com.github.tototoshi.slick.JodaSupport._
import play.api.libs.json.Json
import utils.HCache

/**
 * Helper for otherwise verbose Slick model definitions
 */
trait CRUDSuperPowers[T <: HasId] {
  self: Table[T] =>

  def logger = Logger(this.tableName).logger

  def id: Column[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def createdAt: Column[Option[DateTime]] = column[Option[DateTime]]("created_at", O.NotNull)

  def updatedAt: Column[Option[DateTime]] = column[Option[DateTime]]("updated_at")

  def autoInc = * returning id

  def base = id.? ~ createdAt ~ updatedAt

  def insert(entity: T): Long = DB.withSession {
    implicit session: scala.slick.session.Session => autoInc.insert(entity)
  }

  def insertAndReturn(entity: T): Option[T] = {
    val k = insert(entity)
    findById(k)
  }

  def insertAll(entities: Seq[T]) {
    DB.withSession {
      implicit session: scala.slick.session.Session =>
        autoInc.insertAll(entities: _*)
    }
  }

  def update(id: Long, entity: T): Unit = DB.withSession {
    implicit session: scala.slick.session.Session =>
      HCache.set(key(id), entity)
      tableQueryToUpdateInvoker(
        tableToQuery(this).where(_.id === id)
      ).update(entity)
  }


  def delete(id: Long): Unit =
    DB.withSession {
      HCache.remove(key(id))
      implicit session: scala.slick.session.Session =>
        queryToDeleteInvoker(
          tableToQuery(this).where(_.id === id)
        ).delete
    }


  def deleteAll {
    DB.withSession {
      implicit session: scala.slick.session.Session =>
        queryToDeleteInvoker(
          tableToQuery(this)
        ).delete
    }
  }

  def count = DB.withSession {
    implicit session: scala.slick.session.Session =>
      Query(tableToQuery(this).length).first
  }

  def key(pk: Long): String = s"${tableName}_$pk"

  def findById(pk: Long): Option[T] = DB.withSession {
    implicit session: scala.slick.session.Session =>
      val t = (for {a <- tableToQuery(this) if a.id === pk} yield a).list.headOption
      t
  }


  def findAll(limit: Int = 16000): List[T] = {
    DB.withSession {
      implicit session: scala.slick.session.Session => (for {a <- tableToQuery(this)} yield a).take(limit).list
    }
  }

  def apply(id: Long): Option[T] = {
    findById(id)
  }


  def firstId(default: Long = 1) = {
    val a = findAll().headOption
    a.isEmpty match {
      case true => default
      case false => a.get.id match {
        case Some(id) => id
        case None => default
      }
    }
  }

}
