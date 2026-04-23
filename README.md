# library-catalog

Microservicio responsable del bounded context **Gestión de Catálogo** dentro del sistema de biblioteca digital.

Gestiona el ciclo de vida de los libros: registro, actualización, retiro y búsqueda. Es el único dueño del stock disponible de cada libro y reacciona a eventos de otros servicios para mantenerlo consistente.

**Stack:** Java 21 · Spring Boot 3.x · PostgreSQL · RabbitMQ · Flyway · Maven

---

## Bounded context

| Elemento | Detalle |
|---|---|
| Eventos que publica | `BookRegisteredEvent`, `BookUpdatedEvent`, `BookRetiredEvent` |
| Eventos que consume | `BookLentEvent`, `BookReturnedEvent` (de `library-rental`) |
| Agregado principal | `Book` |
| Políticas | Solo libros PUBLISHED aparecen en búsquedas · stock no puede ser negativo |

---

## Arquitectura interna — Clean Architecture

```
  HTTP / RabbitMQ
        │
        ▼
┌───────────────────────────────────────────────┐
│  INTERFACES                                   │
│  BookController · GlobalExceptionHandler      │
│  RentalEventConsumer                          │
└──────────────────────┬────────────────────────┘
                       │
                       ▼
┌───────────────────────────────────────────────┐
│  APPLICATION                                  │
│  RegisterBookUseCase   SearchBooksUseCase      │
│  UpdateBookUseCase     GetBookUseCase          │
│  RetireBookUseCase     UpdateBookStockUseCase  │
│                                               │
│  DomainEventPublisher  ← puerto (interfaz)    │
└──────────┬────────────────────────────────────┘
           │
           ▼
┌───────────────────────────────────────────────┐
│  DOMAIN  (sin dependencias de frameworks)     │
│  Book (Aggregate Root)                        │
│  ├─ BookId · Title · Author · ISBN  (VOs)     │
│  ├─ Category · BookStatus           (enums)   │
│  └─ BookRegistered/Updated/RetiredEvent       │
│                                               │
│  CatalogSearchService · BookRepository        │
└──────────┬────────────────────────────────────┘
           │ implementado por
           ▼
┌───────────────────────────────────────────────┐
│  INFRASTRUCTURE                               │
│  BookRepositoryAdapter  ← JPA + BookMapper    │
│  OutboxEventPublisher   ← guarda en DB        │
│  OutboxRelayScheduler   ← publica a RabbitMQ  │
│  RabbitMQConfig                               │
└───────────────────────────────────────────────┘
```

Regla de dependencia: las flechas apuntan siempre hacia adentro. El dominio no conoce Spring, JPA ni RabbitMQ.

---

## Flujos

### 1. Registrar / actualizar / retirar un libro

```
Cliente
  │  POST /books  {title, author, isbn, category, totalCopies}
  │  X-Correlation-Id: <uuid>
  ▼
BookController
  │  construye RegisterBookCommand
  ▼
RegisterBookUseCase  @Transactional ──────────────────────────┐
  │  valida Value Objects (Title, ISBN, Author...)             │  misma
  │  Book.register(...)                                        │  transacción
  │    └─ genera BookId (UUID)                                 │
  │    └─ acumula BookRegisteredEvent internamente             │
  │  bookRepository.save(book)   → INSERT INTO books           │
  │  eventPublisher.publish(...)  → INSERT INTO outbox_events  │
  └─────────────────────────────────────────────────────────── ┘ COMMIT

  (5 segundos después)
OutboxRelayScheduler  @Scheduled
  │  SELECT * FROM outbox_events WHERE published = false
  │  rabbitTemplate.send("catalog", "catalog.book.registered.v1", payload)
  └─ UPDATE outbox_events SET published = true
```

### 2. Buscar libros

```
GET /books/search?title=clean&author=martin&category=TECHNOLOGY&page=0&pageSize=20

BookController
  │
  ▼
SearchBooksUseCase
  │  construye SearchCriteria
  ▼
CatalogSearchService  ← valida página/tamaño, normaliza texto a minúsculas
  │
  ▼
BookRepositoryAdapter
  │  SELECT * FROM books
  │  WHERE LOWER(title) LIKE '%clean%'
  │  AND   LOWER(author) LIKE '%martin%'
  │  AND   category = 'TECHNOLOGY'
  │  AND   status = 'PUBLISHED'
  │
  └─ devuelve SearchBooksResult { books[], totalFound, page, pageSize }
```

