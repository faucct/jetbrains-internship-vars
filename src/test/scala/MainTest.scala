import java.io.{ByteArrayOutputStream, PrintStream}

import org.scalatest.FunSuite

class MainTest extends FunSuite {
  test("success") {
    val out = new ByteArrayOutputStream()
    val err = new ByteArrayOutputStream()
    new Main(new PrintStream(out), new PrintStream(err))(Array(getClass.getResource("file1.vars").getPath))
    assertResult("a=10\nb=15\nc=10\n")(out.toString)
    assertResult("")(err.toString)
  }

  test("failure") {
    val out = new ByteArrayOutputStream()
    val err = new ByteArrayOutputStream()
    new Main(new PrintStream(out), new PrintStream(err))(Array(getClass.getResource("wtf.vars").getPath))
    assertResult("")(out.toString)
    assertResult("expected '=':\n  " + getClass.getResource("wtf.vars").getFile + ":1\n")(err.toString)
  }
}
