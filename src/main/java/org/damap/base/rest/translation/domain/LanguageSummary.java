package org.damap.base.rest.translation.domain;

/** Per-language activation summary used by the admin UI. */
public record LanguageSummary(String language, Boolean active) {}
