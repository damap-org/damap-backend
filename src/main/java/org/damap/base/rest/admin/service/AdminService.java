package org.damap.base.rest.admin.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.Banner;
import org.damap.base.domain.InstanceConfig;
import org.damap.base.repo.DamapUserRepo;
import org.damap.base.repo.InstanceConfigRepo;
import org.damap.base.rest.admin.domain.BannerDO;
import org.damap.base.rest.admin.domain.InstanceConfigDO;
import org.damap.base.rest.admin.mapper.BannerDOMapper;

@ApplicationScoped
@JBossLog
public class AdminService {

  @Inject
  InstanceConfigRepo instanceConfigRepo;

  public BannerDO getAppBanner() {
    Banner banner = Banner.findAll().firstResult();
    if (banner == null) {
      return null;
    }
    return BannerDOMapper.mapEntityToDO(banner, new BannerDO());
  }

  @Transactional
  public BannerDO createAppBanner(@Valid BannerDO bannerDO) {
    long existingBannerCount = Banner.count();
    if (existingBannerCount > 0) {
      throw new IllegalStateException("There is already a banner in the database");
    }

    Banner banner = BannerDOMapper.mapDOtoEntity(bannerDO, new Banner());
    banner.persist();

    return bannerDO;
  }

  @Transactional
  public BannerDO updateAppBanner(@Valid BannerDO bannerDO) {
    Banner banner = Banner.findAll().firstResult();
    if (banner == null) {
      throw new IllegalStateException("There is no banner in the database");
    }

    banner = BannerDOMapper.mapDOtoEntity(bannerDO, banner);
    banner.persist();

    return bannerDO;
  }

  @Transactional
  public void deleteAppBanner() {
    Banner banner = Banner.findAll().firstResult();
    if (banner == null) {
      throw new IllegalStateException("There is no banner in the database");
    }

    banner.delete();
  }

  public InstanceConfigDO getInstanceConfig() {
    InstanceConfig instanceConfig = this.instanceConfigRepo.getConfig();
    if (instanceConfig == null) {
      throw new IllegalStateException("There is no instance config in the database");
    }

      return InstanceConfigDO.builder()
            .publicAvailable(instanceConfig.getPublicAvailable())
            .build();
  }

  @Transactional
  public InstanceConfigDO updateInstanceConfig(@Valid InstanceConfigDO updatedInstanceConfig) {
    InstanceConfig instanceConfig = this.instanceConfigRepo.getConfig();
    if (instanceConfig == null) {
      throw new IllegalStateException("There is no instance config in the database");
    }

    instanceConfig.setPublicAvailable(updatedInstanceConfig.isPublicAvailable());


    return InstanceConfigDO.builder()
            .publicAvailable(instanceConfig.getPublicAvailable())
            .build();
  }
}
