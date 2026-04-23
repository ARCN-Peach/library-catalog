# AI Engineering Charter - Proyecto Biblioteca

> Guia operativa oficial para que el equipo y la IA disenen, especifiquen, implementen, prueben, refactoricen y entreguen software en el proyecto Biblioteca.
>
> Estado: vigente para greenfield.
> Stack oficial: Java 21 LTS, Spring Boot 4.x, Maven, RabbitMQ, PostgreSQL, GitHub Actions.
> Alcance principal: `library-user`, `library-catalog`, `library-rental`, `library-reservation`, `library-fine`.

---

## 1. Proposito del documento

Este documento existe para evitar decisiones improvisadas. Su objetivo es que cualquier integrante del equipo o agente de IA pueda trabajar con el mismo marco tecnico, el mismo lenguaje y el mismo nivel de exigencia.

Este charter define:

- como pensar el dominio
- como partir una funcionalidad en servicios y capas
- como escribir especificaciones y criterios de aceptacion
- como programar siguiendo Clean Architecture, SOLID y Software Craftsmanship
- como probar con TDD y quality gates medibles
- como versionar contratos, eventos y APIs
- como revisar, desplegar y operar cambios

Este documento **no es opcional**. Si una implementacion se aparta de lo definido aqui, debe justificarse explicitamente mediante ADR o decision aprobada.

---

## 2. Como usar este charter

### 2.1 Uso por humanos

Toda historia, spike, bugfix, refactor o feature debe recorrer este flujo:

1. Leer este charter y ubicar el bounded context afectado.
2. Redactar o actualizar una especificacion usando la plantilla del proyecto.
3. Identificar contratos, eventos, datos y riesgos impactados.
4. Definir pruebas antes de programar.
5. Implementar respetando capas, invariantes y reglas de integracion.
6. Ejecutar quality gates.
7. Actualizar documentacion, ADRs y checklist de PR.

### 2.2 Uso por IA

Antes de proponer o escribir codigo, la IA debe dejar claro:

- requisito que atiende
- servicio o servicios afectados
- invariante de negocio protegida
- contratos afectados
- estrategia de pruebas
- riesgos y supuestos

Antes de declarar terminado un cambio, la IA debe reportar:

- que se implemento
- que no se implemento
- que pruebas agrego o ajusto
- que contratos o eventos cambio
- que riesgos quedan abiertos
- si introdujo o elimino deuda tecnica

La IA no debe:

- inventar arquitectura paralela al charter
- mezclar dominio con framework
- omitir especificacion en cambios funcionales
- dar por cerrado un cambio sin evidencia de validacion
- introducir servicios o integraciones nuevas sin decision explicita

---

## 3. Fuentes de verdad del proyecto

Este charter operacionaliza los siguientes documentos base:

- [DDD_Planning_Biblioteca_Corregido.md](./DDD_Planning_Biblioteca_Corregido.md)
- [Clean_Architecture_Biblioteca.md](./Clean_Architecture_Biblioteca.md)
- [Productores, Colas y Consumidores biblioteca.txt](./Productores,%20Colas%20y%20Consumidores%20biblioteca.txt)

Regla de precedencia:

- DDD define lenguaje ubicuo, bounded contexts, agregados y reglas base del dominio.
- Clean Architecture define la separacion por capas dentro de cada servicio.
- La topologia de productores/colas/consumidores y el diagrama visual definen la direccion principal de integracion por eventos.
- Este charter traduce todo lo anterior en reglas ejecutables para codificacion, pruebas, contratos y entrega.

Si un documento previo es ambiguo o incompleto, este charter fija el default operativo hasta que exista una decision mejor documentada.

---

## 4. Vision del sistema y alcance tecnico

### 4.1 Servicios canonicos del proyecto

Los nombres oficiales de los servicios sobre los que se va a trabajar son:

- `library-user`
- `library-catalog`
- `library-rental`
- `library-reservation`
- `library-fine`

En algunos diagramas o notas puede aparecer el sufijo `-service`. En este charter se consideran equivalentes:

- `library-user` = `library-user-service`
- `library-catalog` = `library-catalog-service`
- `library-rental` = `library-rental-service`
- `library-reservation` = `library-reservation-service`
- `library-fine` = `library-fine-service`

### 4.2 Servicios de integracion externos o secundarios

El diagrama tambien muestra consumidores complementarios:

- `library-notification-service`
- `library-search-service`

Por ahora estos **no son los servicios principales del alcance de implementacion**, pero sus contratos deben considerarse reales. Eso implica:

