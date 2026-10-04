#!/usr/bin/env python3
"""Reject absent/incomplete/failed GameTest runs, even if a launcher exits zero."""
from pathlib import Path
import re
import sys


def verify(target: str, text: str, root: Path) -> int:
    failures = re.search(r'\b[1-9]\d* required tests failed|Uncaught exception in server thread|Game test server crashed|Loading errors encountered', text)
    if failures:
        raise ValueError('GameTest failure or abnormal shutdown: ' + failures.group(0))
    successful = re.findall(r'All (\d+) required tests passed', text)
    if not successful:
        raise ValueError('Missing successful required-GameTest completion summary')
    source_root = root / target / 'src/main/java'
    if target == 'neoforge-26.1.2':
        expected = sum(len(re.findall(r'\bregister\("[a-z0-9_]+"', p.read_text()))
                       for p in (source_root / 'com/nstut/biotech/gametest').glob('*GameTests.java'))
    else:
        expected = sum(p.read_text().count('@GameTest(') for p in source_root.rglob('*.java'))
    count = int(successful[-1])
    if expected == 0 or count < expected:
        raise ValueError(f'Incomplete GameTest coverage: {count} passed, at least {expected} declared')
    return count


if __name__ == '__main__':
    target, log = sys.argv[1:]
    try:
        count = verify(target, Path(log).read_text(errors='replace'), Path(__file__).resolve().parents[1])
    except (OSError, ValueError) as error:
        raise SystemExit(str(error))
    print(f'Verified {count} required GameTests and normal shutdown for {target}.')
