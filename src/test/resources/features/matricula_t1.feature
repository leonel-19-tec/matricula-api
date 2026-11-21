@T1P1
Feature: Matricula del alumno
  Como alumno de CIBERTEC
  Quiero matricularme y pagar un curso
  Para llevarlo en el ciclo

  Background:
    Given que el alumno "i202610001" inicia sesion con clave "Cibertec2026"

  @cotizacion
  Scenario: Cotizar un curso
    When cotiza el curso "TS5514"
    Then la respuesta de matricula es 200
    And tiene 4 creditos
    And el subtotal es 480.0
    And el descuento es 20
    And el total es 384.0

  @matricula_pago
  Scenario: Matricularse y pagar
    Given que cotiza el curso "TS5514" por 384.0
    When se matricula en el curso "TS5514"
    Then la respuesta de matricula es 201
    And el estado de matricula es "REGISTRADA"
    And el monto de matricula es 384.0
    And cumple el esquema de matricula "matricula.json"
    When paga la matricula con "YAPE"
    Then la respuesta de matricula es 200
    And el estado de matricula es "PAGADA"
    And tiene fecha de pago
    And el pago aparece registrado con "YAPE"

  @regla_negocio
  Scenario: Matricularse en un curso aprobado
    When intenta matricularse en "LP5401"
    Then la respuesta de matricula es 422
    And el error de matricula es "CURSO_APROBADO"
    And cumple el esquema de matricula "error.json"
