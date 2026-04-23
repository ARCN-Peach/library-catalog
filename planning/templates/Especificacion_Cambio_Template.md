# Especificacion de Cambio Template - Biblioteca

> Usar esta plantilla para features, bugs, refactors con impacto funcional, cambios de contrato y cambios multi-servicio.

## Titulo

Nombre del cambio en terminos de negocio.

## Resumen ejecutivo

- objetivo del cambio
- resultado esperado
- criterio corto de exito

## Servicio owner

Seleccionar uno como owner principal:

- [ ] `library-user`
- [ ] `library-catalog`
- [ ] `library-rental`
- [ ] `library-reservation`
- [ ] `library-fine`

## Servicios impactados

Marcar todos los que apliquen:

- [ ] `library-user`
- [ ] `library-catalog`
- [ ] `library-rental`
- [ ] `library-reservation`
- [ ] `library-fine`
- [ ] `library-notification-service`
- [ ] `library-search-service`

## Contexto

- problema actual
- bounded context afectado
- actor o actores involucrados
- flujo actual
- limitacion o dolor existente
- documentos relacionados

## Regla de negocio

- reglas que aplican
- invariantes que deben preservarse
- validaciones de dominio necesarias
- supuestos aceptados

## Alcance

### Incluye

- comportamiento o modulos incluidos

### No incluye

- comportamiento explicitamente fuera

## Contratos y datos impactados

### APIs HTTP

- endpoint afectado o nuevo
- compatibilidad esperada

### Eventos

- evento publicado o consumido
- version
- producer
- consumers esperados

### Persistencia

- tablas o colecciones afectadas
- migraciones requeridas
- uso de outbox, si aplica

## Escenarios

### Escenario principal

Describir el flujo esperado paso a paso.

### Escenarios alternos

- error de validacion
- dependencia externa no disponible
- evento duplicado o reintentado
- consumer ausente temporalmente
- compatibilidad con versiones anteriores

## Criterios de aceptacion formales

```text
Dado ...
Cuando ...
Entonces ...
```

Agregar un criterio por comportamiento observable.

## Observabilidad y seguridad

- `correlationId` requerido
- logs minimos esperados
- metricas a validar
- PII a proteger o mascarar
- alarmas o señales operativas relevantes

## Estrategia de pruebas

- unit tests de dominio
- application tests
- integration tests
- contract tests
- pruebas de idempotencia o retry, si aplica
- E2E, si aplica

## Riesgos y mitigaciones

| Riesgo | Impacto | Mitigacion |
|---|---|---|
| riesgo principal | alto/medio/bajo | accion |

## Orden de implementacion y despliegue

- servicio o modulo que cambia primero
- compatibilidad temporal
- orden recomendado de release
- rollback esperado

## Definition of Done especifica

- que debe existir para cerrar este cambio
- quality gates relevantes
- ADR requerida, si aplica
