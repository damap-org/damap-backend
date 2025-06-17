package org.damap.base.rest.dmp.mapper;

import lombok.experimental.UtilityClass;
import org.damap.base.domain.ExternalStorage;
import org.damap.base.rest.dmp.domain.ExternalStorageDO;

/** ExternalStorageDOMapper class. */
@UtilityClass
public class ExternalStorageDOMapper {

  /**
   * mapEntityToDO.
   *
   * @param storage a {@link org.damap.base.domain.ExternalStorage} object
   * @param storageDO a {@link org.damap.base.rest.dmp.domain.ExternalStorageDO} object
   * @return a {@link org.damap.base.rest.dmp.domain.ExternalStorageDO} object
   */
  public ExternalStorageDO mapEntityToDO(ExternalStorage storage, ExternalStorageDO storageDO) {
    storageDO.setUrl(storage.getUrl());
    storageDO.setBackupFrequency(storage.getBackupFrequency());
    storageDO.setStorageLocation(storage.getStorageLocation());
    storageDO.setBackupLocation(storage.getBackupLocation());
    storageDO.setIsManagedInternally(storage.getIsManagedInternally());

    return storageDO;
  }

  /**
   * mapDOtoEntity.
   *
   * @param storageDO a {@link org.damap.base.rest.dmp.domain.ExternalStorageDO} object
   * @param storage a {@link org.damap.base.domain.ExternalStorage} object
   * @return a {@link org.damap.base.domain.ExternalStorage} object
   */
  public ExternalStorage mapDOtoEntity(ExternalStorageDO storageDO, ExternalStorage storage) {
    storage.setUrl(storageDO.getUrl());
    storage.setBackupFrequency(storageDO.getBackupFrequency());
    storage.setStorageLocation(storageDO.getStorageLocation());
    storage.setBackupLocation(storageDO.getBackupLocation());
    storage.setIsManagedInternally(storageDO.getIsManagedInternally());

    return storage;
  }
}
