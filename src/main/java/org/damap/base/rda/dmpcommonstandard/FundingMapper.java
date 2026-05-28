package org.damap.base.rda.dmpcommonstandard;

import org.damap.base.enums.EFundingState;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.rest.dmp.domain.FundingDO;
import org.damap.base.rest.dmp.domain.IdentifierDO;

/**
 * This class implements Funding conversion from and to the RDA DMP common standard. (See <a
 * href="https://github.com/RDA-DMP-Common/common-madmp-api">github.com/RDA-DMP-Common/common-madmp-api</a>
 * )
 *
 * <p>The conversion from the common standard into DAMAP objects is best-effort since not all data
 * can be represented.
 */
public final class FundingMapper extends AbstractMapper {
  /** Initialize the mapper with the default settings (strict mode turned on). */
  public FundingMapper() {
    this(true);
  }

  /**
   * Initialize the mapper with an option to turn off strict mode. Only use non-strict mode when a
   * human has to review the DMP afterward, never use it for automated imports!
   *
   * @param strict When enabled, any dropped data will cause a {@link
   *     CommonStandardCompatibilityException}. When disabled, DAMAP will do its best to represent
   *     any imported data in the common format but drop data it cannot represent.
   */
  public FundingMapper(boolean strict) {
    super(strict);
  }

  public Funding convert(FundingDO funding) {
    if (funding == null) return null;
    var result = new Funding();
    var funderId = funding.getFunderId();
    if (funderId != null && funderId.getIdentifier() != null) {
      result.setFunderId(convertFunderId(funderId));
    } else {
      result.setFunderId(new FunderID().identifier("0").type("other"));
    }
    result.setFundingStatus(convertFundingStatus(funding.getFundingStatus()));
    result.setGrantId(convertGrantId(funding.getGrantId()));
    return result;
  }

  public FundingDO convert(Funding funding) {
    if (funding == null) return null;
    var result = new FundingDO();
    result.setFunderId(convertFunderId(funding.getFunderId()));
    result.setFundingStatus(convertFundingState(funding.getFundingStatus()));
    result.setGrantId(convertGrantId(funding.getGrantId()));
    return result;
  }

  private IdentifierDO convertFunderId(FunderID funderId) {
    if (funderId == null) return null;
    if ("fundref".equalsIgnoreCase(funderId.getType())) {
      var result = new IdentifierDO();
      result.setIdentifier(funderId.getIdentifier());
      result.setType(EIdentifierType.FUNDREF);
      return result;
    }
    return IdentifierMapper.getIdentifierDO(funderId.getIdentifier());
  }

  private FunderID convertFunderId(IdentifierDO identifier) {
    if (identifier == null) return null;
    var result = new FunderID();
    result.setIdentifier(identifier.getIdentifier());
    result.setType(identifier.getType() == EIdentifierType.FUNDREF ? "fundref" : "other");
    if (identifier.getType() == EIdentifierType.DOI
        || identifier.getType() == EIdentifierType.ORCID) {
      result.setType("url");
    }
    return result;
  }

  private FundingStatus convertFundingStatus(EFundingState fundingState) {
    if (fundingState == null) {
      return null;
    }
    return switch (fundingState) {
      case PLANNED -> FundingStatus.PLANNED;
      case APPLIED -> FundingStatus.APPLIED;
      case GRANTED -> FundingStatus.GRANTED;
      case REJECTED -> FundingStatus.REJECTED;
      case UNSPECIFIED -> null;
    };
  }

  private EFundingState convertFundingState(FundingStatus fundingState) {
    if (fundingState == null) {
      return EFundingState.UNSPECIFIED;
    }
    return switch (fundingState) {
      case PLANNED -> EFundingState.PLANNED;
      case APPLIED -> EFundingState.APPLIED;
      case GRANTED -> EFundingState.GRANTED;
      case REJECTED -> EFundingState.REJECTED;
    };
  }

  private GrantID convertGrantId(IdentifierDO identifier) {
    if (identifier == null
        || identifier.getIdentifier() == null
        || identifier.getIdentifier().isBlank()) {
      return null;
    }
    var result = new GrantID();
    result.setIdentifier(identifier.getIdentifier());
    if (identifier.getType() != null) {
      result.setType(identifier.getType().toString().toLowerCase());
    } else {
      result.setType("other");
    }
    return result;
  }

  private IdentifierDO convertGrantId(GrantID identifier) {
    if (identifier == null) {
      return null;
    }
    var id = identifier.getIdentifier();
    return IdentifierMapper.getIdentifierDO(id);
  }
}
