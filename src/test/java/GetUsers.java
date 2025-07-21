import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import configs.DataBaseHelper;
import configs.WireMockConf;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import models.Users;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;


public class GetUsers {

    private ObjectMapper objectMapper;

    @BeforeSuite
    public void beforeSuite() {
        DataBaseHelper.initializeDatabase();
        DataBaseHelper.clearTestResults();

        WireMockConf.startWireMock();
        WireMockConf.setupStubs();

        RestAssured.baseURI = WireMockConf.BASE_URL;

        objectMapper = new ObjectMapper();
    }

    @AfterSuite
    public void afterSuite() {
        WireMockConf.stopWireMock();
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String status = result.isSuccess() ? "PASSED" : "FAILED";

        DataBaseHelper.saveTestResult(testName, status);
    }

    @Test(dataProvider = "getAllUsersData", dataProviderClass = TestDataProvider.class,
            description = "Getting all users")
    public void getAllUsers(String endpoint, int expectedStatus, int expectedUserCount) {

        Response response = given()
                .when()
                .get(endpoint)
                .then()
                .statusCode(expectedStatus)
                .extract().response();

        try {
            List<Users> users = objectMapper.readValue(response.asString(), new TypeReference<List<Users>>() {});

            Assert.assertNotNull(users, "Users list should not be null");
            Assert.assertEquals(users.size(), expectedUserCount, "Number of users should match expected count");

            // Verify user data structure
            for (Users user : users) {
                Assert.assertNotNull(user.getId(), "User ID should not be null");
                Assert.assertNotNull(user.getName(), "User name should not be null");
                Assert.assertNotNull(user.getAge(), "User age should not be null");
                Assert.assertNotNull(user.getGender(), "User gender should not be null");
                Assert.assertTrue(user.getAge() > 0, "User age should be positive");
            }

        } catch (Exception e) {
            Assert.fail("Failed to parse response JSON: " + e.getMessage());
        }
    }

    @Test(dataProvider = "filterByAgeData", dataProviderClass = TestDataProvider.class,
            description = "Filtering users by age")
    public void filterByAge(String endpoint, int expectedStatus, int expectedCount, String expectedName) {
        Response response = given()
                .when()
                .get(endpoint)
                .then()
                .statusCode(expectedStatus)
                .extract().response();

        try {
            List<Users> users = objectMapper.readValue(response.asString(), new TypeReference<List<Users>>() {});

            Assert.assertEquals(users.size(), expectedCount, "Filtered users count should match expected");

            if (!users.isEmpty()) {
                Users user = users.get(0);
                Assert.assertEquals(user.getName(), expectedName, "User name should match expected");

                // Verify age filter worked correctly
                String ageParam = endpoint.substring(endpoint.indexOf("age=") + 4);
                int expectedAge = Integer.parseInt(ageParam);
                Assert.assertEquals(user.getAge().intValue(), expectedAge, "User age should match filter parameter");
            }

        } catch (Exception e) {
            System.out.println("Failed to parse response JSON: ");
        }

    }

    @Test(dataProvider = "filterByGenderData", dataProviderClass = TestDataProvider.class,
            description = "Filtering users by gender")
    public void filterByGender(String endpoint, int expectedStatus, int expectedCount, String expectedName) {

        Response response = given()
                .when()
                .get(endpoint)
                .then()
                .statusCode(expectedStatus)
                .extract().response();

        try {
            List<Users> users = objectMapper.readValue(response.asString(), new TypeReference<List<Users>>() {});

            Assert.assertEquals(users.size(), expectedCount, "Filtered users count should match expected");

            if (!users.isEmpty()) {
                Users user = users.get(0);
                Assert.assertEquals(user.getName(), expectedName, "User name should match expected");

                // Verify gender filter worked correctly
                String genderParam = endpoint.substring(endpoint.indexOf("gender=") + 7);
                Assert.assertEquals(user.getGender(), genderParam, "User gender should match filter parameter");
            }


        } catch (Exception e) {
            Assert.fail("Failed to parse response JSON: " + e.getMessage());
        }
    }

    @Test(dataProvider = "invalidAgeData", dataProviderClass = TestDataProvider.class,
            description = "Handling of invalid age parameters")
    public void invalidAge(String endpoint, int expectedStatus) {

        Response response = given()
                .when()
                .get(endpoint)
                .then()
                .statusCode(expectedStatus)
                .extract().response();

        Assert.assertTrue(response.asString().contains("error"), "Response should contain error message");
        Assert.assertTrue(response.asString().contains("age"), "Error message should mention age parameter");

    }

    @Test(dataProvider = "invalidGenderData", dataProviderClass = TestDataProvider.class,
            description = "Handling of invalid gender parameters")
    public void invalidGender(String endpoint, int expectedStatus) {

        Response response = given()
                .when()
                .get(endpoint)
                .then()
                .statusCode(expectedStatus)
                .extract().response();

        Assert.assertTrue(response.asString().contains("error"), "Response should contain error message");
        Assert.assertTrue(response.asString().contains("gender"), "Error message should mention gender parameter");

    }

    @Test(dataProvider = "serverErrorData", dataProviderClass = TestDataProvider.class,
            description = "Handling of internal server errors")
    public void internalServerError(String endpoint, int expectedStatus) {

        Response response = given()
                .when()
                .get(endpoint)
                .then()
                .statusCode(expectedStatus)
                .extract().response();

        Assert.assertTrue(response.asString().contains("error"), "Response should contain error message");

    }
}