package org.damap.base.rda.dmpcommonstandard;

import org.damap.base.enums.EIdentifierType;
import org.damap.base.rest.dmp.domain.IdentifierDO;

class IdentifierMapper {
  // TODO: Add documentation about what this method is supposed to be doing - its not mapping all
  // possible types
  // Additionally, we should always get identifier + type as input, so we could also try to map the
  // type first and not
  // only use the identifier string
  public static IdentifierDO getIdentifierDO(String id) {
    if (id == null || id.isBlank()) {
      return null;
    }
    var result = new IdentifierDO();
    result.setType(EIdentifierType.OTHER);
    result.setIdentifier(id);
    if (id.contains("/ark:/")) {
      result.setType(EIdentifierType.ARK);
      return result;
    }
    if (id.startsWith("/doi:/") || id.startsWith("https://doi.org/")) {
      result.setType(EIdentifierType.DOI);
      return result;
    }
    if (id.startsWith("https://ror.org/")) {
      result.setType(EIdentifierType.ROR);
      return result;
    }
    if (id.startsWith("https://orcid.org")) {
      result.setType(EIdentifierType.ORCID);
      return result;
    }
    //noinspection HttpUrlsUsage
    if (id.startsWith("http://") || id.startsWith("https://")) {
      result.setType(EIdentifierType.URL);
      return result;
    }
    return result;
  }
}
