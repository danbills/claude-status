package statusline

/** Wrapper for formatted output that provides access to the ANSI AST
  * and convenience methods for rendering to different formats.
  *
  * Callers can:
  *   - Use `.ast` to get the underlying AnsiStr tree for inspection/transforms
  *   - Use `.render` to get the ANSI-escaped String for terminal output
  *   - Use `.plainText` to get the unstyled text content
  */
final case class FormattedOutput(ast: AnsiStr) {

  /** Render to ANSI-escaped String for terminal display */
  def render: String = ast.render

  /** Get plain text without any ANSI styling */
  def plainText: String = ast.plainText

  /** Get the length of the visible text (excluding ANSI codes) */
  def length: Int = ast.length

  /** Concatenate with another FormattedOutput */
  def ++(other: FormattedOutput): FormattedOutput =
    FormattedOutput(ast ++ other.ast)

  /** Concatenate with an AnsiStr */
  def ++(other: AnsiStr): FormattedOutput =
    FormattedOutput(ast ++ other)

  override def toString: String = render
}

object FormattedOutput {

  /** Create from an AnsiStr AST */
  def apply(ast: AnsiStr): FormattedOutput = new FormattedOutput(ast)

  /** Create from a plain String (no styling) */
  def plain(s: String): FormattedOutput = FormattedOutput(AnsiStr.Text(s))

  /** Create an empty FormattedOutput */
  val empty: FormattedOutput = FormattedOutput(AnsiStr.Empty)

  /** Concatenate multiple FormattedOutputs */
  def concat(outputs: FormattedOutput*): FormattedOutput =
    FormattedOutput(AnsiStr.concat(outputs.map(_.ast)*))

  /** Implicit conversion to allow seamless use where String is expected */
  given Conversion[FormattedOutput, String] = _.render
}
