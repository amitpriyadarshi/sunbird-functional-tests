package org.sunbird.integration.test.badge;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.testng.CitrusParameters;
import javax.ws.rs.core.MediaType;
import org.springframework.http.HttpStatus;
import org.sunbird.common.action.BadgeClassUtil;
import org.sunbird.common.action.ContentStoreUtil;
import org.sunbird.common.action.IssuerUtil;
import org.sunbird.common.action.OrgUtil;
import org.sunbird.common.action.UserUtil;
import org.sunbird.common.util.Constant;
import org.sunbird.integration.test.common.BaseCitrusTestRunner;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SearchAssertionTest extends BaseCitrusTestRunner {

  private static final String BT_TEST_NAME_CREATE_ISSUER_SUCCESS = "testCreateIssuerSuccess";
  private static final String BT_CREATE_ISSUER_TEMPLATE_DIR = "templates/badge/issuer/create";

  private static final String BT_TEST_NAME_CREATE_BADGE_CLASS_SUCCESS =
      "testCreateBadgeClassSuccessWithTypeUser";
  private static final String BT_CREATE_BADGE_CLASS_TEMPLATE_DIR = "templates/badge/class/create";

  private static final String BT_TEST_NAME_CREATE_BADGE_ASSERTION_SUCCESS =
      "testCreateBadgeAssertionSuccessUserWithoutEvidence";
  private static final String BT_CREATE_BADGE_ASSERTION_TEMPLATE_DIR =
      "templates/badge/assertion/create";

  public static final String TEST_NAME_SEARCH_ASSERTION_FAILURE_WITHOUT_FILTER =
      "testSearchAssertionFailureWithoutFilter";

  public static final String TEST_NAME_SEARCH_ASSERTION_SUCCESS_WITH_FILTER_BY_ASSERTION_ID =
      "testSearchAssertionSuccessWithFilterByAssertionId";

  public static final String TEMPLATE_DIR = "templates/badge/assertion/search";

  private String getSearchIssuerUrl() {

    return getLmsApiUriPath(
        "/api/badging/v1/issuer/badge/assertion/search", "/v1/issuer/badge/assertion/search");
  }

  @DataProvider(name = "searchAssertionSuccessDataProvider")
  public Object[][] searchAssertionSuccessDataProvider() {
    return new Object[][] {
      new Object[] {TEST_NAME_SEARCH_ASSERTION_SUCCESS_WITH_FILTER_BY_ASSERTION_ID}
    };
  }

  @DataProvider(name = "searchAssertionFailureDataProvider")
  public Object[][] searchAssertionFailureDataProvider() {

    return new Object[][] {
      new Object[] {TEST_NAME_SEARCH_ASSERTION_FAILURE_WITHOUT_FILTER, HttpStatus.BAD_REQUEST},
    };
  }

  @Test(dataProvider = "searchAssertionFailureDataProvider")
  @CitrusParameters({"testName", "httpStatusCode"})
  @CitrusTest
  public void testSearchAssertionFailure(String testName, HttpStatus httpStatusCode) {

    performPostTest(
        this,
        TEMPLATE_DIR,
        testName,
        getSearchIssuerUrl(),
        REQUEST_JSON,
        MediaType.APPLICATION_JSON,
        false,
        httpStatusCode,
        RESPONSE_JSON);
  }

  @Test(dataProvider = "searchAssertionSuccessDataProvider")
  @CitrusParameters({"testName"})
  @CitrusTest
  public void testSearchAssertionFailure(String testName) {
    beforeTest(testName, true, false, true, true, true);
    performPostTest(
        this,
        TEMPLATE_DIR,
        testName,
        getSearchIssuerUrl(),
        REQUEST_JSON,
        MediaType.APPLICATION_JSON,
        false,
        HttpStatus.OK,
        RESPONSE_JSON);
  }

  private void beforeTest(
      String testName,
      Boolean canCreateUser,
      Boolean canCreateCourse,
      Boolean canCreateIssuer,
      Boolean canCreateBadge,
      Boolean canCreateBadgeAssertion) {

    getTestCase().setName(testName);
    if (canCreateUser) {
      getAuthToken(this, true);
      UserUtil.getUserId(this, testContext);
    }

    if (canCreateCourse) {
      getAuthToken(this, true);
      variable("courseUnitId", ContentStoreUtil.getCourseUnitId());
      variable("resourceId", ContentStoreUtil.getResourceId());
      String courseId = ContentStoreUtil.getCourseId(this, testContext);
      variable("courseId", courseId);
    }

    if (canCreateIssuer) {
      IssuerUtil.createIssuer(
          this,
          testContext,
          config,
          BT_CREATE_ISSUER_TEMPLATE_DIR,
          BT_TEST_NAME_CREATE_ISSUER_SUCCESS,
          HttpStatus.OK);
    }

    if (canCreateBadge) {
      String orgId =
          OrgUtil.getSearchOrgId(this, testContext, System.getenv("sunbird_default_channel"));
      variable("organisationId", orgId);
      BadgeClassUtil.createBadgeClass(
          this,
          testContext,
          config,
          BT_CREATE_BADGE_CLASS_TEMPLATE_DIR,
          BT_TEST_NAME_CREATE_BADGE_CLASS_SUCCESS,
          HttpStatus.OK);
      variable("badgeId", testContext.getVariable(Constant.EXTRACT_VAR_BADGE_ID));
    }

    if (canCreateBadgeAssertion) {
      BadgeClassUtil.createBadgeAssertion(
          this,
          testContext,
          config,
          BT_CREATE_BADGE_ASSERTION_TEMPLATE_DIR,
          BT_TEST_NAME_CREATE_BADGE_ASSERTION_SUCCESS,
          HttpStatus.OK);
      variable("assertionId", testContext.getVariable(Constant.EXTRACT_VAR_ASSERTION_ID));
    }
  }
}
