package stepDefinitions;
import static utils.TestData.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Map;
import java.util.List;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;


public class UserSteps {
    private static final Logger logger = LogManager.getLogger(UserSteps.class);
    Response response;
    String userauthId;
    String usertoken;
    String searchId;
    String estimateId;
    public static String rideotp;
    public static double sourceLat = 13.28874569248109;
    public static double sourceLon= 78.29644181186218;
    public static double destinationLat= 13.281656653247367;
    public static double destinationLon =78.29551758203483;

    @Given("User authenticates and gets token")
    public void user_api_base_is_set() {
        RestAssured.baseURI = USER_BASE_URI;
        logger.info("User Base URI: {}", USER_BASE_URI);
    }

    @When("User sends Auth request with mobile {string}")
    public void user_sends_auth_request_with_mobile(String mobile) {

        response = given()
            .header("Content-Type", "application/json;charset=utf-8")
            .body("{\"mobileNumber\":\"" + mobile + "\",\"mobileCountryCode\":\"+91\",\"merchantId\":\"NAMMA_YATRI\"}")
            .when().post("/auth");

        userauthId = response.jsonPath().getString("authId");
        logger.info("authId of user is:"+userauthId);
    }
    
    @Then("User Auth status should be {int}")
    public void user_auth_status_should_be(Integer status) {
        assertEquals(status, response.getStatusCode());
    }
    
    @When("User sends Verify request with otp {string} and deviceToken {string}")
    public void user_sends_verify_request_with_otp_and_device_token(String otp, String deviceToken) {
        response = given()
            .header("Content-Type", "application/json;charset=utf-8")
            .body("{\"otp\":\"" + otp + "\",\"deviceToken\":\"" + deviceToken + "\"}")
            .when().post("/auth/" + userauthId + "/verify");
            usertoken = response.jsonPath().getString("token");
        logger.info("user token is:" +usertoken);

    }
    
    @Then("User Verify status should be {int}")
    public void user_verify_status_should_be(Integer status) {
        assertEquals(status, response.getStatusCode());
    }

    @Given("User searches ride with source and destination")
    public void user_searches_ride_with_source_and_destination() {
       response = given().header("Content-Type", "application/json").header("token", usertoken).body("{"
       + "\"fareProductType\": \"ONE_WAY\","
       + "\"contents\": {"
       + "    \"origin\": {"
       + "        \"address\": {"
       + "            \"area\": \"8th Block Koramangala\","
       + "            \"areaCode\": \"560047\","
       + "            \"building\": \"Juspay Buildings\","
       + "            \"city\": \"Bangalore\","
       + "            \"country\": \"India\","
       + "            \"door\": \"#444\","
       + "            \"street\": \"18th Main\","
       + "            \"state\": \"Karnataka\""
       + "        },"
       + "        \"gps\": {"
       + "            \"lat\": " + sourceLat + ","
       + "            \"lon\": " + sourceLon
       + "        }"
       + "    },"
       + "    \"destination\": {"
       + "        \"address\": {"
       + "            \"area\": \"6th Block Koramangala\","
       + "            \"areaCode\": \"560047\","
       + "            \"building\": \"Juspay Apartments\","
       + "            \"city\": \"Bangalore\","
       + "            \"country\": \"India\","
       + "            \"door\": \"#444\","
       + "            \"street\": \"18th Main\","
       + "            \"state\": \"Karnataka\""
       + "        },"
       + "        \"gps\": {"
       + "            \"lat\": " + destinationLat + ","
       + "            \"lon\": " + destinationLon 
       + "        }"
       + "    }"
       + "}"
       + "}")
       .when().post("/rideSearch");
       searchId= response.jsonPath().getString("searchId");
       logger.info("serachId is:" +searchId);
    }
    @Given("User fetches search results and selects estimate by variant")
    public void user_fetches_search_results_and_selects_estimate_by_variant() throws InterruptedException {
        int retries = 8;
        int waitMs = 2000;
    
        for (int i = 0; i < retries; i++) {
            response = given()
                    .header("Content-Type", "application/json")
                    .header("token", usertoken)
                    .pathParam("searchId", searchId)
                    .when()
                    .get("/rideSearch/{searchId}/results");    
            List<Map<String, Object>> estimates = response.jsonPath().getList("estimates");
            boolean allJourneysLoaded = response.jsonPath().getBoolean("allJourneysLoaded");
            if (estimates != null && !estimates.isEmpty() && Boolean.TRUE.equals(allJourneysLoaded)) {
                String drivervariant = driverSteps.variant; 
                logger.info("Filtering estimates for driver variant: {}", drivervariant);
    
                // Filter by vehicleVariant
                estimateId = estimates.stream()
                        .filter(e -> drivervariant.equalsIgnoreCase((String) e.get("vehicleVariant")))
                        .map(e -> (String) e.get("id"))
                        .findFirst()
                        .orElse(null);
    
                if (estimateId == null) {
                    logger.warn("No matching estimate found for variant: {}. Retrying...", drivervariant);
                } else {
                    logger.info("Selected estimateId: {}", estimateId);
                    return; 
                }
            }
    
            Thread.sleep(waitMs);
        }
    
        throw new RuntimeException("No estimates found for the driver variant after polling " + retries + " times.");
    }
    
    @Given("User confirms estimate")
    public void user_confirms_estimate(){
        response = given().header("Content-Type", "application/json").header("token", usertoken).log().uri().pathParam("estimateId", estimateId).body("{"
        + "\"customerExtraFee\": null,"
        + "\"autoAssignEnabledV2\": true,"
        + "\"autoAssignEnabled\": true"
        + "}").
        when().post("/estimate/{estimateId}/select2");
        logger.info("Journey Id is:" +response.jsonPath().getString("journeyId"));
       
    }
        @When("User gets ride booking details and OTP")
    public void user_gets_ride_booking_details_and_otp() throws InterruptedException{
        int retries =5;
        int waitMs =2000;

        for(int i=0; i<retries;i++){
            response = given().header("token", usertoken).queryParams("limit", "1", "onlyActive", "true").when().get("/rideBooking/list");
            // String respBody = response.getBody().asString();
            // logger.info("User ride booking list {}:{}", (i+1), respBody);

            List<Map<String, Object>> rides = response.jsonPath().getList("list");
            if(rides !=null && !rides.isEmpty()){
                rideotp = response.jsonPath().getString("list[0].rideList[0].rideOtp");
                logger.info("rideotp={}", rideotp);
                return;
            }
            Thread.sleep(waitMs);
                }
                throw new RuntimeException ("No Active ride found after pooling" +retries+ "times.");
}
}

