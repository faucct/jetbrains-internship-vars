import java.io.{File, PrintStream}

object Main {
  def main(args: Array[String]): Unit = {
    new Main(System.out, System.err)(args)
  }
}

class Main(out: PrintStream, err: PrintStream) {
  def apply(args: Array[String]): Unit = {
    val evaluator = new FilesEvaluator

    for (arg <- args) {
      evaluator(new File(arg)) match {
        case Right(vars) =>
          if (args.length > 1)
            out.println(arg)
          for (elem <- vars) {
            out.println(elem._1 + "=" + elem._2)
          }
        case Left((message, stackTrace)) =>
          err.println(message ++ ":")
          for ((file, position) <- stackTrace) {
            err.println("  " ++ file.toString ++ ":" ++ position.line.toString)
          }
      }
    }
  }
}
