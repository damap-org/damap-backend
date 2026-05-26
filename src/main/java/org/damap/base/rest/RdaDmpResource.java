package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rda.dmpcommonstandard.DMPDocument;
import org.damap.base.rda.dmpcommonstandard.DMPData;
import org.damap.base.rda.dmpcommonstandard.RdaDmpSearchParams;
import org.damap.base.rest.rda.service.RdaDmpService;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@Path("/api/rda/dmps")
@Authenticated
@Produces({
        RdaDmpResource.RDA_MEDIA_TYPE,
        MediaType.APPLICATION_JSON
})
@Consumes({
        RdaDmpResource.RDA_MEDIA_TYPE,
        MediaType.APPLICATION_JSON
})
@JBossLog
public class RdaDmpResource {

    public static final String RDA_MEDIA_TYPE =
            "application/vnd.org.rd-alliance.dmp-common.v1.2+json";

    @Inject RdaDmpService rdaDmpService;
    @Inject
    Validator validator;

    @GET
    public Response listDMPs(
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("count") @DefaultValue("20") int count,

            @QueryParam("sort[]") List<String> sort,
            @QueryParam("sort") List<String> sortPlain,

            @QueryParam("created_before") String createdBefore,
            @QueryParam("created_after") String createdAfter,
            @QueryParam("modified_before") String modifiedBefore,
            @QueryParam("modified_after") String modifiedAfter,

            @QueryParam("languages[]") List<String> languages,
            @QueryParam("languages") List<String> languagesPlain,

            @QueryParam("contact_ids[]") List<String> contactIds,
            @QueryParam("contact_ids") List<String> contactIdsPlain,

            @QueryParam("contributor_ids[]") List<String> contributorIds,
            @QueryParam("contributor_ids") List<String> contributorIdsPlain,

            @QueryParam("dataset_ids[]") List<String> datasetIds,
            @QueryParam("dataset_ids") List<String> datasetIdsPlain,

            @QueryParam("metadata_standard_ids[]") List<String> metadataStandardIds,
            @QueryParam("metadata_standard_ids") List<String> metadataStandardIdsPlain,

            @QueryParam("dmp_ids[]") List<String> dmpIds,
            @QueryParam("dmp_ids") List<String> dmpIdsPlain,

            @QueryParam("funder_ids[]") List<String> funderIds,
            @QueryParam("funder_ids") List<String> funderIdsPlain,

            @QueryParam("grant_ids[]") List<String> grantIds,
            @QueryParam("grant_ids") List<String> grantIdsPlain,

            @QueryParam("query") String query,
            @QueryParam("ethical_issues_exist") String ethicalIssuesExist,
            @QueryParam("embargo_before") String embargoBefore,
            @QueryParam("embargo_after") String embargoAfter,

            @Context HttpHeaders headers
    ) {
        log.info("Searching RDA DMPs");

        RdaDmpSearchParams params =
                rdaDmpService.createSearchParams(
                        offset,
                        count,
                        mergeLists(sort, sortPlain),
                        createdBefore,
                        createdAfter,
                        modifiedBefore,
                        modifiedAfter,
                        mergeLists(languages, languagesPlain),
                        mergeLists(contactIds, contactIdsPlain),
                        mergeLists(contributorIds, contributorIdsPlain),
                        mergeLists(datasetIds, datasetIdsPlain),
                        mergeLists(metadataStandardIds, metadataStandardIdsPlain),
                        mergeLists(dmpIds, dmpIdsPlain),
                        mergeLists(funderIds, funderIdsPlain),
                        mergeLists(grantIds, grantIdsPlain),
                        query,
                        ethicalIssuesExist,
                        embargoBefore,
                        embargoAfter);

        return Response.ok(rdaDmpService.listDMPs(params))
                .type(resolveResponseType(headers))
                .build();
    }

    @POST
    public Response createDMP(
            DMPDocument rdaDmpDocument,
            @Context HttpHeaders headers
    ) {
        if (rdaDmpDocument == null || rdaDmpDocument.getDmp() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error_code\": \"bad_request\", \"error_message\": \"Request body must contain a dmp object\"}")
                    .build();
        }

        DMPData data = rdaDmpDocument.getDmp();

        Set<ConstraintViolation<DMPData>> violations = validator.validate(rdaDmpDocument.getDmp());

