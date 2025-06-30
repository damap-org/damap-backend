# Customisation

## Customizing DAMAP backend

Implement your own institutional backend by creating a new project with [quarkus](https://quarkus.io/).

Download the damap-backend project and then build it using this command:

```shell
mvn clean install -Dquarkus-profile=dev
```

After compiling the public backend we will have a jar file in the target folder with a filename like this:
**damap-backend-1.2.0.jar**.

Add the following dependency to the pom.xml of your project in order to include the public damap-backend JAR as a dependency,
as well as all of its functionalities:

```xml
<dependency>
  <groupId>org.damap</groupId>
  <artifactId>base</artifactId>
  <type>jar</type>
  <version>1.2.0-SNAPSHOT</version>
</dependency>
```

In order to be available for the build this jar file needs to be placed in the institutional backend project
(this step is not required when it is done locally, instead maven will find it by reload all maven projects).
Copy it manually or make it available on your institutional nexus.

After adding the other components to the project you will be able to run it using:

```shell
   mvn compile quarkus:dev
```


### Overriding config values

In order to connect the required components with our project, we need to provide values to the custom config
variables in the [config file of the project](src/main/resources/application.yaml).

In order to do this add the following to your pom.xml,
so your institutional project will also use the .yaml format for its config file:

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-config-yaml</artifactId>
</dependency>
```

Copy and paste the custom config settings variables from the backends
[application.yaml](src/main/resources/application.yaml) into your custom projects application.yaml.
Here you can edit all the relevant config values you need to adapt for the project to run
and they will take precedence over the ones in the dependency.

Alternatively these variables can also be overridden by using your systems environment variables
(see [quarkus configuration properties priority](https://quarkus.io/guides/config-reference)),
which also allows overriding them at runtime.

The value DAMAP_ORIGIN defines your domains, which will be permitted to communicate with the backend.
Include here the domain of your frontend.

```yaml
damap:
   origins: https://your.frontend.com,https://*.yourdomain.com
```


### Configuring Authentication service

Provide the following config values in order to be able to integrate your authentication service:

```yaml
damap:
   auth:
      url: https://your.authentication.server
      client:
         backend: your-backend-authentication-client-id
         frontend: your-frontend-authentication-client-id
      scope: your-authentication-scope
      user: your-authentication-claim-holding-the-user-id
```

Note the value DAMAP_AUTH_USER, which will contain the variable name of your institutional person-ID.
The variable name should be an additional property provided by your authentication service,
and contains the users ID as per your project/researcher management system.
This ID will then be used throughout the project to identify people and users within damap.
This allows you to make people recognizeable through your various systems, without having to map or track new IDs.

Any person that is authenticated through your service will be considered as a user, 
as long as they have the provided person-ID attribute (renamed to your preference).
If you wish to have users with the administrator role, you need to provide them with the role **Damap Admin**
within your authentication service.

You can view our [sample Keycloak realm export](docker/sample-damap-realm-export.json) for better visualization.


### Configuring Database

Provide the following config values in order to be able to integrate your relational database of choice:

```yaml
damap:
   datasource:
      url: jdbc:your-datasource-language:your-datasource.address
      username: your-datasource-username
      password: your-datasource-password
      db-kind: postgresql # your database type
```
### Custom Database Changes

If you want to customize [liquibase](https://www.liquibase.org/get-started) for your own institutional backend:
- Create a new namespace for your institutions name - like e.g. at/ac/tugraz/damap
- Create your own root changelog and change the liquibase path in application.yaml to point to your new root changelog
```yaml
liquibase:
  migrate-at-start: true
  change-log: at/ac/tugraz/damap/db/newChangeLog-root.yaml
```
- Your new root changelog should look something like this:
```yaml
databaseChangeLog:
  - include:
      file: org/damap/base/db/changeLog-root.yaml
```
- This include statement makes sure, that all damap changesets are automatically included in your seperate backend
- After that, you can create your own changelogs in your custom institutional folder and include them in your root
- Damap uses sequential integers as ids - to avoid conflicts, you should adpopt your own id system, that does not clash
  with damap. Liquibase allows any type of id, as long as it is unique - e.g. tuwien_1, tuwien_2...

If you want to completely do your own thing, keep everything above the same, but instead of including the changeLog-root.yaml
file, include every changeset you want to keep separately. Beware - this approach requires a lot of maintenance, since
every new damap version has to be checked for new changesets you might want to include.
Also before version 3, all changesets where kept in db/changeLog.yaml. If you might want to remove certain changesets in
this file, you would have to create your own version in your institutional folder. 

### Configuring Project and Person API

DAMAP supports automatically populating project and person information from your CRIS system. It comes with a number of
industry-standard integrations, such as ORCID or Elsevier Pure, but you can also create your own.

To configure the integration, you may need to change the following settings in `application.yaml` or provide the
corresponding environment variables:

<details><summary>application.yaml</summary>

```yaml
## Custom project service:
# project-service: your-service
## Note: if left unconfigured, the natural Java CDI load order takes precedence. Use the
## @Priority annotation to load your implementation first.

  person-services:
    - display-text: 'University'
      query-value: 'UNIVERSITY'
      class-name: 'org.damap.base.integration.mock.MockUniversityPersonServiceImpl'
    - display-text: 'ORCID'
      query-value: 'ORCID'
      class-name: 'org.damap.base.integration.orcid.ORCIDPersonServiceImpl'
```

</details>

To create your own integration, you need to implement the following two interfaces:

- [ProjectService](src/main/java/org/damap/base/integration/ProjectServiceProvider.java)
- [PersonService](src/main/java/org/damap/base/integration/PersonService.java)

### Providing a FITS service

Integrate and run a [FITS](https://projects.iq.harvard.edu/fits) service.
You can then provide a link to your service in the config, so that damap might make use of it:

```yaml
damap:
   fits-url: http://your.fits.service:1234
```

### Defining Production vs. Development systems

Change the **DEV** property to **PROD** in order to enable production environment specific behavior in the frontend,
such as removing the test-system notification banner.

```yaml
damap:
  env: DEV
```

## Deploying the damap frontend

For running and adapting the frontend refer to the [damap-frontend project](https://github.com/damap-org/damap-frontend/blob/next/CUSTOMISING.md).

## Further customisation

### Recommended repository

If you wish to promote a specific repository from the list of repositories sourced from r3data
then you can provide a list of r3data-IDs in the config file:

```yaml
damap:
   repositories:
      recommendation: [ your-recommened-repositories ]
```

### Storage options

In order to provide the user with a series of storage options, these must be added to the databse beforehand.
After running the project once, liquibase will have added the entire databse structure.
Additionally, database procedures for storage are available. These procedures allow to easily insert, update or upsert storage options. These procedures may be invoked by connecting to the database or by simply creating another change log.
Available storage procedures can be found here: `src/main/resources/org/damap/base/db/changeLog-4.x/storage_procedures.sql`

Here is an example change log which invokes a storage procedures to insert a storage entry as well as a translation for it:

```
databaseChangeLog:
  - changeSet:
      id: storage_insert_1
      author: David Eckhard
      changes:
        - sql:
            dbms:  '!h2, postgresql'
            splitStatements:  false
            stripComments:  false
            sql: >
              CALL damap_insert_storage(
                '/url/of/my/storage', 
                0, # version
                'Storage Location. Can also just be a country code (e.g. AT)',
                'Storage backup location. Can also just be a country code (e.g. AT).'
              );
              CALL damap_insert_storage_translation(
                '/url/of/my/storage', # Specify the url of the storage inserted before. This is used as a foreign key.
                0,
                'eng', # Language code. Has to be available in the database (default available are `deu` and `eng`)
                'English name of my storage',
                'Description of this storage option'
              );
```

Here is an example change log which invokes a storage procedures to upsert a storage entry as well as a translation for it (it will either insert, if such an entity not exist yet or update an existing entity):
```
databaseChangeLog:
  - changeSet:
      id: storage_upsert_1
      author: David Eckhard
      changes:
        - sql:
            dbms:  '!h2, postgresql'
            splitStatements:  false
            stripComments:  false
            sql: >
              CALL damap_upsert_storage(
                'old /url/of/my/storage', # if an existing storage should be updated, put the old url here. Otherwise, use the same value here and in the new url
                'new /url/of/my/storage', # if an existing storage should be updated, put the new url here. Otherwise, use the same value here and in the old url
                0, # version
                'Storage Location. Can also just be a country code (e.g. AT)',
                'Storage backup location. Can also just be a country code (e.g. AT).'
              );
              CALL damap_upsert_storage_translation(
                '/url/of/my/storage', # Specify the url of the storage inserted before. This is used as a foreign key.
                0,
                'eng', # Language code. Has to be available in the database (default available are `deu` and `eng`)
                'English name of my storage',
                'Description of this storage option'
              );
```

### Export customisation

[Export word template](src/main/resources/org/damap/base/template/scienceEuropeTemplate.docx) and
[its resource file](src/main/resources/org/damap/base/template/scienceEuropeTemplate.resource)
can be replaced in the institutional project
by adding a replacement file in the resources folder of your customisation project.
Then write a class to extend
[TemplateFileBrokerServiceImpl](src/main/java/org/damap/base/conversion/TemplateFileBrokerServiceImpl.java)
and have it override the methods which retrieve those files.

The new filepath will look into the local resource folder and take the file placed there.
This .docx file needs to contain the placeholder keywords found in the sample template provided in the generic project, 
in order to replace the text at those specific places.
By replacing the resource file instead, the default texts used to compose the document can be adapted.

The template is selected automatically when exporting, if the project has a funder. You may want to override this functionality, 
if you have custom templates for example. To override this, write a class that extends [TemplateSelectorServiceImpl](src/main/java/org/damap/base/conversion/TemplateSelectorServiceImpl.java)
and have it override the methods that determine the template.
