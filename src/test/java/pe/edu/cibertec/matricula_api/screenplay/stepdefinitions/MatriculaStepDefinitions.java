
package pe.edu.cibertec.matricula_api.screenplay.stepdefinitions;

import io.cucumber.java.en.*;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.model.environment.EnvironmentSpecificConfiguration;
import net.thucydides.model.environment.SystemEnvironmentVariables;

import java.util.List;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MatriculaStepDefinitions {

    private final String url = EnvironmentSpecificConfiguration
            .from(SystemEnvironmentVariables.currentEnvironmentVariables())
            .getProperty("restapi.baseurl");

    private String alumno;
    private String token;
    private Long idMatricula;
    private double totalCotizado;
    private Response respuesta;

    @Given("que el alumno {string} inicia sesion con clave {string}")
    public void iniciarSesion(String codigo, String clave) {
        alumno = codigo;

        respuesta = SerenityRest.given()
                .baseUri(url)
                .contentType("application/json")
                .body(Map.of(
                        "username", codigo,
                        "password", clave
                ))
                .post("/api/auth/login");

        assertThat("El login fallo",
                respuesta.statusCode(), is(200));

        token = respuesta.jsonPath().getString("accessToken");

        assertThat("No se recibio el token",
                token, not(isEmptyOrNullString()));

        System.out.println("Login correcto: " + alumno);
    }

    @When("cotiza el curso {string}")
    public void cotizarCurso(String curso) {
        cotizar(curso);
    }

    private void cotizar(String curso) {
        respuesta = SerenityRest.given()
                .baseUri(url)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of(
                        "codigoAlumno", alumno,
                        "cursos", List.of(curso)
                ))
                .post("/api/matriculas/cotizacion");

        if (respuesta.statusCode() == 200) {
            totalCotizado = respuesta.jsonPath().getDouble("total");
        }

        System.out.println("Cotizacion " + curso
                + " - HTTP " + respuesta.statusCode());
    }

    @Then("la respuesta de matricula es {int}")
    public void verificarCodigo(int codigo) {
        assertThat("Codigo HTTP incorrecto",
                respuesta.statusCode(), is(codigo));
    }

    @And("tiene {int} creditos")
    public void verificarCreditos(int creditos) {
        assertThat(respuesta.jsonPath().getInt("creditos"),
                is(creditos));
    }

    @And("el subtotal es {double}")
    public void verificarSubtotal(double subtotal) {
        assertThat(respuesta.jsonPath().getDouble("subtotal"),
                closeTo(subtotal, 0.01));
    }

    @And("el descuento es {int}")
    public void verificarDescuento(int descuento) {
        assertThat(respuesta.jsonPath().getInt("descuento"),
                is(descuento));
    }

    @And("el total es {double}")
    public void verificarTotal(double total) {
        assertThat(respuesta.jsonPath().getDouble("total"),
                closeTo(total, 0.01));

        System.out.println("Total: S/ " + total);
    }

    @Given("que cotiza el curso {string} por {double}")
    public void cotizarAntesDeMatricular(String curso, double monto) {
        cotizar(curso);

        assertThat(respuesta.statusCode(), is(200));
        assertThat(totalCotizado, closeTo(monto, 0.01));
    }

    @When("se matricula en el curso {string}")
    public void matricularse(String curso) {
        registrar(curso);
    }

    private void registrar(String curso) {
        respuesta = SerenityRest.given()
                .baseUri(url)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of(
                        "codigoAlumno", alumno,
                        "codigoCurso", curso
                ))
                .post("/api/matriculas");

        if (respuesta.statusCode() == 201) {
            idMatricula = respuesta.jsonPath().getLong("id");
        }

        System.out.println("Registro " + curso
                + " - HTTP " + respuesta.statusCode());
    }

    @And("el estado de matricula es {string}")
    public void verificarEstado(String estado) {
        assertThat(respuesta.jsonPath().getString("estado"),
                is(estado));
    }

    @And("el monto de matricula es {double}")
    public void verificarMonto(double monto) {
        assertThat(respuesta.jsonPath().getDouble("monto"),
                closeTo(monto, 0.01));

        assertThat(respuesta.jsonPath().getDouble("monto"),
                closeTo(totalCotizado, 0.01));

        assertThat("No se genero el ID",
                idMatricula, notNullValue());
    }

    @And("cumple el esquema de matricula {string}")
    public void verificarEsquema(String esquema) {
        respuesta.then().assertThat()
                .body(matchesJsonSchemaInClasspath(
                        "schemas/" + esquema
                ));
    }

    @When("paga la matricula con {string}")
    public void pagarMatricula(String metodo) {
        assertThat("No existe ID de matricula",
                idMatricula, notNullValue());

        respuesta = SerenityRest.given()
                .baseUri(url)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of("metodo", metodo))
                .patch("/api/matriculas/" + idMatricula + "/pagar");

        System.out.println("Pago - HTTP "
                + respuesta.statusCode());
    }

    @And("tiene fecha de pago")
    public void verificarFechaPago() {
        String fecha = respuesta.jsonPath().getString("fechaPago");

        assertThat(fecha, not(isEmptyOrNullString()));
    }

    @And("el pago aparece registrado con {string}")
    public void verificarPago(String metodo) {
        Response pagos = SerenityRest.given()
                .baseUri(url)
                .header("Authorization", "Bearer " + token)
                .get("/api/pagos");

        assertThat("No se pudo consultar pagos",
                pagos.statusCode(), is(200));

        List<Map<String, Object>> lista =
                pagos.jsonPath().getList("$");

        Map<String, Object> pago = lista.stream()
                .filter(p -> {
                    Object id = p.get("idMatricula");
                    return id instanceof Number
                            && idMatricula.equals(
                            ((Number) id).longValue());
                })
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No se encontro el pago de la matricula "
                                + idMatricula));

        assertThat(pago.get("metodo").toString(),
                is(metodo));

        assertThat(pago.get("numeroOperacion").toString(),
                matchesPattern("OP-[0-9]{6}"));

        assertThat(
                ((Number) pago.get("monto")).doubleValue(),
                closeTo(totalCotizado, 0.01));

        System.out.println("Pago encontrado: "
                + pago.get("numeroOperacion"));
    }

    @When("intenta matricularse en {string}")
    public void intentarMatricula(String curso) {
        registrar(curso);
    }

    @And("el error de matricula es {string}")
    public void verificarError(String codigo) {
        assertThat(respuesta.jsonPath().getString("codigo"),
                is(codigo));

        assertThat(respuesta.jsonPath().getInt("status"),
                is(respuesta.statusCode()));

        System.out.println("Error esperado: " + codigo);
    }
}
