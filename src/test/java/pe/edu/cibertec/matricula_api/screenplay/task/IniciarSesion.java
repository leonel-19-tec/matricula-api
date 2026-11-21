
package pe.edu.cibertec.matricula_api.screenplay.tasks;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class IniciarSesion implements Task {

    private final String usuario;
    private final String clave;

    public IniciarSesion(String usuario, String clave) {
        this.usuario = usuario;
        this.clave = clave;
    }

    public static IniciarSesion con(String usuario, String clave) {
        return instrumented(IniciarSesion.class, usuario, clave);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {

        String datos = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}",
                usuario, clave
        );

        Response respuesta = SerenityRest.given()
                .baseUri("http://localhost:8080")
                .contentType("application/json")
                .body(datos)
                .post("/api/auth/login");

        actor.remember("respuestaSeguridad", respuesta);

        if (respuesta.statusCode() == 200) {
            actor.remember("accessToken",
                    respuesta.jsonPath().getString("accessToken"));

            actor.remember("refreshToken",
                    respuesta.jsonPath().getString("refreshToken"));
        }

        System.out.println("Login: " + usuario
                + " - HTTP " + respuesta.statusCode());
    }
}
