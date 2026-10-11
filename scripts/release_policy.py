#!/usr/bin/env python3
"""One immutable version/channel policy for automatic, tagged and manual releases."""
import argparse
import hashlib
import json
import re
import zipfile
from pathlib import Path

VERSION = re.compile(r"[0-9]+\.[0-9]+(?:\.[0-9]+)?(?:-(alpha|beta)\.([1-9][0-9]*))?\Z")
CHANNELS = ("alpha", "beta", "release")

def policy(version, requested_channel=None):
    match = VERSION.fullmatch(version)
    if not match:
        raise ValueError("Use X.Y[.Z], X.Y[.Z]-alpha.N or X.Y[.Z]-beta.N (N >= 1)")
    channel = match.group(1) or "release"
    if requested_channel is not None:
        if requested_channel not in CHANNELS:
            raise ValueError("Release channel must be alpha, beta or release")
        if requested_channel != channel:
            raise ValueError(f"Requested {requested_channel} conflicts with immutable version {version} ({channel})")
    return {"version": version, "tag": "v" + version, "channel": channel,
            "github_prerelease": channel != "release", "github_make_latest": channel == "release",
            "curseforge_release_type": channel, "changelog": f"CHANGELOG-{version}.md"}

def read_version(path):
    values = re.findall(r"^mod_version\s*=\s*(\S+)\s*$", Path(path).read_text(), re.M)
    if len(values) != 1:
        raise ValueError("Expected exactly one mod_version")
    return values[0]

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--properties", default="gradle.properties")
    parser.add_argument("--requested-channel", choices=CHANNELS)
    parser.add_argument("--commit", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument("--github-output")
    parser.add_argument("--jar")
    parser.add_argument("--loader")
    parser.add_argument("--minecraft")
    args = parser.parse_args()
    data = policy(read_version(args.properties), args.requested_channel)
    if not re.fullmatch(r"[0-9a-f]{40}", args.commit):
        raise ValueError("Expected full source commit SHA")
    data["commit"] = args.commit
    changelog = Path(args.properties).resolve().parent / data["changelog"]
    if not changelog.is_file() or not changelog.read_text().strip():
        raise ValueError(f"Missing release changelog: {changelog}")
    if args.jar:
        jar = Path(args.jar)
        if data["version"] not in jar.name:
            raise ValueError("JAR filename must contain the immutable release version")
        with zipfile.ZipFile(jar) as archive:
            manifest = archive.read('META-INF/MANIFEST.MF').decode().replace('\r\n', '\n')
        for key, expected in (('Biotech-Release-Version', data['version']), ('Biotech-Release-Channel', data['channel'])):
            if f'{key}: {expected}\n' not in manifest:
                raise ValueError(f'JAR manifest {key} disagrees with publication metadata')
        data.update(jar=jar.name, sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),
                    loader=args.loader, minecraft=args.minecraft)
    Path(args.output).write_text(json.dumps(data, indent=2) + "\n")
    if args.github_output:
        with open(args.github_output, "a") as stream:
            for key in ("version", "tag", "channel", "github_prerelease", "github_make_latest", "changelog"):
                value = str(data[key]).lower() if isinstance(data[key], bool) else data[key]
                stream.write(f"{key}={value}\n")
    print(json.dumps(data, sort_keys=True))

if __name__ == "__main__":
    main()
