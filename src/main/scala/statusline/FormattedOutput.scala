package statusline

import fansi.Str

/** Wrapper for formatted output that provides access to both the rich fansi.Str
  * representation and convenience methods for rendering to different formats.
  *
  * This makes the richer fansi type optional - callers can:
  *   - Use `.styled` to get the underlying fansi.Str for rich manipulation
  *   - Use `.render` to get the ANSI-escaped String for terminal output
  *   - Use `.plainText` to get the unstyled text content
  */
final case class FormattedOutput(styled: Str) {

  /** Render to ANSI-escaped String for terminal display */
  def render: String = styled.render

  /** Get plain text without any ANSI styling */
  def plainText: String = styled.plainText

  /** Get the length of the visible text (excluding ANSI codes) */
  def length: Int = styled.length

  /** Concatenate with another FormattedOutput */
  def ++(other: FormattedOutput): FormattedOutput =
    FormattedOutput(styled ++ other.styled)

  /** Concatenate with a fansi.Str */
  def ++(other: Str): FormattedOutput =
    FormattedOutput(styled ++ other)

  override def toString: String = render
}

object FormattedOutput {

  /** Create from a fansi.Str */
  def apply(s: Str): FormattedOutput = new FormattedOutput(s)

  /** Create from a plain String (no styling) */
  def plain(s: String): FormattedOutput = FormattedOutput(Str(s))

  /** Create an empty FormattedOutput */
  val empty: FormattedOutput = FormattedOutput(Str(""))

  /** Concatenate multiple FormattedOutputs */
  def concat(outputs: FormattedOutput*): FormattedOutput =
    outputs.foldLeft(empty)(_ ++ _)

  /** Implicit conversion to allow seamless use where String is expected */
  given Conversion[FormattedOutput, String] = _.render
}
