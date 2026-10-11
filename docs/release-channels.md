# Release channels

Biotech supports exactly `alpha`, `beta` and `release`. **Release is the default** for an unsuffixed version and for the manual workflow's channel choice. No channel setting carries over to a future version.

## Choose the version before merging

The committed `mod_version` is the immutable source of truth for automatic main-branch and tag publication:

| Version example | Channel | GitHub | CurseForge |
| --- | --- | --- | --- |
| `2.7-alpha.1` | alpha | prerelease; not latest stable | alpha |
| `2.7-beta.1` | beta | prerelease; not latest stable | beta |
| `2.7` or `2.8.1` | release (default) | normal release; latest | release |

Set the version in `gradle.properties` and add its nonempty `CHANGELOG-<version>.md`. Use a new numbered suffix for each alpha/beta build. Existing tags, files and classifications are not overwritten to promote a prerelease. A stable release uses a new unsuffixed version/tag.

This habitat release is explicitly `2.7-alpha.1`. It will be published as alpha when merged to main. It is not the stable `2.7` release.

## Automatic and manual publication

A main-branch push publishes an unpublished version. A matching version tag can also publish. The gate rejects invalid versions, tag/version mismatches, unexpected tag lookup failures, an existing tag pointing to another commit, or a mismatched tag. Before either destination publishes, the existing contents-write GitHub publication job performs a read-only check for an existing GitHub release or draft; a different prerelease classification/draft state fails closed. CurseForge waits for this job to succeed.

Manual workflow dispatch keeps `release=false` by default (build only). To publish or resume a partial publication, choose `release=true` and select a channel matching the committed version. The channel selector defaults to `release`, so an alpha/beta manual publication must explicitly choose its channel; a mismatch fails before publication. It cannot turn `2.7-alpha.1` into a stable release by selecting or leaving `release`.

The repository-wide release concurrency group serializes main/tag/manual runs. On a rerun, the version suffix still determines the same channel. GitHub assets use `overwrite_files: false`; existing tag targets are verified both before building and immediately before publication. CurseForge receives the validated `releaseType` and never an implicit stable fallback. A duplicate-upload response fails closed until its existing file identity and channel are verified; it is not claimed as successful publication.

## Evidence

Every JAR embeds `Biotech-Release-Version` and `Biotech-Release-Channel` in its manifest. Build artifact names include the version/channel, and each loader publishes a `release-<project>.json` provenance file containing its source commit, version, channel, target, JAR filename and SHA-256. Successful CurseForge uploads retain their file IDs in publication receipt artifacts.

`python3 -m unittest discover -s scripts -p 'test_release*.py' -v` covers release defaults, alpha/beta mappings, invalid input, dispatch mismatch, duplicate properties, existing-release promotion/demotion guards and workflow wiring. CI runs these tests on PRs before merge.

Provider references: [GitHub release action inputs](https://github.com/softprops/action-gh-release/blob/master/action.yml), [CurseForge upload API](https://support.curseforge.com/support/solutions/articles/9000197321-curseforge-api).
