# Especificación de Cambio - Baseline library-catalog

> Este documento describe la especificación funcional actual del servicio `library-catalog` tal como se encuentra implementado en el repositorio, sirviendo como línea base para el seguimiento de la metodología SDD.

## Resumen ejecutivo

- **Objetivo**: Proveer una gestión centralizada del catálogo de libros, incluyendo registro, actualización, retiro y búsqueda, manteniendo la consistencia de la disponibilidad y el stock mediante eventos.
- **Resultado esperado**: Un servicio robusto que expone una API REST para la gestión del catálogo y reacciona a eventos de otros servicios para mantener actualizado el estado de los libros.
- **Criterio corto de éxito**: Operaciones CRUD de libros funcionales, búsqueda eficiente y sincronización exitosa de stock vía eventos de préstamo/devolución.

## Servicio owner

- [x] `library-catalog`

## Servicios impactados

- [x] `library-rental` (consumidor de stock/disponibilidad)
- [x] `library-search-service` (consumidor de eventos de registro/actualización)
- [x] `library-notification-service` (consumidor de eventos informativos)

## Contexto

El servicio `library-catalog` es el corazón de la información bibliográfica del sistema. Actualmente, el sistema requiere una forma de gestionar el inventario de libros y permitir que otros servicios (como `library-rental`) conozcan la disponibilidad de los mismos.

## Regla de negocio

- **Registro de Libros**: Cada libro debe tener un ISBN válido, título, autor y categoría.
- **Estado del Libro**: Un libro puede estar en estado `AVAILABLE` o `RETIRED`.
- **Invariante de Stock**: El stock no puede ser negativo.
- **Actualización de Stock**: El stock disminuye con préstamos y aumenta con devoluciones.
- **Retiro de Libros**: Un libro retirado no puede ser prestado ni buscado en el catálogo activo.

## Alcance

### Incluye

- Gestión de metadatos de libros (Título, Autor, ISBN, Categoría).
- Gestión de estado y stock.
- Búsqueda por criterios (Título, Autor, Categoría).
- Integración asíncrona mediante Outbox Pattern para notificar cambios en el catálogo.

### No incluye

- Gestión de préstamos (responsabilidad de `library-rental`).
- Gestión de usuarios (responsabilidad de `library-user`).

## Contratos y datos impactados

### APIs HTTP

- `POST /api/v1/books`: Registrar nuevo libro.
- `PUT /api/v1/books/{id}`: Actualizar metadatos.
- `GET /api/v1/books/{id}`: Obtener detalle.
- `DELETE /api/v1/books/{id}`: Retirar libro (cambio de estado).
- `GET /api/v1/books/search`: Búsqueda con filtros y paginación.

### Eventos

- `BookRegisteredEvent` (v1): Publicado al registrar un libro.
- `BookUpdatedEvent` (v1): Publicado al modificar datos.
- `BookRetiredEvent` (v1): Publicado al retirar un libro del catálogo.
- `BookLentMessage` (v1): Consumido para decrementar stock.
- `BookReturnedMessage` (v1): Consumido para incrementar stock.

### Persistencia

- Tabla `books`: Almacena el estado actual y metadatos.
- Tabla `outbox_events`: Almacena eventos para publicación atómica.

## Escenarios

### Registro de libro exitoso
**Dado** datos de libro válidos
**Cuando** se solicita el registro
**Entonces** el libro se persiste en estado `AVAILABLE`
**Y** se registra un `BookRegisteredEvent` en la tabla outbox.

### Búsqueda de libros
**Dado** un catálogo con varios libros
**Cuando** se busca por título o autor
**Entonces** se retornan solo los libros que coinciden y que NO están en estado `RETIRED`.

### Actualización de stock por préstamo
**Dado** un libro con stock > 0
**Cuando** se recibe un evento `BookLentMessage`
**Entonces** el stock del libro disminuye en 1.

## Estrategia de pruebas

- **Unit tests**: Validación de entidades (`Book`, `ISBN`, `Title`) y servicios de dominio.
- **Application tests**: Pruebas de casos de uso con repositorios mockeados/in-memory.
- **Architecture tests**: Validación de reglas de Clean Architecture con ArchUnit.
- **Integration tests**: Validación de persistencia JPA y mapeo de mappers.

## Riesgos y mitigaciones

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Inconsistencia de stock por eventos duplicados | Medio | Implementación de idempotencia en los consumidores de mensajes. |
| Pérdida de eventos de integración | Alto | Uso del Outbox Pattern para asegurar la entrega "at-least-once". |
