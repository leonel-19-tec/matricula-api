
package pe.edu.cibertec.matricula_api.screenplay.questions;

import io.restassured.response.Response;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class HistorialAcademico implements Question<Response> {

    public static HistorialAcademico obtenido() {
        return new HistorialAcademico();
    }

    @Override
    public Response answeredBy(Actor actor) {
        return actor.recall("respuestaHistorial");
    }
}
