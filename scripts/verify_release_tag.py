#!/usr/bin/env python3
"""Recheck the remote tag immediately before publication, after the build."""
import json
import subprocess
import sys


def verify_tag(metadata, run=subprocess.run):
    tag = metadata['tag']
    result = run(['git','ls-remote','--exit-code','--tags','origin',f'refs/tags/{tag}'], capture_output=True, text=True)
    if result.returncode == 2:
        return
    if result.returncode != 0:
        raise ValueError('Cannot verify current remote tag; refusing publication')
    run(['git','fetch','--no-tags','origin',f'refs/tags/{tag}:refs/tags/{tag}'], check=True, capture_output=True, text=True)
    actual = run(['git','rev-list','-n','1',tag], check=True, capture_output=True, text=True).stdout.strip()
    if actual != metadata['commit']:
        raise ValueError('Remote tag changed or points to another commit; refusing publication')

if __name__ == '__main__':
    with open(sys.argv[1]) as stream:
        verify_tag(json.load(stream))
    print('Remote tag is absent or still matches the exact artifact commit')
