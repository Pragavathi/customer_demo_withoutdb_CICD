package com.example.customerdemowithoutdb.Test;

import org.json.JSONException;
import org.json.JSONObject;
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

public class CustomerAPITest {

    String baseUrl = "http://localhost:9095/api";
    RequestSpecification apiRequest;
    String id;
    String name;
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

        // Configure RestAssured to log all requests/responses to the file
        RestAssured.config = RestAssured.config()
                .logConfig(LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails()
                        .defaultStream(logStream));

        apiRequest = given().baseUri(baseUrl);
    }

    @Test(priority = 0)
    void getAllCustomers() {
        apiRequest
            .log().all() // log request to file
            .when().get("/customers")
            .then()
            .statusCode(200)
            .log().all(); // log response to file
    }

    @Test(priority = 1)
    void postCustomer() throws JSONException {
        String requestBody = jsonObject.getJSONObject("createCustomer").toString();

      response = apiRequest
            .header("Content-Type", "application/json")
            .body(requestBody)
            .log().all() // log request
            .when().post("/customers");

        response.then().statusCode(200).log().all(); // log response

        // Extract id and name using .path()
        id = response.path("id").toString();
        name = response.path("name");

        System.out.println("Created Customer ID: " + id + ", Name: " + name);
    }

    @Test(priority = 2, dependsOnMethods = "postCustomer")
    void getCustomerById() {
        if (id == null) throw new IllegalStateException("Customer ID is null. POST must run first.");

        apiRequest
            .log().all()
            .when().get("/customers/{id}", id)
            .then()
            .statusCode(200)
            .log().all();
    }

    @Test(priority = 3, dependsOnMethods = "postCustomer")
    void updateCustomer() throws JSONException {
        if (id == null) throw new IllegalStateException("Customer ID is null. POST must run first.");

        String requestBody = jsonObject.getJSONObject("updateCustomer").toString();

     response = apiRequest
            .header("Content-Type", "application/json")
            .body(requestBody)
            .pathParam("id", id)
            .log().all() // log request
            .when().put("/customers/{id}");

        response.then().statusCode(200).log().all(); // log response

        // Update local id and name
        id = response.path("id").toString();
        name = response.path("name");

        System.out.println("Updated Customer ID: " + id + ", Name: " + name);
    }

    @Test(priority = 4, dependsOnMethods = "updateCustomer")
    void getUpdatedCustomer() {
        if (id == null) throw new IllegalStateException("Customer ID is null. POST must run first.");

        apiRequest
            .log().all()
            .when().get("/customers/{id}", id)
            .then()
            .statusCode(200)
            .log().all();
    }
    
    @Test(priority = 5)
    void deleteCustomer() {
    	 if (id == null) throw new IllegalStateException("Customer ID is null. POST must run first.");
    	 
    	 apiRequest.pathParam("id",id.toString())         
    	   .log().all()
         .when().delete("/customers/{id}")
         .then()
         .statusCode(200)
         .log().all();
    }
}
