
package pe.edu.cibertec.matricula_api.screenplay.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class IdentificacionT1Hook {

    @Before(value = "@T1P1", order = 1)
    public void mostrarDatos(Scenario escenario) {

        String fecha = LocalDateTime.now(
                ZoneId.of("America/Lima")
        ).format(DateTimeFormatter.ofPattern(
                "dd/MM/yyyy HH:mm:ss"
        ));

        System.out.println();
        System.out.println("====================================");
        System.out.println("PRUEBAS DE SOFTWARE - T1");
        System.out.println("Alumno: Leonel Correa Vera");
        System.out.println("Codigo: I202413800");
        System.out.println("Fecha: " + fecha);
        System.out.println("Escenario: " + escenario.getName());
        System.out.println("====================================");
    }
}
