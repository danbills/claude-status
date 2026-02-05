package statusline

import fansi.{Attr, Str}

trait StatusLineFormatter {
  def format(e: StatusEvent): FormattedOutput

  // Shared helpers - return fansi.Str for composition
  def calculateContextPercent(window: ContextWindow): Double = {
    val total = window.total_input_tokens + window.total_output_tokens
    if window.context_window_size == 0 then 0.0
    else (total.toDouble / window.context_window_size.toDouble) * 100
  }

  def contextColorAttr(pct: Double): Attr =
    if pct < 50 then Colors.Green
    else if pct < 80 then Colors.Yellow
    else Colors.Red

  def contextBar(pct: Double): Str = {
    val filled = (pct / 10).toInt.min(10).max(0)
    val color = contextColorAttr(pct)
    Colors.styled("█" * filled, color) ++ Colors.styled("░" * (10 - filled), Colors.Dim)
  }

  def abbreviateHome(path: String): String = {
    val home = sys.env.getOrElse("HOME", "")
    if path.startsWith(home) then "~" + path.drop(home.length) else path
  }

  def linesChanged(added: Int, removed: Int): Str =
    Colors.styled(s"+$added", Colors.Green) ++ Str("/") ++ Colors.styled(s"-$removed", Colors.Red)

  def modelName(name: String): Str =
    Colors.styled(name, Colors.Cyan, Colors.Bold)

  def cost(usd: Double): Str =
    Colors.styled(f"$$$usd%.4f", Colors.Yellow)

  def directory(path: String): Str =
    Colors.styled(abbreviateHome(path), Colors.Blue, Colors.Dim)

  def prompt: Str =
    Colors.styled(">", Colors.White, Colors.Bold)

  def contextPercent(pct: Double): Str =
    Colors.styled(s"${pct.toInt}%", contextColorAttr(pct))

  def branchName(name: String): Str =
    Colors.styled(name, Colors.Cyan)

  def dirtyMarker(isDirty: Boolean): Str =
    if isDirty then Colors.styled("*", Colors.Red, Colors.Bold) else Str("")

  def aheadBehind(ahead: Int, behind: Int): Str = {
    val parts = List(
      if ahead > 0 then Some(Colors.styled(s"↑$ahead", Colors.Green)) else None,
      if behind > 0 then Some(Colors.styled(s"↓$behind", Colors.Red)) else None
    ).flatten
    if parts.isEmpty then Str("") else parts.reduce(_ ++ _)
  }

  def gitLines(added: Int, deleted: Int): Str =
    Colors.styled(s"+$added", Colors.Green) ++ Str("/") ++ Colors.styled(s"-$deleted", Colors.Red)
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

    FormattedOutput(
      Str("[") ++ model ++ Str("] ") ++ bar ++ Str(" ") ++ percent ++
        Str(" | ") ++ costStr ++ Str(" | ") ++ lines ++ Str(" | ") ++ dir ++ Str(" ") ++ prompt
    )
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

    FormattedOutput(
      model ++ Str("@") ++ percent ++ Str(" ") ++ costStr ++ Str(" ") ++
        lines ++ Str(" ") ++ dir ++ Str(" ") ++ prompt
    )
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

    FormattedOutput(
      Str("🤖 ") ++ model ++ Str(" [") ++ percent ++ Str("] ") ++ costStr ++
        Str(" ") ++ lines ++ Str(" ") ++ dir ++ Str(" ") ++ prompt
    )
  }
}

object GitBarFormatter extends StatusLineFormatter {
  def format(e: StatusEvent): FormattedOutput = {
    val model = modelName(e.model.display_name)
    val pct = calculateContextPercent(e.context_window)
    val bar = contextBar(pct)
    val percent = contextPercent(pct)
    val costStr = cost(e.cost.total_cost_usd)

    val gitPart: Str = GitHelper.getGitInfo(e.workspace.project_dir) match {
      case Some(git) =>
        val branch = branchName(git.branch) ++ dirtyMarker(git.isDirty)
        val ab = aheadBehind(git.ahead, git.behind)
        val lines = gitLines(git.linesAdded, git.linesDeleted)
        Str(" | ") ++ branch ++ ab ++ Str(" | ") ++ lines
      case None => Str("")
    }

    FormattedOutput(
      Str("[") ++ model ++ Str("] ") ++ bar ++ Str(" ") ++ percent ++
        Str(" | ") ++ costStr ++ gitPart ++ Str(" ") ++ prompt
    )
  }
}

object GitCompactFormatter extends StatusLineFormatter {
  def format(e: StatusEvent): FormattedOutput = {
    val model = modelName(e.model.display_name)
    val pct = calculateContextPercent(e.context_window)
    val percent = contextPercent(pct)

    val gitPart: Str = GitHelper.getGitInfo(e.workspace.project_dir) match {
      case Some(git) =>
        val branch = branchName(git.branch) ++ dirtyMarker(git.isDirty)
        Str(" ") ++ branch
      case None => Str("")
    }

    FormattedOutput(
      model ++ Str("@") ++ percent ++ gitPart ++ Str(" ") ++ prompt
    )
  }
}
