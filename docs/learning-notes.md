# PayFlow Learning Notes — Java, Spring Boot & JPA

A running reference of every concept, keyword, and code pattern covered while building PayFlow.

---

## Table of Contents
1. [Java Foundations](#java-foundations)
2. [Object-Oriented Programming (OOP)](#object-oriented-programming-oop)
3. [Collections](#collections)
4. [Streams & Lambdas](#streams--lambdas)
5. [Enums](#enums)
6. [Null & Exceptions](#null--exceptions)
7. [Spring Boot & JPA Annotations](#spring-boot--jpa-annotations)
8. [Entity Design Decisions Made in PayFlow](#entity-design-decisions-made-in-payflow)
9. [Complete Entity Code](#complete-entity-code)
10. [Setup Commands Reference](#setup-commands-reference)

---

## Java Foundations

### Variables — named boxes that hold values
```java
int speed = 50;      // type name = value
String name = "Mohit";
double price = 19.99;
boolean isActive = true;
```

### Methods — reusable named blocks of code
```java
public static int addNumbers(int a, int b) {
   int result = a + b;
   return result;
}

// calling it:
int total = addNumbers(5, 3); // 8
```

### Classes & Objects — blueprint vs. actual thing
```java
public class Book {
    String title;
    int pages;
}

Book book1 = new Book();  // creates a real object from the blueprint
book1.title = "Java Basics";
book1.pages = 100;
```
Each object created with `new` has its own independent copy of the fields.

### Constructors — set up an object's initial state
```java
public class Book {
    String title;
    int pages;

    Book(String title, int pages) {   // NOTE: no return type, not even void
        this.title = title;
        this.pages = pages;
    }
}

Book book1 = new Book("Java Basics", 100);
```
**Common bug:** writing `void Book(...)` makes it a regular method, not a constructor. A real constructor has *no* return type at all.

**`this.field = field`** — `this` refers to the object being built. Without it, `field = field` would just reassign the parameter to itself and leave the object's actual field untouched.

### Conditionals
```java
if (pages > 20) {
    System.out.println("long book");
} else if (pages > 300) {
    System.out.println("very long book");
} else {
    System.out.println("short book");
}
```
- `=` assigns a value. `==` checks equality. Mixing these up is a classic bug.

### Loops
```java
// for loop — known number of repetitions
for (int i = 1; i <= 5; i++) {
    System.out.println(i);
}

// while loop — repeat while condition holds
int count = 0;
while (count < 3) {
    System.out.println(count);
    count++;
}

// for-each loop — visit every item in a collection
for (Book b : books) {
    System.out.println(b.title);
}
```

---

## Object-Oriented Programming (OOP)

### The four pillars

**1. Encapsulation** — protect internal data, expose only controlled access
```java
public class Book {
    private String title;   // private = hidden from outside
    private int pages;

    public Book(String title, int pages) {
        if (pages < 0) throw new IllegalArgumentException("Pages cannot be negative");
        this.title = title;
        this.pages = pages;
    }

    public String getTitle() { return title; }
}
```
This is why `User`/`Wallet` entities use `private` fields with a `protected` no-arg constructor — controlling *how* objects are built and accessed.

**2. Abstraction** — hide complexity behind a simple interface
`userRepository.save(user)` hides all the SQL/JDBC complexity behind one method call.

**3. Inheritance** — share behavior between related classes
```java
public class Book {
    protected String title;
    protected int pages;

    public Book(String title, int pages) {
        this.title = title;
        this.pages = pages;
    }

    public void describe() {
        System.out.println(title + " has " + pages + " pages");
    }
}

public class Ebook extends Book {
    private double fileSizeMb;

    public Ebook(String title, int pages, double fileSizeMb) {
        super(title, pages);           // calls Book's constructor
        this.fileSizeMb = fileSizeMb;
    }
}
```
- `extends` — "is a kind of"
- `super(...)` — calls the parent class's constructor
- `protected` fields — accessible to subclasses, not to outside code

**4. Polymorphism** — one interface, many behaviors
```java
public class Ebook extends Book {
    @Override
    public void describe() {
        System.out.println(title + " is an ebook, " + fileSizeMb + "MB");
    }
}

Book b = new Ebook("Spring Guide", 200, 15.5);
b.describe(); // runs Ebook's version, even though the variable type is Book
```
The **actual object type** decides which method runs, not the declared variable type.

### Interfaces — a contract with no implementation
```java
public interface Payable {
    void pay(double amount);
}

public class CreditCard implements Payable {
    @Override
    public void pay(double amount) {
        System.out.println("Paid " + amount);
    }
}
```
Spring Data JPA repositories are interfaces — you declare *what* you want, Spring generates the real implementation at runtime.

### Access modifiers
| Modifier | Visible from |
|---|---|
| `private` | same class only |
| `protected` | same class + subclasses |
| `public` | anywhere |
| *(none)* | same package only |

---

## Collections

**The mental model — ask "what question am I asking?"**
- "Give me all the things, in order, possibly with repeats" → **List**
- "Give me all the *unique* things, order doesn't matter" → **Set**
- "Let me look something up *by* an identifier" → **Map**

### List — ordered, allows duplicates
```java
List<Book> books = new ArrayList<>();
books.add(book1);
books.add(book2);

for (Book b : books) {
    System.out.println(b.title);
}

books.size();       // count
books.get(0);        // access by index
```

### Set — no duplicates, unordered
```java
Set<String> uniqueEmails = new HashSet<>();
uniqueEmails.add("a@x.com");
uniqueEmails.add("a@x.com"); // ignored, already exists
```

### Map — key-value pairs, lookup by key
```java
Map<String, Integer> pageCountByTitle = new HashMap<>();
pageCountByTitle.put("Java Basics", 100);
int pages = pageCountByTitle.get("Java Basics"); // 100
```

*Why Set/Map are fast: internally use hashing — converting each item to a number that determines where it's stored, so lookups don't require scanning every item like a List would.*

---

## Streams & Lambdas

### Lambda — a compact, unnamed function
```java
// long way:
boolean isLongBook(int pages) { return pages > 100; }

// lambda:
pages -> pages > 100
```
Syntax: `(inputs) -> expression`. Parentheses optional with exactly one input.

### Stream pipeline
```java
List<String> longBookTitles = books.stream()
    .filter(book -> book.pages > 100)   // keep matching items
    .map(book -> book.title)             // transform each item
    .collect(Collectors.toList());       // gather into a list
```

- `.stream()` — turns a collection into a pipeline
- `.filter(condition)` — keep only matching items
- `.map(transform)` — convert each item into something else
- `.reduce(start, combiner)` — combine everything into one value
- `.forEach(action)` — just perform an action per item, no new collection

```java
int totalPages = books.stream()
    .map(book -> book.pages)
    .reduce(0, (sum, pages) -> sum + pages);
```

---

## Enums

A fixed, named set of allowed values — the compiler enforces that nothing else can exist.

```java
public enum TransactionType {
    CREDIT,
    DEBIT
}
```

Mapping into the database:
```java
@Enumerated(EnumType.STRING)   // stores "CREDIT" / "DEBIT" as text
@Column(nullable = false)
private TransactionType type;
```

**Never use `EnumType.ORDINAL`** (stores position number `0`, `1`, ...) — reordering or inserting new enum values later silently corrupts existing stored data. Always use `EnumType.STRING`.

---

## Null & Exceptions

### `null` — "this box exists but holds nothing"
```java
User user = null;
user.getEmail(); // NullPointerException - crashes!
```
Any non-primitive type can be `null`. Always relevant when a field is an object reference (like `Wallet.user`).

### Exceptions — try/catch/throw
```java
try {
    int result = 10 / 0;
} catch (ArithmeticException e) {
    System.out.println("Error: " + e.getMessage());
}
```
- `try { }` — code that might fail
- `catch (Type e) { }` — runs only if that specific failure happens
- `throw new SomeException("message")` — deliberately raise an error when code detects an invalid state

---

## Spring Boot & JPA Annotations

### Class-level
| Annotation | Purpose |
|---|---|
| `@Entity` | Marks a class as mapped to a database table |
| `@Table(name = "...")` | Explicitly names the table (avoids relying on Hibernate's guess) |
| `@Getter` / `@Setter` (Lombok) | Auto-generates getters/setters at compile time |
| `@RestController` | Marks a class as handling HTTP requests, returning data directly |

**Avoid `@Data` (Lombok) on entities** — it bundles `@ToString` (can trigger unwanted lazy-loading queries or crashes) and `@EqualsAndHashCode` (compares all fields, wrong for entities — should compare by `id` only). Use `@Getter`/`@Setter` individually instead.

### Field-level
| Annotation | Purpose |
|---|---|
| `@Id` | Marks the primary key field |
| `@GeneratedValue(strategy = GenerationType.UUID)` | Hibernate generates the ID value in Java before insert |
| `@Column(nullable = false, unique = true)` | Maps a field to a column with constraints matching the SQL schema |
| `@Column(name = "full_name")` | Explicit mapping when Java camelCase differs from SQL snake_case |
| `@ManyToOne(fetch = FetchType.LAZY)` | Declares "many of this entity point to one of another"; LAZY delays loading the related object until actually accessed |
| `@JoinColumn(name = "user_id", nullable = false)` | States exactly which foreign key column implements the relationship |
| `@Enumerated(EnumType.STRING)` | Maps a Java enum to a text column |

### Method-level
| Annotation | Purpose |
|---|---|
| `@PrePersist` | Runs automatically right before an entity's first INSERT — used to set `createdAt` |
| `@PreUpdate` | Runs automatically before an UPDATE — used to refresh `updatedAt` |
| `@GetMapping("/path")` | Maps an HTTP GET request to this method |

### Why `FetchType.LAZY` over `EAGER`
`EAGER` (JPA's default for `@ManyToOne`) loads the related entity immediately every time — even if unused. With many rows, this causes the **N+1 query problem** (1 query for the list + N extra queries for each related object). `LAZY` only fetches when `.getX()` is actually called.

**Caveat:** accessing a `LAZY` field after the database session has closed throws `LazyInitializationException`. This is why DTOs matter — fetch what's needed inside the service layer (session still open), then copy into a plain DTO before returning it to the controller.

---

## Entity Design Decisions Made in PayFlow

1. **ID generation**: Hibernate generates UUIDs in Java (`@GeneratedValue(strategy = GenerationType.UUID)`), not Postgres. Because of this, the `DEFAULT gen_random_uuid()` was removed from all three tables via a `V2` migration — never let two layers both claim ownership of ID generation.

2. **Never edit an already-applied Flyway migration.** Flyway checksums each migration file; editing one that already ran causes a validation failure on next startup. Always write a new migration file instead.

3. **Constructors should only accept what the *caller* decides.** Anything derived/computed/business-rule-controlled should be set inside the constructor body, not passed in:
   - `Wallet.balance` → always hardcoded to `BigDecimal.ZERO` in the constructor (a wallet can only start empty; money enters only through recorded transactions)
   - `Transaction.amount` → **is** a required constructor parameter (it's the actual data the transaction exists to represent, not derived state)

4. **Protected no-arg constructors** on every entity — satisfies JPA's reflection requirement while blocking careless `new User()` calls that would produce half-built objects.

5. **Money fields always use `BigDecimal`, never `double`/`float`** — avoids floating-point rounding errors. Matches SQL's `NUMERIC(19,4)`.

6. **Timestamps always use `Instant`, never `LocalDateTime`** — `Instant` is unambiguous UTC; `LocalDateTime` has no timezone information.

7. **IDs use `UUID`, not auto-incrementing integers** — prevents leaking information (e.g., guessing total row counts) and is safe to expose in URLs.

8. **Unidirectional relationships only, for now** — `Wallet` knows about `User`, but `User` doesn't have a `List<Wallet>` back-reference. Avoids bidirectional sync bugs and serialization issues. Add the reverse side later only if there's a concrete need.

9. **`ddl-auto=validate`, never `update` or `create`** — Hibernate checks that entities match the real schema and fails loudly on mismatch, but never auto-modifies the schema itself. Flyway alone owns schema changes.

---

## Repositories

### What a Repository is

The layer responsible for talking to the database — saving, finding, deleting entities. In Spring Data JPA, you write an `interface` (no implementation code at all), and Spring generates a real, working implementation automatically at startup using a **proxy** (a hidden class Spring creates behind the scenes that implements your interface).

### The base interface: `JpaRepository<EntityType, IdType>`

```java
public interface UserRepository extends JpaRepository<User, UUID> {
}
```

- `public interface UserRepository` — an interface, not a class: you're declaring *what* you want, not *how* it works
- `extends JpaRepository<User, UUID>` — inherits a full set of ready-made methods, specialized for the `User` entity, whose primary key type is `UUID`

This single empty interface already gives you, for free, with zero code written:

```java
userRepository.save(user);          // insert or update
userRepository.findById(id);        // returns Optional<User>
userRepository.findAll();           // returns List<User>
userRepository.deleteById(id);
userRepository.count();
userRepository.existsById(id);
```

### Where the file goes

Same package as the entity it belongs to (feature-based structure) — e.g. `UserRepository` lives in `com.payflow.payflow.user`, right next to `User.java`.

### Derived query methods — Spring writes the SQL from the method name

You can declare your own methods, and Spring Data JPA parses the method *name itself* to generate the correct query — no `@Query` or SQL required for simple cases.

```java
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
```

Spring reads `findByEmail` as: "find one `User` where the `email` column equals the given argument," and builds the SQL automatically (`SELECT * FROM users WHERE email = ?`).

**Return type matters:**
- A lookup that might not find anything → wrap in `Optional<EntityType>` (e.g. `findByEmail`, since a given email might not exist)
- A lookup that returns many rows → `List<EntityType>` (e.g. "find all wallets for a user" — could be zero, one, or many results)

### Naming pattern reference

The method name is parsed piece by piece. General shape:

```
find/get/count/exists + By + FieldName + (Condition keyword, optional) + (And/Or + FieldName...)
```

| Method name | Meaning | Generated SQL (conceptually) |
|---|---|---|
| `findByEmail(String email)` | one match by exact field value | `WHERE email = ?` |
| `findByUser(User user)` | match by a relationship field | `WHERE user_id = ?` |
| `findAllByWallet(Wallet wallet)` | many matches by a relationship field | `WHERE wallet_id = ?` |
| `findByEmailAndFullName(String email, String name)` | multiple conditions, AND | `WHERE email = ? AND full_name = ?` |
| `findByPagesGreaterThan(int pages)` | comparison keyword | `WHERE pages > ?` |
| `existsByEmail(String email)` | returns boolean, no full row fetch | `SELECT EXISTS(... WHERE email = ?)` |
| `countByType(TransactionType type)` | returns a count | `SELECT COUNT(*) ... WHERE type = ?` |

**Key rule:** the field name after `By` must match an actual field on the entity (or a field on a related entity, using the relationship field's name — e.g. `findByUser`, not `findByUserId`, since the entity's field is called `user`, a `User` object, not a raw ID).

### Applied to PayFlow — relationship-based lookups

```java
// WalletRepository — find wallets belonging to a specific user
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    List<Wallet> findByUser(User user);
}

// TransactionRepository — find transaction history for a specific wallet
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByWallet(Wallet wallet);
}
```

Why `findByUser(User user)` and not `findByUserId(UUID userId)`: the `Wallet` entity's field is literally named `user` and is of type `User` (a `@ManyToOne` relationship) — Spring Data JPA can navigate relationship fields directly, matching against the foreign key column under the hood without you writing the join yourself.

### When derived method names aren't enough: `@Query`

For anything more complex than simple field matching (joins across multiple relationships, aggregations, custom sorting logic), you can write JPQL (JPA's own query language, similar to SQL but operates on entity/field names instead of table/column names) directly:

```java
@Query("SELECT t FROM Transaction t WHERE t.wallet.id = :walletId ORDER BY t.createdAt DESC")
List<Transaction> findRecentTransactionsByWalletId(@Param("walletId") UUID walletId);
```

Not needed yet for PayFlow's current scope — worth knowing this escape hatch exists once naming conventions get too complex to express cleanly.

### Full repository files (PayFlow, current state)

```java
package com.payflow.payflow.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
```

```java
package com.payflow.payflow.wallet;

import com.payflow.payflow.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    List<Wallet> findByUser(User user);
}
```

```java
package com.payflow.payflow.transaction;

import com.payflow.payflow.wallet.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByWallet(Wallet wallet);
}
```

---

## DTOs, Service, and Controller — the full request flow

### Why DTOs exist (never return entities directly from a controller)

Returning a raw `@Entity` from a controller leaks your database structure to the outside world (e.g. a future `passwordHash` field would silently appear in API responses), and conflates "what a client sends you" with "what you store" — two genuinely different shapes as a project grows.

**Records are the natural fit for DTOs** — simple, immutable containers, no setters, no JPA lifecycle concerns:

```java
// What the client sends in:
package com.payflow.payflow.user.dto;

public record CreateUserRequest(String email, String fullName) {
}
```

```java
// What we send back out:
package com.payflow.payflow.user.dto;

import java.util.UUID;

public record UserResponse(UUID id, String email, String fullName) {
}
```

Record fields are accessed like methods, not fields: `request.email()`, not `request.email`.

### The Controller → Service → Repository flow

```
HTTP POST /api/users  (JSON body)
        ↓
UserController.createUser()   ← translates HTTP ↔ Java
        ↓
UserService.createUser()      ← business rules (duplicate email check)
        ↓
UserRepository.save()         ← actual database interaction
        ↓
Postgres
```

Each layer only knows about the layer directly below it. This is what makes each piece independently testable and replaceable.

### `@Service` — where business logic lives

```java
package com.payflow.payflow.user;

import com.payflow.payflow.user.dto.CreateUserRequest;
import com.payflow.payflow.user.dto.UserResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(CreateUserRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

        User user = new User(request.email(), request.fullName());
        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getFullName());
    }
}
```

- `@Service` — tells Spring to manage this class as a bean, same underlying mechanism as `@RestController`, `@Component`
- **Constructor injection** — the class declares what it needs (`UserRepository`) as a constructor parameter; Spring supplies the real implementation automatically. Preferred over field-level `@Autowired` for real application code (field injection remains normal in test classes).
- Business rules (like "no duplicate emails") live here, never in the controller — so the same logic is reusable from any entry point (HTTP, a background job, a CLI tool) without duplication.

### `@RestController` — the HTTP-facing layer

```java
package com.payflow.payflow.user;

import com.payflow.payflow.user.dto.CreateUserRequest;
import com.payflow.payflow.user.dto.UserResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }
}
```

- `@RequestMapping("/api/users")` — class-level shared URL prefix for every endpoint in this controller
- `@PostMapping` — maps HTTP POST requests (creating something new, per REST conventions)
- `@RequestBody` — tells Spring to auto-convert the incoming JSON body into a `CreateUserRequest` object (via Jackson, bundled with Spring Web)
- The controller's only job: translate HTTP ↔ Java, and delegate everything else to the service — no business logic here

### REST conventions reference

| HTTP Method | Meaning | Request body? |
|---|---|---|
| `GET` | Retrieve, never changes anything | No |
| `POST` | Create something new | Yes |
| `PUT` | Replace an existing thing entirely | Yes |
| `PATCH` | Partially update | Yes |
| `DELETE` | Remove something | Usually no |

| Status Code | Meaning |
|---|---|
| `200 OK` | Success, returning data |
| `201 Created` | Success, a new resource now exists |
| `204 No Content` | Success, nothing to return |
| `400 Bad Request` | Malformed/invalid request |
| `404 Not Found` | Resource doesn't exist |
| `409 Conflict` | Conflicts with current state (e.g. duplicate email) |
| `500 Internal Server Error` | Unhandled server-side bug |

*Note: the current `createUser` endpoint returns a plain `UserResponse` with Spring's default `200 OK`. A refinement for later: wrap it in `ResponseEntity<UserResponse>` and explicitly return `201 Created`, since this endpoint creates a new resource.*

### Testing the endpoint manually

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"email": "mohit@payflow.com", "fullName": "Mohit Kumar"}'
```

### Unit test vs. integration test

- **Unit test** — tests one piece of code in complete isolation, no Spring/database involved (e.g. testing `UserService` with a *mocked* `UserRepository`, using Mockito).
- **Integration test** — tests multiple real pieces working together (e.g. `UserRepositoryTest`, which uses `@DataJpaTest` and a real Postgres connection). The tell: if a test needs `@Autowired` or a running database, it's an integration test.

---

## Wallet and Transaction — Repeating the Pattern

### Why DTOs use IDs, not nested entities

A client can't send a whole `User`/`Wallet` object over HTTP — only an identifier. And a response should never embed a full related entity (leaks internals, risks `LazyInitializationException` on `LAZY` relationships). Always reduce a relationship to its `UUID` in both directions:

```java
// wallet/dto/CreateWalletRequest.java
public record CreateWalletRequest(UUID userId, String currency) {
}

// wallet/dto/WalletResponse.java
public record WalletResponse(UUID id, UUID userId, String currency, BigDecimal balance, Instant createdAt) {
}

// transaction/dto/CreateTransactionRequest.java
public record CreateTransactionRequest(UUID walletId, BigDecimal amount, TransactionType type) {
}

// transaction/dto/TransactionResponse.java
public record TransactionResponse(UUID id, UUID walletId, BigDecimal amount, TransactionType type, Instant createdAt) {
}
```

### WalletService — looking up a related entity before constructing

```java
package com.payflow.payflow.wallet;

import com.payflow.payflow.user.User;
import com.payflow.payflow.user.UserRepository;
import com.payflow.payflow.wallet.dto.CreateWalletRequest;
import com.payflow.payflow.wallet.dto.WalletResponse;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public WalletService(WalletRepository walletRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    public WalletResponse createWallet(CreateWalletRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = new Wallet(user, request.currency());
        Wallet savedWallet = walletRepository.save(wallet);

        return new WalletResponse(
                savedWallet.getId(),
                savedWallet.getUser().getId(),
                savedWallet.getCurrency(),
                savedWallet.getBalance(),
                savedWallet.getCreatedAt()
        );
    }
}
```

**New pattern here:** a DTO only carries an id (`request.userId()`), but the entity's constructor needs the real object (`Wallet(User user, String currency)`). So the service must **look up the related entity first** — `findById(...).orElseThrow(...)` — before it can construct anything. This is the standard shape any time one entity references another.

### TransactionService — the first real cross-entity business logic

```java
package com.payflow.payflow.transaction;

import com.payflow.payflow.transaction.dto.CreateTransactionRequest;
import com.payflow.payflow.transaction.dto.TransactionResponse;
import com.payflow.payflow.wallet.Wallet;
import com.payflow.payflow.wallet.WalletRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public TransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        BigDecimal newBalance;
        Wallet wallet = walletRepository.findById(request.walletId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (request.type() == TransactionType.CREDIT) {
            newBalance = wallet.getBalance().add(request.amount());
        } else {
            newBalance = wallet.getBalance().subtract(request.amount());
        }

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        Transaction transaction = new Transaction(wallet, request.amount(), request.type());
        Transaction savedTransaction = transactionRepository.save(transaction);

        return new TransactionResponse(
                savedTransaction.getId(),
                savedTransaction.getWallet().getId(),
                savedTransaction.getAmount(),
                savedTransaction.getType(),
                savedTransaction.getCreatedAt()
        );
    }
}
```

**New concepts introduced here:**

- **`BigDecimal.add()` / `.subtract()`** — `BigDecimal` is immutable, so arithmetic can't use `+`/`-`; these methods return a *new* `BigDecimal` rather than modifying the original in place. Always capture the result: `newBalance = wallet.getBalance().add(request.amount());`
- **Comparing enums with `==`, not `.equals()`** — `request.type() == TransactionType.CREDIT` is safe and idiomatic because each enum constant is a single guaranteed unique object in memory.
- **Two separate saves for two separate concerns** — the `Transaction`'s `amount` field always stores the actual amount moved (`request.amount()`); the `Wallet`'s `balance` field stores the running total (`newBalance`). Don't conflate the two — a very easy mistake to make since both are `BigDecimal` and both relate to money.

**WalletController / TransactionController** — identical shape to `UserController`: `@RestController` + `@RequestMapping`, constructor-injected service, one `@PostMapping` method that delegates straight through. No new concepts.

---

## Day 10 — Global Exception Handling & Validation

### Custom exceptions — extending `RuntimeException`

```java
package com.payflow.payflow.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

```java
package com.payflow.payflow.common.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
```

`extends RuntimeException` — same mechanism as `Ebook extends Book`; `super(message)` passes the message up to the parent constructor, identical to `super(title, pages)` in the `Ebook` example. Services now throw these specific types instead of generic `IllegalStateException`/`RuntimeException`, so the meaning is explicit in the code itself.

### `@ControllerAdvice` + `@ExceptionHandler` — centralized error handling

```java
package com.payflow.payflow.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<String> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
```

- **`@ControllerAdvice`** — Spring scans every controller in the app; if any throws an exception, this class is checked first for a matching handler, instead of falling through to a generic `500`
- **`@ExceptionHandler(SomeException.class)`** — one method per exception type this class knows how to handle
- **`MethodArgumentNotValidException`** — Spring's own built-in exception, thrown automatically when `@Valid` validation fails. Unlike the custom exceptions (single message), it can carry *multiple* field errors at once, so `Map<String, String>` (field name → message) is the natural fit, built by looping over `ex.getBindingResult().getFieldErrors()` with a for-each loop

**Result:** `409` for duplicate email, `404` for missing user/wallet, `400` with a clean field-level error map for bad input — no raw stack traces or generic 500s reach the client for any of these expected situations.

### Bean Validation — rejecting bad input at the API boundary

Annotations go directly on record parameters:

```java
public record CreateUserRequest(
    @NotBlank @Email String email,
    @NotBlank String fullName
) {
}

public record CreateWalletRequest(
    @NotNull UUID userId,
    @NotBlank String currency
) {
}

public record CreateTransactionRequest(
    @NotNull UUID walletId,
    @NotNull @Positive BigDecimal amount,
    TransactionType type
) {
}
```

| Annotation | Applies to | Checks |
|---|---|---|
| `@NotBlank` | `String` | Not null and not just whitespace |
| `@NotNull` | Any non-`String` type (`UUID`, `BigDecimal`, etc.) | Not null |
| `@Positive` | Numbers | Greater than zero |
| `@Email` | `String` | Valid email format |

**Why `type` (a `TransactionType` enum) needs no annotation:** if a client sends an invalid value like `"type": "BANANA"`, Jackson fails to convert the JSON into the enum *during parsing itself*, before validation ever runs — the enum's fixed set of values already guarantees correctness at the type level.

**Validation must be triggered explicitly with `@Valid`** on the controller parameter — annotations on the DTO alone do nothing by themselves:

```java
@PostMapping
public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
    return userService.createUser(request);
}
```

### Why validation belongs on the DTO, not the entity

- Validating at the DTO/controller boundary rejects bad requests **immediately**, before any service logic or database calls run — entity-level validation (via Hibernate) only fires right before an `INSERT`/`UPDATE`, after everything else has already run.
- The DTO represents "what a valid API request looks like"; the entity represents "what a valid database row looks like" — these are different contracts, even when they look similar today. Same underlying principle as never returning raw entities from controllers.
- The entity's `@Column(nullable = false)` remains as a last-resort database-level safety net — but the primary, fast-failing checks belong on the DTO.

---

## `@Transactional` — Atomicity for Multi-Step Database Operations

### The problem

`TransactionService.createTransaction` performs two separate database writes: `walletRepository.save(wallet)` and `transactionRepository.save(transaction)`. Without protection, if anything fails between them, one succeeds and the other doesn't — the wallet's balance changes with no matching transaction record to explain it. **This was reproduced for real**: testing without `@Transactional` produced a wallet balance of `700` backed by only one `500` transaction row — a genuinely corrupted ledger, not just a theoretical risk.

### The concept: a database transaction (ACID)

A group of database operations that either **all** succeed, or are **all undone together** (rolled back) — no partial state is ever left behind.

- **Atomicity** — all-or-nothing (the guarantee that fixes this bug)
- **Consistency** — the database always moves between valid states
- **Isolation** — concurrent transactions don't interfere with each other
- **Durability** — committed changes survive a crash

### The fix

```java
import org.springframework.transaction.annotation.Transactional;

@Transactional
public TransactionResponse createTransaction(CreateTransactionRequest request) {
    // unchanged — both saves now succeed or roll back together
}
```

One annotation wraps the entire method in a real database transaction. If any exception is thrown anywhere inside — including a mid-method crash — **every write already performed in this method is automatically rolled back**, including saves that already ran.

**Import carefully:** use `org.springframework.transaction.annotation.Transactional` (Spring's version), not the `jakarta.transaction` one.

### Verified twice, in a real database

1. **Simulated crash test** — added a deliberate `throw new RuntimeException(...)` right after `walletRepository.save(wallet)`. With `@Transactional` present, the wallet's balance remained completely unchanged after the crash — proving the already-executed save was rolled back.
2. **Accidental real-world reproduction** — a request was sent while `@Transactional` was temporarily missing, producing a wallet balance of `700` with only a `500` transaction row in the database — a genuine, visible case of ledger corruption, immediately fixed once `@Transactional` was restored.

---

## Environment Profiles — dev vs. prod Configuration

### The problem

A single `application.properties` hardcodes local database credentials — fine for development, wrong for production, where credentials differ and should never be committed to source control.

### The fix: Spring Profiles — one file per environment

**`application.properties`** (shared/default config):
```properties
spring.application.name=payflow
spring.profiles.active=dev
```

**`application-dev.properties`**:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/payflow
spring.datasource.username=payflow
spring.datasource.password=payflow_dev_pw
spring.jpa.hibernate.ddl-auto=validate
```

**`application-prod.properties`**:
```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
```

`${DATABASE_URL}` reads from an **environment variable** on the machine running the app, rather than a hardcoded value — real credentials never live in a file that gets committed to Git.

### Switching which profile is active

| Method | How |
|---|---|
| Environment variable (real production way) | `export SPRING_PROFILES_ACTIVE=prod` before starting the app — any Spring property can be set this way: uppercase, dots → underscores |
| Command-line argument | `./gradlew bootRun --args='--spring.profiles.active=prod'` |
| IntelliJ Run Configuration | Add `SPRING_PROFILES_ACTIVE=prod` as an environment variable in the run config |

Environment variables **override** whatever's written in the properties files — this is why real deployments never require editing code or config, just setting variables on the target server.

---

## Optimistic Locking — Preventing Race Conditions

### The problem `@Transactional` does NOT solve

`@Transactional` guarantees that all writes *within one method call* succeed or fail together. It says nothing about two *separate, simultaneous* calls to the same method, racing against each other.

**The race condition, concretely:** two requests debit ₹400 each from a ₹500 wallet at nearly the same instant. If both read `balance = 500` before either writes back, both independently calculate `newBalance = 100` — and the wallet ends up at `100` instead of correctly going negative/being rejected, silently losing ₹300 worth of debits with no error at all.

### The fix: `@Version`

```java
import jakarta.persistence.Version;

@Version
private Long version;
```

A hidden field JPA manages entirely automatically — never set or read manually in business logic. On every update, Hibernate:
1. Increments the version number
2. Includes the *original* version in the `UPDATE`'s `WHERE` clause (e.g. `WHERE id = ? AND version = 0`)

If another transaction already updated the row in the meantime (bumping the version), the `WHERE` clause matches zero rows, and Hibernate throws `OptimisticLockingFailureException` — the losing request fails loudly instead of silently overwriting a correct update.

**Requires a new column via migration** (never edit an applied migration):
```sql
-- V3__add_wallet_version.sql
ALTER TABLE wallets ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
```

### Verifying it — and an honest limitation of manual testing

Sequential `curl` calls in separate terminal tabs almost never truly overlap (human reaction time between commands is milliseconds too slow). Even launching two requests genuinely in parallel with bash's `&`/`wait`:

```bash
curl -X POST .../api/transactions -d '{...}' & \
curl -X POST .../api/transactions -d '{...}' & \
wait
```

...may not reliably force a collision on a fast local database — a full read-modify-write cycle can complete in single-digit milliseconds. **Indirect verification works just as well:** after N successful concurrent-ish writes, check that `version` incremented by exactly N, and that the balance reflects the exact sum of all writes, with nothing lost. If the numbers match exactly, the locking mechanism is proven sound — a real collision under genuine load would surface as `OptimisticLockingFailureException` on the losing request, which the current code doesn't yet catch/handle gracefully (a good candidate for a future `@ExceptionHandler` in `GlobalExceptionHandler`).

### Optimistic vs. pessimistic locking (concept only, optimistic chosen here)

- **Optimistic** (used here) — assumes conflicts are rare; only checks for conflict at save time via the version number; losing writes fail and can be retried
- **Pessimistic** — locks the row the moment it's read for update, blocking all other readers/writers until released; heavier-handed, reserved for cases where conflicts are frequent/expected

---

## Idempotency Keys — Preventing Duplicate Charges on Retry

### The problem

A client sends a debit request; the request succeeds on the server, but the *response* is lost (network drop, timeout). The client can't tell "it failed" from "it succeeded but I didn't hear back," so it retries — and without protection, the same debit gets processed twice.

**Idempotent**: an operation where doing it once has the same effect as doing it many times. `GET` is naturally idempotent; `POST` (create/modify) is not, by default — idempotency keys close this gap.

### The mechanism

The client generates a unique key (e.g. a UUID) once, before sending a request, and includes it with every attempt — including retries. The server remembers every key it has already processed; if a request arrives with a key already seen, it returns the *original* result without reprocessing.

### Implementation, piece by piece

**1. Database — unique, required column** (new migration; existing test data wiped first since it predates the column and can't satisfy `NOT NULL`):
```sql
-- V4__add_idempotency_key.sql
DELETE FROM transactions;
ALTER TABLE transactions ADD COLUMN idempotency_key VARCHAR(255) NOT NULL UNIQUE;
```
*Real-world note: you can't just `DELETE` production data. The safe pattern there is: add the column nullable first, backfill every existing row with a placeholder, then lock in `NOT NULL`/`UNIQUE` afterward — a common safe-migration technique for live tables.*

**2. Entity** — new field, mirroring the SQL constraint, plus a constructor update:
```java
@Column(name = "idempotency_key", nullable = false, unique = true)
private String idempotencyKey;

public Transaction(Wallet wallet, BigDecimal amount, TransactionType type, String idempotencyKey) {
    this.wallet = wallet;
    this.amount = amount;
    this.type = type;
    this.idempotencyKey = idempotencyKey;
}
```

**3. DTO** — add the field, validated like any other required `String`:
```java
public record CreateTransactionRequest(
    @NotNull UUID walletId,
    @NotNull @Positive BigDecimal amount,
    TransactionType type,
    @NotBlank String idempotencyKey
) {
}
```

**4. Repository** — one more derived query method, identical shape to every prior one:
```java
Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
```

**5. Service — the actual logic, an early return at the top of the method:**
```java
@Transactional
public TransactionResponse createTransaction(CreateTransactionRequest request) {
    Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(request.idempotencyKey());
    if (existing.isPresent()) {
        Transaction alreadyProcessed = existing.get();
        return new TransactionResponse(
                alreadyProcessed.getId(),
                alreadyProcessed.getWallet().getId(),
                alreadyProcessed.getAmount(),
                alreadyProcessed.getType(),
                alreadyProcessed.getCreatedAt()
        );
    }

    // ...unchanged: look up wallet, update balance, save, create + save the new Transaction
    // (now passing request.idempotencyKey() as the constructor's 4th argument)
}
```

**New concept: early return.** `return` doesn't just end a method with its final value — it can exit the method immediately, partway through, the moment a condition is met, skipping everything written below it for that call. This is the entire mechanism that makes idempotency work: a retry with a known key never reaches the wallet-update logic at all.

**6. Controller — no changes needed.** Since `idempotencyKey` was added directly to the DTO (arrives in the JSON body), the existing `@RequestBody`-based endpoint already carries it through automatically. *(Real-world alternative: many production APIs, e.g. Stripe, pass the key via an `Idempotency-Key` HTTP header instead, read with `@RequestHeader("Idempotency-Key") String key` — a legitimate alternative design, since the key is arguably request metadata rather than business data. Both are valid.)*

### Verified against a real database (sequential retry)

Sent the identical request twice with `idempotencyKey: "test-key-001"`. Both responses returned the **exact same transaction `id` and `createdAt`** — proof the second call never created a new row. Confirmed directly: `SELECT count(*) FROM transactions WHERE idempotency_key = 'test-key-001'` returned exactly `1`, and the wallet balance reflected only one `100` credit despite two identical requests.

### A real race condition discovered and fixed: check-then-act

Testing two genuinely parallel requests with the **same, brand-new** idempotency key (using `&`/`wait`, same technique as the optimistic-locking test) exposed a real gap: `findByIdempotencyKey` and the eventual `save` aren't atomic together — if both requests run the check before either has saved, **both** can see "no existing transaction" and both proceed. This is a classic **check-then-act race condition**.

**What actually caught it:** the database's own `UNIQUE` constraint on `idempotency_key`. The first request to actually `INSERT` succeeds; the second hits `duplicate key value violates unique constraint "transactions_idempotency_key_key"`, surfacing as `DataIntegrityViolationException`.

**Handled with one more `@ExceptionHandler`:**
```java
@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<String> handleIntegrityViolation(DataIntegrityViolationException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body("someone already used this idempotency key");
}
```
*(Note: this exception is broad — it fires on any DB constraint violation, not only idempotency-key collisions. Fine for the app's current size; worth narrowing with `ex.getMessage()` inspection as the app grows.)*

**Critically, `@Transactional` still protected the money.** The losing request's wallet-balance update was fully rolled back when its `Transaction` insert failed — verified directly: wallet balance after the race showed the expected value based on exactly one successful credit, not two. Two independent safety mechanisms (the `UNIQUE` constraint catching the duplicate, and `@Transactional` guaranteeing atomic rollback) worked together correctly under a genuine, deliberately-triggered concurrent collision.

### `@RequestHeader` — the alternative design (not used, but understood)

Many production APIs (e.g. Stripe) pass the idempotency key via an HTTP **header** rather than the request body, since it's arguably request metadata rather than business data:

```java
@PostMapping
public TransactionResponse createTransaction(
    @RequestHeader("Idempotency-Key") String idempotencyKey,
    @Valid @RequestBody CreateTransactionRequest request
) {
```

- `@RequestBody` pulls from the request's **body** (the JSON payload); `@RequestHeader` pulls from its **headers** (request metadata, like `Content-Type`)
- `@RequestHeader(value = "...", required = false)` makes a header optional — missing header becomes `null` instead of a `400`

PayFlow's implementation keeps the key in the DTO/body instead — simpler, no header-name coordination needed, and an equally legitimate design choice.

---

## Spring Security + JWT — In Progress

### Authentication vs. Authorization
- **Authentication** — "who are you?" (login, proving identity)
- **Authorization** — "what are you allowed to do?" (access control on resources)

### Password hashing — why and how

Passwords are never stored in plain text. **Hashing** is one-way: `"mypassword123"` → a fixed-length string, with no way to reverse it back to the original. Login works by hashing the entered password and comparing *hashes*, never by decrypting anything.

**Why BCrypt specifically** (not generic hashes like MD5/SHA-256): BCrypt is deliberately slow and adaptive (configurable to stay slow as hardware improves), defeating brute-force attacks. It also automatically applies a random **salt** per password, so two identical passwords produce different stored hashes — defeating precomputed "rainbow table" attacks. Spring Security's `BCryptPasswordEncoder` provides `.encode(rawPassword)` (hash it) and `.matches(rawPassword, storedHash)` (verify it) — raw passwords are never manually compared.

### Adding Spring Security — the immediate, dramatic effect

```groovy
implementation 'org.springframework.boot:spring-boot-starter-security'
```

**The moment this is added, Spring Security locks down the ENTIRE application by default** — every endpoint, including previously-open ones like `/api/health`, immediately starts requiring authentication. This is intentional: "secure by default, then deliberately open up what should be public."

Verified directly: after adding the dependency and a full `./gradlew clean build --refresh-dependencies` + restart, `curl -i http://localhost:8080/api/health` returned:
```
HTTP/1.1 401
WWW-Authenticate: Basic realm="Realm", charset="UTF-8"
```
`WWW-Authenticate: Basic` shows Spring Security defaulting to HTTP Basic Auth (username/password) on every request until properly configured.

### Password field added to User (Stage 1, complete)

- Migration `V5__add_user_password.sql` — `ALTER TABLE users ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT 'CHANGE_ME';` (placeholder default for existing rows, since deleting them would cascade-break their wallets/transactions — the safer "Option B" backfill technique, not a wipe)
- `User.java` — new `password` field (`@Column(nullable = false)`), added as a required constructor parameter
- `CreateUserRequest` — new `password` field, `@NotBlank` validated

### `@Configuration` + `@Bean` — when to use this vs. a direct annotation

**The core rule:** annotate a class directly (`@Service`, `@RestController`, `@Repository`-style interfaces) only when you own the file and can edit it. For a class that comes from an external library (like `BCryptPasswordEncoder`, from Spring Security's jar), you cannot add an annotation to code you don't own — instead, write a `@Bean` method inside a `@Configuration` class that constructs it yourself and hands the result to Spring:

```java
package com.payflow.payflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- **Bean** — simply the general term for any object Spring creates and manages, so other classes can receive it via constructor injection instead of building it themselves. `@Service`/`@RestController` classes are beans too — this isn't a new kind of thing, just a name for what's already been happening.
- **Special case worth knowing:** `@Repository`-style interfaces (`extends JpaRepository<...>`) don't need `@Configuration`/`@Bean` either — Spring Data JPA has its own automatic scanning mechanism specifically for repository interfaces. `@Configuration`+`@Bean` is the general-purpose fallback for everything else that has no such automatic detection.

**`UserService` updated** to inject the real `PasswordEncoder` bean (not `SecurityConfig` itself — a config class is a "recipe," never used directly elsewhere) and hash the password before saving:
```java
private final PasswordEncoder passwordEncoder;
// ...
String hashedPassword = passwordEncoder.encode(request.password());
User user = new User(request.email(), hashedPassword, request.fullName());
```

### Debugging story: `ClassNotFoundException: PasswordEncoder` despite the dependency being present

After adding `SecurityConfig`, the app failed to start with `Failed to introspect Class [SecurityConfig]` → `NoClassDefFoundError` → `ClassNotFoundException: org.springframework.security.crypto.password.PasswordEncoder`. Verified the dependency itself was genuinely present and resolved correctly via `./gradlew dependencies --configuration compileClasspath | grep security`. The actual cause: IntelliJ's DevTools-based hot-restart (`RestartClassLoader` visible in the stack trace) failed to properly reload the classpath after a **new dependency** was added — DevTools' lightweight restart can handle code changes but sometimes not new libraries. **Fix:** bypass IntelliJ's run button and DevTools entirely for one genuinely clean run: `./gradlew clean bootRun` from the terminal. This succeeded immediately, confirming the diagnosis. General lesson reinforced again: when something that "should" work doesn't, a full clean restart — outside any hot-reload mechanism — is always worth trying early, not as a last resort.

### Confirmed: Spring Security now genuinely active

`Using generated security password: <uuid>` appeared in the startup log, and `curl -i -X POST /api/users` (registration) returned `401 Unauthorized` — confirming Spring Security is locking down *every* endpoint by default, including registration, which obviously needs to become public. This is the expected next configuration step, not a bug.

### `SecurityFilterChain` — defining which endpoints are public

A bean that replaces Spring Security's all-or-nothing default with explicit, custom rules:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/users", "/api/auth/**").permitAll()
            .anyRequest().authenticated()
        );
    return http.build();
}
```

- **`HttpSecurity http`** — a builder object for constructing security rules, handed in automatically
- **`.csrf(AbstractHttpConfigurer::disable)`** — CSRF protection is a browser/cookie-based concern for server-rendered sites with session cookies; disabling it is standard and expected for a stateless, token-based JSON API like this one (not a security hole in this context)
- **`.authorizeHttpRequests(auth -> ...)`** — where the actual access rules live; checked top to bottom, specific rules before the catch-all
- **`.requestMatchers("/api/users", "/api/auth/**").permitAll()`** — these paths need no login (`**` = wildcard, matches the path and everything beneath it)
- **`.anyRequest().authenticated()`** — everything else requires a valid, authenticated identity
- **`throws Exception`** on the method signature — `HttpSecurity.build()` is declared to throw a checked exception internally; the conventional pattern here is to just declare `throws Exception` on the bean method itself rather than wrapping it in `try`/`catch`

**Method reference vs. lambda — the practical rule:** use a method reference (`AbstractHttpConfigurer::disable`) only when a lambda's *entire* body is "call one existing method on the input, unchanged, no extra logic" (same pattern as `Transaction::getAmount` from Streams). Anything with a condition, calculation, or multiple steps must stay a lambda (e.g. `pages -> pages > 100` has no method-reference equivalent).

### Verified end to end

- `POST /api/users` (registration) → `200 OK` with a normal `UserResponse` — confirmed public
- `POST /api/wallets` (no credentials) → rejected — confirmed protected
- `SELECT password FROM users` → genuine BCrypt hash (`$2a$...`), never the plain-text password — confirmed hashing works

### A subtlety worth understanding: `403` instead of `401` on protected endpoints

Earlier (before defining a custom `SecurityFilterChain`), unauthenticated requests to `/api/health` returned `401` with a `WWW-Authenticate: Basic` header — Spring's *auto-configured default* security setup includes a Basic Auth challenge mechanism. Once a custom `SecurityFilterChain` bean is defined, it **fully replaces** that default — and since this custom chain doesn't yet configure any explicit authentication mechanism (no `.httpBasic()`, and no JWT filter built yet), Spring Security has no way to issue a `WWW-Authenticate` challenge, so it returns `403 Forbidden` instead for `.anyRequest().authenticated()` failures. This is expected and not a bug to fix — genuinely correct authentication (a real JWT filter) is still Stage 3–4 ahead; the specific status code is a minor detail until then.

### JWT concept

A JWT has three dot-separated parts: header (metadata), payload (claims — data), signature (proof of integrity). **Not encrypted, only signed** — the payload is plainly readable by anyone (just Base64), but any tampering breaks the signature, which the server checks on every use. Never put sensitive data (passwords) in the payload — only things safe to be technically readable, like an email or user id.

### JWT library and configuration

```groovy
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
```
Split into `-api` (classes you code against) and `runtimeOnly` `-impl`/`-jackson` (the actual implementation, only needed at runtime, never referenced directly).

```properties
jwt.secret=ThisIsATemporaryDevelopmentSecretKeyChangeInProduction123456
jwt.expiration=3600000
```
Secret lives in config, never hardcoded in Java (same principle as DB credentials). Expiration is in **milliseconds** (3600000 = 1 hour).

### `JwtUtil` — token generation

```java
package com.payflow.payflow.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
```

- **`@Value("${jwt.secret}")`** — injects a value from `application*.properties` into a field; `${...}` signals "read from configuration," distinct from a hardcoded string
- **`@Component`** — general-purpose Spring-managed bean, used here for a "helper tool" class rather than a business-logic-holder (`@Service`) or controller
- **`Jwts.builder()`** — JJWT's builder pattern for constructing a token: `.subject()`, `.issuedAt()`, `.expiration()`, `.signWith()`, `.compact()` (finalizes into the actual token string)
- **`Keys.hmacShaKeyFor(secretKey.getBytes())`** — converts the raw secret string into the cryptographic key format the signing algorithm requires

### Login DTOs, `AuthService`, and `AuthController`

```java
public record LoginRequest(@NotBlank String email, @NotBlank String password) {
}

public record LoginResponse(String token) {
}
```

**Caught and fixed an architectural inconsistency:** the login logic was initially written directly inside `AuthController`, breaking the Controller → Service → Repository pattern used everywhere else in the app (`UserController`/`UserService`, `WalletController`/`WalletService`, etc.). Extracted into a proper `AuthService`:

```java
package com.payflow.payflow.auth;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResourceNotFoundException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(request.email());
        return new LoginResponse(token);
    }
}
```

`AuthController` now stays properly thin, matching every other controller in the app — depends only on `AuthService`, no direct access to repositories, encoders, or `JwtUtil`:

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
```

**New method used: `passwordEncoder.matches(rawPassword, storedHash)`** — the counterpart to `.encode(...)`. Hashes the entered password internally and compares it to the stored hash — returns `true`/`false`, never reverses anything.

**Security-conscious error design:** both "email not found" and "wrong password" throw the *same* generic message, `"Invalid email or password"` — deliberately not revealing to an attacker which part of their guess was correct, a standard real-world login security practice.

**Verified end to end:** registered a user, logged in with correct credentials → `200 OK` with a genuine JWT (`eyJhbGc...`). Confirmed rejection behavior with a nonexistent/wrong-password login too.

### Incident: entire local database wiped between sessions

After a gap of several days, restarting the app failed with `FATAL: role "payflow" does not exist`. Investigation (`psql postgres -c "\du"`) showed the `payflow` role was gone entirely, and `psql payflow -c "\dt"` showed no tables at all — the whole local Postgres database had been reset (likely due to a Mac restart or Homebrew service reset; local dev Postgres installs aren't guaranteed persistent across all system events the way a properly configured server would be).

**Recovery:** simply recreated the role/database with the original setup commands, then ran `./gradlew clean bootRun` — Flyway automatically re-ran all 5 migrations from scratch, fully reconstructing the schema with zero manual SQL needed.

**The lesson:** migration files are the real, durable source of truth for schema — actual data can be lost, but as long as the migration files exist in the project (and are committed to Git), the database structure is always 100% reproducible from nothing. This is exactly why hand-editing a database schema directly (instead of via migrations) is risky — it isn't recorded anywhere durable.

### `JwtUtil` — token validation (the reverse of generation)

```java
public String extractEmail(String token) {
    return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
}
```
`Jwts.parser()` mirrors `Jwts.builder()`; `.verifyWith(getSigningKey())` checks the signature against the same secret used to sign it — fails if tampered with or signed by a different key. `.getPayload().getSubject()` retrieves the identity set during generation.

### `JwtAuthFilter` — validating tokens on every request

**Concept: a Filter.** Spring Security's "guard at the door" is implemented as a chain of filters, each inspecting the request before passing it along. `OncePerRequestFilter` is a base class guaranteeing exactly one execution per request; extend it and override `doFilterInternal`.

```java
package com.payflow.payflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        filterChain.doFilter(request, response);
    }
}
```

**Critical ordering rule caught and fixed during development:** `filterChain.doFilter(request, response)` — "let this request continue to the next filter/controller" — must always be the **last** thing this method does. It was initially placed *first* by mistake; since it hands off the entire rest of request processing immediately, anything written after it runs too late to affect whether the request is treated as authenticated. Token inspection must come first, `doFilter(...)` last.

**New Spring Security concepts:**
- `request.getHeader("Authorization")` / `"Bearer <token>"` convention — the standard way a client presents a token; `.startsWith("Bearer ")` + `.substring(7)` extracts the raw token (7 = length of `"Bearer "`)
- **`SecurityContextHolder`** — a request-scoped slot where Spring Security stores "who is making this request." Later checks (like `.anyRequest().authenticated()`) read from here.
- **`UsernamePasswordAuthenticationToken(email, null, Collections.emptyList())`** — Spring Security's standard object representing a successfully authenticated identity, despite the historical name. `null` credentials (already verified via the JWT signature, no need to re-check a password), empty roles list (no role-based authorization built yet).

### Registering the filter with `SecurityFilterChain`

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/users", "/api/auth/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```
`JwtAuthFilter` is injected as a second bean-method parameter (same DI mechanism as always). `.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)` inserts the custom filter *before* Spring Security's own built-in authentication filter — necessary so the identity is already set in `SecurityContextHolder` by the time Spring's own authorization check runs.

### Verified end to end — the complete authentication chain

1. `POST /api/wallets` with **no** `Authorization` header → rejected
2. `POST /api/auth/login` with valid credentials → real JWT returned
3. `POST /api/wallets` **with** `Authorization: Bearer <token>` → succeeds, returns a genuine `WalletResponse`

This confirms the full chain works: registration → hashed password storage → login → signed token issuance → token validation on a protected endpoint → identity correctly recognized and the request allowed through.

### Remaining plan
1-4. ~~Password hashing, security config, login endpoint, JWT generation/validation~~ → all done, fully verified
5. **Authorization** (not yet built) — currently any valid token can access *any* wallet/transaction, regardless of ownership. The next real step is checking that the authenticated user actually owns the resource they're requesting (e.g., a user should only be able to create/view wallets tied to their own account) — true authorization, not just authentication.

---

## Known Gaps — remaining, honest list

1. ~~No global exception handling~~ → ✅ fixed (`@ControllerAdvice`, custom exceptions)
2. ~~No input validation~~ → ✅ fixed (Bean Validation + `@Valid`)
3. ~~No `@Transactional`~~ → ✅ fixed and verified twice against a real database

Next honest gaps for the road ahead (not yet addressed): concurrency/optimistic locking on simultaneous transactions against the same wallet, idempotency keys for safe request retries, authentication/authorization (Spring Security + JWT), automated test coverage beyond the one repository test, API documentation (OpenAPI/Swagger), Testcontainers for integration tests, Dockerization, and CI/CD.

---

## Complete Entity Code

### User.java
```java
package com.payflow.payflow.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected User() {
    }

    public User(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
```

### Wallet.java
```java
package com.payflow.payflow.wallet;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.payflow.payflow.user.User;

@Entity
@Table(name = "wallets")
@Getter
@Setter
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Wallet() {
    }

    public Wallet(User user, String currency) {
        this.user = user;
        this.currency = currency;
        this.balance = BigDecimal.ZERO;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
```

### TransactionType.java
```java
package com.payflow.payflow.transaction;

public enum TransactionType {
    CREDIT,
    DEBIT
}
```

### Transaction.java
```java
package com.payflow.payflow.transaction;

import com.payflow.payflow.wallet.Wallet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Transaction() {
    }

    public Transaction(Wallet wallet, BigDecimal amount, TransactionType type) {
        this.wallet = wallet;
        this.amount = amount;
        this.type = type;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
```

### V1__init_schema.sql
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    balance NUMERIC(19, 4) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL REFERENCES wallets(id),
    amount NUMERIC(19, 4) NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
```

### V2__remove_id_default.sql
```sql
ALTER TABLE users ALTER COLUMN id DROP DEFAULT;
ALTER TABLE wallets ALTER COLUMN id DROP DEFAULT;
ALTER TABLE transactions ALTER COLUMN id DROP DEFAULT;
```

### application.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/payflow
spring.datasource.username=payflow
spring.datasource.password=payflow_dev_pw
spring.jpa.hibernate.ddl-auto=validate
```

---

## Setup Commands Reference

```bash
# Java via SDKMAN
sdk install java 21.0.5-tem
java -version

# Postgres via Homebrew
brew services list
psql --version

# Create DB and user
createdb payflow
psql payflow -c "CREATE USER payflow WITH PASSWORD 'payflow_dev_pw';"
psql payflow -c "GRANT ALL PRIVILEGES ON DATABASE payflow TO payflow;"
psql payflow -c "ALTER DATABASE payflow OWNER TO payflow;"

# Verify connection
psql "postgresql://payflow:payflow_dev_pw@localhost:5432/payflow" -c "SELECT 1;"

# Check tables
psql payflow -c "\dt"
psql payflow -c "\d users"

# Run the Spring Boot app
./gradlew bootRun

# Test the health endpoint
curl http://localhost:8080/api/health
```

---

## Project Package Structure (target)

```
com.payflow.payflow
├── PayflowApplication.java
├── user/
│   ├── User.java
│   ├── UserRepository.java
│   ├── UserService.java
│   ├── UserController.java
│   └── dto/
├── wallet/
│   ├── Wallet.java
│   ├── WalletRepository.java
│   ├── WalletService.java
│   └── WalletController.java
├── transaction/
│   ├── Transaction.java
│   ├── TransactionType.java
│   ├── TransactionRepository.java
│   └── TransactionService.java
└── common/
    ├── exception/
    └── config/
```

**Why organize by feature, not by layer:** everything related to `wallet` (entity, repository, service, controller) lives together, so understanding "how wallet creation works end-to-end" doesn't mean jumping across unrelated top-level folders.