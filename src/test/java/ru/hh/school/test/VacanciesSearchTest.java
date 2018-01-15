package ru.hh.school.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

public class VacanciesSearchTest {

    private final static int default_page_size = 20;
    private final static String search_word = "haskell";

    @BeforeClass
    public static void setUp(){
        RestAssured.baseURI = "https://api.hh.ru";
    }

    @Test
    public void testApiIsAvailable(){
        given().when().get("/vacancies").then().statusCode(200);
    }

    @Test
    public void testAllBaseFieldsPresentInResponse(){
        given().when().get("/vacancies").then()
                .body("$", hasKey("items"))
                .body("$", hasKey("clusters"))
                .body("$", hasKey("pages"))
                .body("$", hasKey("arguments"))
                .body("$", hasKey("found"))
                .body("$", hasKey("alternate_url"))
                .body("$", hasKey("per_page"))
                .body("$", hasKey("page"));
    }

    @Test
    public void testAllBaseFieldsPresentInItemsListInResponse(){
        JsonPath jsonPathResponse = given().when().get("/vacancies").jsonPath();
        assertTrue(jsonPathResponse.getList("items", Map.class).stream().allMatch(item ->
            item.containsKey("id") &&
            item.containsKey("premium") &&
            item.containsKey("address") &&
            item.containsKey("alternate_url") &&
            item.containsKey("apply_alternate_url") &&
            item.containsKey("salary") &&
            item.containsKey("department") &&
            item.containsKey("name") &&
            item.containsKey("url") &&
            item.containsKey("published_at") &&
            item.containsKey("relations") &&
            item.containsKey("employer") &&
            item.containsKey("response_letter_required") &&
            item.containsKey("type") &&
            item.containsKey("archived")
        ));
    }

    @Test
    public void testDefaultPageSize(){
        given().when().get("/vacancies").then().body("per_page", is(default_page_size));
    }

    @Test
    public void testItemsSizeEqualToPerPageValue(){
        JsonPath jsonPathResponse = given().when().get("/vacancies").jsonPath();
        assertEquals(jsonPathResponse.getInt("per_page"), jsonPathResponse.getInt("items.size()"));
    }

    @Test
    public void testSearchTextInVacancyNames(){
        JsonPath jsonPathResponse = given()
                .queryParam("text", "NAME:" + search_word)
                .get("/vacancies").jsonPath();
        assertTrue(jsonPathResponse.getList("items.name", String.class)
                .stream().allMatch(s -> s.toLowerCase().contains(search_word)));
    }

    @Test
    public void testSearchTextIsIgnoringCase() {
        JsonPath upperCaseResponse = given()
                .queryParam("text", "NAME:" + search_word.toUpperCase())
                .get("/vacancies").jsonPath();
        JsonPath lowerCaseResponse = given()
                .queryParam("text", "NAME:" + search_word.toLowerCase())
                .get("/vacancies").jsonPath();
        assertEquals(upperCaseResponse.getInt("items.size()"), lowerCaseResponse.getInt("items.size()"));
        for(int i = 0; i < upperCaseResponse.getInt("items.size()"); i++){
            assertEquals(upperCaseResponse.getList("items.id").get(i), lowerCaseResponse.getList("items.id").get(i));
        }
    }

    @Test
    public void testEmptyTextInSearchActingLikeSearchWithNoTextParameter() {
        JsonPath responseWithEmptyText = given()
                .queryParam("text", "")
                .get("/vacancies").jsonPath();
        JsonPath responseWithoutText = given()
                .get("/vacancies").jsonPath();
        assertEquals(responseWithEmptyText.getInt("found"), responseWithoutText.getInt("found"));
    }

    @Test
    public void testTextInSearchIsNotPartOfWord() {
        JsonPath wholeWordResponse = given()
                .queryParam("text", "NAME:" + search_word)
                .get("/vacancies").jsonPath();
        JsonPath partWordResponse = given()
                .queryParam("text", "NAME:" + search_word.substring(0,4))
                .queryParam("search_field", "name")
                .get("/vacancies").jsonPath();
        assertFalse(wholeWordResponse.getInt("found") <= partWordResponse.getInt("found") );
    }

    @Test
    public void testTextInSearchWithAsteriskIsPartOfWord() {
        JsonPath wholeWordResponse = given()
                .queryParam("text", "NAME:" + search_word)
                .get("/vacancies").jsonPath();
        JsonPath partWordResponse = given()
                .queryParam("text", "NAME:" + search_word.substring(0,4) + "*")
                .get("/vacancies").jsonPath();
        assertTrue(wholeWordResponse.getInt("found") <= partWordResponse.getInt("found") );
    }

    @Test
    public void testVeryLongTextInSearchShouldActLikeNoTextInSearch() {
        JsonPath veryLongTextResponse = given()
                .queryParam("text", String.join("", Collections.nCopies(100, "asdf")))
                .get("/vacancies").jsonPath();
        JsonPath responseWithoutText = given()
                .get("/vacancies").jsonPath();
        assertEquals(veryLongTextResponse.getInt("found"), responseWithoutText.getInt("found"));
    }


}
