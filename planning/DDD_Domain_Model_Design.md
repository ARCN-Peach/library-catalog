# DDD Planning — Sistema de Gestión de Biblioteca

> **Caso de negocio:**
> "Como usuario, quiero poder buscar libros por título, autor o categoría para encontrar fácilmente lo que estoy buscando."

Este documento define el modelo de dominio que guiará la construcción de la aplicación. Es la primera etapa del proceso (DDD), previa a Clean Architecture, Software Craftsmanship, SDD y DevOps.

---

## 1. 📘 Visión del dominio

El sistema permite gestionar una biblioteca digital con funcionalidades que abarcan el registro de usuarios, la gestión del catálogo de libros, los préstamos, las reservas y el control de multas.

**Objetivos del dominio:**

- Mantener un catálogo consistente y consultable de libros.
- Gestionar el ciclo completo de préstamos y devoluciones.
- Controlar reservas activas y notificar a los usuarios.
- Registrar y gestionar multas por devoluciones tardías.
- Alinear el vocabulario entre negocio y desarrollo.

---

## 2. 🟧 Eventos del Dominio

*(Naranja — hechos relevantes que ocurren en el sistema, en pasado)*

### Gestión de Usuarios
- 🟧 Usuario Registrado
- 🟧 Usuario Bloqueado

### Gestión de Catálogo
- 🟧 Libro Registrado
- 🟧 Libro Eliminado
- 🟧 Libro Actualizado

### Gestión de Préstamos
- 🟧 Libro Alquilado
- 🟧 Libro Devuelto
- 🟧 Libro Pendiente por Devolución

### Gestión de Reservas
- 🟧 Libro Reservado
- 🟧 Reserva de Libro Cancelada
- 🟧 Reserva Activada

### Gestión de Multas
- 🟧 Multa Generada
- 🟧 Multa Pagada

---

## 3. 🟨 Actores y 🟦 Comandos

*(Amarillo = Actor, Azul = Comando que dispara el evento)*

### Gestión de Usuarios
| 🟨 Actor | 🟦 Comando |
|---|---|
| 🟨 Usuario | 🟦 Registrarse en biblioteca |

### Gestión de Catálogo
| 🟨 Actor | 🟦 Comando |
|---|---|
| 🟨 Bibliotecario | 🟦 Eliminar libro |
| 🟨 Bibliotecario | 🟦 Presta un libro |
| 🟨 Bibliotecario | 🟦 Registrar un libro |

### Gestión de Préstamos
| 🟨 Actor | 🟦 Comando |
|---|---|
| 🟨 Usuario | 🟦 Alquila un libro |
| 🟨 Sistema gestión de libros | 🟦 Registrar presto |
| 🟨 Bibliotecario | 🟦 Actualiza información del libro |

### Gestión de Reservas
| 🟨 Actor | 🟦 Comando |
|---|---|
| 🟨 Usuario | 🟦 Reservar un libro |

### Gestión de Multas
| 🟨 Actor | 🟦 Comando |
|---|---|
| 🟨 Sistema gestión de libros | 🟦 Generar multa |
| 🟨 Usuario | 🟦 Pagar multa |

---

## 4. 🟩 Agregados y 🟪 Políticas

*(Amarillo lima = Agregado, Morado = Política / regla de negocio)*

### Gestión de Usuarios
| 🟩 Agregado | 🟪 Política aplicada |
|---|---|
| 🟩 Usuario | 🟪 Solo puede pedir prestado una cantidad limitada de libros |

### Gestión de Catálogo
| 🟩 Agregado | 🟪 Política aplicada |
|---|---|
| 🟩 Catálogo | 🟪 Visualizar libros |
| 🟩 Catálogo | 🟪 Mostrar detalles del libro |
| 🟩 Catálogo | 🟪 Filtrar búsquedas de libros |

### Gestión de Préstamos
| 🟩 Agregado | 🟪 Política aplicada |
|---|---|
| 🟩 Libro | 🟪 Solo se puede prestar si está disponible |
| 🟩 Alquiler | 🟪 Verificar que el usuario no tenga algún libro en mora |
| 🟩 Alquiler | 🟪 Ver stock de libros antes de prestarlo |

### Gestión de Reservas
| 🟩 Agregado | 🟪 Política aplicada |
|---|---|
| 🟩 Sistema | 🟪 Cuando un libro es devuelto, el sistema notifica al usuario que lo reservó |