- no romper eventos que ya los alimentan
- documentar payloads y versionado
- dejar integracion desacoplada y tolerante a ausencia temporal del consumidor

### 4.3 Objetivo de arquitectura

La arquitectura objetivo es **microservicios alineados a bounded contexts** con integracion **event-driven** y separacion interna por **Clean Architecture**.

Reglas estructurales:

- cada servicio posee su propia base de datos
- esta prohibido compartir tablas o esquema entre servicios
- el dominio de cada servicio es la unica fuente de verdad de sus invariantes
- la comunicacion entre servicios se hace por API o eventos versionados
- la consulta directa a la base de datos de otro servicio esta prohibida

---

## 5. Principios rectores

### 5.1 Principios obligatorios

- **DDD** para bounded contexts, lenguaje ubicuo, eventos y modelado tactico
- **Clean Architecture** para separar dominio, aplicacion, infraestructura e interfaces
- **SOLID** para bajo acoplamiento y alta cohesion
- **Software Craftsmanship** para disciplina, calidad y mejora continua
- **Clean Code** para legibilidad, nombres correctos y simplicidad
- **TDD** como practica por defecto
- **SDD** para especificar antes de implementar
- **DevOps** para automatizar calidad, despliegue y observabilidad

### 5.2 Regla central

> Todo cambio debe preservar claridad de dominio, trazabilidad, bajo acoplamiento, alta cohesion y una ruta verificable desde la especificacion hasta la puesta en produccion.

### 5.3 Regla de oro del proyecto

Si una decision facilita programar hoy pero degrada el lenguaje de dominio, mezcla capas o rompe contratos sin control, esa decision es incorrecta aunque "funcione".

---

## 6. Lenguaje ubicuo y ownership por servicio

### 6.1 Lenguaje ubicuo base

Terminos obligatorios del proyecto:

- `Book`
- `User`
- `Rental`
- `Reservation`
- `Fine`
- `Catalog`
- `SearchCriteria`
- `SearchResult`
- `BookStatus`
- `UserStatus`
- `RentalStatus`
- `ReservationStatus`
- `FineStatus`

Reglas:

- negocio en espanol, codigo en ingles cuando aplique
- nombres tecnicos no deben reemplazar conceptos del dominio
- si un nombre contradice el lenguaje ubicuo, se corrige

### 6.2 Ownership funcional por servicio

#### `library-user`

Responsable de:

- registro y actualizacion basica de usuarios
- estado del usuario
- bloqueo y desbloqueo por reglas autorizadas
- datos de identificacion del lector

No debe:

- calcular multas
- gestionar prestamos
- decidir disponibilidad de libros

#### `library-catalog`

Responsable de:

- alta, actualizacion y retiro de libros
- metadata del catalogo
- estado de disponibilidad o stock del libro
- criterios de busqueda y consulta del catalogo

No debe:

- crear rentals
- gestionar multas
- bloquear usuarios

#### `library-rental`

Responsable de:

- creacion y devolucion de prestamos
- ciclo de vida del alquiler
- deteccion de vencimientos
- publicacion de eventos de alquiler y devolucion

No debe:

- ser fuente de verdad del catalogo
- gestionar reservas como agregado propio
- calcular o registrar multas como owner final

#### `library-reservation`

Responsable de:

- crear y cancelar reservas
- mantener la cola o prioridad de reserva
- activar la siguiente reserva cuando un libro se libera

No debe:

- actualizar stock del libro como owner
- cobrar multas
- bloquear usuarios

#### `library-fine`

Responsable de:

- generar multas por devolucion tardia
- registrar pago de multas
- publicar eventos de deuda y pago

No debe:

- cambiar directamente el estado de usuario en la base de `library-user`
- gestionar rentals o stock de catalogo

---

## 7. Topologia objetivo basada en el diagrama

### 7.1 Interpretacion oficial del diagrama

El diagrama vigente se toma como referencia de integraciones orientadas a eventos. La interpretacion operativa oficial es:

- `library-catalog` publica `BookRegisteredEvent`
- `library-rental` publica `BookLentEvent`
- `library-user` publica `UserRegisteredEvent`
- `library-reservation` publica `ReservationActivatedEvent`
- `library-fine` publica `FineGeneratedEvent`

Consumidores de referencia:

- `library-search-service` indexa libros nuevos o actualizados
- `library-notification-service` envia correos o avisos
- `library-catalog` actualiza disponibilidad o stock ante prestamos y devoluciones
- `library-user` bloquea usuario al generarse una multa

