# DDD Planning — Sistema de Gestión de Biblioteca

## Caso de negocio

> *"Como usuario, quiero poder buscar libros por título, autor o categoría para encontrar fácilmente lo que estoy buscando."*

Este documento define el modelo de dominio que guiará la construcción de la aplicación. Es la **primera etapa** del proceso (DDD), previa a Clean Architecture, Software Craftsmanship, SDD y DevOps.

---

## 1. 📘 Visión del dominio

El sistema permite que lectores encuentren libros en el catálogo de una biblioteca aplicando filtros por **título**, **autor** y/o **categoría**. El foco inicial está puesto en el flujo de **búsqueda y consulta del catálogo**, dejando préstamos, reservas y devoluciones como contextos fuera del alcance de esta iteración.

**Objetivos del dominio:**

- Mantener un catálogo consistente y consultable de libros.
- Ofrecer búsquedas rápidas, tolerantes y paginadas.
- Alinear el vocabulario entre negocio y desarrollo.

---

## 2. 🟧 Eventos del Dominio

*(Naranja — hechos relevantes que ocurren en el sistema, en pasado)*

- 🟧 **Libro Registrado en el Catálogo**
- 🟧 **Búsqueda Iniciada**
- 🟧 **Criterios de Búsqueda Aplicados**
- 🟧 **Resultados Obtenidos**
- 🟧 **Búsqueda Sin Resultados**
- 🟧 **Libro Seleccionado**
- 🟧 **Detalle de Libro Consultado**

Estos eventos guiarán las trazas, los logs y, en etapas posteriores, la integración event-driven.

---

## 3. 🟨 Actores y 🟦 Comandos

*(Amarillo = Actor, Azul = Comando que dispara el evento)*

| 🟨 Actor | 🟦 Comando |
|---|---|
| 🟨 Bibliotecario | 🟦 Registrar Libro en el Catálogo |
| 🟨 Lector | 🟦 Ingresar Título |
| 🟨 Lector | 🟦 Ingresar Autor |
| 🟨 Lector | 🟦 Seleccionar Categoría |
| 🟨 Lector | 🟦 Ejecutar Búsqueda |
| 🟨 Sistema de Catálogo | 🟦 Filtrar Catálogo |
| 🟨 Lector | 🟦 Ver Detalle del Libro |

---

## 4. 🟩 Agregados y 🟪 Políticas

*(Amarillo lima = Agregado, Morado = Política / regla de negocio)*

| 🟩 Agregado | 🟪 Política aplicada |
|---|---|
| 🟩 Catálogo | 🟪 Normalizar texto antes de buscar (minúsculas, sin acentos) |
| 🟩 Libro | 🟪 Validar que al menos un criterio venga antes de ejecutar la búsqueda |
| 🟩 Búsqueda | 🟪 Si no hay coincidencias exactas, sugerir resultados aproximados |
| 🟩 Catálogo | 🟪 Paginar resultados cuando superen N ítems |
| 🟩 Libro | 🟪 Solo libros con estado "publicado" aparecen en resultados |

---

## 5. 🟥 Decisiones pendientes / riesgos

*(Rosa / Rojo — puntos que deben resolverse antes o durante la implementación)*

- 🟥 ¿Qué se muestra cuando la búsqueda no devuelve resultados? (sugerencias, búsqueda vacía, fallback)
- 🟥 ¿Se soporta tolerancia a errores ortográficos o búsqueda fuzzy?
- 🟥 Estrategia de paginación: offset/limit vs. cursor.
- 🟥 Si un libro está retirado o no disponible, ¿se oculta del resultado o se muestra con indicador?
- 🟥 ¿Se permiten combinaciones de criterios (AND) o búsqueda independiente por criterio (OR)?
- 🟥 ¿Coincidencia exacta o parcial (`contains`, `starts-with`)?
- 🟥 Motor de búsqueda: base de datos relacional con índices vs. motor especializado (Elastic, Meili).

---

## 6. 🟦 Bounded Contexts

*(Turquesa / Cyan — contextos acotados del sistema)*

- 🟦 **Gestión de Catálogo** — alta, actualización y retiro de libros. *Upstream.*
- 🟦 **Búsqueda de Libros** — implementa la historia de usuario actual. *Downstream del Catálogo.*
- 🟦 **Préstamos** — fuera del alcance de esta iteración, pero consumirá libros del Catálogo.

**Context map:**

```
[Gestión de Catálogo]  ──▶  [Búsqueda de Libros]  ──▶  [Préstamos]
      (upstream)              (downstream)              (futuro)
```

---

## 📖 Lenguaje Ubicuo

Vocabulario compartido entre negocio y código. Cualquier desviación en conversaciones, documentos o clases debe corregirse.

