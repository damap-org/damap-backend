# Contributing to DAMAP project

The code for the OpenSource project DAMAP is hosted at [GitHub](https://github.com/damap-org/damap-backend),
which allows contributing issues which will be picked up by the lead developers and directly contributing code
as Pull Requests.

## Issues

Whenever something in the application is not working as expected, describe and report the problem in an issue.
This allows to discuss the issue with a developer, who would then create a Pull Request which solves the issue.

When describing issues, a reporter should describe the issue in a way that a developer can reproduce it. To
help to create good issues, all the points in the issue template should be filled out, if applicable.

---

## Java Docs

When publishing the package, java docs will be generated. In order to keep everything documented, adding java docs is really helpful.
Times to run the java docs command:
- Add, remove or update function
- Add or remove class
- Add, remove or update annotator/decorator
- Add, remove or update test

Creating java docs can be done via a command. We only care about:
- parameters
- return value
- linking classes
- throws info

In order to run the command for the code in the src folder, run `mvn javadoc:fix -DfixTags=param,return,link,throws`
In order to run the command for the code in the test folder, run `mvn javadoc:test-fix -DfixTags=param,return,link,throws`

After that, also make sure to format the code ([see Code Formatting](#code-formatting)).

---

## Changelog

We keep a changelog to keep stakeholders up to date on current changes in DAMAP and to make it easier to maintain custom 
templates and translations.
Our changelog workflow is inspired by [here](https://keepachangelog.com/en/1.1.0/#what), and extended with the types Template, Resource Files and Translations.
Everytime we finish an issue, we write the changes in an .md changelog file, grouped by release and type.
It is also important to note, that one issue can create multiple entries in the changelog, if this improves readability.

Changelogs are not written for developers, but for maintainers, admins and other stakeholders, so try to keep it as
non-technical as possible, especially for the three custom types we introduced:
- **Template:** Should be used whenever changes to one or more templates are made. Should be descriptive enough so maintainers can adapt their custom templates using this description.
- **Resource Files:** Should be used whenever changes to export resource files are made. 
- **Translations:** Should be used whenever something regarding translations has changed. Maintainers should be able to take appropriate actions from this explanation

---

## Code Formatting

This project uses a configured opinionated code formatter [spotless](https://github.com/diffplug/spotless). Letting a specific tool take care of the formatting lets developers
and reviewers focus on the implementation part.

This maven plugin provides CLI commands for the project. Useful (and used) commands are:

- check: Check the code for formatting issues and report them (`mvn spotless:check`)
- apply: Check the code and apply required code changes (`mvn spotless:apply`)

The check command will be run during the `verify` phase of maven as well.

---

## Testing

Before requesting code changes, a contributor should run the test capabilities offered by the project by running
the following command in the source repository:

```shell
mvn verify
```

This allows to discover code defects earlier and ensures the application behaves as it should while code changes.

---

## Pull Requests

Before creating a pull request, the changes should be tested locally as described in the [Testing](#testing) section.

To request the lead developers to pull in code change proposals, fork the repository, create a branch and push the code
to your own fork. Create a Pull Request with `damap-org/damap-backend` as base and write a meaningful description
of your changes. 

If you are working with the main repository, the naming convention for branches is `<contributor initials>/<short-description-separated-with-hyphens>` e.g.:

```
xy/file-upload
```

Once the PR is merged, delete the source branch.

Generally Pull Request should cover only a specific topic in form of one or a few commits. 

### Commit message

The commit message should follow the good practices of https://cbea.ms/git-commit/ and consist of a meaningful subject line with 
a short description of the changes, and an optional body to explain the changes and the reasons for them. 
A blank line separates the subject line from the body.<br>
For commits, try to avoid redundant commits, and squash  them into the previous commit for the same implementation. 
It makes sense to have different commits if you are working on different aspects. For example, commit 1 deals with the implementation, 
commit 2 adds test cases. Each commit should also try to leave the application in a stable state.

For example: Export: Fix bug where export fails without project end date

### Documentation

Whenever you create new methods or work with preexisting ones, please write / update [Javadoc](https://www.baeldung.com/javadoc)
to document the source code. Whenever a contributor changes code, documentation should be updated too. Either in the
form of comments to explain one particular part of the code or in the form of an .md file if the changes are big and need
to be documented separately.
