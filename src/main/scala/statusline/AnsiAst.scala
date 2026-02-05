package statusline

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

// ── Iron-refined ANSI primitives ──────────────────────────────────────

/** RGB channel: 0–255 */
type Channel = Int :| Interval.Closed[0, 255]

/** ANSI 256-color palette index: 0–255 */
type Ansi256Code = Int :| Interval.Closed[0, 255]

/** SGR (Select Graphic Rendition) parameter: 0–107 */
type SgrParam = Int :| Interval.Closed[0, 107]

// ── Color model ───────────────────────────────────────────────────────

/** Full ANSI color hierarchy: 4-bit named, 8-bit palette, 24-bit RGB */
enum Color:
  case Named(value: NamedColor)
  case Bright(value: NamedColor)
  case Palette(code: Ansi256Code)
  case Rgb(r: Channel, g: Channel, b: Channel)

enum NamedColor(val code: SgrParam):
  case Black   extends NamedColor(0.refine)
  case Red     extends NamedColor(1.refine)
  case Green   extends NamedColor(2.refine)
  case Yellow  extends NamedColor(3.refine)
  case Blue    extends NamedColor(4.refine)
  case Magenta extends NamedColor(5.refine)
  case Cyan    extends NamedColor(6.refine)
  case White   extends NamedColor(7.refine)

// ── Style ─────────────────────────────────────────────────────────────

/** Composable text style. All fields optional — None means "inherit". */
final case class Style(
    fg: Option[Color] = None,
    bg: Option[Color] = None,
    bold: Option[Boolean] = None,
    dim: Option[Boolean] = None,
    italic: Option[Boolean] = None,
    underline: Option[Boolean] = None,
    strikethrough: Option[Boolean] = None
):
  /** Merge this style with another; `other` takes precedence. */
  def ++(other: Style): Style = Style(
    fg = other.fg.orElse(fg),
    bg = other.bg.orElse(bg),
    bold = other.bold.orElse(bold),
    dim = other.dim.orElse(dim),
    italic = other.italic.orElse(italic),
    underline = other.underline.orElse(underline),
    strikethrough = other.strikethrough.orElse(strikethrough)
  )

  def isEmpty: Boolean =
    fg.isEmpty && bg.isEmpty && bold.isEmpty && dim.isEmpty &&
      italic.isEmpty && underline.isEmpty && strikethrough.isEmpty

object Style:
  val Empty: Style = Style()

  // Convenience constructors
  def fg(c: Color): Style = Style(fg = Some(c))
  def bg(c: Color): Style = Style(bg = Some(c))
  val bold: Style = Style(bold = Some(true))
  val dim: Style = Style(dim = Some(true))
  val italic: Style = Style(italic = Some(true))
  val underline: Style = Style(underline = Some(true))
  val strikethrough: Style = Style(strikethrough = Some(true))

// ── AST ───────────────────────────────────────────────────────────────

/** An algebraic representation of styled terminal text.
  *
  * Unlike fansi (which tracks attributes on a mutable char array),
  * this is a pure tree you can inspect, transform, and render.
  */
enum AnsiStr:
  /** Literal text content, no styling */
  case Text(content: String)

  /** Apply a style layer to a subtree */
  case Styled(style: Style, child: AnsiStr)

  /** Ordered sequence of fragments */
  case Sequence(children: List[AnsiStr])

  /** Empty node — identity for concatenation */
  case Empty

// ── AnsiStr companion: smart constructors & combinators ───────────────

object AnsiStr:

  /** Create styled text in one step */
  def styled(content: String, style: Style): AnsiStr =
    if style.isEmpty then Text(content)
    else Styled(style, Text(content))

  /** Concatenate, flattening where possible */
  def concat(parts: AnsiStr*): AnsiStr =
    val flat = parts.toList.flatMap:
      case Empty       => Nil
      case Sequence(c) => c
      case other       => List(other)
    flat match
      case Nil      => Empty
      case h :: Nil => h
      case many     => Sequence(many)

  // ── Syntax extensions ───────────────────────────────────────────────

  extension (self: AnsiStr)
    /** Concatenate two AST nodes */
    def ++(other: AnsiStr): AnsiStr = concat(self, other)

    /** Wrap in a style layer */
    def withStyle(style: Style): AnsiStr = self match
      case Empty => Empty
      case _     => Styled(style, self)

    /** Get the plain text content (strip all styling) */
    def plainText: String = self match
      case Text(c)        => c
      case Styled(_, c)   => c.plainText
      case Sequence(cs)   => cs.map(_.plainText).mkString
      case Empty          => ""

    /** Visible character length (excludes ANSI codes) */
    def length: Int = plainText.length

    /** Render to ANSI-escaped terminal string */
    def render: String = AnsiRenderer.render(self)
