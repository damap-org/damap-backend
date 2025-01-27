# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] -

### Added

- Added "Principle Investigator" and "Project Coordinator" as contributor roles [#285](https://github.com/tuwien-csd/damap-backend/pull/285).
- Added option to configure html title [#320](https://github.com/tuwien-csd/damap-backend/pull/320).
- Added backend support for configurable banners [#311](https://github.com/tuwien-csd/damap-backend/pull/311).
- Added develop quickstart guide documentation [#329](https://github.com/tuwien-csd/damap-backend/pull/329).

### Changed

- DMPs, storages, datasets, contributors and costs are now sorted by creation date when retrieved [#298](https://github.com/tuwien-csd/damap-backend/pull/298).

### Fixed

- Fixed a bug where manually added contributors would prevent switching projects [#312](https://github.com/tuwien-csd/damap-backend/pull/312).
- Fixed a problem where contributors ORCID and ROR would not get exported [#314](https://github.com/tuwien-csd/damap-backend/pull/314).
- Fixed a problem where "null" was exported when a contributor had no ORCID [#315](https://github.com/tuwien-csd/damap-backend/pull/315).
- Fixed a bug in the mock services, where an error was thrown when nothing could be found by the services [#317](https://github.com/tuwien-csd/damap-backend/pull/317).
- Fixed a bug that prevented exporting due to incomplete identifiers [#313](https://github.com/tuwien-csd/damap-backend/pull/313).
- Fixed a bug in the FWF template, where dataset descriptions would not be exported [#323](https://github.com/tuwien-csd/damap-backend/pull/323).
- Fixed a bug where internal storage title changes by an admin would not affect older DMP's [#324](https://github.com/tuwien-csd/damap-backend/pull/324).

### Template

- Changed cost coverage text into a placeholders [#307](https://github.com/tuwien-csd/damap-backend/pull/307).
- Removed detailed guidance table from FWF template [#316](https://github.com/tuwien-csd/damap-backend/pull/316).
- Added Internal project ID and renamed grant information to project number in FWF section I. [#327](https://github.com/tuwien-csd/damap-backend/pull/327).
- Added Project code of the funding body and Internal project ID to HE on page 1 [#327](https://github.com/tuwien-csd/damap-backend/pull/327).
- Added Funder field to SE in the project details section [#327](https://github.com/tuwien-csd/damap-backend/pull/327).

### Resource Files

- Added "costCoverage" key [#307](https://github.com/tuwien-csd/damap-backend/pull/307).


## [4.3.1] - 2024-12-13

### Added

- Added possibility to disable the preview feature [#296](https://github.com/tuwien-csd/damap-backend/pull/296).
- Expanded upon contributing documentation [#301](https://github.com/tuwien-csd/damap-backend/pull/301).

### Template

- Fixed wording in Horizon Europe contributors table at the contact row [#300](https://github.com/tuwien-csd/damap-backend/pull/300).


## [4.3.0] - 2024-11-18

### Added

- Created README for release workflow [#270](https://github.com/tuwien-csd/damap-backend/pull/270).
- Added Admin page with functionality to add, create and deactivate storages.

### Changed

- Updated license Url's to be compatible with InvenioRDM v12 [#271](https://github.com/tuwien-csd/damap-backend/pull/271).

### Fixed

- Fixed a bug where identifiers like ORCID where not exported correctly to the contributor section [#268](https://github.com/tuwien-csd/damap-backend/pull/268).

### Template

- Reasons for closing/restricting datasets are now exported into the templates [-].

### Resource Files

- Added keys "closeddatasetreasons.intro" and "restricteddatasetreasons.intro".

...