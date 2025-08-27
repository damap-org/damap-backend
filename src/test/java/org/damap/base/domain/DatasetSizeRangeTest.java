package org.damap.base.domain;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.damap.base.TestProfiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(TestProfiles.DefaultProfile.class)
class DatasetSizeRangeTest {

  @Test
  void testGetLabelForSize() {
    Object[][] testCases = {
      {0L, "< 100 MB"},
      {99_999_999L, "< 100 MB"},
      {100_000_000L, "100 - 1000 MB"},
      {999_999_999L, "100 - 1000 MB"},
      {1_000_000_000L, "1 - 5 GB"},
      {4_999_999_999L, "1 - 5 GB"},
      {5_000_000_000L, "5 - 20 GB"},
      {19_999_999_999L, "5 - 20 GB"},
      {20_000_000_000L, "20 - 50 GB"},
      {49_999_999_999L, "20 - 50 GB"},
      {50_000_000_000L, "50 - 100 GB"},
      {99_999_999_999L, "50 - 100 GB"},
      {100_000_000_000L, "100 - 500 GB"},
      {499_999_999_999L, "100 - 500 GB"},
      {500_000_000_000L, "500 - 1000 GB"},
      {999_999_999_999L, "500 - 1000 GB"},
      {1_000_000_000_000L, "1 - 5 TB"},
      {4_999_999_999_999L, "1 - 5 TB"},
      {5_000_000_000_000L, "5 - 10 TB"},
      {9_999_999_999_999L, "5 - 10 TB"},
      {10_000_000_000_000L, "10 - 100 TB"},
      {99_999_999_999_999L, "10 - 100 TB"},
      {100_000_000_000_000L, "100 - 500 TB"},
      {499_999_999_999_999L, "100 - 500 TB"},
      {500_000_000_000_000L, "500 - 1000 TB"},
      {999_999_999_999_999L, "500 - 1000 TB"},
      {1_000_000_000_000_000L, "> 1 PB"},
      {Long.MAX_VALUE, "> 1 PB"},
      {-1L, "I don't know yet"},
      {-1000L, "Unknown size"}
    };

    for (Object[] testCase : testCases) {
      long size = (long) testCase[0];
      String expectedLabel = (String) testCase[1];
      Assertions.assertEquals(expectedLabel, DatasetSizeRange.getLabelForSize(size));
    }
  }
}
