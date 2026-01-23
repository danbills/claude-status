package statusline

import io.circe.Codec
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.iltotore.iron.circe.given

final case class StatusEvent(
  session_id: NonEmptyString,
  transcript_path: NonEmptyString,
  cwd: NonEmptyString,
  model: Model,
  workspace: Workspace,
  version: SemVer,
  output_style: OutputStyle,
  cost: Cost,
  context_window: ContextWindow,
  exceeds_200k_tokens: Boolean
) derives Codec

final case class Model(
  id: NonEmptyString,
  display_name: NonEmptyString
) derives Codec

final case class Workspace(
  current_dir: NonEmptyString,
  project_dir: NonEmptyString
) derives Codec

final case class OutputStyle(
  name: NonEmptyString
) derives Codec

final case class Cost(
  total_cost_usd: NonNegDouble,
  total_duration_ms: NonNegLong,
  total_api_duration_ms: NonNegLong,
  total_lines_added: NonNegInt,
  total_lines_removed: NonNegInt
) derives Codec

final case class ContextWindow(
  total_input_tokens: NonNegInt,
  total_output_tokens: NonNegInt,
  context_window_size: NonNegInt,
  current_usage: Option[CurrentUsage]
) derives Codec

final case class CurrentUsage(
  input_tokens: NonNegInt,
  output_tokens: NonNegInt,
  cache_creation_input_tokens: NonNegInt,
  cache_read_input_tokens: NonNegInt
) derives Codec
