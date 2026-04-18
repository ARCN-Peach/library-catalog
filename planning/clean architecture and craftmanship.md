# Clean Architecture — Sistema de Gestión de Biblioteca (Python)

## Caso de negocio

> *"Como usuario, quiero poder buscar libros por título, autor o categoría para encontrar fácilmente lo que estoy buscando."*

Este documento define las **prácticas de Clean Architecture** que se aplicarán al proyecto sobre la base del modelo de dominio construido en la etapa previa (DDD). Es la **segunda etapa** del proceso, antes de Software Craftsmanship, SDD y DevOps.

**Stack técnico:** Python 3.11+ con FastAPI, SQLAlchemy, Pydantic, pytest.

---

## 1. 🎯 Propósito de aplicar Clean Architecture

Clean Architecture busca que el código del proyecto sea:

- **Independiente de frameworks.** FastAPI, SQLAlchemy o cualquier librería es un detalle, no el corazón del sistema.
- **Independiente de la UI.** Cambiar de REST a GraphQL (Strawberry), o de web a CLI (Typer), no debe romper la lógica de negocio.
- **Independiente de la base de datos.** Pasar de PostgreSQL a MongoDB (Motor) o a un motor de búsqueda como Elastic no debe afectar las reglas del dominio.
- **Testeable.** El dominio y los casos de uso deben probarse con `pytest` sin levantar servidor, base de datos ni red.
- **Independiente de agentes externos.** El dominio no sabe que existe el mundo exterior.

Para una biblioteca donde las decisiones de búsqueda (🟥 en el DDD) aún están abiertas (motor de búsqueda, estrategia de paginación, fuzzy vs. exacto), esta arquitectura nos permite **posponer esas decisiones** sin bloquear el desarrollo del dominio.

---

## 2. 📐 Principios fundamentales

### 2.1 Regla de la dependencia

> **Las dependencias apuntan siempre hacia adentro.**

Las capas externas conocen a las internas, nunca al revés. El dominio no importa nada de infraestructura ni de frameworks.

```
interfaces/  ──▶  application/  ──▶  domain/  ◀──  infrastructure/
```

### 2.2 Inversión de dependencias (DIP)

Cuando el dominio necesita algo del mundo exterior (persistir un libro, consultar un motor de búsqueda), **declara una interfaz** usando `typing.Protocol` o `abc.ABC` y deja que la capa de infraestructura la implemente.

Ejemplo del proyecto:

- El dominio define `BookRepository` como `Protocol` en `domain/ports/`.
- La infraestructura implementa `SqlAlchemyBookRepository`, `InMemoryBookRepository` o `ElasticBookRepository`.

### 2.3 Separación de responsabilidades

Cada capa tiene un motivo único para cambiar:

- **domain** cambia si cambian las reglas de negocio.
- **application** cambia si cambia la orquestación de casos de uso.
- **infrastructure** cambia si cambian los detalles técnicos (BD, frameworks).
- **interfaces** cambia si cambia la forma de exponer la app (REST, CLI, mensajería).

---

## 3. 🧱 Capas de la arquitectura

### 3.1 Capa de Dominio (`domain/`)

El núcleo. Contiene todo lo modelado en la etapa DDD.

| Elemento | Implementación en Python |
|---|---|
| `Book` (entidad raíz) | `dataclass` mutable con identidad |
| `Title`, `Author`, `Category`, `ISBN` | `@dataclass(frozen=True)` |
| `SearchCriteria`, `SearchResult` | `@dataclass(frozen=True)` |
| `BookStatus` | `enum.Enum` |
| `BookRepository` | `typing.Protocol` |
| `CatalogSearchService` | Clase plana, sin frameworks |

**Reglas de esta capa:**

- Sin imports de `fastapi`, `sqlalchemy`, `pydantic`, `logging` de frameworks, etc.
- Solo dependencias de stdlib y de otros módulos del mismo dominio.
- Value Objects con `@dataclass(frozen=True)` para inmutabilidad.
- Sin llamadas a red, archivos, ni tiempo (`datetime.now()` se inyecta mediante un puerto `Clock`).

