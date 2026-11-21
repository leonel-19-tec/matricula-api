
package pe.edu.cibertec.matricula_api.screenplay.questions;

import io.restassured.response.Response;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class CodigoError implements Question<String> {

    public static CodigoError obtenido() {
        return new CodigoError();
    }

    @Override
    public String answeredBy(Actor actor) {

        Response respuesta = actor.recall("respuestaSeguridad");

        return respuesta.jsonPath().getString("codigo");
    }
}
