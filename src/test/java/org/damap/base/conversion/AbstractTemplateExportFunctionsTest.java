package org.damap.base.conversion;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.damap.base.TestProfiles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(TestProfiles.DefaultProfile.class)
class AbstractTemplateExportFunctionsTest {

  @Inject Instance<AbstractTemplateExportFunctions> documentConversionServices;

  @Inject Instance<TemplateFileBrokerService> templateFileBrokerServices;

  @Test
  void testLoadTemplate() throws Exception {
    for (AbstractTemplateExportFunctions documentConversionService : documentConversionServices) {
      XWPFDocument document =
          documentConversionService.loadTemplate(
              templateFileBrokerServices.get().loadScienceEuropeTemplate(), "[", "]");
      Assertions.assertNotNull(document);

      // testing multiple variable handling
      String result1 = documentConversionService.joinWithComma(testTwoVariable());
      String result2 = documentConversionService.joinWithComma(testThreeVariable());
      String result3 = documentConversionService.joinWithCommaAnd(testTwoVariable());
      String result4 = documentConversionService.joinWithCommaAnd(testThreeVariable());
      Assertions.assertEquals(twoVariables(), result1);
      Assertions.assertEquals(threeVariables(), result2);
      Assertions.assertEquals(twoVariablesAnd(), result3);
      Assertions.assertEquals(threeVariablesAnd(), result4);
    }
  }

  private List<String> testTwoVariable() {
    List<String> variables = Arrays.asList("var1", "var2");
    return variables;
  }

  private List<String> testThreeVariable() {
    List<String> variables = Arrays.asList("var1", "var2", "var3");
    return variables;
  }

  private String twoVariables() {
    return "var1, var2";
  }

  private String threeVariables() {
    return "var1, var2, var3";
  }

  private String twoVariablesAnd() {
    return "var1 and var2";
  }

  private String threeVariablesAnd() {
    return "var1, var2 and var3";
  }
}
