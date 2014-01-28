import akka.actor.{ActorSystem, Props}
import java.io.File
import models.entities._
import models.objects._
import org.joda.time.DateTime
import play.api.cache.Cache
import play.api.db.slick.plugin.TableScanner
import play.api.libs.concurrent.Akka
import play.api.libs.Files
import play.api.mvc.WithFilters
import play.api._
import play.filters.gzip.GzipFilter
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scalax.io.{Codec, Resource}
import play.api.Play.current

object Global extends WithFilters(new GzipFilter(shouldGzip = (request, response) =>
  response.headers.get("Content-Type").exists(_.startsWith("text")))) with GlobalSettings {

  val logger = Logger("Global").logger
  private val configKey = "slick"
  private val ScriptDirectory = "conf/evolutions/"
  private val CreateScript = "create-database.sql"
  private val DropScript = "drop-database.sql"
  private val ScriptHeader = "-- SQL DDL script\n-- Generated file - do not edit\n\n"

  /**
   * Creates SQL DDL scripts on application start-up.
   */
  override def onStart(application: Application) {
    prerequisites

    if (application.mode != Mode.Prod) {
      application.configuration.getConfig(configKey).foreach {
        configuration =>
          configuration.keys.foreach {
            database =>
              val databaseConfiguration = configuration.getString(database).getOrElse {
                throw configuration.reportError(database, "No config: key " + database, None)
              }
              val packageNames = databaseConfiguration.split(",").toSet
              val classloader = application.classloader
              val ddls = TableScanner.reflectAllDDLMethods(packageNames, classloader)
              val scriptDirectory = application.getFile(ScriptDirectory + database)
              Files.createDirectory(scriptDirectory)
              val us = new StringBuilder
              val ds = new StringBuilder
              val is = new StringBuilder
              ds.append(flatten(ddls.map(_.dropStatements)).replaceAll("drop table", "DROP TABLE IF EXISTS"))
              writeScript(ds.toString(), scriptDirectory, DropScript)
              us.append(flatten(ddls.map(_.createStatements)).replaceAll("create table", "CREATE TABLE IF NOT EXISTS"))
              inserts(is)
              writeScript(us.toString(), scriptDirectory, CreateScript)
              val all = new StringBuilder
              all.append("\n;\n# ---!Downs\n")
              all.append("\n;\n# ---!Ups\n")
              all.append(is)
              writeScript(all.toString(), scriptDirectory, "exec.sql")
          }
      }
    }
    if (application.mode == Mode.Test) {
      testData
    }

  }

  override def onStop(application: Application) {
  }


  private def inserts(s: StringBuilder): StringBuilder = {
    s
  }

  def drops(s: StringBuilder): StringBuilder = {
    s
  }

  private def flatten(r: Seq[Iterator[String]]): String = r.flatten.mkString(";\n\n")

  /**
   * Writes the given DDL statements to a file.
   */
  private def writeScript(sql: String, directory: File, fileName: String): Unit = {
    val createScript = new File(directory, fileName)
    Files.writeFileIfChanged(createScript, ScriptHeader + sql)
  }


  /**
   * Run prerequisites
   * @return
   */
  private def prerequisites {
  }

  /**
   * test data seeds
   */
  private def testData {
  }

}


