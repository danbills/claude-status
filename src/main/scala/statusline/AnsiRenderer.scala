package statusline

import io.github.iltotore.iron.*

/** Renders an AnsiStr AST to an ANSI-escaped terminal string.
  *
  * Walks the tree, tracking the current style context so it only emits
  * SGR sequences when the style actually changes.
  */
object AnsiRenderer:

  private val Esc = "\u001b["
  private val Reset = s"${Esc}0m"

  def render(node: AnsiStr): String =
    val sb = StringBuilder()
    walk(node, Style.Empty, sb)
    // Reset at the end if we emitted anything styled
    if sb.nonEmpty then sb.append(Reset)
    sb.toString

  private def walk(node: AnsiStr, inherited: Style, sb: StringBuilder): Unit =
    node match
      case AnsiStr.Empty =>
        ()

      case AnsiStr.Text(content) =>
        if content.nonEmpty then
          emitSgr(inherited, sb)
          sb.append(content)

      case AnsiStr.Styled(style, child) =>
        val merged = inherited ++ style
        walk(child, merged, sb)

      case AnsiStr.Sequence(children) =>
        children.foreach(walk(_, inherited, sb))

  private def emitSgr(style: Style, sb: StringBuilder): Unit =
    val params = collectSgrParams(style)
    if params.nonEmpty then
      sb.append(Esc)
      sb.append(params.mkString(";"))
      sb.append("m")

  private def collectSgrParams(style: Style): List[String] =
    val buf = List.newBuilder[String]

    style.bold.foreach(b => buf += (if b then "1" else "22"))
    style.dim.foreach(b => buf += (if b then "2" else "22"))
    style.italic.foreach(b => buf += (if b then "3" else "23"))
    style.underline.foreach(b => buf += (if b then "4" else "24"))
    style.strikethrough.foreach(b => buf += (if b then "9" else "29"))

    style.fg.foreach(c => buf ++= fgParams(c))
    style.bg.foreach(c => buf ++= bgParams(c))

    buf.result()

  private def fgParams(color: Color): List[String] = color match
    case Color.Named(nc)      => List((30 + nc.code).toString)
    case Color.Bright(nc)     => List((90 + nc.code).toString)
    case Color.Palette(code)  => List("38", "5", code.toString)
    case Color.Rgb(r, g, b)  => List("38", "2", r.toString, g.toString, b.toString)

  private def bgParams(color: Color): List[String] = color match
    case Color.Named(nc)      => List((40 + nc.code).toString)
    case Color.Bright(nc)     => List((100 + nc.code).toString)
    case Color.Palette(code)  => List("48", "5", code.toString)
    case Color.Rgb(r, g, b)  => List("48", "2", r.toString, g.toString, b.toString)
