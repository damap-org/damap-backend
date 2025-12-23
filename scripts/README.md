# Translation Management Scripts

## Overview

This directory contains scripts for managing backend translations in the DAMAP system.

## generate-translation-changelog.py

This Python script generates Liquibase changelog files from flattened JSON translation files.

### Prerequisites

- Python 3.6 or higher (uses standard library only, no external dependencies)

### Input Format

The script expects a JSON file with flat key-value pairs:

```json
{
  "access.button.plans": "Go back to DMP overview",
  "admin.title": "Admin page",
  "yes": "Yes",
  "no": "No"
}
```

### Usage

```bash
python3 generate-translation-changelog.py \
  --input /path/to/flattened-translations.json \
  --language en \
  --changeset-id 26 \
  --author "Your Name" \
  --output ../src/main/resources/org/damap/base/db/changeLog-5.x/changeLog-5.0.0_2.yaml
```

### Parameters

- `--input`: Path to the input JSON file with flat key-value translation pairs
- `--language`: ISO 639-1 language code (e.g., `en`, `de`, `fr`)
- `--changeset-id`: Unique Liquibase changeset ID (must be unique across all changelogs)
- `--author`: Author name for the changeset
- `--output`: Path to the output YAML changelog file

### Examples

Generate English translations:
```bash
python3 scripts/generate-translation-changelog.py \
  --input translations/en.json \
  --language en \
  --changeset-id 26 \
  --author "Translation Management System" \
  --output src/main/resources/org/damap/base/db/changeLog-5.x/changeLog-5.0.0_2.yaml
```

Generate German translations:
```bash
python3 scripts/generate-translation-changelog.py \
  --input translations/de.json \
  --language de \
  --changeset-id 27 \
  --author "Translation Management System" \
  --output src/main/resources/org/damap/base/db/changeLog-5.x/changeLog-5.0.0_3.yaml
```

### Sample Files

- `sample-en.json`: Sample English translations
- `sample-de.json`: Sample German translations

These samples demonstrate the expected JSON format and can be used for testing.

## Notes

- The script automatically escapes special characters in translation values
- Each translation is inserted with `active=true` and `version=0`
- The generated changelog includes a rollback section that deletes all translations for the language
- Make sure to use unique changeset IDs to avoid conflicts with existing changelogs

