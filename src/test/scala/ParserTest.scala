import org.scalatest.{FunSuite, Inside, Matchers}

import scala.util.parsing.input.CharSequenceReader

class ParserTest extends FunSuite with Matchers with Inside {

  test("rValueParser") {
    Parser.statementParser(new CharSequenceReader("a = _")) should matchPattern {
      case Parser.NoSuccess("expected value", next) if next.pos.line == 1 && next.pos.column == 5 =>
    }
  }

  test("importStatementParser") {
    Parser.statementParser(new CharSequenceReader("imported = 3")) should matchPattern {
      case Parser.Success(Parser.AssignmentStatement("imported", "3"), _) =>
    }
  }

  test("import statement") {
    Parser.statementParser(new CharSequenceReader("import foo")) should matchPattern {
      case Parser.Success(Parser.ImportStatement("foo"), _) =>
    }
  }

  test("assignment statement") {
    Parser.statementParser(new CharSequenceReader("a = 10")) should matchPattern {
      case Parser.Success(Parser.AssignmentStatement("a", "10"), _) =>
    }
  }

  test("not a statement") {
    Parser.statementParser(new CharSequenceReader("10")) should matchPattern {
      case Parser.NoSuccess("expected a statement", next) if next.pos.line == 1 && next.pos.column == 1 =>
    }
  }
}
