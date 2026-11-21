
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
import pe.edu.cibertec.matricula_api.screenplay.task.CalificarMatricula;
import pe.edu.cibertec.matricula_api.screenplay.task.ConsultarHistorial;
import pe.edu.cibertec.matricula_api.screenplay.questions.HistorialAcademico;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class CalificacionStepDefinitions {

    private static final String URL = "http://localhost:8080";

    private Actor alumna;
    private Actor docente;
    private int idMatricula;
    private String codigoAlumno;

    @Before("@T1P3")
    public void prepararActores() {
        alumna = Actor.named("Rosa Medina");
        alumna.can(CallAnApi.at(URL));

        docente = Actor.named("Docente");
        docente.can(CallAnApi.at(URL));
    }

    private void buscarMatricula(String codigo, String curso, String estado) {
        codigoAlumno = codigo;

        alumna.attemptsTo(
                IniciarSesion.con(codigo, "Cibertec2026")
        );

        String token = alumna.recall("accessToken");

        Response respuesta = SerenityRest.given()
                .baseUri(URL)
                .header("Authorization", "Bearer " + token)
                .get("/api/matriculas");

        assertThat(respuesta.statusCode()).isEqualTo(200);

        List<Map<String, Object>> matriculas =
                respuesta.jsonPath().getList("$");

        Map<String, Object> matricula = matriculas.stream()
                .filter(m -> curso.equals(m.get("codigoCurso")))
                .filter(m -> estado.equals(m.get("estado")))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No se encontro la matricula del curso " + curso
                ));

        idMatricula = ((Number) matricula.get("id")).intValue();

        System.out.println("Matricula encontrada del curso " + curso);
    }

    @Given("que la alumna {string} tiene una matricula pagada del curso {string}")
    public void matriculaPagada(String codigo, String curso) {
        buscarMatricula(codigo, curso, "PAGADA");
    }

    @Given("que la alumna {string} tiene una matricula no pagada del curso {string}")
    public void matriculaNoPagada(String codigo, String curso) {
        buscarMatricula(codigo, curso, "REGISTRADA");
    }

    @Given("que el docente {string} inicia sesion")
    public void iniciarDocente(String codigo) {
        docente.attemptsTo(
                IniciarSesion.con(codigo, "Cibertec2026")
        );
    }

    @Given("que inicia sesion un docente que no dicta el curso")
    public void iniciarDocenteNoAutorizado() {
        iniciarDocente("D0002");
    }

    @Given("que el docente correspondiente inicia sesion")
    public void iniciarDocenteCorrespondiente() {
        iniciarDocente("D0001");
    }

    private void registrarNota(String nota) {
        double valor = Double.parseDouble(nota.replace(",", "."));

        docente.attemptsTo(
                CalificarMatricula.con(idMatricula, valor)
        );
    }

    @When("registra la nota {word} en la matricula")
    public void registrarCalificacion(String nota) {
        registrarNota(nota);
    }

    @When("intenta registrar la nota {word} en la matricula")
    public void intentarCalificar(String nota) {
        registrarNota(nota);
    }

    @Then("la calificacion se registra correctamente")
    public void verificarCalificacion() {
        Response respuesta = docente.recall("respuestaCalificacion");

        assertThat(respuesta.statusCode()).isBetween(200, 299);

        alumna.attemptsTo(
                ConsultarHistorial.de(codigoAlumno)
        );
    }

    @Then("el historial cumple el esquema {string}")
    public void validarEsquema(String archivo) {
        Response historial = alumna.asksFor(
                HistorialAcademico.obtenido()
        );

        historial.then()
                .body(matchesJsonSchemaInClasspath("schemas/" + archivo));
    }

    @Then("el curso {string} tiene la condicion {string}")
    public void verificarCondicion(String curso, String condicion) {
        Response historial = alumna.asksFor(
                HistorialAcademico.obtenido()
        );

        List<Map<String, Object>> cursos =
                historial.jsonPath().getList("cursos");

        Map<String, Object> cursoEncontrado = cursos.stream()
                .filter(c -> curso.equals(c.get("codigoCurso")))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No se encontro el curso " + curso
                ));

        assertThat(cursoEncontrado.get("condicion"))
                .isEqualTo(condicion);
    }

    @Then("el historial muestra {int} creditos aprobados")
    public void verificarCreditos(int creditos) {
        Response historial = alumna.asksFor(
                HistorialAcademico.obtenido()
        );

        assertThat(historial.jsonPath().getInt("creditosAprobados"))
                .isEqualTo(creditos);
    }

    @Then("el promedio ponderado es {word}")
    public void verificarPromedio(String promedio) {
        Response historial = alumna.asksFor(
                HistorialAcademico.obtenido()
        );

        double esperado = Double.parseDouble(promedio.replace(",", "."));
        double obtenido = historial.jsonPath().getDouble("promedioPonderado");

        assertThat(obtenido).isCloseTo(esperado,
                org.assertj.core.data.Offset.offset(0.01));
    }

    @Then("el sistema rechaza la calificacion por falta de permisos")
    public void verificarPermisos() {
        Response respuesta = docente.recall("respuestaCalificacion");

        assertThat(respuesta.statusCode()).isEqualTo(403);
    }

    @Then("el sistema rechaza la calificacion por falta de pago")
    public void verificarFaltaPago() {
        Response respuesta = docente.recall("respuestaCalificacion");

        assertThat(respuesta.statusCode()).isBetween(400, 499);
    }
}