### Gestión de Multas
| 🟩 Agregado | 🟪 Política aplicada |
|---|---|
| 🟩 Libro | 🟪 Si no está devuelto en la fecha límite, genera multa |
| 🟩 Usuario | 🟪 No puede solicitar nuevos préstamos si tiene multas pendientes |

---

## 5. 🟥 Decisiones pendientes / riesgos

*(Rosa / Rojo — puntos que deben resolverse antes o durante la implementación)*

- 🟥 ¿Cómo manejamos libros sin stock?
- 🟥 ¿Cómo gestionamos devoluciones?

---

## 6. 🟦 Bounded Contexts

*(Turquesa / Cyan — contextos acotados del sistema)*

- 🟦 **Gestión de Usuarios** — registro, bloqueo y control de acceso de lectores.
- 🟦 **Gestión de Catálogo** — alta, actualización, eliminación y consulta de libros. Upstream.
- 🟦 **Gestión de Préstamos** — control del ciclo de alquiler y devolución de libros.
- 🟦 **Gestión de Reservas** — reserva de libros y activación cuando están disponibles.
- 🟦 **Gestión de Multas** — generación y pago de multas por devoluciones fuera de plazo.

**Context map:**

```
[Gestión de Usuarios]
        │
        ▼
[Gestión de Catálogo] ──▶ [Gestión de Préstamos] ──▶ [Gestión de Multas]
                                   │
                                   ▼
                         [Gestión de Reservas]
```

---

## 📖 Lenguaje Ubicuo

Vocabulario compartido entre negocio y código. Cualquier desviación en conversaciones, documentos o clases debe corregirse.

| Término (negocio) | Nombre en código | Definición |
|---|---|---|
| Libro | Book | Obra registrada en el catálogo. |
| Título | Title | Nombre bajo el cual se publica el libro. |
| Autor | Author | Persona que escribió el libro. |
| Categoría | Category | Clasificación temática. |
| Catálogo | Catalog | Colección completa de libros buscables. |
| Criterio de Búsqueda | SearchCriteria | Combinación de filtros aplicados por el lector. |
| Resultado de Búsqueda | SearchResult | Conjunto paginado de libros que coinciden. |
| Lector | Reader | Usuario que consulta el catálogo. |
| Bibliotecario | Librarian | Actor que mantiene el catálogo. |
| Alquiler | Rental | Registro de un préstamo activo. |
| Reserva | Reservation | Solicitud de un libro actualmente no disponible. |
| Multa | Fine | Penalización económica por devolución tardía. |

---

## 🗂️ Modelo de datos (building blocks tácticos)

### Entidad raíz: Book

```
Book                          [Aggregate Root]
 ├─ bookId       : UUID       (identidad)
 ├─ title        : Title          [VO]
 ├─ author       : Author         [VO]
 ├─ category     : Category       [VO / enum]
 ├─ isbn         : ISBN           [VO]
 └─ status       : BookStatus     (PUBLISHED, RETIRED)
```

### Entidad raíz: User

```
User                          [Aggregate Root]
 ├─ userId       : UUID       (identidad)
 ├─ name         : Name           [VO]
 ├─ email        : Email          [VO]
 └─ status       : UserStatus     (ACTIVE, BLOCKED)
```

### Entidad raíz: Rental

```
Rental                        [Aggregate Root]
 ├─ rentalId     : UUID       (identidad)
 ├─ bookId       : UUID       (ref)
 ├─ userId       : UUID       (ref)
 ├─ startDate    : Date
 ├─ dueDate      : Date
 └─ status       : RentalStatus   (ACTIVE, RETURNED, OVERDUE)
```

### Entidad raíz: Reservation

```
Reservation                   [Aggregate Root]
 ├─ reservationId : UUID      (identidad)
 ├─ bookId        : UUID      (ref)
 ├─ userId        : UUID      (ref)
 ├─ createdAt     : Date
 └─ status        : ReservationStatus (PENDING, ACTIVE, CANCELLED)
```

### Entidad raíz: Fine

```
Fine                          [Aggregate Root]
 ├─ fineId       : UUID       (identidad)
 ├─ rentalId     : UUID       (ref)
 ├─ userId       : UUID       (ref)
 ├─ amount       : Money          [VO]
 └─ status       : FineStatus     (PENDING, PAID)
```

### Value Objects

