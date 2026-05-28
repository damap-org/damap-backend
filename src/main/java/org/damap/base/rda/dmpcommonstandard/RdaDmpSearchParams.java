package org.damap.base.rda.dmpcommonstandard;

import java.time.OffsetDateTime;
import java.util.List;

public record RdaDmpSearchParams(
    int offset,
    int count,
    List<String> sort,
    OffsetDateTime createdBefore,
    OffsetDateTime createdAfter,
    OffsetDateTime modifiedBefore,
    OffsetDateTime modifiedAfter,
    List<String> languages,
    List<String> contactIds,
    List<String> contributorIds,
    List<String> datasetIds,
    List<String> metadataStandardIds,
    List<String> dmpIds,
    List<String> funderIds,
    List<String> grantIds,
    String query,
    Booleanish ethicalIssuesExist,
    String embargoBefore,
    String embargoAfter) {}
