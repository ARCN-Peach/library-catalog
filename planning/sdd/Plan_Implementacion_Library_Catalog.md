# System Design Document (SDD) - library-catalog Implementation

> Este documento detalla el diseño técnico y las decisiones de implementación del microservicio `library-catalog`, siguiendo los principios de Clean Architecture y DDD definidos en el Charter del proyecto.

## 1. Arquitectura del Sistema

El servicio sigue estrictamente la Clean Architecture, dividiéndose en las siguientes capas:

### 1.1 Capa de Dominio (`domain`)
- **Entidades**: `Book` es el Aggregate Root. Posee invariantes sobre su estado y stock.
- **Value Objects**: `BookId`, `ISBN`, `Title`, `Author`, `Category`. Aseguran validación en la construcción.
- **Repositorios**: `BookRepository` (interfaz) define las operaciones de persistencia necesarias para el dominio.
- **Domain Services**: `CatalogSearchService` coordina la lógica de búsqueda compleja que no pertenece únicamente a la entidad.

### 1.2 Capa de Aplicación (`application`)
- **Casos de Uso**:
    - `RegisterBookUseCase`: Orquesta la creación de libros y publicación de eventos.
    - `UpdateBookUseCase`: Gestiona la modificación de metadatos.
    - `SearchBooksUseCase`: Ejecuta búsquedas filtradas.
    - `UpdateBookStockUseCase`: Actualiza el inventario ante eventos externos.
- **DTOs**: `RegisterBookCommand`, `SearchBooksQuery`, `BookSummary`, etc. Separan el contrato de aplicación del dominio.
- **Puertos**: `DomainEventPublisher` define la interfaz para la salida de eventos.

### 1.3 Capa de Infraestructura (`infrastructure`)
- **Persistencia**:
    - `BookRepositoryAdapter`: Implementa el repositorio de dominio usando `BookJpaRepository`.
    - `BookJpaEntity`: Mapeo a base de datos relacional.
    - `BookMapper`: Conversión bidireccional entre Dominio y JPA.
- **Mensajería**:
    - `OutboxEventPublisher`: Implementa el patrón Outbox guardando eventos en `OutboxEventEntity`.
    - `OutboxRelayScheduler`: Proceso en segundo plano que publica eventos pendientes a RabbitMQ.
    - `RentalEventConsumer`: Escucha mensajes `BookLent` y `BookReturned`.
- **Configuración**: `CatalogApplicationConfig` y `RabbitMQConfig`.

### 1.4 Capa de Interfaces (`interfaces`)
- **REST**: `BookController` expone los endpoints HTTP.
- **Manejo de Errores**: `GlobalExceptionHandler` traduce excepciones de dominio a códigos de estado HTTP apropiados (404, 400, etc.).

## 2. Decisiones de Diseño Clave

### 2.1 Patrón Outbox
Para garantizar que los eventos de integración se publiquen exactamente cuando la transacción de base de datos tiene éxito, se utiliza una tabla de `outbox`. Un scheduler lee esta tabla y envía los mensajes a RabbitMQ.

### 2.2 Validación de Dominio
Las validaciones de formato (como ISBN) y reglas de negocio (stock no negativo) residen en el núcleo del dominio, asegurando que el sistema nunca esté en un estado inválido, independientemente de la interfaz de entrada.

### 2.3 Desacoplamiento de Mensajería
El servicio no depende directamente de RabbitMQ en su lógica de negocio. Usa una interfaz `DomainEventPublisher` que la infraestructura implementa.

## 3. Modelo de Datos (Esquema SQL)

### Tabla `books`
- `id`: UUID (PK)
- `isbn`: VARCHAR(13)
- `title`: VARCHAR(255)
- `author_first_name`: VARCHAR(100)
- `author_last_name`: VARCHAR(100)
- `category`: VARCHAR(50)
- `status`: VARCHAR(20)
- `stock`: INT

### Tabla `outbox_events`
- `id`: UUID (PK)
- `aggregate_id`: VARCHAR(255)
- `event_type`: VARCHAR(255)
- `payload`: TEXT (JSON)
- `occurred_at`: TIMESTAMP
- `processed`: BOOLEAN

## 4. Diagrama de Flujo: Registro de Libro

1. `BookController` recibe `RegisterBookRequest`.
2. Mapeo a `RegisterBookCommand`.
3. `RegisterBookUseCase.execute()`:
    a. Crea entidad `Book` (valida reglas).
    b. `BookRepository.save(book)`.
    c. `DomainEventPublisher.publish(BookRegisteredEvent)`.
4. La infraestructura guarda el evento en la tabla `outbox` dentro de la misma transacción.
5. El `OutboxRelayScheduler` detecta el nuevo evento y lo envía a RabbitMQ.
