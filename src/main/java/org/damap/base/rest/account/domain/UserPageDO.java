package org.damap.base.rest.account.domain;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPageDO {

  List<UserDO> users;

  int totalElements;

  int totalPages;

  int size;

  int number;
}
