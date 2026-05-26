package org.damap.base.rda.dmpcommonstandard;

import java.math.BigDecimal;
import org.damap.base.enums.ECostType;
import org.damap.base.rest.dmp.domain.CostDO;

/**
 * This class implements Cost conversion from and to the RDA DMP common standard. (See <a
 * href="https://github.com/RDA-DMP-Common/common-madmp-api">github.com/RDA-DMP-Common/common-madmp-api</a>
 * )
 *
 * <p>The conversion from the common standard into DAMAP objects is best-effort since not all data
 * can be represented.
 */
public class CostsMapper extends AbstractMapper {
  /** Initialize the mapper with the default settings (strict mode turned on). */
  public CostsMapper() {
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
  public CostsMapper(boolean strict) {
    super(strict);
  }

  public CostDO convert(Cost cost) {
    if (cost == null) {
      return null;
    }
    var result = new CostDO();
    if (cost.getType() != null) {
      try {
        result.setType(ECostType.valueOf(cost.getType().toUpperCase()));
      } catch (IllegalArgumentException e) {
        result.setType(ECostType.OTHER);
      }
    } else {
      result.setType(ECostType.OTHER);
    }
    result.setDescription(cost.getDescription());
    result.setTitle(cost.getTitle());
    var value = cost.getValue();
    if (value != null) {
      result.setValue(cost.getValue().floatValue());
    }
    var currencyCode = cost.getCurrencyCode();
    if (currencyCode != null) {
      result.setCurrencyCode(currencyCode.getValue());
    }
    result.setType(ECostType.OTHER);
    return result;
  }

  public Cost convert(CostDO costDO) {
    var result = new Cost();
    result.setDescription(costDO.getDescription());
    result.setTitle(costDO.getTitle() != null && !costDO.getTitle().isBlank() ? costDO.getTitle() : "Cost item");
    var currencyCode = costDO.getCurrencyCode();
    if (costDO.getType() != null) {
      result.setType(costDO.getType().toString());
    }
    if (currencyCode != null) {
      result.setCurrencyCode(CurrencyCode.valueOf(currencyCode));
    }
    var value = costDO.getValue();
    if (value != null) {
      result.setValue(BigDecimal.valueOf(costDO.getValue()));
    }
    return result;
  }
}
