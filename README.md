# xml-db-importer

A one-shot Java batch job: it downloads a zipped XML file, parses out address-registry
records, and saves them into a SQL database. Built as a take-home test assignment for
**Trixi**, for the position of **Java Developer** (see [Assignment](#assignment) below).

Concretely, it downloads the zipped XML export for the municipality of Kopidlno from
[smartform.cz](https://www.smartform.cz/download/kopidlno.xml.zip), extracts the single
`obec` (municipality) record and its `cast_obce` (municipality part) records, and saves
both into Postgres tables.

## Tech stack

- **Java 21**, **Gradle** (wrapper included, no local Gradle install needed)
- **Spring Boot 3** / **Spring Data JPA** (Hibernate) for the DB layer
- **StAX** (`javax.xml.stream`) for streaming XML parsing - no DOM tree, no extra library
- **PostgreSQL** via **Docker** / **docker-compose**
- **JUnit 5**, **AssertJ**, **Mockito**, **H2** (in-memory DB for tests) for testing
- **Checkstyle** for a code-quality gate
- **GitHub Actions** for CI (build, checkstyle, test on every push/PR)

## Running

```bash
docker compose up --build
```

This starts Postgres (with `schema.sql` applied automatically on first run), then the app,
which downloads, parses and saves the data, and exits.

Running without Docker requires a Postgres instance with the schema applied manually (see
`src/main/resources/db/init/schema.sql`) and these environment variables set: `DB_HOST`,
`DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (`DB_USER`/`DB_PASSWORD` have no default on
purpose - see `docker-compose.yml` for the values used in the Docker setup).

To inspect the result afterwards:

```bash
docker compose exec db psql -U xmldb -d xmldb -c "SELECT * FROM obec;" -c "SELECT * FROM cast_obce;"
```

## Project structure

```
src/main/java/com/example/xmldbimporter/
├── XmlDbImporterApplication.java   Spring Boot entry point
├── config/ImportProperties.java    source-url config (import.source-url / IMPORT_SOURCE_URL)
├── download/XmlZipDownloader.java  downloads the zip over HTTP, streams out the XML entry
├── parser/                         StAX parser (XmlParser) + its DTOs (ObecData/CastObceData/ParsedData)
├── model/                          JPA entities: Obec, CastObce
├── repository/                     Spring Data JPA repositories for the entities above
├── service/ImportService.java      validates ParsedData and persists it, in one transaction
└── runner/ImportRunner.java        CommandLineRunner - wires download -> parse -> save at startup

src/main/resources/
├── application.yml                 datasource config, ddl-auto: none
└── db/init/schema.sql              manual DDL, mounted into Postgres' docker-entrypoint-initdb.d
```

The pipeline: `XmlZipDownloader` → `XmlParser` → `ImportService`, run once by `ImportRunner`
when the Spring context starts. There's no REST API - the assignment describes a program
that runs once and exits, not a service.

## Testing

```bash
./gradlew test
```

- `XmlParserTest` - parses a synthetic fixture ([sample-obec-export.xml](src/test/resources/sample-obec-export.xml))
  modelled on the real export's namespaces/structure, including "decoy" nested references
  (inside `vf:Ulice`, `vf:StavebniObjekt`) that the parser must correctly ignore.
- `XmlParserRealFileTest` - parses the actual Kopidlno export
  ([kopidlno.xml.zip](src/test/resources/kopidlno.xml.zip), 13 MB unzipped) end to end, as a
  regression guard against the parser's assumptions drifting from the real format.
- `XmlZipDownloaderTest` - spins up a local `HttpServer` (JDK built-in, no extra dependency)
  to test the download+unzip logic without hitting the network.
- `ObecRepositoryTest` / `CastObceRepositoryTest` - CRUD against an in-memory H2 database,
  using the same `schema.sql` that Postgres uses.
- `ImportServiceTest` - the validation rules (missing obec, mismatched/duplicate cast obce
  codes) and that a rejected import writes nothing to the DB.
- `ImportRunnerTest` - the download → parse → save wiring, with mocked collaborators.

## How each requirement was met

| Requirement | How |
|---|---|
| Download XML from a URL | [`XmlZipDownloader`](src/main/java/com/example/xmldbimporter/download/XmlZipDownloader.java) - `java.net.http.HttpClient`, streams the response straight into `java.util.zip.ZipInputStream` |
| Parse the XML with a standard tool | [`XmlParser`](src/main/java/com/example/xmldbimporter/parser/XmlParser.java) - StAX (`XMLStreamReader`), extracts only `kod`/`nazev`/parent reference, ignores everything else (geometry, cadastral data, parcels, ...) |
| Two tables, obec (kod, nazev) and část obce (kod, nazev, kod obce) | [`schema.sql`](src/main/resources/db/init/schema.sql) - `obec(kod, nazev)`, `cast_obce(kod, nazev, kod_obce)`; mapped by JPA entities [`Obec`](src/main/java/com/example/xmldbimporter/model/Obec.java)/[`CastObce`](src/main/java/com/example/xmldbimporter/model/CastObce.java) |
| Populate both tables from the XML (one obec, a few cast obce) | [`ImportService`](src/main/java/com/example/xmldbimporter/service/ImportService.java); verified against the real file - 1 obec (Kopidlno) + 5 cast obce |
| Schema doesn't need to be created by the program | `schema.sql` is applied by Postgres itself (`docker-entrypoint-initdb.d`) on container init, not by the app; Hibernate runs with `ddl-auto: none` |
| Any SQL database | PostgreSQL, via `docker-compose.yml` |
| Java, any framework; Spring and/or Docker as a plus | Spring Boot 3 + Spring Data JPA + Docker/docker-compose |
| Room for creativity | CI pipeline, Checkstyle gate, fail-fast validation before any DB write (missing obec, cast-obce/obec mismatch, duplicate cast-obce codes), a real-file regression test alongside the synthetic fixture, idempotent re-imports (re-running updates existing rows instead of failing on duplicate keys) |

## Assignment

> Create a Java application that downloads XML data from the internet, processes it, and stores it in an SQL database.
>
> - The zipped XML at [https://www.smartform.cz/download/kopidlno.xml.zip](https://www.smartform.cz/download/kopidlno.xml.zip) contains all addresses in the municipality of Kopidlno.
> - The goal is to write a program that downloads the file from the given URL, parses the data from the XML, and stores (some of) it in the database.
> - The application has two tables - `obec` (municipality) and `část obce` (part of municipality).
>   - For `obec`, it is enough to insert the code and name into the DB; for `část obce`, the code, name, and the code of the `obec` it belongs to.
> - The program should populate these two tables (the XML should contain one municipality - element `vf:Obec` - and a few parts of the municipality - `vf:CastObce`).
> - The program does not need to create the database schema - that can be done manually.
> - Use a standard tool for parsing (a library such as DOM, SAX, StAX, ...).
>   - It is not necessary to extract all the data present in the XML - only what will be stored in the DB.
> - Any SQL database can be used.
> - The program should be written in Java (you can use any framework you know that makes the work easier).
>   - Using Spring and/or Docker is a plus.
> - There are no limits to creativity - feel free to show what you can do. We look forward to your innovative solution.
