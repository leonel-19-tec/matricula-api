
package pe.edu.cibertec.matricula_api.screenplay.task;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class ConsultarHistorial implements Task {

    private final String codigoAlumno;

    public ConsultarHistorial(String codigoAlumno) {
        this.codigoAlumno = codigoAlumno;
    }

    public static ConsultarHistorial de(String codigoAlumno) {
        return instrumented(
                ConsultarHistorial.class,
                codigoAlumno
        );
    }

    @Override
    public <T extends Actor> void performAs(T actor) {

        String token = actor.recall("accessToken");

        Response respuesta = SerenityRest.given()
                .baseUri("http://localhost:8080")
                .header("Authorization", "Bearer " + token)
                .get("/api/alumnos/" + codigoAlumno + "/historial");

        actor.remember("respuestaHistorial", respuesta);

        System.out.println(
                "Consultar historial de " + codigoAlumno
                        + " - HTTP " + respuesta.statusCode()
        );
    }
}