### 3.2 Capa de Aplicación (`application/`)

Orquesta el dominio para resolver casos de uso.

| Elemento | Descripción |
|---|---|
| `SearchBooksUseCase` | Recibe input, construye `SearchCriteria`, invoca `CatalogSearchService`, devuelve output. |
| `RegisterBookUseCase` | Alta de libro (feature 1 del MVP). |
| `GetBookDetailUseCase` | Consulta del detalle. |
| DTOs de entrada/salida | `SearchBooksInput`, `SearchBooksOutput` como `@dataclass(frozen=True)`. |

**Reglas de esta capa:**

- Conoce al dominio; lo usa.
- **No conoce** la infraestructura ni la UI.
- Depende solo de puertos declarados en el dominio.
- No decide códigos de estado HTTP, no serializa JSON.

### 3.3 Capa de Infraestructura (`infrastructure/`)

Implementa los detalles técnicos que el dominio exige.

| Elemento | Descripción |
|---|---|
| `SqlAlchemyBookRepository` | Implementación del `BookRepository` usando SQLAlchemy 2.x. |
| `BookOrmModel` | Clase con `Mapped[...]`, `__tablename__`, separada de `Book` de dominio. |
| `book_mapper.py` | Funciones `to_domain()` / `to_orm()` entre `Book` y `BookOrmModel`. |
| `InMemoryBookRepository` | Implementación alternativa para tests y prototipado. |
| Config de base de datos | `engine`, `SessionLocal`, migraciones con **Alembic**. |

**Reglas de esta capa:**

- Puede conocer el dominio y la aplicación.
- Contiene todas las dependencias "sucias": frameworks, drivers, clientes HTTP.
- Cada adaptador implementa un **puerto** definido en el dominio o la aplicación.

### 3.4 Capa de Interfaces (`interfaces/`)

Expone la aplicación al mundo exterior.

| Elemento | Descripción |
|---|---|
| `book_router.py` | Router de FastAPI con endpoints `/books/search`, `/books/{book_id}`. |
| Schemas Pydantic | `SearchBooksRequest`, `SearchBooksResponse` específicos de la API. |
| `exception_handlers.py` | Mapea excepciones de dominio/aplicación a códigos HTTP. |
| OpenAPI | Generado automáticamente por FastAPI. |

**Reglas de esta capa:**

- Traduce del protocolo (HTTP) a los inputs de la aplicación y viceversa.
- No contiene reglas de negocio.
- Puede cambiar por completo (pasar a gRPC, mensajería) sin afectar dominio ni aplicación.

---

## 4. 🔌 Puertos y adaptadores (Hexagonal)

Clean Architecture y Hexagonal son compatibles. Se usará el patrón **puertos y adaptadores** con `typing.Protocol`.

### 4.1 Puertos de entrada (driving)

Protocolos que la aplicación expone al mundo para ser invocada.

```python
class SearchBooksPort(Protocol):
    def execute(self, input_data: SearchBooksInput) -> SearchBooksOutput: ...

class RegisterBookPort(Protocol):
    def execute(self, input_data: RegisterBookInput) -> RegisterBookOutput: ...

class GetBookDetailPort(Protocol):
    def execute(self, book_id: BookId) -> BookDetailOutput: ...
```

El `book_router` depende del **puerto**, no del caso de uso concreto.

### 4.2 Puertos de salida (driven)

Protocolos que la aplicación/dominio necesita del exterior.

```python
class BookRepository(Protocol):
    def find_by_id(self, book_id: BookId) -> Book | None: ...
    def search(self, criteria: SearchCriteria, pagination: Pagination) -> SearchResult: ...
    def save(self, book: Book) -> None: ...

class TextNormalizer(Protocol):
    def normalize(self, text: str) -> str: ...

class Clock(Protocol):
    def now(self) -> datetime: ...
```