        if (data.getDataset() == null || data.getDataset().isEmpty()) {
            return Response.status(400)
                    .header("Content-Type", "application/json")
                    .entity("{\"error_code\": \"dmp_invalid\", \"error_message\": \"Validation failed: dataset must contain at least one item\"}")
                    .build();
        }

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        log.info("Creating RDA DMP");

        RdaDmpService.RdaDmpResult result =
                rdaDmpService.createDMP(rdaDmpDocument);

        return Response.ok(result.body())
                .type(resolveResponseType(headers))
                .header(HttpHeaders.LAST_MODIFIED, result.lastModified())
                .build();
    }

    @GET
    @Path("/{id}")
    public Response getDMP(
            @PathParam("id") String id,
            @Context HttpHeaders headers
    ) {
        log.info("Retrieving RDA DMP with id: " + id);

        RdaDmpService.RdaDmpResult result = rdaDmpService.getDMP(id);

        return Response.ok(result.body())
                .type(resolveResponseType(headers))
                .header(HttpHeaders.LAST_MODIFIED, result.lastModified())
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response putDMP(
            @PathParam("id") String id,
            DMPDocument rdaDmpDocument,
            @HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince,
            @Context HttpHeaders headers
    ) {
        if (rdaDmpDocument == null || rdaDmpDocument.getDmp() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error_code\": \"bad_request\", \"error_message\": \"Request body must contain a dmp object\"}")
                    .build();
        }

        DMPData data = rdaDmpDocument.getDmp();

        Set<ConstraintViolation<DMPData>> violations = validator.validate(data);

        if (data.getDataset() == null || data.getDataset().isEmpty()) {
            return Response.status(400)
                    .header("Content-Type", "application/json")
                    .entity("{\"error_code\": \"dmp_invalid\", \"error_message\": \"Validation failed: dataset must contain at least one item\"}")
                    .build();
        }

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        log.info("Updating RDA DMP with id: " + id);

        RdaDmpService.RdaDmpResult result =
                rdaDmpService.updateDMP(id, rdaDmpDocument, ifUnmodifiedSince);

        return Response.ok(result.body())
                .type(resolveResponseType(headers))
                .header(HttpHeaders.LAST_MODIFIED, result.lastModified())
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteDMP(@PathParam("id") String id) {
        log.info("Deleting RDA DMP with id: " + id);

        rdaDmpService.deleteDMP(id);

        return Response.noContent().build();
    }

    private List<String> mergeLists(List<String> first, List<String> second) {
        if ((first == null || first.isEmpty()) && (second == null || second.isEmpty())) {
            return List.of();
        }

        if (first == null || first.isEmpty()) {
            return second;
        }

        if (second == null || second.isEmpty()) {
            return first;
        }

        return java.util.stream.Stream.concat(first.stream(), second.stream()).toList();
    }

    private String resolveResponseType(HttpHeaders headers) {
        String accept = headers.getHeaderString(HttpHeaders.ACCEPT);

        if (accept == null || accept.isBlank() || accept.contains("*/*")) {
            return MediaType.APPLICATION_JSON;
        }

        if (accept.contains(RDA_MEDIA_TYPE)) {
            return RDA_MEDIA_TYPE;
        }

        if (accept.contains(MediaType.APPLICATION_JSON)) {
            return MediaType.APPLICATION_JSON;
        }

        throw new NotAcceptableException("Requested media type is not supported");
    }

    @ServerExceptionMapper
    public Response mapConstraintViolationException(ConstraintViolationException exception) {
        String missingFields = exception.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " is required")
                .collect(java.util.stream.Collectors.joining(", "));

        String jsonError = String.format(
                "{\"error_code\": \"dmp_invalid\", \"error_message\": \"Validation failed: %s\"}",
                missingFields.replace("\"", "'")
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .entity(jsonError)
                .build();
    }

    @ServerExceptionMapper
    public Response mapBadRequestException(jakarta.ws.rs.BadRequestException exception) {
        String errorMessage = exception.getMessage() != null ? exception.getMessage() : "Bad Request";

        String jsonError = String.format(
                "{\"error_code\": \"dmp_invalid\", \"error_message\": \"%s\"}",
                errorMessage.replace("\"", "'")
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .entity(jsonError)
                .build();
    }
}