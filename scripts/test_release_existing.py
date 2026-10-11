import unittest
from release_policy import policy
from verify_existing_release import verify_existing

class ExistingReleaseTest(unittest.TestCase):
    def test_same_channel_retry_allowed(self):
        for version in ('2.7','2.7-alpha.1','2.7-beta.1'):
            p=policy(version)
            verify_existing(p,dict(tag_name=p['tag'],prerelease=p['github_prerelease'],draft=False))
    def test_promotion_demotion_tag_change_or_draft_rejected(self):
        for version in ('2.7','2.7-alpha.1','2.7-beta.1'):
            p=policy(version)
            for release in (dict(tag_name=p['tag'],prerelease=not p['github_prerelease'],draft=False),dict(tag_name='v99.0',prerelease=p['github_prerelease'],draft=False),dict(tag_name=p['tag'],prerelease=p['github_prerelease'],draft=True)):
                with self.assertRaises(ValueError):verify_existing(p,release)
if __name__=='__main__':unittest.main()

class ExistingLookupTest(unittest.TestCase):
    def test_tag_404_still_checks_drafts_and_paginates(self):
        import io, json, urllib.error
        from verify_existing_release import find_existing
        calls=[]
        draft=dict(tag_name='v2.7-alpha.1',prerelease=True,draft=True)
        def opener(request, timeout):
            calls.append(request.full_url)
            if '/tags/' in request.full_url:
                raise urllib.error.HTTPError(request.full_url,404,'Not Found',None,None)
            data=[dict(tag_name='other') for _ in range(100)] if request.full_url.endswith('page=1') else [draft]
            return io.StringIO(json.dumps(data))
        result=find_existing('test/repo','v2.7-alpha.1','test-token',opener)
        self.assertEqual(draft,result)
        self.assertEqual(3,len(calls))
        with self.assertRaises(ValueError):verify_existing(policy('2.7-alpha.1'),result)

    def test_unexpected_api_failure_is_not_treated_as_absence(self):
        import urllib.error
        from verify_existing_release import find_existing
        def opener(request, timeout):
            raise urllib.error.HTTPError(request.full_url,403,'Forbidden',None,None)
        with self.assertRaises(urllib.error.HTTPError):find_existing('test/repo','v2.7','test-token',opener)
