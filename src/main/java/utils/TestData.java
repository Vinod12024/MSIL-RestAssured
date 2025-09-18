package utils;

import config.ConfigReader;

public class TestData{

    public static final String BASE_URI = ConfigReader.get("baseURI");
    public static final String MARCHANT_ID = ConfigReader.get("merchantId");
    public static final String MOBILE_NUMBER = ConfigReader.get("mobileNumber");
    public static final String COUNTRY_CODE = ConfigReader.get("countryCode");
    public static final String DEVICE_TOKEN = ConfigReader.get("deviceToken");
    public static final String OPERATING_CITY = ConfigReader.get("merchantOperatingCity");



}