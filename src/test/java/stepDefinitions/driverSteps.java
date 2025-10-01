package stepDefinitions;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import io.restassured.response.Response;
import static org.hamcrest.Matchers.equalTo;
import static utils.TestData.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class driverSteps{
    private static final Logger logger = LogManager.getLogger(driverSteps.class);

    Response response;
    static String authId;
    static String token;
    static String variant;
    static String driverId;
    static String searchRequestId;
    static String rideId;
    // static String variant;
    @Given("Driver authenticates and gets token")
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
        + "}").when().post("auth");

        authId = response.jsonPath().getString("authId");
        // logger.info("Auth API Response: {}", response.getBody().asPrettyString());
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
        String verifyEndpoint = "auth/" + authId + "/verify";
        logger.info("Sending Verify API request with otp={} and deviceToken={}", otp, deviceToken);

        response = given()
            .header("Content-Type", "application/json;charset=utf-8")
            .body("{"
                + "\"otp\": \"" + otp + "\","
                + "\"deviceToken\": \"" + deviceToken + "\""
                + "}")
            .when().post(verifyEndpoint);

        driverId = response.jsonPath().getString("person.id");
        logger.info("Extracted driverId: {}", driverId);
        token = response.jsonPath().getString("token");
        logger.info("Extracted token: {}", token);
    }

    @Then("verify status code should be {int}")
    public void verify_status_code_should_be(int expectedStatusCode){
        logger.info("Validating Verify API status code. Expected={}, Actual={}", 
                    expectedStatusCode, response.getStatusCode());
        assertEquals(expectedStatusCode, response.getStatusCode(), "Verify API Status code mismatch!");
    }

    @When("Driver sets location with lat {double} and lon {double}")
    public void i_send_driverloaction_request(Double lat, Double lon){
        logger.info("Sending Driver Location API request with lat={}, lon={}", lat, lon);

        String currentTime = ZonedDateTime.now(java.time.ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
        response = given().header("Content-Type", "application/json;charset=utf-8")
              .header("token", token)
              .header("mId", MARCHANT_ID)
              .header("dm", "ONLINE")
              .header("vt", "SEDAN")
              .body("[{"
              + "\"pt\": {\"lat\": " + lat + ", \"lon\": " + lon + "},"
              + "\"ts\": \"" + currentTime + "\""
              + "}]")
              .when().post("driver/location");
        logger.info("Current Time: {}", currentTime);
        logger.info("Driver Location API Response: {}", response.getBody().asString());
    }

    @Then("driver location status code should be {int}")
    public void driver_location_status_code_should_be(int expectedStatusCode) {
        logger.info("Validating Driver Location API status code. Expected={}, Actual={}",
                    expectedStatusCode, response.getStatusCode());
        assertEquals(expectedStatusCode, response.getStatusCode(),"Driver Location API Status code mismatch!" );
    }

    @When("Driver sets activity {string}")
    public void i_send_setActivity_request(String activeStatus){
        logger.info("Sending SetActivity API request with mode=ONLINE, active=true");

        response = given()
            .header("Content-Type", "application/json")
            .header("token", token)
            .queryParam("active", true)
            .queryParam("mode", "\"ONLINE\"")   
            .when()
            .post("driver/setActivity");

        logger.info("SetActivity API Response: {}", response.getBody().asPrettyString());
        logger.info("Status Code: {}", response.getStatusCode());
    }

    @Then("setActivity status code should be {int}")
    public void setActivity_status_code_should_be(int expectedStatusCode){
        logger.info("Validating SetActivity API status code. Expected={}, Actual={}", 
                    expectedStatusCode, response.getStatusCode());
        assertEquals(expectedStatusCode, response.getStatusCode(), "setActivity API Status code mismatch!");
    }
    @When("Driver fetches profile and stores variant")
    public void i_send_driver_profile(){
        logger.info("Sending Driver Profile API request");
        response = given().header("token", token).when().get("driver/profile");
        variant = response.jsonPath().getString("linkedVehicle.variant");
        logger.info("Driver variant:{}", variant);
    }

    @Given("Driver fetches ride request")
    public void driver_fetches_ride_request() throws InterruptedException{
        int retries =5;
        int waitMs =2000;
        for (int i=0; i<retries; i++){
        response = given().header("token", token).log().uri().when().get(BASE_URI+ "driver/nearbyRideRequest");
        // String respBody = response.getBody().asString();
        // logger.info("Driver ride request attempt {}: {}", (i + 1), respBody);
            List<Map<String, Object>> requests = response.jsonPath().getList("searchRequestsForDriver");
            if (requests!=null && !requests.isEmpty()){
                searchRequestId= response.jsonPath().getString("searchRequestsForDriver[0].searchRequestId");
                logger.info("searchRequestId:{}",searchRequestId);
                return;
            }
            Thread.sleep(waitMs);
        }
        throw new RuntimeException("No ride requests found after polling " + retries + " times.");
    }
    @When("Driver offers quote")
    public void driver_offer_quote(){
        response = given().header("token", token).header("Content-Type", "application/json").body("{"
        + "\"searchRequestId\": \"" + searchRequestId + "\","
        + "\"offeredFare\": null"
        + "}").log().uri().when().post(BASE_URI+"driver/searchRequest/quote/offer");
        logger.info("result:{}", response.jsonPath().getString("result"));
    }
    @When("Driver Ride List")
    public void driver_ride_list() throws InterruptedException {
        int retries = 5;
        int waitMs = 2000;
    
        for (int i = 0; i < retries; i++) {
            response = given()
                .header("token", token)
                .queryParam("limit", "1")
                .queryParam("onlyActive", "true")
                .when()
                .get(BASE_URI+"driver/ride/list");
    
            String respBody = response.getBody().asString();
            // logger.info("Driver Ride List attempt {}: {}", (i+1), respBody);
    
            if (response.getStatusCode() == 200 && respBody.contains("\"list\"") && !respBody.contains("Not Found")) {
                List<Map<String, Object>> rides = response.jsonPath().getList("list");
                if (rides != null && !rides.isEmpty()) {
                    rideId = (String) rides.get(0).get("id");
                    logger.info("Extracted rideId: {}", rideId);
                    return;
                }
            }
    
            Thread.sleep(waitMs);
        }
    
        throw new RuntimeException("No rides found in Driver Ride List after polling " + retries + " times.");
    }

@When("Driver starts ride with ride id and OTP")
public void driver_starts_ride_with_ride_id_and_otp() {
    response = given().header("token", token).header("Content-Type", "application/json").pathParam("rideId", rideId).body("{"
    + "\"rideOtp\": \"" + UserSteps.rideotp + "\","
    + "\"point\": {"
    + "    \"lat\": " + UserSteps.sourceLat + ","
    + "    \"lon\": " + UserSteps.sourceLon
    + "}"
    + "}").when().post(BASE_URI+"driver/ride/{rideId}/start");
}
@When("Driver ends ride with ride id")
public void driver_ends_ride_with_ride_id() {
    response = given().header("token", token).header("Content-Type", "application/json").pathParam("rideId", rideId).body("{"
    + "\"point\": {"
    + "    \"lat\": "+ UserSteps.destinationLat +","
    + "    \"lon\": "+ UserSteps.destinationLon +""
    + "}"
    + "}").when().post(BASE_URI+"driver/ride/{rideId}/end");
}
@Then("Driver sets activity to {string}")
public void driver_sets_activity(String string) {
response = given().header("token", token).queryParams("active", "false", "mode", "\"OFFLINE\"").when().post(BASE_URI+"driver/setActivity");
}

}
