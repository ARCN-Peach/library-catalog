# PR Checklist - Biblioteca

## Resumen del cambio

- requisito implementado
- servicio owner
- servicios impactados
- contrato o evento afectado, si aplica

## Checklist obligatorio

- [ ] Existe especificacion vinculada y actualizada.
- [ ] El servicio owner del cambio esta claro.
- [ ] Los criterios de aceptacion son verificables.
- [ ] El cambio respeta DDD y Clean Architecture.
- [ ] No se agrego logica de negocio en controllers, listeners, schedulers o adapters.
- [ ] No se expusieron entidades de dominio en APIs o mensajes.
- [ ] Si hubo evento publicado, existe estrategia de idempotencia.
- [ ] Si hubo publicacion de evento, se considero Outbox Pattern o equivalente.
- [ ] Se agregaron o ajustaron tests relevantes.
- [ ] Los tests de contrato se actualizaron si hubo cambios de API o eventos.
- [ ] Los tests pasan localmente o en pipeline.
- [ ] Si cambio un contrato, existe evidencia de versionado o compatibilidad.
- [ ] Si hubo decision arquitectonica, existe ADR.
- [ ] Se reviso impacto en observabilidad, logs, metricas, `correlationId` y trazabilidad.
- [ ] Se reviso impacto en seguridad y manejo de datos sensibles.
- [ ] No se introdujo deuda tecnica sin registrarla.

## Quality gates

- [ ] Cobertura minima general del servicio o modulo: 80%.
- [ ] Cobertura de dominio/aplicacion critica: 90% cuando aplica.
- [ ] ArchUnit en verde.
- [ ] Analisis estatico sin issues bloqueantes.
- [ ] Contract tests en verde si aplican.

## Evidencia de validacion

- comandos ejecutados
- resultados clave
- cobertura relevante
- evidencia de contract tests
- evidencia de compatibilidad backward, si aplica

## Riesgos residuales

| Riesgo | Impacto | Mitigacion o seguimiento |
|---|---|---|
| riesgo | alto/medio/bajo | accion |

## Notas para reviewers

- areas que merecen mayor atencion
- excepciones aprobadas, si existen
- orden de despliegue, si aplica
