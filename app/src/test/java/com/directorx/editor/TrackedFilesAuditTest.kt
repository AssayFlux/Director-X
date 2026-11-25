package com.directorx.editor

import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Locks the tracked-file set so cross-project artefacts can't sneak into git.
 *
 * Context: the working tree on contributor machines may contain leftover docs
 * from sibling projects (most notably HostShield's DNS/blocklist research). They
 * are correctly gitignored, but a contributor running `git add -A` without
 * looking could still slip them in. This test runs `git ls-files` and fails if
 * any tracked path matches the forbidden patterns.
 *
 * The test is a no-op when `git` is not on PATH (e.g. unusual CI environments)
 * so it never blocks builds for the wrong reason.
 */
class TrackedFilesAuditTest {

    @Test
    fun trackedFilesDoNotIncludeCrossProjectPollution() {
        val repoRoot = locateRepoRoot()
            ?: error(
                "Could not locate the repository root. The audit walks up from the " +
                    "test working directory looking for the .git folder; adjust this " +
                    "resolver if the repo layout moves."
            )
        val tracked = gitLsFiles(repoRoot) ?: run {
            assumeTrue("git command unavailable; skipping tracked-files audit", false)
            return
        }

        val offenders = tracked.filter { path ->
            FORBIDDEN_PREFIXES.any { path.startsWith(it) }
        }

        assertTrue(
            "Tracked files include cross-project pollution. The following paths " +
                "must not be committed to Director X — they belong to a sibling project " +
                "(HostShield) and are blocked by .gitignore for that reason. Either " +
                "delete the file from the working tree or run `git rm --cached <path>`. " +
                "Offenders: $offenders",
            offenders.isEmpty()
        )
    }

    /**
     * Sanity check: the public local-build/release contract files must remain
     * tracked. Planning/research markdown remains local-only; the README,
     * release-contract documents, approved public engineering audit reports, and
     * explicitly approved Upgrade 2/3 delivery records are the tracked Markdown groups.
     */
    @Test
    fun requiredPublicFilesRemainTracked() {
        val repoRoot = locateRepoRoot() ?: return
        val tracked = gitLsFiles(repoRoot)?.toSet() ?: run {
            assumeTrue("git command unavailable; skipping tracked-docs audit", false)
            return
        }

        val missing = REQUIRED_TRACKED.filterNot { it in tracked }
        assertTrue(
            "Required public files must stay tracked. Missing: $missing",
            missing.isEmpty()
        )
    }

    @Test
    fun markdownDocsStayLocalExceptReadme() {
        val repoRoot = locateRepoRoot() ?: return
        val tracked = gitLsFiles(repoRoot) ?: run {
            assumeTrue("git command unavailable; skipping tracked-markdown audit", false)
            return
        }

        // The privacy policy and Data safety worksheet are part of the public
        // release contract: the Play listing's privacy URL points at the tracked
        // GitHub blob, so they must not be swept up by the local-only *.md rule.
        val releaseContractMarkdown = setOf(
            "README.md",
            "ACKNOWLEDGMENTS.md",
            "docs/BUILD.md",
            "docs/models.md",
            "docs/ENGINEERING_STATUS.md",
            "docs/privacy-policy.md",
            "docs/play-data-safety.md",
            "BRANDING_GUIDE.md",
            "docs/ARCHITECTURE_BOUNDARIES.md",
            "docs/ARCHITECTURE.md",
            "docs/PERFORMANCE_VALIDATION.md",
            "docs/RELEASE_READINESS.md",
            "docs/RELEASE_DEVICE_ACCEPTANCE_TEMPLATE.md",
            "docs/UPGRADE_ROADMAP.md",
            "docs/DIRECTOR_X_PRODUCT_UPGRADE_PLAN.md",
            "docs/UPGRADE_2_3_IMPLEMENTATION_PROGRESS.md",
            "docs/UPGRADE_2_3_LANGUAGE_CAPABILITY_MATRIX.md",
            "docs/UPGRADE_2_3_PR_SCOPE_AND_BLOCKERS.md",
            "docs/monetization-tiers.md",
            "docs/security/FFmpeg-8.1.3-candidate-assessment-2026-11.md",
            "docs/security/FFmpeg-Native-Review-2026-11.md",
            // Public engineering audit reports are approved repository artifacts,
            // distinct from private working notes and unfinished planning drafts.
            "docs/P1_MEDIA_EXPORT_RELIABILITY_AUDIT_2026-11-24.md",
            "docs/P2_PRODUCT_QUALITY_AUDIT_2026-11-24.md",
            "docs/ANDROID_TEST_FAILURE_AUDIT_2026-11-24.md",
        )
        val trackedPrivateMarkdown = tracked.filter { path ->
            path.endsWith(".md") && path !in releaseContractMarkdown
        }

        assertTrue(
            "Only public release-contract markdown may be tracked; private planning docs " +
                "must stay local-only. Tracked private markdown: $trackedPrivateMarkdown",
            trackedPrivateMarkdown.isEmpty()
        )
    }

    @Test
    fun githubAutomationFilesStayUntracked() {
        val repoRoot = locateRepoRoot() ?: return
        val tracked = gitLsFiles(repoRoot) ?: run {
            assumeTrue("git command unavailable; skipping GitHub automation audit", false)
            return
        }

        val offenders = tracked.filter { path ->
            path !in APPROVED_AUTOMATION_FILES &&
                (FORBIDDEN_AUTOMATION_PREFIXES.any { path.startsWith(it) } ||
                    path in FORBIDDEN_AUTOMATION_FILES)
        }

        assertTrue(
            "Director X only tracks approved local CI automation. Unapproved GitHub workflow, " +
                "Dependabot, and Renovate files must not be tracked. Offenders: $offenders",
            offenders.isEmpty()
        )
    }

    @Test
    fun applicationSourcesDoNotUseGlobalScope() {
        val repoRoot = locateRepoRoot() ?: return
        val sourceRoot = File(repoRoot, "app/src/main/java")
        val offenders = sourceRoot
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { file -> file.readText().contains("GlobalScope") }
            .map { file -> file.relativeTo(repoRoot).invariantSeparatorsPath }
            .toList()

        assertTrue(
            "Application source must use lifecycle-owned CoroutineScope instances " +
                "instead of GlobalScope. Offenders: $offenders",
            offenders.isEmpty()
        )
    }

    private fun gitLsFiles(repoRoot: File): List<String>? {
        return try {
            val process = ProcessBuilder("git", "ls-files")
                .directory(repoRoot)
                .redirectErrorStream(false)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readLines() }
            val finished = process.waitFor(15, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                null
            } else if (process.exitValue() != 0) {
                null
            } else {
                output.filter { it.isNotBlank() }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun locateRepoRoot(): File? {
        val userDir = System.getProperty("user.dir") ?: return null
        var dir: File? = File(userDir).absoluteFile
        repeat(6) {
            val current = dir ?: return null
            if (File(current, ".git").exists()) return current
            dir = current.parentFile
        }
        return null
    }

    companion object {
        private val FORBIDDEN_PREFIXES = listOf(
            "HostShield",
            "research/",
        )

        private val FORBIDDEN_AUTOMATION_PREFIXES = listOf(
            ".github/workflows/",
        )

        private val FORBIDDEN_AUTOMATION_FILES = setOf(
            ".github/dependabot.yml",
            "renovate.json",
        )

        private val APPROVED_AUTOMATION_FILES = setOf(
            ".github/workflows/android-full-ci.yml",
            ".github/workflows/code-quality.yml",
        )

        private val REQUIRED_TRACKED = listOf(
            "README.md",
            "LICENSE",
            "docs/privacy-policy.md",
            "docs/play-data-safety.md",
            "app/build.gradle.kts",
            "app/src/main/AndroidManifest.xml",
            "gradle/libs.versions.toml",
        )
    }
}
