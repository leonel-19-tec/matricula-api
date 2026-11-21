@T1P3
Feature: Calificacion de matriculas
  Como docente
  Quiero registrar las notas de mis alumnos
  Para actualizar su historial academico

  @calificacion
  Scenario Outline: El docente registra la nota y el historial se actualiza
    Given que la alumna "i202610005" tiene una matricula pagada del curso "PI5620"
    And que el docente "D0001" inicia sesion
    When registra la nota <nota> en la matricula
    Then la calificacion se registra correctamente
    And el historial cumple el esquema "historial.json"
    And el curso "PI5620" tiene la condicion "<condicion>"
    And el historial muestra <creditos> creditos aprobados
    And el promedio ponderado es <promedio>

    Examples:
      | nota | condicion   | creditos | promedio |
      | 10   | DESAPROBADO | 7        | 12,92    |
      | 12,5 | DESAPROBADO | 7        | 14,08    |

  @docente_no_autorizado
  Scenario: Un docente que no dicta el curso intenta registrar la nota
    Given que la alumna "i202610005" tiene una matricula pagada del curso "PI5620"
    And que inicia sesion un docente que no dicta el curso
    When intenta registrar la nota 16 en la matricula
    Then el sistema rechaza la calificacion por falta de permisos

  @matricula_no_pagada
  Scenario: No se puede calificar una matricula que no esta pagada
    Given que la alumna "i202610005" tiene una matricula no pagada del curso "PS5511"
    And que el docente correspondiente inicia sesion
    When intenta registrar la nota 16 en la matricula
    Then el sistema rechaza la calificacion por falta de pago
