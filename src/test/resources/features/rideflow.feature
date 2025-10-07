Feature: Ride Flow End-to-End

  Scenario: Driver and User complete a ride
    Given Driver authenticates and gets token
    When I send a post request to API
    Then auth id should be successfully created and the status code is 200
    When I send a verify request with otp "7891" and deviceToken "5497873d-10ca-42f3-8a32-22de4c916026"
    Then verify status code should be 200
    And Driver sets location with lat 13.289403595108185 and lon 78.29650059483477
    Then driver location status code should be 200
    And Driver sets activity "true"
    Then setActivity status code should be 200
    And Driver fetches profile and stores variant
    Given User authenticates and gets token
    When User sends Auth request with mobile "8978567859"
    When User sends Verify request with otp "7891" and deviceToken "5497873d-10ca-42f3-8a32-22de4c916556"
    Given User searches ride with source and destination
    Given User fetches search results and selects estimate by variant
    And User confirms estimate
    Given Driver fetches ride request
    And Driver offers quote
    And Driver Ride List
    When User gets ride booking details and OTP
    When Driver starts ride with ride id and OTP
    And Driver ends ride with ride id
    And Driver sets activity to "false"
