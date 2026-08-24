# Spring Data JPA Repository Guide

How to define, name, and query Spring Data JPA repository interfaces in PayFlow.

---

## Table of Contents
1. [What a Repository Is](#what-a-repository-is)
2. [Basic Interface Syntax](#basic-interface-syntax)
3. [Choosing a Base Interface](#choosing-a-base-interface)
4. [Method Naming Conventions (Derived Queries)](#method-naming-conventions-derived-queries)
5. [Query Keywords Reference](#query-keywords-reference)
6. [Return Type Conventions](#return-type-conventions)
7. [Custom Queries with `@Query`](#custom-queries-with-query)
8. [PayFlow Repositories](#payflow-repositories)
9. [Common Mistakes](#common-mistakes)

---

## What a Repository Is

A repository is an **interface with no implementation body** — you declare the method signatures you want, and Spring Data JPA generates a working implementation at runtime (via a dynamic proxy). This is the *Abstraction* pillar in practice: `userRepository.save(user)` hides all the SQL/JDBC underneath.

You never write `implements UserRepository { ... }` yourself. You never call `new UserRepositoryImpl()`. Spring wires the generated bean in wherever you `@Autowired` or constructor-inject the interface type.

---

## Basic Interface Syntax

```java
package com.payflow.payflow.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
```

Breaking this down:
- `public interface UserRepository` — declares the interface. No `class`, no constructor, no fields.
- `extends JpaRepository<User, UUID>` — inherits a full set of CRUD methods for free. The two generic parameters are:
  - `User` — the entity type this repository manages
  - `UUID` — the type of that entity's `@Id` field
- The empty body `{ }` is valid and common — you get `save`, `findById`, `findAll`, `deleteById`, etc. without writing a single line inside.

You only add method signatures inside the body when you need a query beyond what the base interface already provides.

---

## Choosing a Base Interface

| Interface | Gives you | When to use |
|---|---|---|
| `Repository<T, ID>` | Nothing — a marker only | Rare; full manual control |
| `CrudRepository<T, ID>` | `save`, `findById`, `findAll`, `deleteById`, `count`, `existsById` | Simple CRUD, no pagination needed |
| `PagingAndSortingRepository<T, ID>` | Adds `findAll(Pageable)`, `findAll(Sort)` | Need paged/sorted results |
| `JpaRepository<T, ID>` | Everything above + JPA-specific extras (`saveAll`, `flush`, batch deletes, `getById`) | **Default choice for this project** |

PayFlow always extends `JpaRepository<Entity, UUID>` unless there's a concrete reason not to.

---

## Method Naming Conventions (Derived Queries)

Spring Data JPA parses the **method name itself** to build a query — no SQL, no annotations needed for straightforward lookups.

Pattern: `find|read|get|query|count|exists|delete` + `By` + `PropertyExpression` + optional `And`/`Or` + optional keyword

```java
public interface UserRepository extends JpaRepository<User, UUID> {

    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // SELECT * FROM users WHERE full_name = ? AND email = ?
    Optional<User> findByFullNameAndEmail(String fullName, String email);

    // SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)
    boolean existsByEmail(String email);

    // SELECT COUNT(*) FROM users WHERE full_name = ?
    long countByFullName(String fullName);
}
```

- The part after `By` must match your entity's **field names** (Java camelCase), not the SQL column names. `findByFullName` works because the entity field is `fullName` — it doesn't matter that the column is `full_name`.
- `And`/`Or` chain multiple properties: `findByEmailAndFullName`, `findByEmailOrFullName`.
- The prefix (`find`, `get`, `read`, `query`) is interchangeable — pick one and be consistent. This project uses `find`.

---

## Query Keywords Reference

Keywords you can drop into a derived query method name, after a property:

| Keyword | Example | Meaning |
|---|---|---|
| `Is` / `Equals` | `findByStatusIs` | equality (default, can be omitted) |
| `Not` | `findByStatusNot` | inequality |
| `GreaterThan` / `LessThan` | `findByAmountGreaterThan` | numeric/date comparison |
| `GreaterThanEqual` / `LessThanEqual` | `findByAmountGreaterThanEqual` | inclusive comparison |
| `Between` | `findByCreatedAtBetween` | range, takes two params |
| `Like` / `Containing` / `StartingWith` / `EndingWith` | `findByFullNameContaining` | string pattern match |
| `IgnoreCase` | `findByEmailIgnoreCase` | case-insensitive match |
| `In` | `findByTypeIn` | matches any value in a collection |
| `IsNull` / `IsNotNull` | `findByCreatedAtIsNull` | null checks |
| `OrderBy...Asc/Desc` | `findByWalletOrderByCreatedAtDesc` | sorting, appended at the end |
| `True` / `False` | `findByActiveTrue` | boolean shortcuts |

Combine freely, e.g.:
```java
List<Transaction> findByWalletAndTypeOrderByCreatedAtDesc(Wallet wallet, TransactionType type);
```

---

## Return Type Conventions

| Return type | Use when |
|---|---|
| `Optional<T>` | Looking up a single row that might not exist — forces the caller to handle absence explicitly, avoids `null` |
| `T` | Looking up a single row you're certain must exist (rare — prefer `Optional`) |
| `List<T>` | Zero or more rows expected |
| `boolean` | `existsBy...` checks |
| `long` | `countBy...` checks |
| `Page<T>` | Paginated results — method parameter is `Pageable` |

PayFlow convention: **always `Optional<T>` for single-row lookups**, never a bare entity return, so callers can't accidentally dereference `null`.

```java
Optional<User> user = userRepository.findByEmail(email);
user.orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
```

---

## Custom Queries with `@Query`

When the method name would get unreadable, or you need a join/aggregate the naming convention can't express, write JPQL explicitly:

```java
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.currency = :currency")
    Optional<Wallet> findByUserIdAndCurrency(@Param("userId") UUID userId, @Param("currency") String currency);
}
```

- JPQL references **entity names and field names** (`Wallet`, `w.user.id`), not table/column names.
- `@Param("name")` binds a method parameter to a `:name` placeholder in the query string.
- Use `nativeQuery = true` only when JPQL genuinely can't express what you need (e.g. a Postgres-specific function) — it bypasses entity mapping and couples you to the database dialect.

```java
@Modifying
@Query("UPDATE Wallet w SET w.balance = w.balance + :amount WHERE w.id = :walletId")
int incrementBalance(@Param("walletId") UUID walletId, @Param("amount") BigDecimal amount);
```
`@Modifying` is required on any `@Query` that performs `UPDATE`/`DELETE` rather than a `SELECT` — without it Spring Data JPA rejects the query at startup.

---

## PayFlow Repositories

Concrete interfaces for this project's three entities:

```java
package com.payflow.payflow.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

```java
package com.payflow.payflow.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    List<Wallet> findByUserId(UUID userId);
    Optional<Wallet> findByUserIdAndCurrency(UUID userId, String currency);
}
```

```java
package com.payflow.payflow.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId);
    List<Transaction> findByWalletIdAndType(UUID walletId, TransactionType type);
}
```

These aren't created as actual source files yet — say the word if you want them scaffolded into `src/main/java` like the entities were.

---

## Common Mistakes

1. **Referencing SQL column names instead of Java field names** — `findByFull_name` doesn't compile; the entity field is `fullName`, so it's `findByFullName`.
2. **Forgetting `@Modifying` on write queries** — Spring Data JPA throws at startup if a `@Query` runs `UPDATE`/`DELETE` without it.
3. **Returning bare entities instead of `Optional`** for single-row lookups — invites `NullPointerException` at the call site.
4. **Putting business logic inside the repository** — repositories only fetch/persist data. Validation, calculations, and orchestration belong in a `@Service` class that calls the repository.
5. **Calling `.get()` on an `Optional` without checking** — defeats the entire purpose of using `Optional`. Use `.orElseThrow(...)` or `.map(...)` instead.
6. **Deep property traversal without checking it's mapped** — `findByWalletUserEmail` works because JPA follows `Transaction.wallet.user.email` through the relationship chain, but each hop must be an actual `@ManyToOne`/`@OneToOne` field, not a computed property.
