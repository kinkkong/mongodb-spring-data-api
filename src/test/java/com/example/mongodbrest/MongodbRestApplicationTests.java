package com.example.mongodbrest;

import com.example.mongodbrest.model.JsonPathOperation;
import com.google.common.collect.Lists;
import com.sun.istack.NotNull;
import groovy.util.logging.Slf4j;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = MongodbRestApplicationTests.Setup.class)
@ActiveProfiles("test")
@Slf4j
class MongodbRestApplicationTests {

  @Autowired
  protected RestAssuredConfig restAssuredConfig;

  @LocalServerPort
  protected int port;

  @BeforeEach
  public void setUp() {
    RestAssured.config = restAssuredConfig;
    RestAssured.port = port;
    RestAssured.baseURI = "http://localhost";
  }

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @ClassRule
  public static final SpringClassRule springClassRule = new SpringClassRule();

  @ClassRule
  public static GenericContainer mongoContainer =
      new GenericContainer("mongo:4.4-bionic")
          .withEnv("MONGO_INITDB_ROOT_USERNAME", "admin")
          .withEnv("MONGO_INITDB_ROOT_PASSWORD", "pass")
          .withClasspathResourceMapping(
              "mongo/init.script.js",
              "/docker-entrypoint-initdb.d/init.database.js",
              BindMode.READ_ONLY)
          .withStartupTimeout(Duration.ofSeconds(5));

  @Test
  public void shouldGetAllPeople() {
    // @formatter:off
		given()
				.contentType(APPLICATION_JSON_VALUE)
		  .when()
				.log().all()
				.get("/api/people")
			.then()
				.log().all()
				.statusCode(200);
		// @formatter:on
  }

  @Test
  public void shouldUpdateStatus() {
    // @formatter:off
    given()
        .contentType("application/json-patch+json")
        .body(Lists.newArrayList(JsonPathOperation.builder().op("replace").path("/status").value("disabled").build()))
      .when()
        .log().all()
        .patch("/api/people/100")
      .then()
        .log().body()
        .statusCode(200)
        .body("status",is("disabled"));
    // @formatter:on
  }

  @Test
  public void shouldUpdateLastLoginDate() {
    // @formatter:off
    given()
        .contentType("application/json")
        .body("{\"lastLogin\":\"2020-01-01 00:00:00\"}")
      .when()
        .log().all()
        .patch("/api/people/100")
      .then()
        .log().body()
        .statusCode(200)
        .body("lastLogin",is("2020-01-01 00:00:00"));
    // @formatter:on
  }

  @Test
  public void shouldUpdateLastLoginDateAndStatus() {
    // @formatter:off
    given()
        .contentType("application/json-patch+json")
        .body(Lists.newArrayList(
            JsonPathOperation.builder().op("replace").path("/status").value("disabled").build(),
            JsonPathOperation.builder().op("replace").path("/lastLogin").value("2020-01-01 00:00:00").build()))
      .when()
        .log().all()
        .patch("/api/people/100")
      .then()
        .log().body()
        .statusCode(200)
        .body("status",is("disabled"))
        .body("lastLogin",is("2020-01-01 00:00:00"));
    // @formatter:on
  }

  static class Setup implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext context) {

      mongoContainer.start();
      TestPropertyValues.of(
          "spring.data.mongodb.uri=mongodb://admin:pass@"
          + mongoContainer.getContainerIpAddress()
          + ":"
          + mongoContainer.getMappedPort(27017)
          + "/testdb?gssapiServiceName"
          + "=mongodb&authSource"
          + "=admin&retryWrites=true&maxIdleTimeMS=5000")
          .applyTo(context);
    }
  }

}
