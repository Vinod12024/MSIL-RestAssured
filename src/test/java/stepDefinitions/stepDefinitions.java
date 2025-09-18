package stepDefinitions;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import io.restassured.response.Response;
import static org.hamcrest.Matchers.equalTo;
import static utils.TestData.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class stepDefinitions{
    private static final Logger logger = LogManager.getLogger(stepDefinitions.class);

    Response response;
    static String authId;
    static String token;
    static String variant;

    @Given("Auth API payload")
    public void i_have_ath_api_payload(){
        RestAssured.baseURI = BASE_URI;
        logger.info("Base URI set to: {}", BASE_URI);
    }

    @When("I send a post request to API")
    public void i_send_post_request_to_API(){
        logger.info("Sending Auth API request with mobileNumber={}, countryCode={}, merchantId={}, city={}", 
                     MOBILE_NUMBER, COUNTRY_CODE, MARCHANT_ID, OPERATING_CITY);

        response = given().header("Content-Type", "application/json")
        .body("{"
        + "\"mobileNumber\": \""+MOBILE_NUMBER+"\","
        + "\"mobileCountryCode\": \"" + COUNTRY_CODE + "\","
        + "\"merchantId\": \""+ MARCHANT_ID +"\","
        + "\"merchantOperatingCity\": \"" +OPERATING_CITY+ "\""
        + "}").when().post("/auth");

        authId = response.jsonPath().getString("authId");
        logger.info("Auth API Response: {}", response.getBody().asPrettyString());
        logger.info("Extracted Auth ID: {}", authId);
    }

    @Then("auth id should be successfully created and the status code is {int}")
    public void validate_status_code(int expectedStatusCode) {
        logger.info("Validating Auth API status code. Expected={}, Actual={}", 
                    expectedStatusCode, response.getStatusCode());
        assertEquals(expectedStatusCode, response.getStatusCode(), "Status code mismatch!");
    }

    @Then("{string} in response body is {string}")
    public void validate_response_body(String key, String value) {
        logger.info("Validating response body key={} expectedValue={}", key, value);
        response.then().body(key, equalTo(value));
    }

    @When("I send a verify request with otp {string} and deviceToken {string}")
    public void i_send_verify_request(String otp, String deviceToken) {
        String verifyEndpoint = "/auth/" + authId + "/verify";
        logger.info("Sending Verify API request with otp={} and deviceToken={}", otp, deviceToken);

        response = given()
            .header("Content-Type", "application/json;charset=utf-8")
            .body("{"
                + "\"otp\": \"" + otp + "\","
                + "\"deviceToken\": \"" + deviceToken + "\""
                + "}")
            .when().post(verifyEndpoint);

        logger.info("Verify API Response: {}", response.getBody().asPrettyString());
        token = response.jsonPath().getString("token");
        logger.info("Extracted token: {}", token);
    }

    @Then("verify status code should be {int}")
    public void verify_status_code_should_be(int expectedStatusCode) {
        logger.info("Validating Verify API status code. Expected={}, Actual={}", 
                    expectedStatusCode, response.getStatusCode());
        assertEquals(expectedStatusCode, response.getStatusCode(), "Verify API Status code mismatch!");
    }

    @When("I send driver location with lat {double} and lon {double}")
    public void i_send_driverloaction_request(Double lat, Double lon){
        logger.info("Sending Driver Location API request with lat={}, lon={}", lat, lon);

        response = given().header("Content-Type", "application/json;charaset=utf-8").header("token", token)
              .header("mId", MARCHANT_ID)
              .header("dm", "ONLINE")
              .header("vt", "SEDAN")
              .body("[{"
              + "\"pt\": {\"lat\": " + lat + ", \"lon\": " + lon + "},"
              + "\"ts\": \"2025-09-12T08:32:54+00:00\""
              + "}]")
              .when().post("/driver/location");

        logger.info("Driver Location API Response: {}", response.getBody().asPrettyString());
    }

    @Then("driver location status code should be {int}")
    public void driver_location_status_code_should_be(int expectedStatusCode) {
        logger.info("Validating Driver Location API status code. Expected={}, Actual={}", 
                    expectedStatusCode, response.getStatusCode());
        assertEquals(expectedStatusCode, response.getStatusCode(),"Driver Location API Status code mismatch!" );
    }

    @When("I send setActivity API")
    public void i_send_setActivity_request(){
        logger.info("Sending SetActivity API request with mode=ONLINE, active=true");

        response = given()
            .header("Content-Type", "application/json")
            .header("token", token)
            .queryParam("active", true)
            .queryParam("mode", "\"ONLINE\"")   
            .when()
            .post("/driver/setActivity");

        logger.info("SetActivity API Response: {}", response.getBody().asPrettyString());
        logger.info("Status Code: {}", response.getStatusCode());
    }

    @Then("setActivity status code should be {int}")
    public void setActivity_status_code_should_be(int expectedStatusCode){
        logger.info("Validating SetActivity API status code. Expected={}, Actual={}", 
                    expectedStatusCode, response.getStatusCode());
        assertEquals(expectedStatusCode, response.getStatusCode(), "setActivity API Status code mismatch!");
    }

    @When("I Send Driver Profile API")
    public void i_send_driver_profile(){
        logger.info("Sending Driver Profile API request");
        response = given().header("token", token).when().get("/driver/profile");
        logger.info("Driver Profile API Response: {}", response.getBody().asPrettyString());
    }

    @Then ("Store the variant")
    public void store_the_variant(){
        variant = response.jsonPath().getString("variant");
        logger.info("Extracted variant: {}", variant);
    }
}
