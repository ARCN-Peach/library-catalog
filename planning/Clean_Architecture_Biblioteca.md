# Clean Architecture — Sistema de Gestión de Biblioteca

> **Etapa 2 del proceso:** DDD ✅ → **Clean Architecture** ← → Software Craftsmanship → SDD → DevOps
>
> Este documento organiza el código definido en el DDD Planning en capas independientes y desacopladas, siguiendo los principios de Clean Architecture de Robert C. Martin. La regla fundamental: **las dependencias apuntan siempre hacia adentro** — el dominio no conoce nada de infraestructura.

---

## 1. 📐 Principio central

```
┌─────────────────────────────────────────────────────────────┐
│                        Interfaces                           │  ← HTTP, CLI, WebSocket
├─────────────────────────────────────────────────────────────┤
│                      Infrastructure                         │  ← BD, Email, Cache, MQ
├─────────────────────────────────────────────────────────────┤
│                       Application                           │  ← Use Cases, DTOs
├─────────────────────────────────────────────────────────────┤
│                         Domain                              │  ← Entidades, VO, Servicios
└─────────────────────────────────────────────────────────────┘
            ↑ las dependencias solo van hacia aquí
```

**Regla de dependencia:** ninguna capa interna importa nada de una capa externa. El dominio es puro: no depende de frameworks, bases de datos ni librerías externas.

---

## 2. 🗂️ Estructura de capas

### 2.1 Domain (núcleo — sin dependencias externas)

Contiene todo lo definido en el DDD: entidades, value objects, interfaces de repositorios y domain services. **No importa nada de Spring, JPA, ni ningún framework.**

```
src/domain/
├── model/
│   ├── book/
│   │   ├── Book.ts                  # Aggregate Root
│   │   ├── BookId.ts                # Value Object
│   │   ├── Title.ts                 # Value Object
│   │   ├── Author.ts                # Value Object
│   │   ├── Category.ts              # Enum / Value Object
│   │   ├── ISBN.ts                  # Value Object
│   │   └── BookStatus.ts            # Enum (PUBLISHED, RETIRED)
│   ├── user/
│   │   ├── User.ts                  # Aggregate Root
│   │   ├── UserId.ts
│   │   ├── Email.ts                 # Value Object
│   │   └── UserStatus.ts            # Enum (ACTIVE, BLOCKED)
│   ├── rental/
│   │   ├── Rental.ts                # Aggregate Root
│   │   ├── RentalId.ts
│   │   └── RentalStatus.ts          # Enum (ACTIVE, RETURNED, OVERDUE)
│   ├── reservation/
│   │   ├── Reservation.ts           # Aggregate Root
│   │   ├── ReservationId.ts
│   │   └── ReservationStatus.ts     # Enum (PENDING, ACTIVE, CANCELLED)
│   └── fine/
│       ├── Fine.ts                  # Aggregate Root
│       ├── FineId.ts
│       ├── Money.ts                 # Value Object
│       └── FineStatus.ts            # Enum (PENDING, PAID)
│
├── repository/                      # Interfaces — implementadas en infrastructure
│   ├── BookRepository.ts
│   ├── UserRepository.ts
│   ├── RentalRepository.ts
│   ├── ReservationRepository.ts
│   └── FineRepository.ts
│
├── service/                         # Domain Services — lógica que no pertenece a un solo agregado
│   ├── CatalogSearchService.ts
│   ├── RentalService.ts
│   └── FineService.ts
│
└── event/                           # Domain Events
    ├── BookRegistered.ts
    ├── BookLent.ts
    ├── BookReturned.ts
    ├── ReservationActivated.ts
    └── FineGenerated.ts
```

### 2.2 Application (casos de uso — orquesta el dominio)

Contiene los Use Cases: cada uno representa una historia de usuario. Los Use Cases solo dependen del dominio. Reciben y devuelven DTOs (objetos de transferencia planos, sin lógica de negocio).

```
src/application/
├── catalog/
│   ├── SearchBooksUseCase.ts
│   ├── RegisterBookUseCase.ts
│   ├── UpdateBookUseCase.ts
│   └── RetireBookUseCase.ts
│
├── user/
│   ├── RegisterUserUseCase.ts
│   └── BlockUserUseCase.ts
│
├── rental/
│   ├── LendBookUseCase.ts
│   └── ReturnBookUseCase.ts
│
├── reservation/
│   ├── ReserveBookUseCase.ts
│   └── CancelReservationUseCase.ts
│
├── fine/
│   ├── GenerateFineUseCase.ts
│   └── PayFineUseCase.ts
│
└── dto/                             # Input / Output por caso de uso
    ├── SearchBooksInput.ts          # { title?, author?, category?, page, pageSize }
    ├── SearchBooksOutput.ts         # { items: BookSummary[], totalFound, page }
    ├── RegisterBookInput.ts
    ├── LendBookInput.ts             # { bookId, userId }
    ├── LendBookOutput.ts            # { rentalId, dueDate }
    ├── ReserveBookInput.ts
    └── PayFineInput.ts              # { fineId, userId }
```

