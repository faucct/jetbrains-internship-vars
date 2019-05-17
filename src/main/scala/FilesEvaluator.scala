import java.io.File

import Parser.{AssignmentStatement, ImportStatement}

import scala.collection.mutable
import scala.util.parsing.input.{PagedSeq, PagedSeqReader, Position}

class FilesEvaluator {
  type Vars = Map[String, String]
  type Result = Either[(String, List[(File, Position)]), Vars]
  private val fileProgress: mutable.Map[File, Option[Vars]] = mutable.Map.empty

  def apply(file: File): Result = {
    val canonicalFile = file.getCanonicalFile
    fileProgress.get(canonicalFile) match {
      case None =>
        fileProgress.put(canonicalFile, None)
        var input = Parser.preParser(new PagedSeqReader(PagedSeq.fromFile(canonicalFile))).next
        var privateVars : Vars = Map.empty
        var vars: Vars = Map.empty
        while (!input.atEnd) {
          Parser.statementParser(input) match {
            case Parser.Success(statement, next) =>
              statement match {
                case ImportStatement(fileName) =>
                  val importedFile = new File(canonicalFile.getParentFile, fileName + ".vars").getCanonicalFile
                  apply(importedFile) match {
                    case Left((message, stackTrace)) =>
                      return Left(message, (importedFile, input.pos) :: stackTrace)
                    case Right(importedMap) => privateVars ++= importedMap
                  }
                case AssignmentStatement(key, value) =>
                  vars ++= Map((key, vars.getOrElse(value, privateVars.getOrElse(value, value))))
              }
              fileProgress.put(canonicalFile, Some(vars))
              input = next
            case Parser.NoSuccess(msg, next) => return Left((msg, List((canonicalFile, next.pos))))
          }
        }
        Right(vars)
      case Some(None) => Left(("cyclic reference", List.empty))
      case Some(Some(map)) => Right(map)
    }
  }
}