### 7.2 Matriz de eventos de referencia

| Producer | Evento | Consumidor principal | Proposito |
|---|---|---|---|
| `library-catalog` | `BookRegisteredEvent` | `library-search-service` | Indexar libro en motor de busqueda |
| `library-rental` | `BookLentEvent` | `library-catalog` | Actualizar stock o disponibilidad |
| `library-rental` | `BookLentEvent` | `library-notification-service` | Enviar comprobante de prestamo |
| `library-user` | `UserRegisteredEvent` | `library-notification-service` | Enviar bienvenida |
| `library-reservation` | `ReservationActivatedEvent` | `library-notification-service` | Avisar reserva lista |
| `library-fine` | `FineGeneratedEvent` | `library-user` | Bloquear usuario segun politica |
| `library-fine` | `FineGeneratedEvent` | `library-notification-service` | Avisar deuda |

Eventos complementarios recomendados para completar el dominio:

- `BookReturnedEvent`
- `FinePaidEvent`
- `ReservationCancelledEvent`
- `BookUpdatedEvent`
- `BookRetiredEvent`
- `RentalOverdueEvent`
- `UserBlockedEvent`

### 7.3 Reglas de RabbitMQ

Defaults operativos:

- un exchange por bounded context: `catalog`, `rental`, `user`, `reservation`, `fine`
- routing key estandar: `<context>.<aggregate>.<event>.v<version>`
- todo mensaje debe tener `eventId`, `eventType`, `occurredAt`, `version`, `correlationId`
- consumidores deben ser idempotentes
- reintentos y dead-letter queue son obligatorios en eventos de integracion

### 7.4 Patron de publicacion

Toda publicacion de evento de integracion debe usar **Outbox Pattern** o un mecanismo equivalente de atomicidad entre persistencia y emision. Esta regla es obligatoria para evitar perdida o duplicidad inconsistente de eventos.

---

## 8. Clean Architecture obligatoria por servicio

### 8.1 Estructura base

Cada servicio debe seguir esta estructura logica:

```text
src/main/java/com/library/<service>/
  domain/
  application/
  infrastructure/
  interfaces/
```

Ejemplo:

```text
src/main/java/com/library/rental/
  domain/
    model/
    service/
    event/
    repository/
  application/
    usecase/
    dto/
    port/
  infrastructure/
    persistence/
    messaging/
    config/
    mapper/
  interfaces/
    rest/
    messaging/
    scheduler/
```

### 8.2 Regla de dependencia

- `domain` no conoce Spring, JPA, RabbitMQ, REST ni frameworks
- `application` depende solo de `domain` y de puertos
- `infrastructure` implementa repositorios, clients, mappers, publishers y configuracion
- `interfaces` recibe HTTP, AMQP, jobs o CLI y delega a `application`

### 8.3 Restricciones no negociables

- no poner logica de negocio en controllers, listeners, schedulers, adapters o mappers
- no usar entidades JPA como entidades de dominio
- no devolver entidades de dominio en responses
- no hacer queries cross-service a BD ajenas
- no crear dependencias circulares entre paquetes o modulos

### 8.4 Estructura recomendada por feature dentro de la capa

Preferencia del proyecto:

- dividir por bounded context y feature, no por tipo tecnico global
- usar nombres de caso de uso explicitos
- aislar DTOs y contratos de integracion

Ejemplo:

- `RegisterUserUseCase`
- `LendBookUseCase`
- `ReturnBookUseCase`
- `ReserveBookUseCase`
- `GenerateFineUseCase`

---

## 9. Reglas especificas de diseno y codificacion

### 9.1 Entidades y value objects

- las entidades protegen invariantes
- los value objects son inmutables
- la validacion ocurre al construir el objeto, no al usarlo tarde
- fechas y dinero deben modelarse con tipos apropiados
- usar `UUID` como identificador por default

### 9.2 Casos de uso

Todo caso de uso debe:

- representar una accion de negocio
- tener input y output explicitos
- coordinar llamadas a repositorios y servicios de dominio
- no contener detalles de framework

### 9.3 DTOs y contratos

- usar DTOs separados para request, response y mensajes
- DTOs son estructuras de transporte, no contienen logica de negocio
- para Java, preferir `record` en DTOs inmutables cuando sea apropiado

### 9.4 Manejo de errores

- errores de dominio se modelan explicitamente
- errores tecnicos se traducen en bordes del sistema
- no lanzar excepciones genericas sin contexto
- responses y mensajes deben exponer fallos esperados de forma consistente

