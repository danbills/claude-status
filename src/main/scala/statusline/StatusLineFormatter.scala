package statusline

trait StatusLineFormatter {
  def format(e: StatusEvent): String

  // Shared helpers
  def calculateContextPercent(window: ContextWindow): Double = {
    val total = window.total_input_tokens + window.total_output_tokens
    if window.context_window_size == 0 then 0.0
    else (total.toDouble / window.context_window_size.toDouble) * 100
  }

  def contextColor(pct: Double): String =
    if pct < 50 then Colors.Green
    else if pct < 80 then Colors.Yellow
    else Colors.Red

  def contextBar(pct: Double): String = {
    val filled = (pct / 10).toInt.min(10).max(0)
    val color = contextColor(pct)
    Colors.colored("█" * filled, color) + Colors.colored("░" * (10 - filled), Colors.Dim)
  }

  def abbreviateHome(path: String): String = {
    val home = sys.env.getOrElse("HOME", "")
    if path.startsWith(home) then "~" + path.drop(home.length) else path
  }

  def linesChanged(added: Int, removed: Int): String =
    Colors.colored(s"+$added", Colors.Green) + "/" + Colors.colored(s"-$removed", Colors.Red)

  def modelName(name: String): String =
    Colors.colored(name, Colors.Cyan, Colors.Bold)

  def cost(usd: Double): String =
    Colors.colored(f"$$$usd%.4f", Colors.Yellow)

  def directory(path: String): String =
    Colors.colored(abbreviateHome(path), Colors.Blue, Colors.Dim)

  def prompt: String =
    Colors.colored(">", Colors.White, Colors.Bold)

  def contextPercent(pct: Double): String =
    Colors.colored(s"${pct.toInt}%", contextColor(pct))

  def branchName(name: String): String =
    Colors.colored(name, Colors.Cyan)

  def dirtyMarker(isDirty: Boolean): String =
    if isDirty then Colors.colored("*", Colors.Red, Colors.Bold) else ""

  def aheadBehind(ahead: Int, behind: Int): String = {
    val parts = List(
      if ahead > 0 then Some(Colors.colored(s"↑$ahead", Colors.Green)) else None,
      if behind > 0 then Some(Colors.colored(s"↓$behind", Colors.Red)) else None
    ).flatten
    if parts.isEmpty then "" else parts.mkString("")
  }

  def gitLines(added: Int, deleted: Int): String =
    Colors.colored(s"+$added", Colors.Green) + "/" + Colors.colored(s"-$deleted", Colors.Red)
}

object BarFormatter extends StatusLineFormatter {
  def format(e: StatusEvent): String = {
    val model = modelName(e.model.display_name)
    val pct = calculateContextPercent(e.context_window)
    val bar = contextBar(pct)
    val percent = contextPercent(pct)
    val costStr = cost(e.cost.total_cost_usd)
    val lines = linesChanged(e.cost.total_lines_added, e.cost.total_lines_removed)
    val dir = directory(e.workspace.project_dir)

    s"[$model] $bar $percent | $costStr | $lines | $dir $prompt"
  }
}

object CompactFormatter extends StatusLineFormatter {
  def format(e: StatusEvent): String = {
    val model = modelName(e.model.display_name)
    val pct = calculateContextPercent(e.context_window)
    val percent = contextPercent(pct)
    val costStr = cost(e.cost.total_cost_usd)
    val lines = linesChanged(e.cost.total_lines_added, e.cost.total_lines_removed)
    val dir = directory(e.workspace.project_dir)

    s"$model@$percent $costStr $lines $dir $prompt"
  }
}

object EmojiFormatter extends StatusLineFormatter {
  def format(e: StatusEvent): String = {
    val model = modelName(e.model.display_name)
    val pct = calculateContextPercent(e.context_window)
    val percent = contextPercent(pct)
    val costStr = cost(e.cost.total_cost_usd)
    val lines = linesChanged(e.cost.total_lines_added, e.cost.total_lines_removed)
    val dir = directory(e.workspace.project_dir)

    s"🤖 $model [$percent] $costStr $lines $dir $prompt"
  }
}

object GitBarFormatter extends StatusLineFormatter {
  def format(e: StatusEvent): String = {
    val model = modelName(e.model.display_name)
    val pct = calculateContextPercent(e.context_window)
    val bar = contextBar(pct)
    val percent = contextPercent(pct)
    val costStr = cost(e.cost.total_cost_usd)

    val gitPart = GitHelper.getGitInfo(e.workspace.project_dir) match {
      case Some(git) =>
        val branch = branchName(git.branch) + dirtyMarker(git.isDirty)
        val ab = aheadBehind(git.ahead, git.behind)
        val lines = gitLines(git.linesAdded, git.linesDeleted)
        s" | $branch$ab | $lines"
      case None => ""
    }

    s"[$model] $bar $percent | $costStr$gitPart $prompt"
  }
}

object GitCompactFormatter extends StatusLineFormatter {
  def format(e: StatusEvent): String = {
    val model = modelName(e.model.display_name)
    val pct = calculateContextPercent(e.context_window)
    val percent = contextPercent(pct)

    val gitPart = GitHelper.getGitInfo(e.workspace.project_dir) match {
      case Some(git) =>
        val branch = branchName(git.branch) + dirtyMarker(git.isDirty)
        s" $branch"
      case None => ""
    }

    s"$model@$percent$gitPart $prompt"
  }
}
