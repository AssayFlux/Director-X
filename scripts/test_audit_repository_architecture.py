from __future__ import annotations

import tempfile
import unittest
from pathlib import Path

from audit_repository_architecture import audit_source, render_markdown


class RepositoryArchitectureAuditTest(unittest.TestCase):
    def test_reports_engine_to_ui_dependency_and_source_concentration(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            engine = root / "app/src/main/java/com/directorx/editor/engine"
            ui = root / "app/src/main/java/com/directorx/editor/ui"
            model = root / "app/src/main/java/com/directorx/editor/model"
            tests = root / "app/src/test/java"
            engine.mkdir(parents=True)
            ui.mkdir(parents=True)
            model.mkdir(parents=True)
            tests.mkdir(parents=True)
            (engine / "BadBoundary.kt").write_text(
                "package com.directorx.editor.engine\n"
                "import com.directorx.editor.ui.editor.EditorState\n",
                encoding="utf-8",
            )
            (ui / "LargeFile.kt").write_text("\n".join(["// line"] * 1001), encoding="utf-8")
            (model / "PlatformModel.kt").write_text(
                "package com.directorx.editor.model\nimport android.net.Uri\n",
                encoding="utf-8",
            )
            (tests / "ExampleTest.kt").write_text("class ExampleTest", encoding="utf-8")

            report = audit_source(root, "fixture-revision")

        self.assertEqual(report["revision"], "fixture-revision")
        self.assertEqual(report["summary"]["productionKotlinFiles"], 3)
        self.assertEqual(report["summary"]["testKotlinFiles"], 1)
        self.assertEqual(report["summary"]["engineUiImports"], 1)
        self.assertEqual(report["summary"]["modelPlatformImports"], 1)
        self.assertEqual(report["summary"]["largeFilesAtLeast1000Lines"], 1)
        finding_ids = {finding["id"] for finding in report["findings"]}
        self.assertIn("engine-ui-dependency", finding_ids)
        self.assertIn("large-source-concentration", finding_ids)
        self.assertIn("model-platform-coupling", finding_ids)
        self.assertIn("fixture-revision", render_markdown(report))

    def test_clean_source_tree_has_no_findings(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            source = root / "app/src/main/java/com/directorx/editor/engine"
            source.mkdir(parents=True)
            (source / "Policy.kt").write_text(
                "package com.directorx.editor.engine\ninternal fun safe() = true\n",
                encoding="utf-8",
            )
            report = audit_source(root, "clean-fixture")

        self.assertEqual(report["summary"]["findings"], 0)
        self.assertEqual(report["findings"], [])


if __name__ == "__main__":
    unittest.main()