### 9.5 Convenciones Java del proyecto

- paquetes en minuscula
- clases en PascalCase
- metodos y variables en camelCase
- interfaces con nombre de rol, no con prefijo `I`
- tests con nombres que expresen comportamiento
- evitar `Utils` genericos cuando el problema pertenece al dominio

### 9.6 Lo que esta prohibido

- logica de negocio en controladores o listeners
- entidades anemicas cuando el comportamiento pertenece al agregado
- servicios gigantes que mezclan multiples razones de cambio
- helpers genericos que ocultan reglas de negocio
- mocks excesivos en pruebas de dominio
- duplicacion de reglas entre servicios

---

## 10. Contratos entre servicios

### 10.1 Regla general

Todo endpoint publico y todo evento de integracion es un contrato. Todo contrato debe tener:

- owner
- version
- schema
- consumer conocido o esperado
- reglas de compatibilidad
- estrategia de validacion

### 10.2 APIs HTTP

Reglas:

- documentar con OpenAPI o contrato equivalente
- usar versionado cuando el cambio sea incompatible
- explicitar errores de dominio y tecnicos
- incluir trazabilidad mediante `correlationId`
- no exponer internals de persistencia

### 10.3 Eventos

Todo evento debe definir:

- nombre en pasado
- version
- producer owner
- payload minimo necesario
- `eventId`
- `correlationId`
- `occurredAt` en UTC
- estrategia de idempotencia
- politica de retry y DLQ

### 10.4 Compatibilidad

Defaults:

- los cambios aditivos son preferidos
- los cambios rompientes requieren version nueva
- los consumidores no deben asumir orden fuerte salvo contrato explicito
- si un cambio afecta dos o mas servicios, la estrategia de migracion debe quedar en la especificacion

---

## 11. Politica de especificacion y SDD

### 11.1 Regla base

No se programa una funcionalidad sin especificacion suficiente. La especificacion minima del proyecto debe incluir:

- objetivo
- contexto
- servicio owner
- servicios impactados
- reglas de negocio
- escenarios
- criterios de aceptacion formales
- impacto en API, evento, persistencia y observabilidad
- riesgos y supuestos

### 11.2 Formato de criterios de aceptacion

Se debe usar Given/When/Then o equivalente verificable.

Ejemplo:

```text
Dado un usuario bloqueado
Cuando intenta realizar un prestamo
Entonces library-rental rechaza la operacion
Y no crea un Rental
Y registra la razon del rechazo con correlationId
```

### 11.3 Definition of Ready

Un cambio esta listo para implementarse solo si:

- el servicio owner esta claro
- la regla de negocio esta definida
- el contrato afectado esta identificado
- la compatibilidad esta decidida
- la estrategia de prueba esta acordada
- los riesgos principales estan visibles

---

## 12. TDD, testing y estrategia de calidad

### 12.1 TDD por defecto

TDD es la practica por defecto del proyecto:

- `rojo`: escribir primero la prueba que falle
- `verde`: implementar lo minimo para pasar
- `refactor`: mejorar sin romper comportamiento

Excepciones aceptables:

- exploracion tecnica corta
- wiring complejo de infraestructura
- migraciones o integraciones donde el riesgo principal es tecnico y no de dominio

Toda excepcion debe dejar justificacion.

### 12.2 Piramide de pruebas del proyecto

- unit tests para dominio
- application tests para casos de uso
- integration tests para persistencia, broker y adapters
- contract tests para APIs y eventos
- E2E solo para flujos criticos transversales

### 12.3 Que probar en cada servicio

#### `library-user`

- registro de usuario
- bloqueo por multa generada
- reglas de estado del usuario

#### `library-catalog`

- registro y actualizacion de libro
- disponibilidad o stock
- busqueda y filtrado
- reaccion a `BookLentEvent` y `BookReturnedEvent`

#### `library-rental`

- prestamo permitido y rechazado
- devolucion
- vencimiento
- publicacion de eventos correctos

#### `library-reservation`

- creacion de reserva
- cancelacion
- activacion por disponibilidad
- idempotencia en reconsumo de eventos

#### `library-fine`

- generacion de multa
- pago
- publicacion de `FineGeneratedEvent` y `FinePaidEvent`

### 12.4 Herramientas obligatorias o preferidas

- JUnit 5
- AssertJ
- Mockito solo cuando un fake o doble simple no sea razonable
- Testcontainers
- ArchUnit
- JaCoCo

