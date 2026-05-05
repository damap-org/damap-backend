Liquibase is an [open source library](https://docs.liquibase.com/) for maintaining and applying database schema changes,
independent of the underlying database distribution.
This project uses liquibase primarily to be database independent, but also for provenance of schema changes.
At startup, liquibase automatically applies all changes to the configured datasource, that haven't been applied yet.

## Changelogs and Changesets naming conventions
Liquibase applies changes in units called changesets - they look something like this:
``` 
  - changeSet:
      id: 69
      author: Your name
      changes:
        - update:
          tableName: pokemons
          columns:
            - column:
                name: in-party
                valueBoolean: true
          where: nickname = 'Odd dish'
```
Changesets are one unit of a database change and can include multiple changes. 
Changesets live in a changelog file, which can have multiple changesets, but in this project we usually
keep to one changeset per changelog.
Changesets have a unique id, for which we use consecutive integers.
Keep in mind the id your predecessor used. 
Changelogs follow a strict naming structure. 
Each major version X has its own folder named `changeLog-X.x` (the x here is not to be substituted!!!).
Changesets that were included during this version are stored inside of this folder.
Each changeset has its own file named changeLog-4.3_0_i, where 4 is the version of the current folder, 3 is the minor
version and 0 the fix version the change belongs to.
i is the running index of the changes for the same release.
For example `changeLog-3.0.0_1.yaml`.
If two changes occur in the same minor version, use `changeLog-3.0.0_2.yaml` to distinguish between them.
Please stick to these naming guidelines, to avoid conflicts.
> [!IMPORTANT]
> The major version of the filename is not the version the change is going to be released under,
> but the current version during development.

For each major version, one change log is created which imports all change logs for this version.
This helps to keep the root change log smaller.
The version root changelog is named like `changeLog-4.x.yaml`. 4 is the current major version and the x is not to be substituted.
Version root changelogs are then imported into changeLog-root.yaml.

### Creating a changeset file quickstart

1. Generate a new version folder like `changeLog-4.x` (if not already present)
2. Generate a new version changelog inside like `changeLog-4.x.yaml` (if not already present)
   1. Include this version changelog into the root changelog (if not already present)
3. Generate a changeLog file like `changeLog-4.0.0_1.yaml`
   1. Keep in mind the naming scheme
   2. Create an entry in the version changelog file `changeLog-4.x.yaml`
4. Create your changeset in the changelog file you created
   1. Write your name and the next sequential integer to keep ids consistent
   2. Add a rollback statement after your changes

## Writing a changeset

Liquibase uses its own scripting language to abstract from vendor specific SQL. 
E.g. some DBs use 1/0 as booleans and some have true boolean types.
In liquibase you just write `valueBoolean: true` and it automatically translates the value correctly into the
underlying database vendor specific SQL. 
This works for all common operations that every DB is able to do.
For the liquibase syntax consult the [liquibase docs](https://docs.liquibase.com/secure/reference-guide-5-1/change-types/what-is-a-change-type)
The most common operations you will do will look something like this:

### Create
```      
    changes:
        - createSequence:
            sequenceName: pokemons_seq
            startValue: 1
            incrementBy: 50

        - createTable:
            tableName: pokemons
            columns:
              - column:
                  name: id
                  type: BIGINT
                  defaultValueSequenceNext: pokemons_seq
                  constraints:
                    primaryKey: true
              - column:
                  name: species-name
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
                    unique: true
              - column:
                  name: nickname
                  type: VARCHAR(255)
              - column:
                  name: in-party
                  type: BOOLEAN
                  defaultValueBoolean: false
                  constraints:
                    nullable: false
      rollback:
        - dropTable:
            tableName: translation
```

> [!IMPORTANT]
> Notice the create createSequence and defaultValueSequenceNext - they are used by hibernate under the hood to enable
> more efficient id generation.
> They should be added everytime you create a new table with the same parameters.
> Naming scheme is "tablename_seq" 

> [!IMPORTANT]
> When creating columns, you have to specify data types.
> Liquibase supports base liquibase types [listed here](https://docs.liquibase.com/pro/user-guide-4-33/how-does-liquibase-handle-data-types)
> Whenever you have to create a new table, always choose a type from the left, it gets mapped automatically to the
> appropriate type of your specific database.

### Delete
```     
    - changes   
        - delete:
            tableName: pokemons
            where: id = '69'
    
    - rollback:
        - insert
            ...
```

### Update
```
    - changes
        - update:
          tableName: pokemons
          columns:
            - column:
                name: nickname
                value: 'dish'
          where: nickname = 'odd dish'
          
    - rollback
        - update:
          tableName: pokemons
          columns:
            - column:
                name: nickname
                value: 'odd dish'
          where: nickname = 'dish'
```

### Insert
```
    - changes
        - insert:
            tableName: pokemons
            columns:
              - column:
                  name: species-name
                  value: 'oddish'
              - column:
                  name: nickname
                  value: 'odd dish'
              - column:
                  name: in-party
                  valueBoolean: true
     
    - rollback
        - delete:
            tableName: pokemons
            where: species-name = 'oddish' AND nickname = 'odd dish'
```

### Common mistakes when writing changesets

1. ID is not sequential and author of the changeset wasnt filled out
2. Wrote custom SQL that doesnt run on every DB - avoid SQL unless its absolutely necessary
3. Didnt use liquibase specific data types, e.g. Numeric instead of Number
4. Used more unusual operations like inserting a trigger, which doesnt run on every DB
5. Forgot to add sequence when creating a new table
6. Forgot rollback operation

## Inserting data into translation table

The translation table is one of the more complex tables, since admin users can generate new languages per instance,
without us knowing which languages got created.
This creates an issue when adding new translations, since they need to be inserted per language and we dont know which
languages there are.
You can circumvent this issue with the below SQL script, which is save across different DB vendors:

```
databaseChangeLog:
  - changeSet:
      id: 69
      author: Your name
      changes:
        - sql: |
              INSERT INTO translation (translation_key, language, default_value, active, version)
              SELECT 'New Translation Key', l.language, 'New Default Value', l.active, 0 
              FROM (SELECT DISTINCT language, active FROM translation) l;
```

> [!WARNING]
> This only works as long as the active boolean is consistent per language, so all english rows are either true or all
> rows are false.
> Of course, if the column names change, you need to adapt the script.