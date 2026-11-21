
package pe.edu.cibertec.matricula_api.screenplay.tasks;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class RenovarToken implements Task {

    public static RenovarToken ahora() {
        return instrumented(RenovarToken.class);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {

        String refreshAnterior = actor.recall("refreshToken");
        String accessAnterior = actor.recall("accessToken");

        actor.remember("refreshAnterior", refreshAnterior);
        actor.remember("accessAnterior", accessAnterior);

        String datos = String.format(
                "{\"refreshToken\":\"%s\"}",
                refreshAnterior
        );

        Response respuesta = SerenityRest.given()
                .baseUri("http://localhost:8080")
                .contentType("application/json")
                .body(datos)
                .post("/api/auth/refresh");

        actor.remember("respuestaSeguridad", respuesta);

        if (respuesta.statusCode() == 200) {
            actor.remember("accessToken",
                    respuesta.jsonPath().getString("accessToken"));

            actor.remember("refreshToken",
                    respuesta.jsonPath().getString("refreshToken"));
        }

        System.out.println("Renovacion de token - HTTP "
                + respuesta.statusCode());
    }
}