### 12.5 Reglas de pruebas

- no mockear entidades ni value objects
- los tests de dominio no dependen de Spring
- cada bugfix debe incluir una prueba que reproduzca el error
- los tests de contrato son obligatorios cuando cambia un API o evento
- los consumers de eventos deben probar idempotencia y reintento

### 12.6 Quality gates

Minimos exigidos por PR:

- cobertura total por servicio o modulo: **80%**
- dominio y aplicacion en componentes criticos: **90%**
- `mvn test` y pruebas de integracion sin fallos
- JaCoCo dentro de umbral
- reglas ArchUnit sin violaciones
- analisis estatico sin issues bloqueantes
- contract tests obligatorios cuando cambien APIs o eventos

No se hace merge si:

- hay tests rojos
- se rompe compatibilidad sin versionado o plan
- se viola la arquitectura sin excepcion aprobada

---

## 13. ArchUnit y reglas arquitectonicas minimas

Cada servicio debe tener tests de arquitectura para validar al menos:

- `domain` no depende de Spring
- `domain` no depende de `infrastructure` ni `interfaces`
- `application` no depende de `interfaces`
- `interfaces` no accede directo a clases de persistencia saltando `application`
- clases anotadas con framework no viven en `domain`

Estas reglas son parte del quality gate, no una recomendacion opcional.

---

## 14. Integracion sincrona vs asincrona

### 14.1 Default del proyecto

Usar eventos para coordinacion entre bounded contexts y mantener bajo acoplamiento.

### 14.2 Cuando se permite HTTP sincrono

Solo se permite cuando hace falta consistencia inmediata en el camino del comando y no existe otra estrategia razonable. Ejemplos tipicos:

- validacion puntual del estado de usuario antes de prestar
- validacion puntual de disponibilidad del libro antes de crear el rental

Reglas:

- no encadenar llamadas sincronicamente entre muchos servicios
- no usar HTTP para replicar procesos batch que deben ser event-driven
- la dependencia sincrona debe quedar documentada en la especificacion

### 14.3 Cuando se requiere asincronia

- actualizacion de read models
- indexacion de busqueda
- notificaciones
- bloqueo de usuario disparado por eventos de deuda
- sincronizacion de disponibilidad posterior a eventos de rental

---

## 15. Persistencia, transacciones y datos

### 15.1 Reglas de persistencia

- una base de datos por servicio
- migraciones versionadas y automatizadas
- usar Flyway por default salvo ADR en contra
- no mezclar entidades ORM con entidades de dominio
- mapear persistencia a dominio mediante mappers o ensambladores

### 15.2 Reglas transaccionales

- una transaccion local por servicio para cambios internos
- consistencia cross-service por eventos, no por transacciones distribuidas
- cuando una operacion local publique evento, usar outbox

### 15.3 Datos y tiempo

- todas las fechas en UTC para contratos y persistencia compartida
- usar reloj inyectable para pruebas sensibles al tiempo
- no loggear datos personales innecesarios

---

## 16. Observabilidad, operacion y seguridad minima

Cada servicio debe incluir desde el inicio:

- logs estructurados
- `correlationId` propagado en HTTP y eventos
- health checks
- metricas basicas
- trazabilidad entre request, evento y efecto

Seguridad minima:

- secretos fuera del codigo
- configuracion por ambiente
- validacion de entradas
- mascarado de PII sensible en logs
- principio de menor privilegio en accesos tecnicos

---

## 17. Flujo de trabajo obligatorio para cambios

Todo cambio debe seguir este orden:

1. Identificar servicio owner y bounded context.
2. Redactar especificacion.
3. Identificar impacto en eventos, APIs, DB y consumers.
4. Escribir pruebas o definir claramente la estrategia TDD.
5. Implementar el cambio minimo.
6. Refactorizar.
7. Ejecutar quality gates.
8. Actualizar ADR, contratos y documentacion si aplica.
9. Abrir PR con checklist completo.

Para cambios multi-servicio:

1. definir owner de la decision
2. versionar contrato si hace falta
3. acordar orden de despliegue
4. dejar plan de compatibilidad temporal

---

## 18. Reglas de refactoring y deuda tecnica

### 18.1 Refactoring permitido

Se permite refactorizar cuando:

- hay cobertura suficiente del comportamiento actual
- mejora claridad, duplicacion, cohesion o testabilidad
- no rompe contratos externos sin plan explicito

### 18.2 Refactoring obligatorio

Debe hacerse cuando se detecta:

