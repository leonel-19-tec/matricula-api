@T1P2
Feature: Seguridad del sistema
  Como usuario del sistema
  Quiero comprobar la seguridad de mi cuenta
  Para proteger mis datos

  @refresh
  Scenario: Renovar token y evitar reutilizarlo
    Given que el alumno "i202610001" inicia sesion para seguridad
    When renueva su token de sesion
    Then la respuesta de seguridad es 200
    And recibe nuevos tokens
    When intenta reutilizar el token anterior
    Then la respuesta de seguridad es 401
    And cumple el esquema de error de seguridad "error.json"
    And el error de seguridad es "REFRESH_REUTILIZADO"
    When consulta su perfil con el token renovado
    Then la respuesta de seguridad es 401
    And cumple el esquema de error de seguridad "error.json"

  @logout
  Scenario: Cerrar sesion
    Given que el alumno "i202610001" inicia sesion para seguridad
    When cierra su sesion
    Then la respuesta de seguridad es 204
    When consulta su perfil con el token anterior
    Then la respuesta de seguridad es 401
    And cumple el esquema de error de seguridad "error.json"

  @bloqueo
  Scenario: Bloquear cuenta por intentos fallidos
    When el alumno "i202610004" falla su clave 5 veces
    And intenta iniciar sesion con su clave correcta
    Then la respuesta de seguridad es 423
    And cumple el esquema de error de seguridad "error.json"
    And el error de seguridad es "CUENTA_BLOQUEADA"

  @permisos
  Scenario: Alumno sin permiso para listar alumnos
    Given que el alumno "i202610001" inicia sesion para seguridad
    When intenta consultar la lista de alumnos
    Then la respuesta de seguridad es 403
    And cumple el esquema de error de seguridad "error.json"
