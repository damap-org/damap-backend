package org.damap.base.rest.admin.mapper;

import lombok.experimental.UtilityClass;
import org.damap.base.domain.Banner;
import org.damap.base.rest.admin.domain.BannerDO;

/** BannerDOMapper class. */
@UtilityClass
public class BannerDOMapper {

  /**
   * mapEntityToDO.
   *
   * @param banner a {@link Banner} object
   * @param bannerDO a {@link BannerDO} object
   * @return a {@link BannerDO} object
   */
  public BannerDO mapEntityToDO(Banner banner, BannerDO bannerDO) {
    bannerDO.setTitle(banner.getTitle());
    bannerDO.setDescription(banner.getDescription());
    bannerDO.setDismissible(banner.getDismissible());
    bannerDO.setColor(banner.getColor());

    return bannerDO;
  }

  /**
   * mapDOtoEntity.
   *
   * @param bannerDO a {@link BannerDO} object
   * @param banner a {@link Banner} object
   * @return a {@link BannerDO} object
   */
  public Banner mapDOtoEntity(BannerDO bannerDO, Banner banner) {
    banner.setTitle(bannerDO.getTitle());
    banner.setDescription(bannerDO.getDescription());
    banner.setDismissible(bannerDO.getDismissible());
    banner.setColor(bannerDO.getColor());

    return banner;
  }
}