| Término (negocio) | Nombre en código | Definición |
|---|---|---|
| Libro | `Book` | Obra registrada en el catálogo. |
| Título | `Title` | Nombre bajo el cual se publica el libro. |
| Autor | `Author` | Persona que escribió el libro. |
| Categoría | `Category` | Clasificación temática. |
| Catálogo | `Catalog` | Colección completa de libros buscables. |
| Criterio de Búsqueda | `SearchCriteria` | Combinación de filtros aplicados por el lector. |
| Resultado de Búsqueda | `SearchResult` | Conjunto paginado de libros que coinciden. |
| Lector | `Reader` | Usuario que consulta el catálogo. |
| Bibliotecario | `Librarian` | Actor que mantiene el catálogo. |

---

## 🗂️ Modelo de datos (building blocks tácticos)

### Entidad raíz: `Book`

```
Book                          [Aggregate Root]
 ├─ bookId       : UUID       (identidad)
 ├─ title        : Title          [VO]
 ├─ author       : Author         [VO]
 ├─ category     : Category       [VO / enum]
 ├─ isbn         : ISBN           [VO]
 └─ status       : BookStatus     (PUBLISHED, RETIRED)
```

### Value Objects

```
Title            { value: String }          — validado: no vacío, longitud máxima
Author           { firstName, lastName }    — inmutable, comparación por valor
Category         enum { FICTION, HISTORY, SCIENCE, ... }
ISBN             { value: String }          — validación de formato
SearchCriteria   { title?, author?, category? }
SearchResult     { items, totalFound, page, pageSize }
```

### Repositorio (interfaz en el dominio)

```
interface BookRepository {
    Optional<Book> findById(BookId id);
    SearchResult search(SearchCriteria criteria, Pagination p);
}
```

La implementación concreta (JPA, Mongo, in-memory) se define en la etapa de Clean Architecture.

### Domain Service

```
CatalogSearchService
 └─ search(criteria: SearchCriteria): SearchResult
    ├─ valida criterios (política)
    ├─ normaliza texto (política)
    └─ delega en BookRepository
```

### Application Service (punto de entrada de la historia)

```
SearchBooksUseCase
 └─ execute(input: SearchBooksInput): SearchBooksOutput
```

---

## 🧭 Blueprint del dominio

```
             🟨 Lector
                │
                │ 🟦 Ejecutar Búsqueda
                ▼
        ┌────────────────────┐
        │ SearchBooksUseCase │  (Application)
        └─────────┬──────────┘
                  │
                  ▼
        ┌────────────────────────┐
        │ 🟩 CatalogSearchService │──── 🟪 Validar criterios
        │    (Domain Service)     │──── 🟪 Normalizar texto
        └────────────┬────────────┘
                     │
                     ▼
        ┌────────────────────┐     🟧 Búsqueda Iniciada
        │  BookRepository    │ ──▶ 🟧 Criterios Aplicados
        │   (interfaz)       │     🟧 Resultados Obtenidos
        └──────────┬─────────┘     🟧 Búsqueda Sin Resultados
                   │
                   ▼
             🟩 Book (Aggregate Root)
             Title · Author · Category · ISBN · Status

        🟦 Bounded Contexts:
        [Gestión de Catálogo] ──▶ [Búsqueda] ──▶ [Préstamos]
```

---

## 🏗️ Estructura sugerida del repositorio

```
library-catalog/
├─ src/
│  ├─ domain/
│  │  ├─ model/
│  │  │  ├─ Book.java
│  │  │  ├─ Title.java
│  │  │  ├─ Author.java
│  │  │  ├─ Category.java
│  │  │  ├─ ISBN.java
│  │  │  └─ BookStatus.java
│  │  ├─ search/
│  │  │  ├─ SearchCriteria.java
│  │  │  ├─ SearchResult.java
│  │  │  └─ CatalogSearchService.java
│  │  └─ repository/
│  │     └─ BookRepository.java
│  └─ application/
│     └─ SearchBooksUseCase.java
└─ README.md
```

Esta estructura se expandirá en la siguiente etapa (Clean Architecture) con capas `infrastructure/` y `interfaces/`.

---

## ✅ Definition of Ready para pasar a Clean Architecture

Antes de avanzar a la siguiente etapa, el equipo debe poder responder con claridad:

1. El lenguaje ubicuo está documentado y validado con el área de negocio.
2. `Book` está identificada como entidad raíz del agregado.
3. Se distinguen entidades y value objects.
4. La interfaz `BookRepository` vive en el dominio, no en infraestructura.
5. `CatalogSearchService` (dominio) y `SearchBooksUseCase` (aplicación) están diferenciados.
6. Las decisiones pendientes (🟥) están priorizadas: se sabe cuáles se resuelven antes de codificar y cuáles se posponen.

---

## 📅 Siguientes etapas del proyecto

1. **DDD** ← este documento.
2. **Clean Architecture** — organizar el código por capas (domain / application / infrastructure / interfaces).
3. **Software Craftsmanship** — TDD, refactoring, principios SOLID sobre el modelo.
4. **SDD (Specification-Driven / System Design Document)** — formalizar contratos, APIs y criterios de aceptación.
5. **DevOps** — CI/CD, infraestructura como código, observabilidad, despliegue continuo.