# xml-db-importer

## Assignment

This project is a take-home test assignment for **Trixi**, for the position of **Java Developer**.

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

## Running

```bash
docker compose up --build
```

This starts Postgres (with `schema.sql` applied automatically on first run), then the app, which downloads, parses and saves the data, and exits.

Running without Docker requires a Postgres instance with the schema applied manually (see `src/main/resources/db/init/schema.sql`) and these environment variables set: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (`DB_USER`/`DB_PASSWORD` have no default on purpose - see `docker-compose.yml` for the values used in the Docker setup).
