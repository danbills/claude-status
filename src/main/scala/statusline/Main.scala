package statusline

import io.circe.parser.decode

object Main {
  def main(args: Array[String]): Unit = {
    val format = args.headOption.getOrElse("bar")
    val formatter = format match {
      case "compact" => CompactFormatter
      case "emoji"   => EmojiFormatter
      case _         => BarFormatter
    }

    val json = scala.io.Source.stdin.getLines().mkString

    decode[StatusEvent](json) match {
      case Right(event) => println(formatter.format(event))
      case Left(error)  =>
        System.err.println(s"Error parsing JSON: ${error.getMessage}")
        sys.exit(1)
    }
  }
}
