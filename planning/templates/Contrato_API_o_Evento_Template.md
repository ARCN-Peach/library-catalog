# Contrato de API o Evento Template - Biblioteca

## Tipo de contrato

- API HTTP
- Evento

## Nombre

Nombre del endpoint o evento.

## Servicio owner

Seleccionar uno:

- [ ] `library-user`
- [ ] `library-catalog`
- [ ] `library-rental`
- [ ] `library-reservation`
- [ ] `library-fine`
- [ ] `library-notification-service`
- [ ] `library-search-service`

## Version

`v1`, `v2` o equivalente.

## Objetivo

Que necesidad de negocio cubre este contrato.

## Productor y consumidores

- productor
- consumidores actuales
- consumidores esperados
- dependencias externas, si aplica

## Metadata obligatoria

### Para API HTTP

- metodo
- ruta
- auth requerida
- `correlationId` requerido

### Para Evento

- `eventId`
- `eventType`
- `occurredAt` en UTC
- `correlationId`
- routing key
- exchange

## Request o payload

### Campos

| Campo | Tipo | Requerido | Descripcion | Regla |
|---|---|---|---|---|
| example | string | si/no | descripcion | validacion o invariante |

## Response o efecto esperado

- respuesta HTTP o efecto de consumo
- codigos de estado o resultado
- side effects esperados

## Errores y casos borde

- validacion
- entidad inexistente
- duplicados
- reintentos
- idempotencia
- orden de mensajes, si aplica
- comportamiento si un consumer esta caido

## Compatibilidad

- cambios aditivos permitidos
- cambios rompientes prohibidos sin version nueva
- plan de migracion, si aplica
- ventana de compatibilidad temporal, si aplica

## Observabilidad

- logs minimos
- metricas
- trazas o auditoria
- propagacion de `correlationId`

## Estrategia de validacion

- contract tests
- consumers afectados
- evidencia de backward compatibility
- pruebas de retry e idempotencia, si aplica

## Notas de implementacion

- uso de Outbox Pattern, si aplica
- orden de despliegue, si aplica
- flags o compatibilidad transitoria, si aplica
