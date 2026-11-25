from __future__ import annotations

import unittest

from verify_published_release_sidecars import missing_sidecars


class PublishedReleaseSidecarTest(unittest.TestCase):
    def test_each_apk_requires_checksum_and_signing_fingerprint(self) -> None:
        assets = ["directorx-universal.apk", "directorx-arm64.apk"]
        missing = missing_sidecars(assets)
        self.assertEqual(
            missing,
            [
                "directorx-universal.apk.sha256",
                "directorx-universal.apk.signing-cert-sha256",
                "directorx-arm64.apk.sha256",
                "directorx-arm64.apk.signing-cert-sha256",
            ],
        )

    def test_complete_sidecars_are_accepted(self) -> None:
        apk = "directorx-arm64.apk"
        assets = [
            apk,
            f"{apk}.sha256",
            f"{apk}.signing-cert-sha256",
            "release-notes.txt",
        ]
        self.assertEqual(missing_sidecars(assets), [])

    def test_releases_without_apks_have_no_sidecar_requirement(self) -> None:
        self.assertEqual(
            missing_sidecars(["release-notes.txt", "checksums.txt"]),
            [],
        )


if __name__ == "__main__":
    unittest.main()
