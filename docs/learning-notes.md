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
