package statusline

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{BranchTrackingStatus, Repository}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.diff.{DiffFormatter, RawTextComparator}
import org.eclipse.jgit.util.io.DisabledOutputStream
import java.io.File
import scala.util.{Try, Using}
import scala.jdk.CollectionConverters.*

object GitHelper {
  case class GitInfo(
    branch: String,
    isDirty: Boolean,
    linesAdded: Int,
    linesDeleted: Int,
    ahead: Int,
    behind: Int
  )

  def getGitInfo(directory: String): Option[GitInfo] = {
    Try {
      val builder = new FileRepositoryBuilder()
        .findGitDir(new File(directory))
        .setMustExist(true)

      Using.resource(builder.build()) { repo =>
        Using.resource(new Git(repo)) { git =>
          val branch = repo.getBranch
          val status = git.status().call()
          val isDirty = !status.isClean

          // Get diff stats for uncommitted changes
          val (added, deleted) = getDiffStats(git)

          // Get ahead/behind counts
          val (ahead, behind) = getAheadBehind(repo, branch)

          GitInfo(branch, isDirty, added, deleted, ahead, behind)
        }
      }
    }.toOption
  }

  private def getDiffStats(git: Git): (Int, Int) = {
    Try {
      Using.resource(new DiffFormatter(DisabledOutputStream.INSTANCE)) { formatter =>
        formatter.setRepository(git.getRepository)
        formatter.setDiffComparator(RawTextComparator.DEFAULT)
        formatter.setDetectRenames(true)

        // Get diffs between HEAD and working tree
        val diffs = git.diff().call().asScala
        var added = 0
        var deleted = 0

        diffs.foreach { diff =>
          val fileHeader = formatter.toFileHeader(diff)
          fileHeader.toEditList.asScala.foreach { edit =>
            added += edit.getEndB - edit.getBeginB
            deleted += edit.getEndA - edit.getBeginA
          }
        }

        (added, deleted)
      }
    }.getOrElse((0, 0))
  }

  private def getAheadBehind(repo: Repository, branch: String): (Int, Int) = {
    Option(BranchTrackingStatus.of(repo, branch))
      .map(s => (s.getAheadCount, s.getBehindCount))
      .getOrElse((0, 0))
  }
}
