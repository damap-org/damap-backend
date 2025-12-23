#!/usr/bin/env python3
"""
Generate Liquibase changelog from flattened JSON translation file.

This script reads a JSON file with flat key-value pairs and generates
a Liquibase YAML changelog with INSERT statements for the translation table.

Usage:
    python generate-translation-changelog.py \
        --input /path/to/flattened-en.json \
        --language en \
        --changeset-id 26 \
        --author "Your Name" \
        --output changeLog-5.0.0_2.yaml

python generate-translation-changelog.py --input translations_new.json --language en --changeset-id 29 --author "Geoffrey Karnbach" --output ../src/main/resources/org/damap/base/db/changeLog-5.x/changeLog-5.0.0_2.yaml

"""

import argparse
import json
import sys
from typing import Any, Dict


def escape_yaml_string(value) -> str:
    """
    Escape a value for use in YAML.

    Args:
        value: The value to escape (string or will be converted to string)

    Returns:
        The escaped string, wrapped in single quotes if necessary
    """
    # Convert non-string values to JSON string representation
    if not isinstance(value, str):
        value = json.dumps(value, ensure_ascii=False)

    if not value:
        return "''"

    # Check if we need to quote the string
    needs_quoting = any(c in value for c in ['\n', '\r', '\'', '"', ':', '#', '&', '*', '!', '|', '>', '@', '`', ',', '[', ']', '{', '}', '-'])

    # Also quote if string starts with special chars or has leading/trailing whitespace
    starts_with_special = len(value) > 0 and value[0] in [',', '-', '?', ':', '@', '`', '"', '\'', '|', '>', '*', '&', '!', '%', '#', '[', ']', '{', '}']

    if needs_quoting or value.strip() != value or starts_with_special:
        # Use double quotes for multi-line strings (escape newlines and quotes)
        if '\n' in value or '\r' in value:
            # Escape newlines, carriage returns, backslashes, and quotes for double-quoted strings
            escaped = (value.replace('\\', '\\\\')  # Escape backslashes first
                          .replace('"', '\\"')      # Escape double quotes
                          .replace('\n', '\\n')     # Escape newlines
                          .replace('\r', '\\r')     # Escape carriage returns
                          .replace('\t', '\\t'))    # Escape tabs
            return f'"{escaped}"'
        else:
            # Use single quotes for single-line strings
            escaped = value.replace("'", "''")
            return f"'{escaped}'"

    return value


def generate_changelog(
    translations: Dict[str, Any],
    language: str,
    changeset_id: int,
    author: str
) -> str:
    """
    Generate a Liquibase changelog YAML from translations.

    Args:
        translations: Dictionary of key-value translation pairs
        language: Language code (e.g., 'en', 'de')
        changeset_id: Liquibase changeset ID
        author: Author name for the changeset

    Returns:
        YAML changelog as string
    """
    lines = [
        "databaseChangeLog:",
        "  - changeSet:",
        f"      id: {changeset_id}",
        f"      author: {author}",
        f"      comment: Insert {language} translations",
        "      changes:"
    ]

    non_string_count = 0
    for key, value in sorted(translations.items()):
        # Warn about non-string values
        if not isinstance(value, str):
            non_string_count += 1
            print(f"Warning: Key '{key}' has a non-string value (type: {type(value).__name__}), converting to JSON string", file=sys.stderr)
        lines.extend([
            "        - insert:",
            "            tableName: translation",
            "            columns:",
            "              - column:",
            "                  name: key",
            f"                  value: {escape_yaml_string(key)}",
            "              - column:",
            "                  name: language",
            f"                  value: {escape_yaml_string(language)}",
            "              - column:",
            "                  name: default_value",
            f"                  value: {escape_yaml_string(value)}",
            "              - column:",
            "                  name: active",
            "                  valueBoolean: true",
            "              - column:",
            "                  name: version",
            "                  valueNumeric: 0",
        ])

    # Add rollback
    lines.extend([
        "",
        "      rollback:",
        "        - delete:",
        "            tableName: translation",
        f"            where: language = '{language}'"
    ])

    if non_string_count > 0:
        print(f"\nNote: {non_string_count} non-string value(s) were converted to JSON strings", file=sys.stderr)

    return '\n'.join(lines) + '\n'


def main():
    parser = argparse.ArgumentParser(
        description='Generate Liquibase changelog from flattened JSON translation file'
    )
    parser.add_argument(
        '--input',
        required=True,
        help='Path to input JSON file with flat key-value pairs'
    )
    parser.add_argument(
        '--language',
        required=True,
        help='Language code (e.g., en, de)'
    )
    parser.add_argument(
        '--changeset-id',
        type=int,
        required=True,
        help='Liquibase changeset ID'
    )
    parser.add_argument(
        '--author',
        required=True,
        help='Author name for the changeset'
    )
    parser.add_argument(
        '--output',
        required=True,
        help='Path to output YAML file'
    )

    args = parser.parse_args()

    # Read input JSON
    try:
        with open(args.input, 'r', encoding='utf-8') as f:
            translations = json.load(f)
    except FileNotFoundError:
        print(f"Error: Input file not found: {args.input}", file=sys.stderr)
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(f"Error: Invalid JSON in input file: {e}", file=sys.stderr)
        sys.exit(1)

    if not isinstance(translations, dict):
        print("Error: JSON must be an object with key-value pairs", file=sys.stderr)
        sys.exit(1)

    # Generate changelog
    changelog = generate_changelog(
        translations,
        args.language,
        args.changeset_id,
        args.author
    )

    # Write output
    try:
        with open(args.output, 'w', encoding='utf-8') as f:
            f.write(changelog)
        print(f"Successfully generated changelog with {len(translations)} translations")
        print(f"Output written to: {args.output}")
    except IOError as e:
        print(f"Error writing output file: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()

