package statusline

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

type NonEmptyString = String :| Not[Empty]
type NonNegInt = Int :| GreaterEqual[0]
type NonNegLong = Long :| GreaterEqual[0L]
type NonNegDouble = Double :| GreaterEqual[0.0]
type Percentage = Double :| Interval.Closed[0.0, 100.0]
type SemVer = String :| Match["^\\d+\\.\\d+\\.\\d+$"]
