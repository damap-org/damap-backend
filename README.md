# DAMAP
 
DAMAP is an open source tool co-developed by TU Wien and TU Graz that supports researchers in managing both data and code along the research data lifecycle. It is based on the concept of machine-actionable data management plans (maDMPs) and aims to simplify the creation of data management plans (DMPs) for researchers.
 
The tool integrates with an institution's existing databases — such as project management and HR systems (CRIS applications) — to automatically pull relevant information into a DMP, increasing accuracy and reducing the effort required to complete one. This saves DMP authors from having to enter the same data multiple times.
 
DAMAP guides users through all sections of a DMP in ten steps by asking questions, suggesting text, and providing helpful information. It exports a pre-filled DMP as a Word document that can be customized and submitted to European and national research funders. Supported export templates include FWF, Horizon Europe, and Science Europe. Additionally, DAMAP is compatible with the [RDA recommendation on machine-actionable DMPs](https://doi.org/10.15497/rda00039) and offers a JSON export.
 
The content and structure of DAMAP is based on [Science Europe's Core Requirements for Data Management Plans](https://doi.org/10.5281/zenodo.4915861).
 
DAMAP is available under the MIT license and can be self-hosted by any institution. Alternatively, TU Wien offers a cloud-hosted version of DAMAP, allowing institutions to get started without running their own infrastructure. For more information on both options, visit [damap.org](https://damap.org/).

## Components

DAMAP is a typical Three-Tier-Architecture project composed out of the database tier and the following two projects 
residing in separate source code repositories.

1. **[damap-backend](https://github.com/damap-org/damap-backend):** maDMPs backend project
2. **[damap-frontend](https://github.com/damap-org/damap-frontend):** maDMPs frontend project

Also there are some components that you should already have at your institution that you can integrate with DAMAP,
in order to make this tool your own:

1. Authentication service with OpenID support (e.g. [Keycloak](https://www.keycloak.org/))
2. [CRIS](https://en.wikipedia.org/wiki/Current_research_information_system) system (system managing research projects)
3. System managing researcher data

The actively supported database is [Postgres](https://www.postgresql.org/) and to a lesser extent Oracle.
Other databases could be used as well, but they may not be tested.

## Installation

DAMAP supports deployment both in Docker-based containerized environments and on Kubernetes clusters.
It can be deployed either as a standalone installation for a single institution or in a multitenant setup
where one instance serves multiple universities or organizations.
You can find detailed installation instructions in the [Reference Manual](https://damap.org/manual/) on our website.

For instructions on how to run the project locally, read the development guidelines in the [DEVELOPMENT.md](docs/DEVELOPMENT.md) file.

## Customisation

To make use of the powers of DAMAP, customisations should be added to integrate it in your system environment.
Common customisations include CRIS systems to get up-to-date person and project data, color theme of the interface and
DMP templates.
Our [Reference Manual](https://damap.org/manual) documents the steps needed in order to adapt DAMAP to your needs.

## Contributing

Before contributing code, please carefully read the contribution guidelines in the [CONTRIBUTING.md](CONTRIBUTING.md) 
file and the developer documentation in the [DEVELOPMENT.md](docs/DEVELOPMENT.md) file.

## License

The project is licensed under the MIT license. See [LICENSE](LICENSE) file for further information.

## Funding
 
DAMAP has received funding from [FAIR Data Austria](https://forschungsdaten.at/fda/), [SharedRDM](https://forschung-daten.at/shared-rdm/), and the EC project [OSTrails](https://ostrails.eu/).