```
Title            { value: String }          — validado: no vacío, longitud máxima
Author           { firstName, lastName }    — inmutable, comparación por valor
Category         enum { FICTION, HISTORY, SCIENCE, ... }
ISBN             { value: String }          — validación de formato
SearchCriteria   { title?, author?, category? }
SearchResult     { items, totalFound, page, pageSize }
Money            { amount: Decimal, currency: String }
```

### Repositorios (interfaces en el dominio)

```typescript
interface BookRepository {
    Optional<Book> findById(BookId id);
    SearchResult search(SearchCriteria criteria, Pagination p);
}

interface RentalRepository {
    Optional<Rental> findById(RentalId id);
    List<Rental> findActiveByUser(UserId userId);
}

interface ReservationRepository {
    Optional<Reservation> findById(ReservationId id);
    List<Reservation> findPendingByBook(BookId bookId);
}

interface FineRepository {
    Optional<Fine> findById(FineId id);
    List<Fine> findPendingByUser(UserId userId);
}
```

Las implementaciones concretas (JPA, Mongo, in-memory) se definen en la etapa de Clean Architecture.

### Domain Services

```
CatalogSearchService
 └─ search(criteria: SearchCriteria): SearchResult
    ├─ valida criterios (política)
    ├─ normaliza texto (política)
    └─ delega en BookRepository

RentalService
 └─ lend(bookId, userId): Rental
    ├─ verifica disponibilidad del libro
    ├─ verifica que el usuario no tenga mora
    └─ registra el préstamo

FineService
 └─ generateIfOverdue(rentalId): Optional<Fine>
    └─ evalúa fecha límite y genera multa si aplica
```

### Application Services (puntos de entrada)

```
SearchBooksUseCase        └─ execute(input: SearchBooksInput): SearchBooksOutput
RegisterBookUseCase       └─ execute(input: RegisterBookInput): void
LendBookUseCase           └─ execute(input: LendBookInput): RentalOutput
ReserveBookUseCase        └─ execute(input: ReserveBookInput): ReservationOutput
PayFineUseCase            └─ execute(input: PayFineInput): void
```

---

## 🏗️ Estructura sugerida del repositorio

```
library-catalog/
├─ src/
│  ├─ domain/
│  │  ├─ model/
│  │  │  ├─ Book / Title / Author / Category / ISBN / BookStatus
│  │  │  ├─ User / UserStatus
│  │  │  ├─ Rental / RentalStatus
│  │  │  ├─ Reservation / ReservationStatus
│  │  │  └─ Fine / FineStatus / Money
│  │  ├─ search/
│  │  │  ├─ SearchCriteria
│  │  │  ├─ SearchResult
│  │  │  └─ CatalogSearchService
│  │  ├─ rental/
│  │  │  └─ RentalService
│  │  ├─ fines/
│  │  │  └─ FineService
│  │  └─ repository/
│  │     ├─ BookRepository
│  │     ├─ RentalRepository
│  │     ├─ ReservationRepository
│  │     └─ FineRepository
│  └─ application/
│     ├─ SearchBooksUseCase
│     ├─ RegisterBookUseCase
│     ├─ LendBookUseCase
│     ├─ ReserveBookUseCase
│     └─ PayFineUseCase
└─ README.md
```

Esta estructura se expandirá en la siguiente etapa (Clean Architecture) con capas `infrastructure/` e `interfaces/`.

---

## ✅ Definition of Ready para pasar a Clean Architecture

Antes de avanzar a la siguiente etapa, el equipo debe poder responder con claridad:

- El lenguaje ubicuo está documentado y validado con el área de negocio.
- `Book`, `User`, `Rental`, `Reservation` y `Fine` están identificadas como entidades raíz de sus respectivos agregados.
- Se distinguen entidades y value objects.
- Las interfaces de repositorios viven en el dominio, no en infraestructura.
- Los Domain Services y Application Services están diferenciados por contexto.
- Las decisiones pendientes (🟥) están priorizadas: se sabe cuáles se resuelven antes de codificar y cuáles se posponen.

---

## 📅 Siguientes etapas del proyecto

1. **DDD** ← este documento.
2. **Clean Architecture** — organizar el código por capas (domain / application / infrastructure / interfaces).
3. **Software Craftsmanship** — TDD, refactoring, principios SOLID sobre el modelo.
4. **SDD** (Specification-Driven / System Design Document) — formalizar contratos, APIs y criterios de aceptación.
5. **DevOps** — CI/CD, infraestructura como código, observabilidad, despliegue continuo.
