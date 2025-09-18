
Feature: Auth and Driver APIs

  Scenario: Verify user login and location update
    Given Auth API payload
    When I send a post request to API
    Then auth id should be successfully created and the status code is 200
    When I send a verify request with otp "7891" and deviceToken "5497873d-10ca-42f3-8a32-22de4c916026"
    Then verify status code should be 200
    When I send driver location with lat 12.9716 and lon 77.5946
    Then driver location status code should be 200
	When I send setActivity API
	Then setActivity status code should be 200
	When I Send Driver Profile API
	Then Store the variant


