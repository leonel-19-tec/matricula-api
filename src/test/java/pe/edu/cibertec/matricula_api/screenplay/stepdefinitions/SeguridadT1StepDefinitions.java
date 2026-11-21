
package pe.edu.cibertec.matricula_api.screenplay.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;

import pe.edu.cibertec.matricula_api.screenplay.tasks.IniciarSesion;
import pe.edu.cibertec.matricula_api.screenplay.tasks.RenovarToken;
import pe.edu.cibertec.matricula_api.screenplay.tasks.CerrarSesion;
import pe.edu.cibertec.matricula_api.screenplay.questions.CodigoError;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class SeguridadT1StepDefinitions {

    private static final String URL = "http://localhost:8080";

    private Actor alumno;

    @Before("@T1P2")
    public void prepararActor() {
        alumno = Actor.named("Alumno");
        alumno.can(CallAnApi.at(URL));
    }

    @Given("que el alumno {string} inicia sesion para seguridad")
    public void iniciarSesion(String codigo) {
        alumno.attemptsTo(
                IniciarSesion.con(codigo, "Cibertec2026")
        );

        Response respuesta = alumno.recall("respuestaSeguridad");

        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat((String) alumno.recall("accessToken"))
                .isNotBlank();
        assertThat((String) alumno.recall("refreshToken"))
                .isNotBlank();
    }

    @When("renueva su token de sesion")
    public void renovarToken() {
        alumno.attemptsTo(RenovarToken.ahora());
    }

    @Then("la respuesta de seguridad es {int}")
    public void verificarRespuesta(int codigo) {
        Response respuesta = alumno.recall("respuestaSeguridad");

        assertThat(respuesta.statusCode()).isEqualTo(codigo);
    }

    @Then("recibe nuevos tokens")
    public void verificarNuevosTokens() {
        String accessAnterior = alumno.recall("accessAnterior");
        String refreshAnterior = alumno.recall("refreshAnterior");

        String accessNuevo = alumno.recall("accessToken");
        String refreshNuevo = alumno.recall("refreshToken");

        assertThat(accessNuevo).isNotBlank();
        assertThat(refreshNuevo).isNotBlank();

        assertThat(accessNuevo).isNotEqualTo(accessAnterior);
        assertThat(refreshNuevo).isNotEqualTo(refreshAnterior);
    }

    @When("intenta reutilizar el token anterior")
    public void reutilizarToken() {
        String refreshAnterior = alumno.recall("refreshAnterior");

        Response respuesta = SerenityRest.given()
                .baseUri(URL)
                .contentType("application/json")
                .body(Map.of("refreshToken", refreshAnterior))
                .post("/api/auth/refresh");

        alumno.remember("respuestaSeguridad", respuesta);
    }

    @When("cierra su sesion")
    public void cerrarSesion() {
        alumno.attemptsTo(CerrarSesion.ahora());
    }

    @When("consulta su perfil con el token anterior")
    public void consultarPerfil() {
        String token = alumno.recall("accessToken");

        Response respuesta = SerenityRest.given()
                .baseUri(URL)
                .header("Authorization", "Bearer " + token)
                .get("/api/auth/perfil");

        alumno.remember("respuestaSeguridad", respuesta);
    }

    @When("el alumno {string} falla su clave 5 veces")
    public void fallarClave(String codigo) {

        for (int intento = 1; intento <= 5; intento++) {

            Response respuesta = SerenityRest.given()
                    .baseUri(URL)
                    .contentType("application/json")
                    .body(Map.of(
                            "username", codigo,
                            "password", "ClaveIncorrecta123"
                    ))
                    .post("/api/auth/login");

            alumno.remember("respuestaSeguridad", respuesta);

            System.out.println(
                    "Intento fallido " + intento
                            + " - HTTP " + respuesta.statusCode()
            );

            assertThat(respuesta.statusCode())
                    .isBetween(400, 499);
        }
    }

    @When("intenta iniciar sesion con su clave correcta")
    public void intentarClaveCorrecta() {

        alumno.attemptsTo(
                IniciarSesion.con("i202610004", "Cibertec2026")
        );
    }

    @When("intenta consultar la lista de alumnos")
    public void consultarAlumnos() {
        String token = alumno.recall("accessToken");

        Response respuesta = SerenityRest.given()
                .baseUri(URL)
                .header("Authorization", "Bearer " + token)
                .get("/api/alumnos");

        alumno.remember("respuestaSeguridad", respuesta);
    }

    @Then("el error de seguridad es {string}")
    public void verificarError(String codigo) {

        Response respuesta = alumno.recall("respuestaSeguridad");

        System.out.println("Respuesta del error:");
        respuesta.prettyPrint();
        assertThat(alumno.asksFor(CodigoError.obtenido()))
                .isEqualTo(codigo);
    }

    @When("consulta su perfil con el token renovado")
    public void consultarPerfilConTokenRenovado() {

        String token = alumno.recall("accessToken");

        Response respuesta = SerenityRest.given()
                .baseUri(URL)
                .header("Authorization", "Bearer " + token)
                .get("/api/auth/perfil");

        alumno.remember("respuestaSeguridad", respuesta);

        System.out.println("Perfil con token renovado - HTTP "
                + respuesta.statusCode());
    }

    @Then("cumple el esquema de error de seguridad {string}")
    public void verificarEsquemaError(String archivo) {

        Response respuesta = alumno.recall("respuestaSeguridad");

        respuesta.then()
                .body(matchesJsonSchemaInClasspath(
                        "schemas/" + archivo
                ));

        System.out.println("Esquema validado: " + archivo);
    }


}
