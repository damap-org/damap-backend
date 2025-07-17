package org.damap.base.integration.pure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import org.damap.base.rest.dmp.domain.ProjectDO;

/**
 * This class is a simplified representation of the Pure AwardManagementProject and BasicProject
 * objects. We are unifying it here because the extra fields in either subtype has no relevance to
 * us.
 *
 * @see <a href="https://api.elsevierpure.com/ws/api/rapidoc.html#cmp--schemas-project">Project</a>}
 * @see <a href="cmp--schemas-awardmanagementproject">AwardManagementProject</a>
 * @see <a href="cmp--schemas-basicproject">BasicProject</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class PureAPIProject {
  @JsonProperty Long pureId;

  @JsonProperty String uuid;

  @JsonProperty String acronym;

  @JsonProperty ArrayList<PureAPIParticipantAssociation> participants;

  @JsonProperty ArrayList<PureAPIClassifiedFormattedLocalizedValue> descriptions;

  @JsonProperty HashMap<String, String> title;

  @JsonProperty PureAPIDateRange period;

  boolean titleContains(String text) {
    if (title == null) {
      return false;
    }
    if (text == null) {
      return true;
    }
    String finalText = text.toLowerCase();
    return title.values().stream()
        .filter(Objects::nonNull)
        .map(String::toLowerCase)
        .anyMatch(value -> value.contains(finalText));
  }

  ProjectDO toProjectDO(String descriptionClassificationURI) {
    ProjectDO result = new ProjectDO();
    if (pureId != null) {
      result.setId(pureId);
    }
    if (uuid != null) {
      result.setUniversityId(uuid);
    }
    if (acronym != null) {
      result.setAcronym(acronym);
    }

    if (title != null) {
      result.setTitle(extractLocalizedString(title));
    }

    if (descriptions != null) {
      descriptions.stream()
          .filter(v -> v.type != null)
          .filter(v -> !v.type.term.isEmpty())
          .filter(v -> v.value != null)
          .filter(v -> v.type.getUri().equals(descriptionClassificationURI))
          .map(v -> v.value)
          .map(PureAPIProject::extractLocalizedString)
          .filter(Objects::nonNull)
          .filter(str -> !str.isEmpty())
          .findFirst()
          .ifPresent(result::setDescription);
    }

    if (period != null) {
      if (period.startDate != null) {
        result.setStart(period.startDate);
      }
      if (period.endDate != null) {
        result.setEnd(period.endDate);
      }
    }

    return result;
  }

  private static String extractLocalizedString(Map<String, String> v) {
    if (v.containsKey("de_DE")) {
      return v.get("de_DE");
    }
    if (v.containsKey("en_GB")) {
      return v.get("en_GB");
    }
    if (v.containsKey("en_US")) {
      return v.get("en_US");
    }
    return v.keySet().stream().map(v::get).findFirst().orElse(null);
  }
}
