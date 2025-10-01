package com.example.customerdemowithoutdb.Test;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CustomerAPITest {

    String baseUrl = "http://localhost:9095/api";
    String id;
    String name;
    Object city;
    JSONObject jsonObject;
    PrintStream logStream;
    String testDataFile = "src/test/resources/testData.json";
    Response response;

    @BeforeClass
    public void setup() throws IOException, JSONException {
        // Load JSON file once
        String testData = new String(Files.readAllBytes(Paths.get(testDataFile)));
        jsonObject = new JSONObject(testData);

        // Create log file stream
        logStream = new PrintStream(new FileOutputStream("CustomerAPITest.log", true));

        // Configure RestAssured logging
        RestAssured.config = RestAssured.config()
                .logConfig(LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails()
                        .defaultStream(logStream));
    }

    /** Helper to return a fresh base request spec every time */
    private RequestSpecification baseRequest() {
        return given().baseUri(baseUrl).log().all();
    }

    @Test(priority = 0)
    void getAllCustomers() {
        baseRequest()
            .when().get("/customers")
            .then().statusCode(200)
            .log().all();
    }

    @Test(priority = 1)
    void postCustomer() throws JSONException {
        String requestBody = jsonObject.getJSONObject("createCustomer").toString();

        response = baseRequest()
            .header("Content-Type", "application/json")
            .body(requestBody)
            .when().post("/customers");

        response.then().statusCode(200).log().all();

        id = response.path("id").toString();
        name = response.path("name");

        System.out.println("Created Customer ID: " + id + ", Name: " + name);
    }

    @Test(priority = 2, dependsOnMethods = "postCustomer")
    void getCustomerById() {
        Assert.assertNotNull(id, "Customer ID should not be null");

        baseRequest()
            .when().get("/customers/{id}", id)
            .then().statusCode(200)
            .log().all();
    }

    @Test(priority = 3, dependsOnMethods = "postCustomer")
    void updateCustomer() throws JSONException {
        Assert.assertNotNull(id, "Customer ID should not be null");

        String requestBody = jsonObject.getJSONObject("updateCustomer").toString();

        response = baseRequest()
            .header("Content-Type", "application/json")
            .body(requestBody)
            .pathParam("id", id)
            .when().put("/customers/{id}");

        response.then().statusCode(200).log().all();

        id = response.path("id").toString();
        name = response.path("name");

        System.out.println("Updated Customer ID: " + id + ", Name: " + name);
    }

    @Test(priority = 4, dependsOnMethods = "updateCustomer")
    void getUpdatedCustomer() {
        Assert.assertNotNull(id, "Customer ID should not be null");

        response = baseRequest()
            .when().get("/customers/{id}", id);

        response.then().statusCode(200).log().all();

        city = response.path("city");
        Assert.assertNotNull(city, "City should not be null after update");
    }

    @Test(priority = 5, dependsOnMethods = "getUpdatedCustomer")
    void getCustomer_Search() {
        Assert.assertNotNull(id, "Customer ID should not be null");
        Assert.assertNotNull(city, "City should not be null");

        baseRequest()
            .queryParam("city", city)
            .when().get("/customers/search")
            .then()
            .statusCode(200)
            .body("id", hasItem(Integer.valueOf(id))) // ensure updated customer is returned
            .log().all();
    }

    @Test(priority = 6, dependsOnMethods = "getUpdatedCustomer")
    void deleteCustomer() {
        Assert.assertNotNull(id, "Customer ID should not be null");

        baseRequest()
            .pathParam("id", id)
            .when().delete("/customers/{id}")
            .then()
            .statusCode(200)
            .log().all();
    }
}
