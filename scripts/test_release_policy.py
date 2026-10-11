import json
from pathlib import Path
import tempfile
import subprocess
import sys
import zipfile
import unittest
from release_policy import policy, read_version

ROOT = Path(__file__).resolve().parents[1]

class ReleasePolicyTest(unittest.TestCase):
    def test_unsuffixed_versions_default_to_stable(self):
        for version in ("2.7", "2.8", "3.0.1"):
            p = policy(version)
            self.assertEqual("release", p["channel"])
            self.assertFalse(p["github_prerelease"])
            self.assertTrue(p["github_make_latest"])
            self.assertEqual("release", p["curseforge_release_type"])

    def test_alpha_and_beta_are_prereleases_on_both_destinations(self):
        for channel in ("alpha", "beta"):
            for version in (f"2.7-{channel}.1", f"3.0.1-{channel}.12"):
                p = policy(version, channel)
                self.assertEqual(channel, p["channel"])
                self.assertTrue(p["github_prerelease"])
                self.assertFalse(p["github_make_latest"])
                self.assertEqual(channel, p["curseforge_release_type"])
                self.assertEqual("v" + version, p["tag"])

    def test_default_dispatch_cannot_promote_alpha_or_beta(self):
        for version in ("2.7-alpha.1", "2.7-beta.1"):
            with self.assertRaises(ValueError): policy(version, "release")
        with self.assertRaises(ValueError): policy("2.7", "alpha")
        with self.assertRaises(ValueError): policy("2.7-alpha.1", "beta")

    def test_invalid_channels_and_versions_fail_closed(self):
        for channel in ("stable", "ALPHA", "", "../release"):
            with self.assertRaises(ValueError): policy("2.7", channel)
        for version in ("", "2.7-rc.1", "2.7-alpha", "2.7-alpha.0", "2.7-alpha.01", "v2.7", "2.7\nrelease=true", "../../2.7"):
            with self.assertRaises(ValueError): policy(version)

    def test_current_version_has_changelog(self):
        p = policy(read_version(ROOT / "gradle.properties"))
        self.assertTrue((ROOT / p["changelog"]).is_file())

    def test_duplicate_version_property_rejected(self):
        with tempfile.TemporaryDirectory() as folder:
            p = Path(folder) / "gradle.properties"
            p.write_text("mod_version=2.7\nmod_version=2.8\n")
            with self.assertRaises(ValueError): read_version(p)

    def test_artifact_manifest_must_match_channel(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            (root / 'gradle.properties').write_text('mod_version=2.7-alpha.1\n')
            (root / 'CHANGELOG-2.7-alpha.1.md').write_text('Alpha changelog')
            jar = root / 'biotech-2.7-alpha.1.jar'
            command = [sys.executable, str(ROOT / 'scripts/release_policy.py'), '--properties', str(root / 'gradle.properties'), '--commit', 'a'*40, '--output', str(root / 'provenance.json'), '--jar', str(jar)]
            for channel in ('release', 'alpha'):
                with zipfile.ZipFile(jar, 'w') as archive:
                    archive.writestr('META-INF/MANIFEST.MF', 'Manifest-Version: 1.0\nBiotech-Release-Version: 2.7-alpha.1\nBiotech-Release-Channel: '+channel+'\n')
                result = subprocess.run(command, capture_output=True, text=True)
                self.assertEqual(channel == 'alpha', result.returncode == 0, result.stderr)
            saved = json.loads((root / 'provenance.json').read_text())
            self.assertEqual('alpha', saved['channel'])
            self.assertEqual(64, len(saved['sha256']))

    def test_workflow_wires_channel_to_both_publishers(self):
        source = (ROOT / ".github/workflows/release.yml").read_text()
        for expected in ("default: release", "options: [release, alpha, beta]", "releaseType:$releaseType", "--arg releaseType", "prerelease: ${{ needs.release-gate.outputs.github_prerelease }}", "make_latest: ${{ needs.release-gate.outputs.github_make_latest }}", "target_commitish: ${{ github.sha }}", "group: biotech-release", "--requested-channel", "overwrite_files: false"):
            self.assertIn(expected, source)
        self.assertNotIn('releaseType:"release"', source)
        self.assertIn("Biotech-Release-Channel", (ROOT / "build.gradle").read_text())

if __name__ == "__main__": unittest.main()
