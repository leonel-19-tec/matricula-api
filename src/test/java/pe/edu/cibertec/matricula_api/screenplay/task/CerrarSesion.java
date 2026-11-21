
package pe.edu.cibertec.matricula_api.screenplay.tasks;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class CerrarSesion implements Task {

    public static CerrarSesion ahora() {
        return instrumented(CerrarSesion.class);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {

        String token = actor.recall("accessToken");

        Response respuesta = SerenityRest.given()
                .baseUri("http://localhost:8080")
                .header("Authorization", "Bearer " + token)
                .post("/api/auth/logout");

        actor.remember("respuestaSeguridad", respuesta);

        System.out.println("Cierre de sesion - HTTP "
                + respuesta.statusCode());
    }
}