---

## 5. 🗂️ Estructura de carpetas propuesta

```
library-catalog/
├─ src/
│  └─ library_catalog/
│     ├─ domain/
│     │  ├─ model/
│     │  │  ├─ book/
│     │  │  │  ├─ __init__.py
│     │  │  │  ├─ book.py
│     │  │  │  ├─ book_id.py
│     │  │  │  ├─ book_status.py
│     │  │  │  ├─ title.py
│     │  │  │  ├─ author.py
│     │  │  │  ├─ category.py
│     │  │  │  └─ isbn.py
│     │  │  └─ search/
│     │  │     ├─ __init__.py
│     │  │     ├─ search_criteria.py
│     │  │     └─ search_result.py
│     │  ├─ service/
│     │  │  └─ catalog_search_service.py
│     │  └─ ports/
│     │     ├─ book_repository.py
│     │     ├─ text_normalizer.py
│     │     └─ clock.py
│     │
│     ├─ application/
│     │  ├─ use_cases/
│     │  │  ├─ search_books_use_case.py
│     │  │  ├─ register_book_use_case.py
│     │  │  └─ get_book_detail_use_case.py
│     │  ├─ ports/
│     │  │  ├─ search_books_port.py
│     │  │  ├─ register_book_port.py
│     │  │  └─ get_book_detail_port.py
│     │  └─ dto/
│     │     ├─ search_books_input.py
│     │     └─ search_books_output.py
│     │
│     ├─ infrastructure/
│     │  ├─ persistence/
│     │  │  ├─ sqlalchemy/
│     │  │  │  ├─ book_orm_model.py
│     │  │  │  ├─ sqlalchemy_book_repository.py
│     │  │  │  └─ book_mapper.py
│     │  │  └─ in_memory/
│     │  │     └─ in_memory_book_repository.py
│     │  ├─ search/
│     │  │  └─ ascii_text_normalizer.py
│     │  └─ config/
│     │     ├─ database.py
│     │     └─ container.py       # wiring de dependencias
│     │
│     └─ interfaces/
│        └─ rest/
│           ├─ book_router.py
│           ├─ schemas/
│           │  ├─ search_books_request.py
│           │  └─ search_books_response.py
│           ├─ exception_handlers.py
│           └─ main.py             # FastAPI app
│
├─ tests/
│  ├─ unit/
│  │  ├─ domain/
│  │  └─ application/
│  ├─ integration/
│  │  └─ infrastructure/
│  └─ e2e/
│     └─ interfaces/
│
├─ alembic/                        # migraciones
├─ pyproject.toml
├─ requirements.txt
└─ README.md
```

---

## 6. 🔄 Flujo de una petición (búsqueda de libros)

```
  Cliente HTTP
        │  GET /books/search?title=...&author=...
        ▼
 ┌──────────────────────────┐
 │ book_router (FastAPI)    │  (interfaces)
 └─────────┬────────────────┘
           │ SearchBooksPort
           ▼
 ┌──────────────────────────┐
 │ SearchBooksUseCase       │  (application)
 └─────────┬────────────────┘
           │ SearchCriteria
           ▼
 ┌──────────────────────────────┐
 │ CatalogSearchService         │  (domain)
 │  + políticas 🟪               │
 └─────────┬────────────────────┘
           │ BookRepository (Protocol)
           ▼
 ┌──────────────────────────────────┐
 │ SqlAlchemyBookRepository         │  (infrastructure)
 └─────────┬────────────────────────┘
           │
           ▼
     Base de datos (PostgreSQL)
```

La **dirección de llamada** va de fuera hacia adentro, pero la **dirección de dependencia** (imports) va también de afuera hacia adentro. El dominio no depende de nadie.

---

## 7. ✅ Prácticas que se aplicarán

### 7.1 Separación estricta dominio / infraestructura

