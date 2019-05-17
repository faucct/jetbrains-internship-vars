import java.io.File
import java.nio.file.Files

import org.scalatest.{FunSuite, Inside, Matchers}

import scala.util.parsing.input.Position

class FilesEvaluatorTest extends FunSuite with Matchers with Inside {
  test("file1") {
    assertResult(Right(Map(
      "a" -> "10",
      "b" -> "15",
      "c" -> "10",
    )))(new FilesEvaluator()(new File(getClass.getResource("file1.vars").getFile)))
  }

  test("file2") {
    assertResult(Right(Map(
      "d" -> "40",
      "e" -> "10",
      "foo" -> "10",
    )))(new FilesEvaluator()(new File(getClass.getResource("file2.vars").getFile)))
  }

  test("file3") {
    assertResult(Right(Map(
      "a3" -> "10",
      "b3" -> "40",
      "foo3" -> "10",
    )))(new FilesEvaluator()(new File(getClass.getResource("file3.vars").getFile)))
  }

  private class SameFile(file1: File) {
    def unapply(file2: File) = Files.isSameFile(file1.toPath, file2.toPath)
  }

  private object Position {
    def unapply(position: Position) = Some(position.line, position.column)
  }

  test("cyclic") {
    val cyclic1 = new File(getClass.getResource("cyclic1.vars").getFile)
    val cyclic2 = new File(getClass.getResource("cyclic2.vars").getFile)
    new FilesEvaluator()(cyclic1) should matchPattern {
      case Left(("cyclic reference", (file1: File, Position(1, 1)) :: (file2: File, Position(1, 1)) :: Nil))
        if Files.isSameFile(file1.toPath, cyclic2.toPath) && Files.isSameFile(file2.toPath, cyclic1.toPath) =>
    }
  }

  test("foo") {
    new FilesEvaluator()(new File(getClass.getResource("wtf.vars").getFile)) should matchPattern {
      case Left(("expected '='", (_ : File, Position(1, 4)) :: Nil)) =>
    }
  }
}