**Estructura de un Use Case:**

```typescript
// application/rental/LendBookUseCase.ts
export class LendBookUseCase {
  constructor(
    private readonly bookRepository: BookRepository,       // interfaz del dominio
    private readonly rentalRepository: RentalRepository,   // interfaz del dominio
    private readonly rentalService: RentalService          // domain service
  ) {}

  async execute(input: LendBookInput): Promise<LendBookOutput> {
    // 1. Obtener entidades
    const book = await this.bookRepository.findById(input.bookId);
    const user = await this.userRepository.findById(input.userId);

    // 2. Delegar lógica al domain service
    const rental = this.rentalService.lend(book, user);

    // 3. Persistir
    await this.rentalRepository.save(rental);

    // 4. Devolver DTO (nunca devolver la entidad directamente)
    return { rentalId: rental.id.value, dueDate: rental.dueDate };
  }
}
```

### 2.3 Infrastructure (implementaciones concretas)

Contiene todo lo que toca el mundo exterior: base de datos, correo, caché, mensajería. Implementa las interfaces definidas en el dominio. **Solo esta capa conoce frameworks como Spring, TypeORM, Prisma, etc.**

```
src/infrastructure/
├── persistence/
│   ├── postgres/
│   │   ├── BookRepositoryPostgres.ts       # implementa BookRepository
│   │   ├── UserRepositoryPostgres.ts
│   │   ├── RentalRepositoryPostgres.ts
│   │   ├── ReservationRepositoryPostgres.ts
│   │   └── FineRepositoryPostgres.ts
│   ├── inmemory/                           # para tests y desarrollo local
│   │   ├── BookRepositoryInMemory.ts
│   │   └── RentalRepositoryInMemory.ts
│   └── mapper/                            # convierte entidades de dominio ↔ modelos de BD
│       ├── BookMapper.ts
│       ├── RentalMapper.ts
│       └── FineMapper.ts
│
├── notification/
│   └── EmailNotificationService.ts        # envía email al activar una reserva
│
├── cache/
│   └── RedisCatalogCache.ts               # caché de búsquedas frecuentes
│
└── config/
    ├── DatabaseConfig.ts
    └── DependencyContainer.ts             # inyección de dependencias / wiring
```

### 2.4 Interfaces (puntos de entrada al sistema)

Adaptadores que convierten peticiones externas en llamadas a Use Cases. Incluye controllers HTTP, jobs programados, consumers de mensajes, etc.

```
src/interfaces/
├── http/
│   ├── controller/
│   │   ├── BookController.ts              # GET /books/search, POST /books
│   │   ├── RentalController.ts            # POST /rentals, PUT /rentals/:id/return
│   │   ├── ReservationController.ts       # POST /reservations
│   │   └── FineController.ts             # POST /fines/:id/pay
│   ├── middleware/
│   │   ├── AuthMiddleware.ts
│   │   └── ErrorHandler.ts
│   └── router/
│       └── AppRouter.ts
│
├── scheduler/
│   └── OverdueCheckJob.ts                 # job diario: detecta préstamos vencidos y genera multas
│
└── messaging/
    └── ReservationActivatedConsumer.ts    # escucha evento de devolución para activar reservas
```

---

## 3. 🔄 Flujo de una petición (ejemplo: Buscar libros)

```
HTTP GET /books/search?title=dune&page=1
          │
          ▼
 [BookController]           ← interfaces/http
  parsea query params
  llama al use case
          │
          ▼
 [SearchBooksUseCase]       ← application
  valida el DTO de entrada
  llama al domain service
          │
          ▼
 [CatalogSearchService]     ← domain
  valida criterios (política)
  normaliza texto (política)
  delega en el repositorio
          │
          ▼
 [BookRepository — interfaz] ← domain
          │
          ▼ (implementación real)
 [BookRepositoryPostgres]   ← infrastructure
  ejecuta SQL / ORM
  devuelve registros
          │
          ▼ (mapper)
 [BookMapper]               ← infrastructure
  convierte modelo BD → entidad Book
          │
          ▼ (regresa por la cadena)
 [BookController]
  convierte SearchBooksOutput → JSON 200 OK
```

---

## 4. 🔄 Flujo de un préstamo (ejemplo: Prestar libro)

```
HTTP POST /rentals  { bookId, userId }
          │
          ▼
 [RentalController]
          │
          ▼
 [LendBookUseCase]
  ├── BookRepository.findById()
  ├── UserRepository.findById()
  ├── FineRepository.findPendingByUser()   ← verifica mora
  ├── RentalService.lend(book, user)       ← aplica políticas de dominio
  └── RentalRepository.save(rental)
          │
          ▼
 Emite evento: BookLent
          │
          ▼
 [ReservationActivatedConsumer]            ← si hay reservas en espera
```

---

## 5. 💉 Inyección de dependencias

Las dependencias se conectan en la capa de infraestructura, nunca en el dominio ni en la aplicación.

