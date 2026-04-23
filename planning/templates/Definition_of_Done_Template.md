# Definition of Done Template - Biblioteca

Un cambio esta terminado solo si todas las casillas aplicables estan completas.

## Negocio y especificacion

- [ ] Existe especificacion actualizada.
- [ ] El servicio owner esta identificado.
- [ ] Los criterios de aceptacion quedaron cubiertos.
- [ ] Se preservan las invariantes del dominio.

## Arquitectura y codigo

- [ ] El cambio respeta DDD y Clean Architecture.
- [ ] No hay dependencias circulares nuevas.
- [ ] No hay logica de negocio fuera de dominio o aplicacion.
- [ ] Los nombres reflejan el lenguaje ubicuo.
- [ ] No se expusieron entidades de dominio como contrato externo.

## Contratos e integracion

- [ ] Los contratos HTTP afectados fueron validados.
- [ ] Los eventos afectados fueron versionados o mantenidos compatibles.
- [ ] Se definio idempotencia para consumers o publishers involucrados.
- [ ] Se uso Outbox Pattern o mecanismo equivalente cuando hubo publicacion de eventos de integracion.
- [ ] El orden de despliegue esta claro si el cambio es multi-servicio.

## Testing y calidad

- [ ] Existen pruebas relevantes para el cambio.
- [ ] No hay tests fallando.
- [ ] Se mantienen los quality gates.
- [ ] Los contract tests se actualizaron si hubo cambios de API o eventos.
- [ ] Los tests de arquitectura siguen en verde.

## Operacion y entrega

- [ ] La observabilidad minima sigue vigente.
- [ ] Se propaga `correlationId` donde aplica.
- [ ] La configuracion y secretos estan fuera del codigo.
- [ ] El pipeline requerido cubre este cambio.
- [ ] Existe plan de rollback si el cambio lo requiere.

## Documentacion y decisiones

- [ ] La documentacion tecnica quedo actualizada.
- [ ] Existe ADR si la decision fue arquitectonica.
- [ ] La deuda tecnica aceptada esta registrada.
