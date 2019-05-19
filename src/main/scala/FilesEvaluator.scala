import java.io.{File, FileReader}

import Parser.{AssignmentStatement, ImportStatement}

import scala.collection.mutable
import scala.util.control.NonFatal
import scala.util.parsing.input.{PagedSeq, PagedSeqReader, Position}

class FilesEvaluator {
  type Vars = Map[String, String]
  type Result = Either[(String, List[(File, (Int, Int))]), Vars]
  private val fileProgress: mutable.Map[File, Option[Vars]] = mutable.Map.empty

  def apply(file: File): Result = {
    val canonicalFile = file.getCanonicalFile
    fileProgress.get(canonicalFile) match {
      case None =>
        fileProgress.put(canonicalFile, None)
        withResources(new FileReader(canonicalFile)) { reader =>
          var input = Parser.preParser(new PagedSeqReader(PagedSeq.fromReader(reader))).next
          var privateVars: Vars = Map.empty
          var vars: Vars = Map.empty
          while (!input.atEnd) {
            Parser.statementParser(input) match {
              case Parser.Success(statement, next) =>
                statement match {
                  case ImportStatement(fileName) =>
                    val importedFile = new File(canonicalFile.getParentFile, fileName + ".vars").getCanonicalFile
                    apply(importedFile) match {
                      case Left((message, stackTrace)) =>
                        return Left(message, (importedFile, (input.pos.line, input.pos.column)) :: stackTrace)
                      case Right(importedMap) => privateVars ++= importedMap
                    }
                  case AssignmentStatement(key, value) =>
                    vars ++= Map((key, vars.getOrElse(value, privateVars.getOrElse(value, value))))
                }
                fileProgress.put(canonicalFile, Some(vars))
                input = next
              case Parser.NoSuccess(msg, next) =>
                return Left((msg, List((canonicalFile, (next.pos.line, next.pos.column)))))
            }
          }
          Right(vars)
        }
      case Some(None) => Left(("cyclic reference", List.empty))
      case Some(Some(map)) => Right(map)
    }
  }

  private def withResources[T <: AutoCloseable, V](r: => T)(f: T => V): V = {
    val resource: T = r
    require(resource != null, "resource is null")
    var exception: Throwable = null
    try {
      f(resource)
    } catch {
      case NonFatal(e) =>
        exception = e
        throw e
    } finally {
      closeAndAddSuppressed(exception, resource)
    }
  }

  private def closeAndAddSuppressed(e: Throwable,
    resource: AutoCloseable): Unit = {
    if (e != null) {
      try {
        resource.close()
      } catch {
        case NonFatal(suppressed) =>
          e.addSuppressed(suppressed)
      }
    } else {
      resource.close()
    }
  }
}