### 3. Outbox Pattern — publicación confiable de eventos

```
SIN outbox (problema):
  1. INSERT INTO books   ✓
  2. COMMIT              ✓
  3. rabbitMQ.send()     ✗  falla → evento perdido para siempre

CON outbox (implementado):
  1. INSERT INTO books          ✓  ┐
  2. INSERT INTO outbox_events  ✓  ┘ misma transacción → atómico
  3. COMMIT                     ✓

  (scheduler independiente cada 5s)
  4. rabbitMQ.send()            ✓  si falla → outbox queda published=false
  5. UPDATE published = true    ✓  → reintento automático en el siguiente ciclo
```

### 4. Actualización de stock por eventos de préstamos

```
library-rental  (aún no implementado)
  │
  │  BookLentEvent { bookId, userId, dueDate }
  │  routing key: rental.book.lent.v1
  ▼
RabbitMQ
  Exchange: rental
  Queue:    catalog.rental.book-lent
  DLQ:      catalog.rental.book-lent.dlq  ← si falla reiteradamente
  ▼
RentalEventConsumer.onBookLent()
  │
  ▼
UpdateBookStockUseCase.decrementStock()
  └─ UPDATE books SET available_stock = available_stock - 1


library-rental
  │
  │  BookReturnedEvent { bookId, userId, returnDate }
  │  routing key: rental.book.returned.v1
  ▼
RabbitMQ → Queue: catalog.rental.book-returned
  ▼
RentalEventConsumer.onBookReturned()
  │
  ▼
UpdateBookStockUseCase.incrementStock()
  └─ UPDATE books SET available_stock = available_stock + 1
```

### 5. Contexto del sistema completo

```
                     ┌──────────────────────────────────┐
                     │         RabbitMQ (Event Bus)      │
                     └──────┬──────────┬────────┬────────┘
                            │          │        │
             ┌──────────────┘          │        └──────────────┐
             ▼                         ▼                        ▼
  ┌──────────────────┐    ┌────────────────────┐   ┌───────────────────────┐
  │ library-catalog  │◄───│  library-rental    │   │  library-reservation  │
  │  (este servicio) │    │  BookLentEvent      │   │  ReservationActivated │
  │  puerto 8082     │◄───│  BookReturnedEvent  │   └───────────────────────┘
  └──────────────────┘    └────────────────────┘
           │
           │ publica
           ▼
  BookRegisteredEvent ──► library-search-service   (indexa libros)
  BookUpdatedEvent    ──► library-search-service   (actualiza índice)
  BookRetiredEvent    ──► library-search-service   (elimina del índice)
                     ──► library-reservation       (cancela reservas)
```

---

## API REST

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/books` | Registrar un libro |
| `GET` | `/books/{id}` | Obtener libro por ID |
| `PUT` | `/books/{id}` | Actualizar metadata |
| `DELETE` | `/books/{id}` | Retirar libro del catálogo |
| `GET` | `/books/search` | Buscar por título, autor y/o categoría |

**Parámetros de búsqueda:** `title`, `author`, `category`, `page` (default 0), `pageSize` (default 20, máx 100)

**Header opcional:** `X-Correlation-Id` — trazabilidad entre servicios

**Documentación interactiva:** `http://localhost:8082/swagger-ui.html`

---

## Levantar con Docker

```bash
# Primera vez o al cambiar código
docker compose up --build

# Arrancar sin reconstruir
docker compose up

# Limpiar todo incluyendo datos
docker compose down -v
```

| Servicio | URL |
|---|---|
| API | http://localhost:8082 |
| Swagger UI | http://localhost:8082/swagger-ui.html |
| Actuator health | http://localhost:8082/actuator/health |
| RabbitMQ Management | http://localhost:15672 (guest / guest) |
| PostgreSQL | localhost:5432 — db: `library_catalog` |

---

## Base de datos

```sql
-- Flyway V1
books (id, title, author_first_name, author_last_name,
       category, isbn, status, total_copies, available_stock)

-- Flyway V2
outbox_events (id, event_type, payload, occurred_at,
               correlation_id, published, published_at)
```

---

## Tests

```bash
# Unit tests (sin infraestructura)
mvn test

# Con reporte de cobertura (mínimo 80%)
mvn verify
```

| Capa | Tipo | Qué verifica |
|---|---|---|
| Domain | Unit | Invariantes de `Book`, validación de VOs |
| Application | Unit | Use cases con repositorios in-memory |
| Architecture | ArchUnit | Dependencias entre capas |