- `Book` (dominio) y `BookOrmModel` (infra) son clases **distintas**. Se usará `book_mapper.py` con funciones `to_domain()` y `to_orm()`.
- El dominio nunca importa `sqlalchemy`, `fastapi` ni `pydantic`.

### 7.2 Inyección de dependencias explícita

- Constructor injection en todas las clases (`__init__` con tipos).
- Sin decoradores mágicos que "descubran" dependencias.
- El wiring se centraliza en `infrastructure/config/container.py`, usando **`dependency-injector`** o un contenedor manual.
- FastAPI usa `Depends(...)` únicamente en la capa `interfaces/`, resolviendo los puertos al casos de uso concretos.

### 7.3 Inmutabilidad por defecto

- Value Objects: `@dataclass(frozen=True, slots=True)`.
- DTOs de aplicación: `@dataclass(frozen=True)`.
- Schemas Pydantic (solo en `interfaces/`): `model_config = ConfigDict(frozen=True)`.
- Colecciones: se devuelven tuplas o `tuple(...)` en vez de listas mutables cuando sea posible.

### 7.4 Validación en la frontera

- **Pydantic** valida los requests HTTP en `interfaces/` (formatos, tipos básicos, longitudes).
- **El dominio valida reglas de negocio** en sus constructores (ej. `Title` rechaza strings vacíos, `ISBN` verifica formato).
- Nunca se confía en que "ya vino validado de afuera" al entrar al dominio.

### 7.5 Test pyramid alineada con la arquitectura

| Tipo de test | Capa que prueba | Herramientas |
|---|---|---|
| Unit tests | Dominio (`Book`, `CatalogSearchService`, VOs) | `pytest`, sin mocks externos |
| Unit tests | Aplicación (casos de uso con `InMemoryBookRepository`) | `pytest`, fakes in-memory |
| Integration tests | Infraestructura (`SqlAlchemyBookRepository` contra Postgres) | `pytest` + **Testcontainers** |
| End-to-end | FastAPI + app + BD | `pytest` + `httpx.AsyncClient` + `TestClient` |

**Regla:** el 80% de las pruebas deben vivir en dominio y aplicación, porque ahí está el valor del negocio. Se apunta a cobertura ≥ 90% en `domain/` y `application/`.

### 7.6 Feature flags para las decisiones 🟥 del DDD

Las decisiones pendientes (búsqueda fuzzy, motor de búsqueda, paginación cursor vs. offset) se implementan como **adaptadores intercambiables** detrás del mismo puerto:

- `SqlAlchemyBookRepository` (por defecto).
- `ElasticBookRepository` (detrás de flag en configuración).

Se elige por variable de entorno o configuración, sin tocar dominio ni aplicación.

### 7.7 Excepciones tipadas de dominio

- `BookNotFoundError`, `InvalidSearchCriteriaError`, `InvalidIsbnError`, etc.
- Se lanzan desde el dominio o la aplicación.
- El handler de `interfaces/rest/exception_handlers.py` las traduce a `404`, `400`, etc.

### 7.8 DTOs en cada frontera

- **Interface → Application:** `SearchBooksRequest` (Pydantic) → `SearchBooksInput` (dataclass).
- **Application → Domain:** `SearchBooksInput` → `SearchCriteria`.
- **Domain → Application:** `SearchResult` → `SearchBooksOutput`.
- **Application → Interface:** `SearchBooksOutput` → `SearchBooksResponse` (Pydantic).

Esto evita filtrar tipos del dominio al mundo externo.

### 7.9 Logging y observabilidad fuera del dominio

- El dominio no loguea. Si necesita emitir eventos, publica **domain events** que la infraestructura captura.
- La capa de aplicación loguea a nivel de caso de uso usando `logging.getLogger(__name__)`.
- La capa de interfaces loguea peticiones/respuestas mediante middleware de FastAPI.

### 7.10 Tipado estricto

