package statusline

trait StatusLineFormatter {
  def format(e: StatusEvent): FormattedOutput

  // ── Shared helpers (return AnsiStr for composition) ─────────────────

  def calculateContextPercent(window: ContextWindow): Double = {
    val total = window.total_input_tokens + window.total_output_tokens
    if window.context_window_size == 0 then 0.0
    else (total.toDouble / window.context_window_size.toDouble) * 100
  }

  def contextColorStyle(pct: Double): Style =
    if pct < 50 then Colors.Green
    else if pct < 80 then Colors.Yellow
    else Colors.Red

  def contextBar(pct: Double): AnsiStr = {
    val filled = (pct / 10).toInt.min(10).max(0)
    val color = contextColorStyle(pct)
    Colors.styled("█" * filled, color) ++ Colors.styled("░" * (10 - filled), Colors.Dim)
  }

  def abbreviateHome(path: String): String = {
    val home = sys.env.getOrElse("HOME", "")
    if path.startsWith(home) then "~" + path.drop(home.length) else path
  }

  def linesChanged(added: Int, removed: Int): AnsiStr =
    Colors.styled(s"+$added", Colors.Green) ++ AnsiStr.Text("/") ++ Colors.styled(s"-$removed", Colors.Red)

  def modelName(name: String): AnsiStr =
    Colors.styled(name, Colors.Cyan, Colors.Bold)

  def cost(usd: Double): AnsiStr =
    Colors.styled(f"$$$usd%.4f", Colors.Yellow)

  def directory(path: String): AnsiStr =
    Colors.styled(abbreviateHome(path), Colors.Blue, Colors.Dim)

  def prompt: AnsiStr =
    Colors.styled(">", Colors.White, Colors.Bold)

  def contextPercent(pct: Double): AnsiStr =
    Colors.styled(s"${pct.toInt}%", contextColorStyle(pct))

  def branchName(name: String): AnsiStr =
    Colors.styled(name, Colors.Cyan)

  def dirtyMarker(isDirty: Boolean): AnsiStr =
    if isDirty then Colors.styled("*", Colors.Red, Colors.Bold) else AnsiStr.Empty

  def aheadBehind(ahead: Int, behind: Int): AnsiStr = {
    val parts = List(
      if ahead > 0 then Some(Colors.styled(s"↑$ahead", Colors.Green)) else None,
      if behind > 0 then Some(Colors.styled(s"↓$behind", Colors.Red)) else None
    ).flatten
    if parts.isEmpty then AnsiStr.Empty else AnsiStr.concat(parts*)
  }

  def gitLines(added: Int, deleted: Int): AnsiStr =
    Colors.styled(s"+$added", Colors.Green) ++ AnsiStr.Text("/") ++ Colors.styled(s"-$deleted", Colors.Red)
}

object BarFormatter extends StatusLineFormatter {
  def format(e: StatusEvent): FormattedOutput = {
    val model = modelName(e.model.display_name)
    val pct = calculateContextPercent(e.context_window)
    val bar = contextBar(pct)
    val percent = contextPercent(pct)
    val costStr = cost(e.cost.total_cost_usd)
    val lines = linesChanged(e.cost.total_lines_added, e.cost.total_lines_removed)
    val dir = directory(e.workspace.project_dir)

    FormattedOutput(AnsiStr.concat(
      AnsiStr.Text("["), model, AnsiStr.Text("] "), bar, AnsiStr.Text(" "), percent,
      AnsiStr.Text(" | "), costStr, AnsiStr.Text(" | "), lines, AnsiStr.Text(" | "), dir,
      AnsiStr.Text(" "), prompt
    ))
  }
}

object CompactFormatter extends StatusLineFormatter {
  def format(e: StatusEvent): FormattedOutput = {
    val model = modelName(e.model.display_name)
    val pct = calculateContextPercent(e.context_window)
    val percent = contextPercent(pct)
    val costStr = cost(e.cost.total_cost_usd)
    val lines = linesChanged(e.cost.total_lines_added, e.cost.total_lines_removed)
    val dir = directory(e.workspace.project_dir)

    FormattedOutput(AnsiStr.concat(
      model, AnsiStr.Text("@"), percent, AnsiStr.Text(" "), costStr, AnsiStr.Text(" "),
      lines, AnsiStr.Text(" "), dir, AnsiStr.Text(" "), prompt
    ))
  }
}

object EmojiFormatter extends StatusLineFormatter {
  def format(e: StatusEvent): FormattedOutput = {
    val model = modelName(e.model.display_name)
    val pct = calculateContextPercent(e.context_window)
    val percent = contextPercent(pct)
    val costStr = cost(e.cost.total_cost_usd)
    val lines = linesChanged(e.cost.total_lines_added, e.cost.total_lines_removed)
    val dir = directory(e.workspace.project_dir)

    FormattedOutput(AnsiStr.concat(
      AnsiStr.Text("🤖 "), model, AnsiStr.Text(" ["), percent, AnsiStr.Text("] "), costStr,
      AnsiStr.Text(" "), lines, AnsiStr.Text(" "), dir, AnsiStr.Text(" "), prompt
    ))
  }
}

object GitBarFormatter extends StatusLineFormatter {
  def format(e: StatusEvent): FormattedOutput = {
    val model = modelName(e.model.display_name)
    val pct = calculateContextPercent(e.context_window)
    val bar = contextBar(pct)
    val percent = contextPercent(pct)
    val costStr = cost(e.cost.total_cost_usd)

    val gitPart: AnsiStr = GitHelper.getGitInfo(e.workspace.project_dir) match {
      case Some(git) =>
        val branch = branchName(git.branch) ++ dirtyMarker(git.isDirty)
        val ab = aheadBehind(git.ahead, git.behind)
        val lines = gitLines(git.linesAdded, git.linesDeleted)
        AnsiStr.concat(AnsiStr.Text(" | "), branch, ab, AnsiStr.Text(" | "), lines)
      case None => AnsiStr.Empty
    }

    FormattedOutput(AnsiStr.concat(
      AnsiStr.Text("["), model, AnsiStr.Text("] "), bar, AnsiStr.Text(" "), percent,
      AnsiStr.Text(" | "), costStr, gitPart, AnsiStr.Text(" "), prompt
    ))
  }
}

object GitCompactFormatter extends StatusLineFormatter {
  def format(e: StatusEvent): FormattedOutput = {
    val model = modelName(e.model.display_name)
    val pct = calculateContextPercent(e.context_window)
    val percent = contextPercent(pct)

    val gitPart: AnsiStr = GitHelper.getGitInfo(e.workspace.project_dir) match {
      case Some(git) =>
        val branch = branchName(git.branch) ++ dirtyMarker(git.isDirty)
        AnsiStr.concat(AnsiStr.Text(" "), branch)
      case None => AnsiStr.Empty
    }

    FormattedOutput(AnsiStr.concat(
      model, AnsiStr.Text("@"), percent, gitPart, AnsiStr.Text(" "), prompt
    ))
  }
}
