package statusline

import io.circe.parser.decode

object Main {
  def main(args: Array[String]): Unit = {
    val format = args.headOption.getOrElse("bar")
    val formatter = format match {
      case "compact"    => CompactFormatter
      case "emoji"      => EmojiFormatter
      case "git"        => GitBarFormatter
      case "gitcompact" => GitCompactFormatter
      case _            => BarFormatter
    }

    val json = scala.io.Source.stdin.getLines().mkString

    decode[StatusEvent](json) match {
      case Right(event) =>
        val output = formatter.format(event)
        // Use .render to get ANSI string, or .styled for the rich fansi.Str
        println(output.render)
      case Left(error) =>
        System.err.println(s"Error parsing JSON: ${error.getMessage}")
        sys.exit(1)
    }
  }
}
