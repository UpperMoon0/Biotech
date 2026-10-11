"""Execute the real workflow gate against isolated Git stubs."""
import os
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
import textwrap
import unittest


ROOT = Path(__file__).resolve().parents[1]
SHA = "a" * 40
TAG = "v2.7-alpha.1"


class ReleaseGateTest(unittest.TestCase):
    def setUp(self):
        source = (ROOT / ".github/workflows/release.yml").read_text()
        match = re.search(
            r"(?m)^      - name: Check whether this version is unpublished\n"
            r"[\s\S]*?^        run: \|\n"
            r"(?P<body>(?:^          [^\n]*(?:\n|$)|^\n)*)",
            source,
        )
        self.assertIsNotNone(match, "Cannot locate the actual release-gate shell block")
        self.gate = textwrap.dedent(match.group("body"))
        self.assertIn("should_release", self.gate)
        self.folder = tempfile.TemporaryDirectory(prefix="biotech-release-gate-")
        self.addCleanup(self.folder.cleanup)
        self.root = Path(self.folder.name)
        (self.root / "scripts").mkdir()
        (self.root / "bin").mkdir()
        (self.root / "gradle.properties").write_text("mod_version=2.7-alpha.1\n")
        (self.root / "CHANGELOG-2.7-alpha.1.md").write_text("Alpha test fixture\n")
        shutil.copy(ROOT / "scripts/release_policy.py", self.root / "scripts/release_policy.py")
        git = self.root / "bin/git"
        git.write_text(
            "#!/bin/bash\n"
            "case \"$1\" in\n"
            "  ls-remote) exit \"$FAKE_TAG_STATUS\";;\n"
            "  fetch) exit \"${FAKE_FETCH_STATUS:-0}\";;\n"
            "  rev-list) echo \"${FAKE_TAG_SHA:-$GITHUB_SHA}\";;\n"
            "  rev-parse) echo \"$GITHUB_SHA\";;\n"
            "  *) echo \"Unexpected git command: $*\" >&2; exit 99;;\n"
            "esac\n"
        )
        git.chmod(0o755)

    def run_gate(self, *, event="push", ref="refs/heads/main", release="false",
                 channel="release", tag_status=2, **overrides):
        output = self.root / "github-output"
        output.write_text("")
        env = dict(
            os.environ,
            PATH=str(self.root / "bin") + os.pathsep + os.environ["PATH"],
            EVENT_NAME=event,
            REF=ref,
            MANUAL_RELEASE=release,
            REQUESTED_CHANNEL=channel,
            GITHUB_SHA=SHA,
            GITHUB_OUTPUT=str(output),
            FAKE_TAG_STATUS=str(tag_status),
            PYTHONDONTWRITEBYTECODE="1",
        )
        env.update(overrides)
        result = subprocess.run(
            ["bash", "-c", self.gate], cwd=self.root, env=env,
            text=True, capture_output=True, timeout=15,
        )
        values = dict(line.split("=", 1) for line in output.read_text().splitlines())
        return result, values

    def assert_decision(self, expected, **kwargs):
        result, values = self.run_gate(**kwargs)
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        self.assertEqual(str(expected).lower(), values.get("should_release"))
        return values

    def assert_rejected(self, **kwargs):
        result, values = self.run_gate(**kwargs)
        self.assertNotEqual(0, result.returncode, result.stdout + result.stderr)
        self.assertNotEqual("true", values.get("should_release"))

    def test_new_main_version_publishes_its_committed_channel(self):
        values = self.assert_decision(True)
        self.assertEqual("alpha", values["channel"])
        self.assertEqual("true", values["github_prerelease"])
        self.assertEqual("false", values["github_make_latest"])

    def test_main_with_existing_tag_does_not_publish_again(self):
        self.assert_decision(False, tag_status=0)

    def test_matching_tag_push_publishes(self):
        self.assert_decision(True, ref="refs/tags/" + TAG, tag_status=0)

    def test_manual_main_defaults_to_build_only(self):
        self.assert_decision(False, event="workflow_dispatch")

    def test_manual_tag_defaults_to_build_only(self):
        self.assert_decision(False, event="workflow_dispatch", ref="refs/tags/" + TAG, tag_status=0)

    def test_explicit_manual_main_alpha_publishes(self):
        self.assert_decision(True, event="workflow_dispatch", release="true", channel="alpha")

    def test_explicit_manual_non_main_does_not_publish(self):
        self.assert_decision(False, event="workflow_dispatch", ref="refs/heads/feature",
                             release="true", channel="alpha")

    def test_default_manual_channel_cannot_promote_alpha(self):
        self.assert_rejected(event="workflow_dispatch", release="true")

    def test_tag_version_mismatch_is_rejected(self):
        self.assert_rejected(ref="refs/tags/v2.7", tag_status=0)

    def test_tag_lookup_network_failure_is_not_treated_as_absence(self):
        self.assert_rejected(tag_status=128)

    def test_existing_tag_on_another_commit_is_rejected(self):
        self.assert_rejected(ref="refs/tags/" + TAG, tag_status=0, FAKE_TAG_SHA="b" * 40)

    def test_existing_tag_fetch_failure_is_rejected(self):
        self.assert_rejected(ref="refs/tags/" + TAG, tag_status=0, FAKE_FETCH_STATUS="128")

    def test_existing_release_preflight_precedes_both_publishers(self):
        source = (ROOT / ".github/workflows/release.yml").read_text()
        github = source.split("\n  publish-github-release:\n", 1)[1]
        curseforge = source.split("\n  publish-curseforge:\n", 1)[1].split("\n  publish-github-release:\n", 1)[0]
        self.assertIn("contents: write", github)
        self.assertLess(github.index("scripts/verify_existing_release.py"),
                        github.index("uses: softprops/action-gh-release@"))
        self.assertRegex(curseforge, r"(?m)^    needs: \[[^\n]*publish-github-release[^\n]*\]$")


if __name__ == "__main__":
    unittest.main()
