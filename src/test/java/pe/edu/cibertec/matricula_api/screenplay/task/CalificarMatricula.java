
package pe.edu.cibertec.matricula_api.screenplay.task;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

import java.util.Map;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class CalificarMatricula implements Task {

    private final int idMatricula;
    private final double nota;

    public CalificarMatricula(int idMatricula, double nota) {
        this.idMatricula = idMatricula;
        this.nota = nota;
    }

    public static CalificarMatricula con(int idMatricula, double nota) {
        return instrumented(
                CalificarMatricula.class,
                idMatricula,
                nota
        );
    }

    @Override
    public <T extends Actor> void performAs(T actor) {

        String token = actor.recall("accessToken");

        Response respuesta = SerenityRest.given()
                .baseUri("http://localhost:8080")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of("nota", nota))
                .patch("/api/matriculas/" + idMatricula + "/nota");

        actor.remember("respuestaCalificacion", respuesta);

        System.out.println(
                "Registrar nota " + nota
                        + " - HTTP " + respuesta.statusCode()
        );
    }
}
