import scala.util.parsing.combinator.Parsers

object Parser extends Parsers {
  type Elem = Char

  sealed trait Statement

  case class ImportStatement(fileName: String) extends Statement

  case class AssignmentStatement(key: String, value: String) extends Statement

  private val rValueParser =
    rep1(acceptIf(_.isLetterOrDigit)(_ => "expected an identifier"))
      .withFailureMessage("expected value") ^^ { value => value.mkString }

  private val lValueParser =
    acceptIf(_.isLetter)(_ => "") ~! rep(acceptIf(_.isLetterOrDigit)(_ => ""))
      .withFailureMessage("expected an identifier") ^^ {
      case firstLetter ~ identifier => firstLetter + identifier.mkString
    }

  private val fileNameParser =
    rep1(acceptIf(_.isLetterOrDigit)(_ => "expected a filename"))
      .withErrorMessage("expected a filename") ^^ { fileName => fileName.mkString }

  private val spaceCharParser =
    acceptIf(_.isSpaceChar)(_ => "expected space char")

  private val whitespaceParser =
    acceptIf(_.isWhitespace)(_ => "expected whitespace")

  private val importStatementParser: Parser[ImportStatement] =
    acceptSeq("import") ~>
      rep1(whitespaceParser) ~>!
      fileNameParser <~!
      (whitespaceParser | phrase(success(None))) ^^
      ImportStatement

  private val assignmentStatementParser =
    (
      lValueParser <~!
        rep(spaceCharParser) <~!
        accept('=').withFailureMessage("expected '='") <~!
        rep(whitespaceParser)) ~
      rValueParser <~!
      (whitespaceParser | phrase(success(None))) ^^ { case key ~ value => AssignmentStatement(key, value) }

  val preParser: Parser[Object] = rep(whitespaceParser)

  val statementParser: Parser[Statement] =
    (
      importStatementParser |
        assignmentStatementParser
      ).withFailureMessage("expected a statement") <~ rep(whitespaceParser)
}
