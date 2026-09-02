# Database Migrations

Flyway manages schema migrations, running automatically on application startup.

## Location

SQL migration files live in `src/main/resources/db/migration/`, named `V<version>__<description>.sql` (e.g. `V1__init_schema.sql`).

## Adding a migration

1. Create a new file: `V<next_version>__<short_description>.sql`.
2. Write plain SQL (DDL/DML) — no special Flyway syntax needed for simple scripts.
3. Start the app (`./gradlew bootRun`); Flyway applies any pending migrations before Hibernate validates the schema (`spring.jpa.hibernate.ddl-auto=validate`).

## Verifying migrations ran

```bash
psql -h localhost -p 5432 -U payflow -d payflow -c "SELECT version, description, success FROM flyway_schema_history;"
```

## Known gotcha: Spring Boot 4 modularization

In Spring Boot 4.1.0, Flyway's Spring integration (`FlywayAutoConfiguration`) was split out of `spring-boot-autoconfigure` into a dedicated starter, `org.springframework.boot:spring-boot-starter-flyway`.

Declaring only the Flyway driver dependency —

```groovy
implementation 'org.flywaydb:flyway-database-postgresql'
```

— is **not enough**. Without the starter, Spring never registers Flyway's autoconfiguration, so migrations silently never run: no error, no log line, `flyway_schema_history` never gets created, and the app just proceeds straight to Hibernate.

The fix is to also declare:

```groovy
implementation 'org.springframework.boot:spring-boot-starter-flyway'
```

Both lines are present in `build.gradle`. If migrations ever silently stop running again on a Spring Boot upgrade, check for this first: run `./gradlew bootRun` and confirm you see `org.flywaydb.core.FlywayExecutor` log lines during startup.