- duplicacion de reglas de negocio
- nombres que rompen el lenguaje ubicuo
- acoplamiento de infraestructura en dominio
- clases con multiples razones de cambio
- listeners o controllers con logica de negocio

### 18.3 Deuda tecnica

Toda deuda aceptada debe registrar:

- descripcion
- impacto
- riesgo
- owner
- fecha o condicion de cierre

No se acepta deuda tecnica invisible.

---

## 19. DevOps, CI/CD y entrega continua

### 19.1 Pipeline minimo de PR en GitHub Actions

Cada PR debe ejecutar al menos:

- build Maven
- unit tests
- integration tests aplicables
- cobertura con JaCoCo
- analisis estatico
- tests ArchUnit
- escaneo basico de dependencias

### 19.2 Pipeline de main o release

Debe incluir:

- empaquetado versionado
- construccion y publicacion de imagen
- validacion de contratos
- despliegue por ambiente
- mecanismo simple de rollback

### 19.3 Practicas obligatorias

- branch protection
- Conventional Commits o convencion equivalente
- PRs pequenos y trazables
- versionado semantico
- release notes sinteticas
- aprobacion humana para cambios de arquitectura, seguridad o contratos rompientes

### 19.4 Configuracion operativa

- perfiles por ambiente
- variables por entorno
- secretos gestionados fuera del repo
- readiness y liveness probes donde aplique

---

## 20. Checklist operativo para IA y equipo

Antes de implementar:

- [ ] Identifique el servicio owner.
- [ ] Confirme la regla de negocio.
- [ ] Liste contratos y eventos afectados.
- [ ] Defina estrategia de testing.
- [ ] Revise impacto en observabilidad y seguridad.
- [ ] Confirme si hay migracion o versionado.

Antes de cerrar:

- [ ] La especificacion esta actualizada.
- [ ] Los tests relevantes pasan.
- [ ] Los contracts tests cubren el cambio.
- [ ] Los logs, metricas y `correlationId` siguen presentes.
- [ ] La deuda tecnica quedo registrada si aplica.
- [ ] El PR incluye evidencia de validacion.

---

## 21. Definition of Done del proyecto

Un cambio esta terminado solo si:

- la especificacion esta actualizada
- los criterios de aceptacion quedaron cubiertos
- existen pruebas relevantes y pasan
- los contratos afectados fueron validados
- los quality gates estan en verde
- la observabilidad minima se mantiene
- la documentacion tecnica se actualizo si corresponde
- existe ADR si la decision fue arquitectonica

Plantillas asociadas:

- [ADR_Template.md](./templates/ADR_Template.md)
- [Especificacion_Cambio_Template.md](./templates/Especificacion_Cambio_Template.md)
- [PR_Checklist_Biblioteca.md](./templates/PR_Checklist_Biblioteca.md)
- [Definition_of_Done_Template.md](./templates/Definition_of_Done_Template.md)
- [Contrato_API_o_Evento_Template.md](./templates/Contrato_API_o_Evento_Template.md)

---

## 22. Excepciones y proceso de decision

Las excepciones se permiten solo si:

- el costo de cumplir hoy supera claramente el riesgo inmediato
- existe una limitacion externa real
- se trata de una exploracion controlada y temporal

Toda excepcion debe documentar:

- motivo
- alcance
- riesgo asumido
- owner
- fecha o condicion de cierre
- ADR o registro equivalente

Excepciones verbales o implicitas no son validas.

---

## 23. Defaults fijos del proyecto

Si no existe una decision mas especifica, aplicar estos defaults:

- Java 21 LTS
- Spring Boot 3.x
- Maven
- PostgreSQL
- RabbitMQ
- Flyway
- JUnit 5
- AssertJ
- Mockito con uso restringido
- Testcontainers
- ArchUnit
- JaCoCo
- OpenAPI para contratos HTTP
- Outbox Pattern para publicacion de eventos
- UTC como timezone de contratos
- `UUID` como identificador

---

## 24. Resumen ejecutivo de cumplimiento

La implementacion correcta en este proyecto debe verse asi:

- el servicio correcto es owner del cambio
- el dominio contiene las reglas
- la aplicacion orquesta
- la infraestructura adapta
- las interfaces exponen o consumen
- los contratos estan versionados
- los eventos son idempotentes
- las pruebas prueban comportamiento real
- el pipeline bloquea regresiones
- la observabilidad permite seguir el flujo completo

Si alguno de esos puntos falla, el cambio no esta alineado con el proyecto Biblioteca aunque compile.
