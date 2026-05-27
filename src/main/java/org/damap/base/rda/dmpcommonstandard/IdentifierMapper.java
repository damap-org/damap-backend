package org.damap.base.rda.dmpcommonstandard;

import org.damap.base.enums.EIdentifierType;
import org.damap.base.rest.dmp.domain.IdentifierDO;

class IdentifierMapper {
  public static IdentifierDO getIdentifierDO(String id) {
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
