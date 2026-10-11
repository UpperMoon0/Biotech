#!/usr/bin/env python3
"""Read-only guard: a retry must never change an existing release's channel."""
import json
import os
import sys
import urllib.error
import urllib.request

def verify_existing(metadata, release):
    if release.get('tag_name') != metadata['tag']:
        raise ValueError('Existing release tag differs')
    if release.get('prerelease') is not metadata['github_prerelease']:
        raise ValueError('Existing GitHub release channel differs; refusing automatic promotion/demotion')
    if release.get('draft'):
        raise ValueError('Existing release is a draft; refusing implicit publication')

def find_existing(repository, tag, token, opener=urllib.request.urlopen):
    def get(path):
        request = urllib.request.Request(f'https://api.github.com/repos/{repository}/{path}',
            headers={'Authorization': f'Bearer {token}', 'Accept':'application/vnd.github+json', 'X-GitHub-Api-Version':'2022-11-28'})
        with opener(request, timeout=30) as response:
            return json.load(response)
    try:
        return get(f'releases/tags/{tag}')
    except urllib.error.HTTPError as error:
        if error.code != 404:
            raise
    # The tag endpoint omits drafts; the publisher also scans the release list.
    # Mirror that lookup so an existing draft cannot be implicitly published.
    page = 1
    while True:
        releases = get(f'releases?per_page=100&page={page}')
        matches = [release for release in releases if release.get('tag_name') == tag]
        if matches:
            if len(matches) != 1:
                raise ValueError('Multiple same-tag releases; refusing ambiguous publication')
            return matches[0]
        if len(releases) < 100:
            return None
        page += 1


def main():
    with open(sys.argv[1]) as stream:
        metadata = json.load(stream)
    release = find_existing(os.environ['GITHUB_REPOSITORY'], metadata['tag'], os.environ['GH_TOKEN'])
    if release is None:
        print('No existing GitHub release or draft; channel selected by immutable version')
        return
    verify_existing(metadata, release)
    print('Existing release classification matches; rerun cannot change channel')

if __name__ == '__main__': main()