- Se aplicará `mypy --strict` sobre `src/`.
- Todas las funciones públicas tienen anotaciones completas.
- `ruff` + `black` para estilo y linting.

---

## 8. 🧭 Mapeo DDD → Clean Architecture

| Elemento DDD | Ubicación en Clean Architecture (Python) |
|---|---|
| 🟩 Entidad `Book` | `domain/model/book/book.py` |
| Value Objects (`Title`, `Author`, `Category`, `ISBN`) | `domain/model/book/` |
| 🟩 `SearchCriteria`, `SearchResult` | `domain/model/search/` |
| 🟩 `CatalogSearchService` (domain service) | `domain/service/catalog_search_service.py` |
| `BookRepository` (puerto) | `domain/ports/book_repository.py` |
| 🟪 Políticas (normalización, validación) | `domain/service/` + puertos |
| `SearchBooksUseCase` (application service) | `application/use_cases/search_books_use_case.py` |
| 🟧 Eventos de dominio (cuando aparezcan) | `domain/events/` + publisher en `infrastructure/` |
| 🟦 Bounded Context `Catálogo` / `Búsqueda` | Paquete raíz `library_catalog` |

---

## 9. 🚦 Reglas que no se negocian

1. **El dominio no importa nada de fuera del dominio.** Si un PR agrega un import `fastapi`, `sqlalchemy` o `pydantic` en `domain/`, se rechaza.
2. **Toda implementación de infraestructura implementa un puerto.** Nada de clases sueltas en infra que la aplicación use directamente.
3. **No hay lógica de negocio en routers ni en repositories.** Los routers traducen HTTP; los repositorios traducen persistencia.
4. **Los tests del dominio no importan FastAPI ni SQLAlchemy.** Si uno lo hace, está mal diseñado.
5. **Una capa externa puede depender de una interna, nunca al revés.** Se verificará con **import-linter** o revisiones de código.

---

## 10. 🛠️ Stack y herramientas

| Propósito | Herramienta |
|---|---|
| Lenguaje | Python 3.11+ |
| Framework web | FastAPI |
| ORM | SQLAlchemy 2.x |
| Validación HTTP | Pydantic v2 |
| Base de datos | PostgreSQL |
| Migraciones | Alembic |
| Inyección de dependencias | `dependency-injector` o wiring manual |
| Tests | pytest, pytest-asyncio, Testcontainers |
| Tipado | mypy (modo estricto) |
| Linter / formatter | ruff + black |
| Verificación de capas | import-linter |
| Gestión de dependencias | `pyproject.toml` + Poetry o pip-tools |

---

## 11. ✅ Definition of Ready para pasar a Software Craftsmanship

Antes de avanzar a la siguiente etapa, debe cumplirse:

1. La estructura de carpetas está creada y vacía de lógica, pero lista para recibir código.
2. Los puertos (`BookRepository`, `TextNormalizer`, puertos de entrada) están definidos como `Protocol`.
3. Existe una implementación `InMemoryBookRepository` que permite probar la aplicación sin base de datos.
4. Hay un esqueleto de `SearchBooksUseCase` con un test unitario de `pytest` que pasa.
5. `import-linter` está configurado y verificando la regla de dependencia.
6. `mypy --strict` corre limpio sobre `src/`.
7. Se documentó qué decisiones 🟥 del DDD quedan detrás de puertos para decidir después.

---

## 12. 📅 Siguientes etapas del proyecto

1. ~~**DDD**~~ ✅ Completada.
2. **Clean Architecture** ← este documento.
3. **Software Craftsmanship** — TDD con pytest, refactoring, SOLID, clean code sobre el esqueleto ya armado.
4. **SDD (Specification-Driven Development)** — contratos OpenAPI generados por FastAPI, criterios de aceptación formales.
5. **DevOps** — CI/CD (GitHub Actions), Docker, infraestructura como código (Terraform), observabilidad, despliegue continuo.