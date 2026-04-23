# ADR Template - Biblioteca

> Usar esta plantilla para toda decision arquitectonica, de integracion, persistencia, versionado, seguridad o despliegue que cambie defaults del proyecto.

## ID

`ADR-YYYYMMDD-<slug>`

## Titulo

Decision breve, concreta y accionable.

## Estado

- Proposed
- Accepted
- Rejected
- Superseded

## Fecha

`YYYY-MM-DD`

## Owner

- responsable de la decision
- servicio owner principal

## Servicios impactados

Marcar los que apliquen:

- [ ] `library-user`
- [ ] `library-catalog`
- [ ] `library-rental`
- [ ] `library-reservation`
- [ ] `library-fine`
- [ ] `library-notification-service`
- [ ] `library-search-service`

## Contexto

- problema que se quiere resolver
- regla de negocio o restriccion tecnica involucrada
- impacto actual si no se decide
- documentos o especificaciones relacionadas

## Decision

Describir la decision exacta:

- que se va a hacer
- donde aplica
- desde cuando aplica
- que queda explicitamente fuera

## Drivers de decision

- consistencia de dominio
- simplicidad operativa
- compatibilidad de contratos
- costo de implementacion
- riesgo tecnico
- observabilidad
- seguridad

## Opciones evaluadas

### Opcion A

- descripcion
- ventajas
- desventajas

### Opcion B

- descripcion
- ventajas
- desventajas

### Opcion C

- descripcion
- ventajas
- desventajas

## Consecuencias

### Positivas

- beneficios esperados
- simplificaciones introducidas

### Negativas o trade-offs

- costo
- deuda
- complejidad agregada

## Impacto tecnico

- capas o modulos afectados
- contratos HTTP afectados
- eventos afectados
- cambios de base de datos o migraciones
- cambios en observabilidad o seguridad

## Estrategia de adopcion

- pasos de implementacion
- orden de despliegue si hay multiples servicios
- estrategia de compatibilidad temporal
- plan de rollback

## Validacion

- pruebas requeridas
- quality gates relevantes
- evidencia esperada para considerar la decision adoptada

## Excepciones o deuda aceptada

- excepcion aprobada, si aplica
- riesgo asumido
- owner
- fecha o condicion de cierre
