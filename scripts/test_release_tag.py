import subprocess
import unittest
from verify_release_tag import verify_tag

class ReleaseTagTest(unittest.TestCase):
    def check(self, status, actual='a'*40, fetch_status=0):
        def run(command, **kwargs):
            if command[1]=='ls-remote':return subprocess.CompletedProcess(command,status,'','')
            if command[1]=='fetch' and fetch_status:raise subprocess.CalledProcessError(fetch_status,command)
            return subprocess.CompletedProcess(command,0,actual+'\n','')
        verify_tag(dict(tag='v2.7-alpha.1',commit='a'*40),run)
    def test_absent_tag_is_safe(self):self.check(2)
    def test_existing_matching_tag_is_safe(self):self.check(0)
    def test_racing_tag_on_another_commit_rejected(self):
        with self.assertRaises(ValueError):self.check(0,'b'*40)
    def test_lookup_failure_rejected(self):
        with self.assertRaises(ValueError):self.check(128)
    def test_fetch_failure_rejected(self):
        with self.assertRaises(subprocess.CalledProcessError):self.check(0,fetch_status=1)
