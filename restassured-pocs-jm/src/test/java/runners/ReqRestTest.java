package runners;

import in.reqres.models.CreateUserRequest;
import in.reqres.models.CreateUserResponse;
import in.reqres.models.User;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.matcher.DetailedCookieMatcher;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.*;
import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ReqRestTest {

    @BeforeEach
    public void setup() {
        RestAssured.requestSpecification = defaultRequestSpecification();
    }


    @Test
    public void postLoginTest() {

        given()
                //introducimos la peticion como un String
                .body("{\n" +
                        "    \"email\": \"eve.holt@reqres.in\",\n" +
                        "    \"password\": \"cityslicka\"\n" +
                        "}")
                //aqui indicamos que es un POST hacia la ruta login de /api
                .post("login")
                //aqui validamos el estado de la respuesta
                .then()
                //primero que el codigo de respuesta sea 200
                .statusCode(HttpStatus.SC_OK)
                //y que dentro del body de la respuesta la variable token no este vacia
                .body("token", notNullValue());


    }

    @Test
    public void getSingleUserTest() {
        given()
                //para las GET no existe body, ya que es una peticion para traer datos
                .get("users/2")
                .then()
                .statusCode(HttpStatus.SC_OK)
                //aqui usamos el "." para acceder a un elemento cuando esta dentro de otro, en este caso "id" esta dentro de "data" y validamos que su valor sea "2"
                .body("data.id", equalTo(2));


    }

    @Test
    public void deleteUserTest() {

        given()
                //DELETE borra el recurso al que apuntamos
                .delete("users/2")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void patchUserTest() {

        String nameUpadate = given()
                .when()
                .body("{\n" +
                        "    \"name\": \"morpheus\",\n" +
                        "    \"job\": \"zion resident\"\n" +
                        "}")
                //PATCH modifica solo una propiedad del recurso
                .patch("users/2")
                .then()
                .statusCode(HttpStatus.SC_OK)
                //con la ayuda de la libreria de "jsonPath" extraemos el campo nombre
                .extract()
                .jsonPath().getString("name");
        //validamos que el campo "name" coincida con la modificacion que hicimos en la peticion
        assertThat(nameUpadate, equalTo("morpheus"));
    }

    @Test
    public void putUserTest() {

        String jobUpadate = given()
                .when()
                .body("{\n" +
                        "    \"name\": \"morpheus\",\n" +
                        "    \"job\": \"zion resident\"\n" +
                        "}")
                //PUT modifica el recurso completo
                .put("users/2")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("job");
        assertThat(jobUpadate, equalTo("zion resident"));
    }

    @Test
    public void getAllUsersTest() {

        Response response = given()
                .get("users?page=2");
        Headers headers = response.getHeaders();
        int statusCode = response.statusCode();
        String body = response.getBody().asString();
        String contentType = response.getContentType();

        assertThat(statusCode, equalTo(HttpStatus.SC_OK));
        System.out.println("Body:" + body);
        System.out.println("Content type:" + contentType);
        System.out.println("Headers:" + headers.toString());
        System.out.println("*********************************");
        System.out.println("*********************************");
        //aqui podemos acceder a los valores de la respuesta para analizar informacion
        System.out.println(headers.get("Content-Type"));
        System.out.println(headers.get("Transfer-Encoding"));

    }

    @Test
    public void getAllUsersTest2() {

        String response = given()
                .when().get("users?page=2").then().extract().body().asString();

        //con "from" de JsonPath podemos extraer informacion de la respuesta
        int page = from(response).get("page");
        int totalPages = from(response).get("total_pages");
        //aqui indicamos el elemento (data) y accedemos a la posicion del valor (id) en este caso [0]
        int idFirtUser = from(response).get("data[0].id");

        System.out.println("page: " + page);
        System.out.println("total page: " + totalPages);
        System.out.println("id first user: " + idFirtUser);


        //extraemos la informacion en una lista, con .findAll traemos todos los campos y creamos una variable (user) que apunte a id y filtre por valores mayores a 10
        List<Map> usersWithIdGreaterThan10 = from(response).get("data.findAll { user -> user.id > 10 }");
        //de la lista que creamos, llamamos al id en la posicion 0, extraemos el campo email y lo convertimos en un String para imprimir
        String email = usersWithIdGreaterThan10.get(0).get("email").toString();

        List<Map> user = from(response).get("data.findAll { user -> user.id > 10 && user.last_name == 'Howell'}");
        int id = Integer.valueOf(user.get(0).get("id").toString());


    }

    @Test
    //aqui convertimos un JSON a un POJO (Plain Old Java Objects) poder extraer los datos de manera mas comoda
    public void createUserTest() {

        String response = given()
                .when()
                .body("{\n" +
                        "    \"name\": \"morpheus\",\n" +
                        "    \"job\": \"leader\"\n" +
                        "}")
                .post("users")
                .then().extract().body().asString();

        //accedemos a la clase con la informacion de la respuesta
        User user = from(response).getObject("", User.class);
        //pedimos los datos que trae la respuesta
        System.out.println(user.getId());
        System.out.println(user.getJob());

    }




    @Test
    public void registerUserTest() {
        CreateUserRequest reqUser = new CreateUserRequest();

        CreateUserResponse userResponse =
           given()
                .when()
                .body(reqUser)
                .post("register")
                .then()
                .spec(defaultResponseSpecification())
                .contentType("application/json; charset=utf-8")
                .extract()
                .body()
                .as(CreateUserResponse.class);

        assertThat(userResponse.getId(), equalTo(4));
        assertThat(userResponse.getToken(), equalTo("QpwL5tke4Pnpja7X4"));
    }






    private RequestSpecification defaultRequestSpecification(){

        List<Filter> filters = new ArrayList<>();
        filters.add(new RequestLoggingFilter());
        filters.add(new ResponseLoggingFilter());

        return new RequestSpecBuilder()
                .setBaseUri("https://reqres.in")
                .setBasePath("/api")
                .addFilters(filters)
                .setContentType(ContentType.JSON).build();
    }

    private RequestSpecification prodRequestSpecification(){

        return new RequestSpecBuilder()
                .setBaseUri("https://prod.reqres.in")
                .setBasePath("/api")
                .setContentType(ContentType.JSON).build();
    }

    private ResponseSpecification defaultResponseSpecification(){
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectContentType(ContentType.JSON)
                .build();
    }

}