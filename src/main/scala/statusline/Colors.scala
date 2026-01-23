package statusline

object Colors {
  val Reset = "\u001b[0m"
  val Bold = "\u001b[1m"
  val Dim = "\u001b[2m"

  val Red = "\u001b[31m"
  val Green = "\u001b[32m"
  val Yellow = "\u001b[33m"
  val Blue = "\u001b[34m"
  val Cyan = "\u001b[36m"
  val White = "\u001b[37m"

  def colored(s: String, codes: String*): String =
    codes.mkString + s + Reset
}
