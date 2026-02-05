package statusline

import fansi.{Attr, Bold => FBold, Color => FColor, Str}

/** ANSI color utilities using the fansi library.
  *
  * Provides both fansi.Attr-based styling (recommended) and legacy
  * String-based escape codes for backwards compatibility.
  */
object Colors {
  // fansi attributes for rich styling
  val Reset: Attr = fansi.Attr.Reset
  val Bold: Attr = FBold.On
  val Dim: Attr = FColor.True(128, 128, 128) // fansi doesn't have Dim, approximate with gray

  val Red: Attr = FColor.Red
  val Green: Attr = FColor.Green
  val Yellow: Attr = FColor.Yellow
  val Blue: Attr = FColor.Blue
  val Cyan: Attr = FColor.Cyan
  val White: Attr = FColor.White

  /** Apply fansi styling to create a styled Str */
  def styled(s: String, attrs: Attr*): Str =
    attrs.foldLeft(Str(s))((str, attr) => attr(str))

  /** Apply fansi styling and wrap in FormattedOutput */
  def formatted(s: String, attrs: Attr*): FormattedOutput =
    FormattedOutput(styled(s, attrs*))

  // Legacy String-based escape codes for backwards compatibility
  private val EscReset = "\u001b[0m"
  private val EscBold = "\u001b[1m"
  private val EscDim = "\u001b[2m"
  private val EscRed = "\u001b[31m"
  private val EscGreen = "\u001b[32m"
  private val EscYellow = "\u001b[33m"
  private val EscBlue = "\u001b[34m"
  private val EscCyan = "\u001b[36m"
  private val EscWhite = "\u001b[37m"

  /** Legacy: Apply raw ANSI escape codes (returns String) */
  def colored(s: String, codes: String*): String =
    codes.mkString + s + EscReset

  /** Map legacy escape codes to fansi attrs for migration */
  def escapeToAttr(code: String): Attr = code match {
    case `EscBold`   => Bold
    case `EscDim`    => Dim
    case `EscRed`    => Red
    case `EscGreen`  => Green
    case `EscYellow` => Yellow
    case `EscBlue`   => Blue
    case `EscCyan`   => Cyan
    case `EscWhite`  => White
    case _           => Reset
  }
}
