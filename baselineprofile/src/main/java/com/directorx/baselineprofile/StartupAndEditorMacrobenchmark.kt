package com.directorx.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMacrobenchmarkApi
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.MemoryUsageMetric
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@OptIn(ExperimentalMacrobenchmarkApi::class, ExperimentalMetricApi::class)
class StartupAndEditorMacrobenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun aColdStartupDefaultCompilation() {
        benchmarkRule.measureRepeated(
            packageName = DIRECTOR_X_PACKAGE,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = CompilationMode.Ignore(),
            startupMode = StartupMode.COLD,
            iterations = 5
        ) {
            openProjectGallery()
        }
    }

    @Test
    fun bColdStartupWithBaselineProfile() {
        benchmarkRule.measureRepeated(
            packageName = DIRECTOR_X_PACKAGE,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = CompilationMode.Partial(
                baselineProfileMode = BaselineProfileMode.Require
            ),
            startupMode = StartupMode.COLD,
            iterations = 5
        ) {
            openProjectGallery()
        }
    }

    @Test
    fun cWarmStartupWithBaselineProfile() {
        benchmarkRule.measureRepeated(
            packageName = DIRECTOR_X_PACKAGE,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = CompilationMode.Partial(
                baselineProfileMode = BaselineProfileMode.Require
            ),
            startupMode = StartupMode.WARM,
            iterations = 5
        ) {
            openProjectGallery()
        }
    }

    @Test
    fun dBlankEditorEntryAndTimelineScrubFrames() {
        benchmarkRule.measureRepeated(
            packageName = DIRECTOR_X_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(
                baselineProfileMode = BaselineProfileMode.Require
            ),
            startupMode = StartupMode.COLD,
            iterations = 3
        ) {
            openProjectGallery()
            openBlankEditor()
            scrubTimelineViewport()
        }
    }
    @Test
    fun eExportSheetOpenFrameTiming() {
        benchmarkRule.measureRepeated(
            packageName = DIRECTOR_X_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(
                baselineProfileMode = BaselineProfileMode.Require
            ),
            startupMode = StartupMode.COLD,
            iterations = 3
        ) {
            openProjectGallery()
            openBlankEditor()
            openExportSheet()
        }
    }

    @Test
    fun fSettingsAndPrivacyNavigationFrameTiming() {
        benchmarkRule.measureRepeated(
            packageName = DIRECTOR_X_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(
                baselineProfileMode = BaselineProfileMode.Require
            ),
            startupMode = StartupMode.COLD,
            iterations = 3
        ) {
            openProjectGallery()
            openSettingsAndPrivacyDashboard()
        }
    }


    @Test
    fun gRepeatedEditorOpenCloseMemoryAndFrameTiming() {
        benchmarkRule.measureRepeated(
            packageName = DIRECTOR_X_PACKAGE,
            metrics = listOf(
                FrameTimingMetric(),
                MemoryUsageMetric(mode = MemoryUsageMetric.Mode.Max),
            ),
            compilationMode = CompilationMode.Partial(
                baselineProfileMode = BaselineProfileMode.Require
            ),
            startupMode = StartupMode.COLD,
            iterations = 3
        ) {
            openProjectGallery()
            repeat(3) {
                openBlankEditor()
                scrubTimelineViewport()
                returnToProjectGallery()
            }
        }
    }

}
