package utils;

import org.junit.jupiter.api.BeforeAll;

import java.io.File;

public class TestHooks {

    @BeforeAll
    static void cleanAllureResults() {
        File allureResults = new File("allure-results");
        if (allureResults.exists()) {
            for (File file : allureResults.listFiles()) {
                file.delete();
            }
        }
        System.out.println("✅ Cleaned allure-results folder before test execution.");
    }
}
