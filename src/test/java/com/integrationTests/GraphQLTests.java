package com.integrationTests;

import com.mutations.User;
import com.queries.GrahpQLQueries;
import io.restassured.http.ContentType;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class GraphQLTests {

    String user_id = "xyz";
    /**
     * Verify existing data with sample query
     */
    @Test
    public void verifyCompanyData_checkCeo_shouldBeElonMusk() throws IOException {
        String grapdhqlquery = getGraphQLFile("src/test/resources/queries/getCompanyDetails");
        GrahpQLQueries query = new GrahpQLQueries();
        query.setQuery(grapdhqlquery);

        given().
                contentType(ContentType.JSON).
                body(query).
                when().
                post("https://api.spacex.land/graphql/").
                then().
                assertThat().
                statusCode(200).
                and().
                body("data.company.ceo", is("Elon Musk")).
                body("data.company.name", is("SpaceX"));
    }

    /**
     * Verify user data upon selecting user with ID for newly added user
     */
    @Test
    public void selectUserById_verifyReturnedData() throws IOException {
        String addUserquery = getGraphQLFile("src/test/resources/queries/addUser");
        GrahpQLQueries add_user_query = new GrahpQLQueries();
        add_user_query.setQuery(addUserquery);

        User myUser = new User(
                UUID.randomUUID(),
                "Darth Vader",
                "Space Odessy",
                "@DarthVader"
        );

        add_user_query.setVariables(myUser);
        String user_id = given().
                contentType(ContentType.JSON).
                body(add_user_query).
                when().
                post("https://api.spacex.land/graphql/").
                then().
                log().
                body().
                assertThat().
                statusCode(200).
                and().
                body("data.insert_users.returning[0].id", equalTo(myUser.getId().toString())).
                body("data.insert_users.returning[0].name", equalTo(myUser.getName())).
                body("data.insert_users.returning[0].rocket", equalTo(myUser.getRocket())).
                log().body().
                extract().path("data.insert_users.returning[0].id");


        GrahpQLQueries select_user_query = new GrahpQLQueries();
        select_user_query.setQuery("{ users(where: {id: {_eq: \""+user_id+"\"}}) { id name rocket timestamp twitter } }");

        given().
                contentType(ContentType.JSON).
                body(select_user_query).
                when().
                post("https://api.spacex.land/graphql/").
                then().
                assertThat().
                statusCode(200).
                and().
                body("data.users[0].name", is(myUser.getName())).
                body("data.users[0].rocket", is(myUser.getRocket())).
                body("data.users[0].twitter", is(myUser.getTwitter()));
    }

    /**
     * Add a new user -  verify data upon retrun
     */
    @Test
    public void addUser_checkReturnedData_shouldCorrespondToDataSent(){
        GrahpQLQueries query = new GrahpQLQueries();
        query.setQuery("mutation insert_users ($id: uuid!, $name: String!, $rocket: String!) { insert_users(objects: {id: $id, name: $name, rocket: $rocket}) { returning { id name rocket } } }");

        User myUser = new User(
                UUID.randomUUID(),
                "Darth Vader",
                "Space Odessy"
        );

        query.setVariables(myUser);

        String user_id = given().
                contentType(ContentType.JSON).
                body(query).
                when().
                post("https://api.spacex.land/graphql/").
                then().
                assertThat().
                statusCode(200).
                and().
                body("data.insert_users.returning[0].id", equalTo(myUser.getId().toString())).
                body("data.insert_users.returning[0].name", equalTo(myUser.getName())).
                body("data.insert_users.returning[0].rocket", equalTo(myUser.getRocket())).log().body().extract().path("data.insert_users.returning[0].id");


        user_id="\""+user_id+"\"";
        query.setQuery("{users_by_pk(id: "+user_id+") { id name rocket } }");
        System.out.println(query.getQuery());
        given().
                contentType(ContentType.JSON).
                body(query).
                when().
                post("https://api.spacex.land/graphql/").
                then().
                assertThat().
                statusCode(200).
                and().
                body("data.users_by_pk.name", equalTo(myUser.getName())).
                body("data.users_by_pk.rocket", equalTo(myUser.getRocket())).
                log().body();
    }

    /**
     * update user data for an exisitng user - verify updated data
     */
    @Test
    public void updateUser_checkReturnedData(){
        // mutation { update_users(where: {id: {_eq: ""}}, _set: {name: "Tyrone") }

        GrahpQLQueries insert_query = new GrahpQLQueries();
        insert_query.setQuery("mutation insert_users ($id: uuid!, $name: String!, $rocket: String!) { insert_users(objects: {id: $id, name: $name, rocket: $rocket}) { returning { id name rocket } } }");

        User myUser = new User(
                UUID.randomUUID(),
                "Darth Vader",
                "Space Odessy"
        );

        insert_query.setVariables(myUser);

        String user_id = given().
                contentType(ContentType.JSON).
                body(insert_query).
                when().
                post("https://api.spacex.land/graphql/").
                then().
                assertThat().
                statusCode(200).
                and().
                body("data.insert_users.returning[0].id", equalTo(myUser.getId().toString())).
                body("data.insert_users.returning[0].name", equalTo(myUser.getName())).
                body("data.insert_users.returning[0].rocket", equalTo(myUser.getRocket())).log().body().extract().path("data.insert_users.returning[0].id");

        GrahpQLQueries update_query = new GrahpQLQueries();
        String name = "newname";
        update_query.setQuery("mutation {update_users(where: {id: {_eq: \""+user_id+"\"}}, _set: {name: \""+name+"\"}) {affected_rows}}");
        System.out.println(update_query.getQuery());

        given().
                contentType(ContentType.JSON).
                body(update_query).
                when().
                post("https://api.spacex.land/graphql/").
                then().
                assertThat().
                statusCode(200).log().body();
    }

    /**
     * Delete an existing user - verify if the the id still exists
     */
    @Test
    public void deleteUser_checkAffectedRows(){

        addUser();

        GrahpQLQueries delete_query = new GrahpQLQueries();
        delete_query.setQuery("mutation { delete_users(where: {id: {_eq: \""+user_id+"\"}}) { affected_rows } }");

        given().
                contentType(ContentType.JSON).
                body(delete_query).
                when().
                post("https://api.spacex.land/graphql/").
                then().
                assertThat().
                statusCode(200).
                log().body();
    }


    public void addUser(){
        GrahpQLQueries insert_query = new GrahpQLQueries();
        insert_query.setQuery("mutation insert_users ($id: uuid!, $name: String!, $rocket: String!) { insert_users(objects: {id: $id, name: $name, rocket: $rocket}) { returning { id name rocket } } }");

        User myUser = new User(
                UUID.randomUUID(),
                "Darth Vader",
                "Space Odessy"
        );

        insert_query.setVariables(myUser);

         user_id = given().
                contentType(ContentType.JSON).
                body(insert_query).
                when().
                post("https://api.spacex.land/graphql/").
                then().
                assertThat().
                statusCode(200).
                and().
                body("data.insert_users.returning[0].id", equalTo(myUser.getId().toString())).
                body("data.insert_users.returning[0].name", equalTo(myUser.getName())).
                body("data.insert_users.returning[0].rocket", equalTo(myUser.getRocket())).log().body().extract().path("data.insert_users.returning[0].id");

        GrahpQLQueries select_user_query = new GrahpQLQueries();
        select_user_query.setQuery("{ users(where: {id: {_eq: \""+user_id+"\"}}) { id name rocket timestamp twitter } }");

        given().
                contentType(ContentType.JSON).
                body(select_user_query).
                when().
                post("https://api.spacex.land/graphql/").
                then().
                assertThat().
                statusCode(200).
                and().
                body("data.users[0].name", is(myUser.getName())).
                body("data.users[0].rocket", is(myUser.getRocket())).
                body("data.users[0].twitter", is(myUser.getTwitter()));
    }

    public String getGraphQLFile(String filepath) throws IOException {
        String data = "";
        data = new String(Files.readAllBytes(Paths.get(filepath)));
        return data;
    }
}
