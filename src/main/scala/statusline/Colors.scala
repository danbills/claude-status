package statusline

/** ANSI color utilities built on the AnsiStr AST.
  *
  * Provides Style values and a `styled` constructor that
  * produces AnsiStr nodes — no raw escape codes, no fansi.
  */
object Colors:

  // ── Named style shortcuts ─────────────────────────────────────────

  val Bold: Style = Style.bold
  val Dim: Style = Style.dim

  val Red: Style = Style.fg(Color.Named(NamedColor.Red))
  val Green: Style = Style.fg(Color.Named(NamedColor.Green))
  val Yellow: Style = Style.fg(Color.Named(NamedColor.Yellow))
  val Blue: Style = Style.fg(Color.Named(NamedColor.Blue))
  val Cyan: Style = Style.fg(Color.Named(NamedColor.Cyan))
  val White: Style = Style.fg(Color.Named(NamedColor.White))

  /** Create a styled AnsiStr node from text and one or more styles.
    *
    * Styles are merged left-to-right, so later styles override earlier ones.
    */
  def styled(s: String, styles: Style*): AnsiStr =
    val merged = styles.foldLeft(Style.Empty)(_ ++ _)
    AnsiStr.styled(s, merged)
