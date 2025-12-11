# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] -

## [4.7.0] - 2025-12-11

### Added
 
- Support for Elsevier Pure project/person database integration [#392](https://github.com/damap-org/damap-backend/pull/392)

### Changed

- Switched to different image provider for keycloak [#429](https://github.com/damap-org/damap-backend/pull/429)
- Quarkus now reaugments for all db-kinds to support multitenancy [#446](https://github.com/damap-org/damap-backend/pull/446)
- Changed permissions for /quarkus-app/quarkus in images to allow rewriting by reaugmentation [#446](https://github.com/damap-org/damap-backend/pull/446)
- Upgraded org.apache.poi and poi-ooxml dependencies from 4.1.12 to 5.4.0 to [#379](https://github.com/damap-org/damap-backend/pull/379)

### Fixed

- Reverted unnecessary datasource environment variable name change in docker compose file [#424](https://github.com/damap-org/damap-backend/pull/424)
- Fixed broken Maven and GitHub deployment pipelines [#436](https://github.com/damap-org/damap-backend/pull/436)

### Resource Files

- Changed wording for closed/restricted dataset reasons [#443](https://github.com/damap-org/damap-backend/pull/443)

## [4.6.0] - 2025-07-08

### Added

- Added values for "no license" and "custom license" to license dataset [#373](https://github.com/damap-org/damap-backend/pull/373)
- Added a startup healthcheck to CI pipeline [#364](https://github.com/damap-org/damap-backend/pull/364)
- Added data transfer agreement to options of legal restrictions [#360](https://github.com/damap-org/damap-backend/pull/360)
- Added a javadoc check to mvn verify to insure correct documentation [#397](https://github.com/damap-org/damap-backend/pull/397)
- Added support for marking external storages as managed internally by an institution [#355](https://github.com/damap-org/damap-backend/pull/355)
- Added support for granting and revoking owner rights, editors now cannot create more editor [#399](https://github.com/damap-org/damap-backend/pull/399)
- Added new endpoint for updating the ORCID affiliation of contributors [#381](https://github.com/damap-org/damap-backend/pull/381)


## [4.5.3] - 2025-06-11

- Fixed container permissions for OpenShift deployment [#385](https://github.com/damap-org/damap-backend/pull/385)
- Update Lombok dependency to 1.18.30 for developers on macOS [#367](https://github.com/damap-org/damap-backend/pull/367)
- Added support for assigning contributors multiple roles [#339](https://github.com/damap-org/damap-backend/pull/339)
- Created single docker-compose files for oracle and postgres to make deployment easier [#376](https://github.com/damap-org/damap-backend/pull/376)
- Removed misleading warning logs [#375](https://github.com/damap-org/damap-backend/pull/375)
- Refactored how project and person service integrations are managed (backwards compatibility is still provided) [#398](https://github.com/damap-org/damap-backend/pull/398)
- Replaced mock person ORCIDs with ISNIs to avoid clashes with ORCID API calls [#402](https://github.com/damap-org/damap-backend/pull/402)

### Removed

- Cleaned up CI pipeline by removing SonarCube [#383](https://github.com/damap-org/damap-backend/pull/383)

### Fixed

- Test suite is now independent of R3Data API being available [#382](https://github.com/damap-org/damap-backend/pull/382)
- Fixed bug in exported DMP filename generation [#352](https://github.com/damap-org/damap-backend/pull/352)

### Template

- Added \[contact\] placeholder to FWF template to fill in "Co-ordination of data management responsibilities across partners:" in section I.2 [#356](https://github.com/damap-org/damap-backend/pull/356)
- Changed table settings in Word templates so that they do not split over two pages [#396](https://github.com/damap-org/damap-backend/pull/396)

## [4.5.2] - 2025-04-15

### Fixed

- Fixed a bug which prevented DAMAP from working with an oracle database [#362](https://github.com/tuwien-csd/damap-backend/pull/362).

## [4.5.1] - 2025-04-14

### Fixed

- Fixed a bug which prevented DAMAP from working with an oracle database [#358](https://github.com/tuwien-csd/damap-backend/pull/358).
- Fixed a bug where placeholders were left in the exported document when no project was chosen [#354](https://github.com/tuwien-csd/damap-backend/pull/354).

### Template

- SE relevant policies and guidelines section: Updated broken link to ethics and data protection document [#353](https://github.com/tuwien-csd/damap-backend/pull/353).

## [4.5.0] - 2025-04-01

### Added

- Added the "Technical Resource" field to dataset, which describes the hardware used to capture the dataset [#337](https://github.com/tuwien-csd/damap-backend/pull/337).

### Changed

- Work was done to make the code more bug resistant [#332](https://github.com/tuwien-csd/damap-backend/pull/332).

### Template

- HE terminology section: removed the link above the table and the "EOSC" row [#336](https://github.com/tuwien-csd/damap-backend/pull/336).
- Fixed broken header column text "dataset ID" in FWF template dataset tables [#344](https://github.com/tuwien-csd/damap-backend/pull/344).
- Assigned more space to each "dataset ID" column so the "dataset" is not split and stays in one line [#344](https://github.com/tuwien-csd/damap-backend/pull/344).
- Added cover pages to each template, explaining what do to after exporting to word. This covers replacing placeholders, rewriting automatically generated text and deletions [#345](https://github.com/tuwien-csd/damap-backend/pull/345).

### Resource Files

- Added FWF resource file; the only difference from SE is the following from under "personal.avail": (see section II.1) vs (see section 1a) [#343](https://github.com/tuwien-csd/damap-backend/pull/343).
- Removed mentions of the storage table from resource files, since there is no such table [#347](https://github.com/tuwien-csd/damap-backend/pull/347).

## [4.4.0] - 2025-02-10

### Added

- Added "Principal Investigator" and "Project Coordinator" as contributor roles [#285](https://github.com/tuwien-csd/damap-backend/pull/285).
- Added option to configure html title [#320](https://github.com/tuwien-csd/damap-backend/pull/320).
- Added backend support for configurable banners [#311](https://github.com/tuwien-csd/damap-backend/pull/311).
- Added develop quickstart guide documentation [#329](https://github.com/tuwien-csd/damap-backend/pull/329).
- Added support for ethical issues report number, can be disabled via config [#302](https://github.com/tuwien-csd/damap-backend/pull/302).

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
- Fixed a bug where editor name in version table was just a hash [#322](https://github.com/tuwien-csd/damap-backend/pull/322).

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