```typescript
// infrastructure/config/DependencyContainer.ts

const bookRepository        = new BookRepositoryPostgres(db);
const rentalRepository      = new RentalRepositoryPostgres(db);
const fineRepository        = new FineRepositoryPostgres(db);
const userRepository        = new UserRepositoryPostgres(db);

const rentalService         = new RentalService(bookRepository, fineRepository);
const catalogSearchService  = new CatalogSearchService(bookRepository);

const lendBookUseCase       = new LendBookUseCase(bookRepository, userRepository, rentalRepository, rentalService);
const searchBooksUseCase    = new SearchBooksUseCase(catalogSearchService);
const payFineUseCase        = new PayFineUseCase(fineRepository, userRepository);

const bookController        = new BookController(searchBooksUseCase);
const rentalController      = new RentalController(lendBookUseCase);
const fineController        = new FineController(payFineUseCase);
```

---

## 6. 🧪 Estrategia de testing por capa

| Capa | Tipo de test | Herramienta | Qué se testea |
|---|---|---|---|
| Domain | Unit tests | Jest / JUnit | Lógica de entidades, VO, domain services (sin mocks de BD) |
| Application | Unit tests | Jest / JUnit | Use Cases con repositorios en memoria (InMemory) |
| Infrastructure | Integration tests | Jest + TestContainers | Repositorios contra BD real en contenedor |
| Interfaces | E2E / Contract tests | Supertest / RestAssured | Endpoints HTTP completos |

**Ejemplo de test de dominio (puro, sin framework):**

```typescript
// domain/service/RentalService.test.ts
describe('RentalService', () => {
  it('no permite prestar si el libro no está disponible', () => {
    const book = Book.create({ status: BookStatus.RETIRED, ... });
    const user = User.create({ status: UserStatus.ACTIVE, ... });

    expect(() => rentalService.lend(book, user))
      .toThrow('El libro no está disponible para préstamo');
  });

  it('no permite prestar si el usuario tiene multas pendientes', () => {
    // ...
  });
});
```

---

## 7. 📦 Mappers: dominio ↔ persistencia

Los mappers evitan contaminar las entidades de dominio con anotaciones de ORM.

```typescript
// infrastructure/persistence/mapper/BookMapper.ts

export class BookMapper {
  static toDomain(record: BookRecord): Book {
    return Book.reconstitute({
      id:       new BookId(record.id),
      title:    new Title(record.title),
      author:   new Author(record.first_name, record.last_name),
      category: Category[record.category],
      isbn:     new ISBN(record.isbn),
      status:   BookStatus[record.status],
    });
  }

  static toPersistence(book: Book): BookRecord {
    return {
      id:         book.id.value,
      title:      book.title.value,
      first_name: book.author.firstName,
      last_name:  book.author.lastName,
      category:   book.category.toString(),
      isbn:       book.isbn.value,
      status:     book.status.toString(),
    };
  }
}
```

---

## 8. 🟥 Decisiones pendientes / riesgos

- 🟥 **Motor de búsqueda:** ¿SQL con índices full-text o motor especializado (Elasticsearch, Meilisearch)? La interfaz `BookRepository` lo abstrae — se puede cambiar la implementación sin tocar el dominio.
- 🟥 **Estrategia de paginación:** offset/limit vs. cursor — definir antes de implementar `BookRepositoryPostgres`.
- 🟥 **Event bus:** ¿eventos síncronos (in-process) o asíncronos (RabbitMQ, Kafka)? Impacta el diseño del `DependencyContainer` y los consumers.
- 🟥 **Autenticación:** ¿JWT propio o proveedor externo (Auth0, Keycloak)? Solo afecta a `AuthMiddleware` en la capa de interfaces.
- 🟥 **Gestión de libros sin stock y devoluciones:** pendiente de DDD (ver 🟥 del documento anterior).

---

## 9. ✅ Definition of Ready para pasar a Software Craftsmanship

- Las cuatro capas están definidas y sus responsabilidades son claras.
- Ninguna entidad de dominio importa clases de framework o BD.
- Los Use Cases dependen solo de interfaces, nunca de implementaciones concretas.
- Existe al menos una implementación InMemory de cada repositorio para correr tests sin BD.
- El `DependencyContainer` conecta todas las piezas correctamente.
- Los mappers convierten entre modelo de dominio y modelo de persistencia sin mezclar responsabilidades.
- Las decisiones pendientes (🟥) están priorizadas.

---

## 📅 Siguientes etapas del proyecto

1. **DDD** ✅ — modelo de dominio, lenguaje ubicuo, bounded contexts.
2. **Clean Architecture** ✅ ← este documento.
3. **Software Craftsmanship** — TDD, refactoring, principios SOLID aplicados sobre este esqueleto.
4. **SDD** (Specification-Driven / System Design Document) — contratos de API, criterios de aceptación, diagramas de secuencia.
5. **DevOps** — CI/CD, infraestructura como código, observabilidad, despliegue continuo.
