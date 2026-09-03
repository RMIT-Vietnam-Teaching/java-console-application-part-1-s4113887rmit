[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/vLkFEkw0)

# ClaimShield — Health Insurance Claim Management System

**Course:** COSC3110/3111 — Java Programming
**Assignment:** 2 — Java Console Application (Part 2)
**Student:** Nguyen Ngoc Quang Dang — S4113887

A console-based health insurance claim management system built in plain Java SE.
It manages **Customers**, **Insurance Cards**, and **Claims**, and exposes three
role-driven interfaces: **Administrator**, **Claims Officer**, and **Customer**.

---

## 1. Requirements

| Item | Requirement |
|---|---|
| JDK | Java SE 17 or newer (developed and tested on JDK 26) |
| Build tools | None — plain `javac` / `java` |
| Dependencies | None — standard library only |
| OS | Any OS with a JVM (paths in `data/` use `/`, fine on Windows too) |

---

## 2. How to Build and Run

> **Important:** always run from the **project root**. The application resolves its
> data files through relative paths (`data/users.txt`, `data/customers.txt`, ...),
> so launching from another directory will make it start with an empty dataset.

```bash
# 1. Compile (from the project root)
javac -d out claimshield/*.java

# 2. Run (still from the project root)
java -cp out claimshield.Main
```

To compile with all lint warnings enabled (the project compiles with **zero warnings**):

```bash
javac -Xlint:all -d out claimshield/*.java
```

### Stopping the application

- Choose the **Logout** option in any menu to return to the login screen.
- Type **`exit`** at the username prompt to quit.
- Press **Ctrl-D** (EOF) at any prompt — the application catches end-of-input,
  saves every dataset, and exits cleanly instead of throwing a stack trace.

The welcome banner printed on startup:

```
=======================================
COSC3110/3111 HEALTH INSURANCE SYSTEM
                        Student ID: s4113887
                    Student Name: Nguyen Ngoc Quang Dang
=======================================
```

---

## 3. Demo Accounts

All demo passwords are `pass123` for customers, `admin123` / `officer123` for staff.

### Administrator — full system control

| Username | Password | Name |
|---|---|---|
| `admin` | `admin123` | System Admin |
| `superadmin` | `admin123` | Senior Admin Bruce |

### Claims Officer — claim review and approval

| Username | Password | Name |
|---|---|---|
| `officer1` | `officer123` | Claims Officer Jane |
| `officer2` | `officer123` | Claims Officer Mark |

### Customer — read-only self-service portal

Pick accounts from different membership tiers to show the co-pay discount changing.

| Username | Password | Customer | Approved Total | Tier | Co-pay Discount |
|---|---|---|---|---|---|
| `saka` | `pass123` | Bukayo Saka | 640,000 | SILVER | 5% |
| `dangnn` | `pass123` | Nguyen Ngoc Quang Dang | 2,920,000 | GOLD | 10% |
| `messi` | `pass123` | Lionel Messi | 7,260,000 | PLATINUM | 15% |

Other customer accounts (`zoro`, `nami`, `natsu`, `erza`, `sherlock`, `haaland`,
`saka`, ...) all use the password `pass123`.

---

## 4. Menu Structure

### Administrator (`ADMIN MAIN MENU`)

```
1. Manage Customer Directory (CRUD)
2. Manage Insurance Cards (CRUD)
3. Manage Claims (CRUD, Processing, Advanced Filters)
4. Manage User Accounts & Soft-Delete
5. View System Audit Log
6. Financial Analytics & Reports
7. Save All Changes to Files
8. Logout to Login Screen
```

- **Customer Management** — add PolicyHolder / Dependent, view all, view by ID,
  search by name, list by type, list dependents of a PolicyHolder, update, soft-delete.
- **Insurance Card Management** — register, view all, view by card number, find by
  card holder ID, view cards funded by a policy owner, view cards expiring before a
  date, update, remove.
- **Claim Management** — create, add documents, update status, view all / by ID,
  filter by status / date range / PolicyHolder family, remove.
- **User Account Management** — view all, add Admin / Officer, update, toggle status,
  search by username.
- **Financial Analytics** — overall volume and breakdown by status, approved payout by
  timeframe (Today / This Week / This Month / Custom range), payout per Claims Officer.

### Claims Officer (`CLAIMS OFFICER MENU`)

```
 1. Process & Update Claim Status (NEW -> PROCESSING -> DONE)
 2. Add Supporting Document to Claim
 3. View All Claims
 4. Filter Claims by Status
 5. Filter Claims by Date Range
 6. Filter Claims by PolicyHolder Family
 7. View Customer Directory (Read-only)
 8. View Insurance Cards (Read-only)
 9. View Claim by ID
10. Save All Changes to Files
11. Logout to Login Screen
```

### Customer (`CUSTOMER PORTAL`) — strictly read-only

```
1. View My Profile & Account Information
2. View My Insurance Card Details
3. View My Submitted Claims & Status
4. View Family Dependents & Plan Summary   (PolicyHolders)
   View Covering PolicyHolder Details      (Dependents)
5. Logout to Login Screen
```

---

## 5. Architecture Overview

35 source files in `claimshield/`, organised into seven groups.

```
claimshield/
├── entities/ (conceptual)   User, Admin, ClaimsOfficer, Customer,
│                            PolicyHolder, Dependent, InsuranceCard, Claim
├── enums                    UserRole, UserStatus, ClaimStatus, MembershipTier
├── contracts (interfaces)   Manageable<T>, UserManageable,
│                            CustomerManageable, CardManageable, ClaimManageable
├── repositories             UserRepository, CustomerRepository,
│                            CardRepository, ClaimRepository
├── exceptions               ClaimShieldException (abstract),
│                            InvalidClaimDateException,
│                            InvalidStatusTransitionException
├── ui / console controllers AdminMenu, OfficerMenu, CustomerPortal,
│                            CustomerConsole, CardConsole, ClaimConsole, ConsoleSupport
└── infrastructure           Main, AppContext, AuditLogger, Validator
```

### 5.1 Inheritance and polymorphism

```
User (abstract)                      Customer (abstract)
 ├── Admin                            ├── PolicyHolder   (holds List<Dependent>)
 ├── ClaimsOfficer                    └── Dependent      (holds parentPolicyHolderId)
 └── Customer (abstract)
```

- `User` is abstract and declares `abstract void displayDashboard()`; each concrete
  subclass renders its own role-specific dashboard. `Main` never branches on role —
  it calls `currentUser.displayDashboard()` and dynamic dispatch picks the right one.
- `Customer` is abstract and declares `getCustomerType()`, `getParentPolicyHolderId()`
  and `toCustomerFileString()`. `PolicyHolder` and `Dependent` implement them
  differently, so repository filtering and file persistence stay free of
  `instanceof` checks.
- A `PolicyHolder` **composes** a `List<Dependent>` — the family plan that backs the
  "filter claims by PolicyHolder family" report.

### 5.2 Interfaces and the repository layer

`Manageable<T>` is the generic CRUD contract:

```java
public interface Manageable<T> {
    boolean add(T item)    throws ClaimShieldException;
    boolean update(T item) throws ClaimShieldException;
    boolean delete(String id);
    T      getById(String id);
    List<T> getAll();
}
```

Each aggregate extends it with domain-specific queries, so every repository has a
dedicated interface rather than a shared one-size-fits-all contract:

| Interface | Repository | Extra operations |
|---|---|---|
| `UserManageable` | `UserRepository` | `authenticate` |
| `CustomerManageable` | `CustomerRepository` | `filterByType`, `filterByParentPolicyHolder`, `searchByName` |
| `CardManageable` | `CardRepository` | `getByCardHolderId`, `getByPolicyOwnerId`, `filterExpiringBefore` |
| `ClaimManageable` | `ClaimRepository` | `filterByStatus`, `filterByDateRange`, `filterByPolicyHolderFamily`, `filterByCardNumber` |

Repositories also carry operations that are not part of the shared contract — for
example `ClaimRepository.updateClaimStatus`, `addDocument`, and the payout analytics
queries (`getApprovedPayoutByDateRange`, `getApprovedPayoutByOfficer`).

The concrete repositories narrow the base contract further: `ClaimRepository.add`
declares `throws InvalidClaimDateException` and `ClaimRepository.update` declares
`throws InvalidStatusTransitionException`, so callers catch the specific failure they
can actually cause.

`add` and `update` throw `ClaimShieldException` — a precise, narrow contract instead
of a blanket `throws Exception`.

### 5.3 Business rules enforced in code

| Rule | Where | Failure |
|---|---|---|
| Claim status moves one step at a time: `NEW → PROCESSING → DONE` | `ClaimRepository.updateClaimStatus` | `InvalidStatusTransitionException` |
| A `DONE` claim is immutable | `ClaimRepository` | `InvalidStatusTransitionException` |
| `examDate` must be on/before `claimDate` and strictly before the card's `expirationDate` | `ClaimRepository.add` | `InvalidClaimDateException` |
| Document names must follow `ClaimId_CardNumber_DocName.pdf` | `Validator` | rejected on entry |
| IDs: `u-` + 7 digits, `c-` + 7 digits, `f-` + 10 digits, card = 10 digits | `Validator` | rejected on entry |
| Claim amounts must be strictly positive | `Validator` | rejected on entry |

Example of the status rule rejecting an illegal jump:

```
Claim ID: f-1000000010
New Status (NEW / PROCESSING / DONE): DONE
Business Rule Violation (Invalid Status Transition): Invalid status transition for
claim f-1000000010: cannot transition from NEW to DONE. Status must move forward
one step at a time (NEW -> PROCESSING -> DONE).
```

### 5.4 Membership tiers

Tier is derived from the customer's cumulative **approved (`DONE`)** claim total:

| Tier | Approved total | Co-pay discount |
|---|---|---|
| SILVER | < 2,000,000 | 5% |
| GOLD | ≥ 2,000,000 and < 5,000,000 | 10% |
| PLATINUM | ≥ 5,000,000 | 15% |

The standard 20% co-pay is reduced by the tier discount, so a PLATINUM holder pays
`20% × (1 − 0.15) = 17%`.

### 5.5 Audit trail

Every add / update / delete — plus every login, logout and session start — is
appended to `data/logs.txt`:

```
timestamp,userId,actionPerformed,targetEntityId
2026-09-01T20:44:04,u-0000001,LOGIN_SUCCESS,u-0000001
```

`AuditLogger` is append-only and synchronised; log entries are never rewritten, so
the trail survives restarts.

### 5.6 Persistence and start-up wiring

All state lives in plain CSV files under `data/`. `AppContext` loads them in a
**two-pass** order so cross-references always resolve:

1. users → 2. customers → 3. cards → 4. **wire the relationships** →
5. claims → 6. recompute claim totals → 7. audit log.

Mutations auto-save immediately, and every role menu also offers an explicit
"Save All Changes to Files" option.

---

## 6. Data Files

| File | Records | Format |
|---|---|---|
| `data/users.txt` | 26 | `userId,username,password,fullName,email,role,status,customerId` |
| `data/customers.txt` | 22 | `customerId,fullName,customerType,parentPolicyHolderId` |
| `data/cards.txt` | 22 | `cardNumber,cardHolderId,policyOwnerId,expirationDate` |
| `data/claims.txt` | 63 | `claimId,claimDate,insuredPersonId,cardNumber,examDate,claimAmount,status,documents,processedByUserId` |
| `data/logs.txt` | append-only | `timestamp,userId,actionPerformed,targetEntityId` |

Notes:

- Dates use `yyyy-MM-dd'T'HH:mm` (e.g. `2028-12-31T00:00`).
- Empty trailing fields are preserved on read (the loader uses `split(",", -1)`),
  which is why a claim with no officer assigned ends with an empty last column.
- `documents` is a `|`-separated list of document file names.
- Required minimums are **20 customers, 20 cards, 30 claims, 5 users** — the shipped
  dataset comfortably exceeds all four.
- Approved (`DONE`) claims are spread at roughly two-day intervals from
  **2026-08-28 to 2026-10-15**. The Financial Analytics reports are computed against
  `LocalDateTime.now()`, so a dataset clustered in a single month would report
  `0` for "Today" / "This Week" / "This Month" once the demonstration date moved
  past it. Spreading the approved claims keeps every timeframe report populated
  regardless of when the application is run or marked.

---

## 7. Class Diagram

The full UML class diagram for the final architecture is at:

**`docs/class_diagram.png`**

It covers all core domain types, interfaces, repositories, enums, exceptions, and infrastructure, formatted into two clear sections:
- **Diagram 1:** User Hierarchy & Domain Model
- **Diagram 2:** Service Layer - Interfaces, Repositories & Orchestrator

---

## 8. Project Layout

```
.
├── README.md
├── claimshield/            # 35 Java source files (package claimshield)
├── data/                   # persistence files (loaded at start-up)
│   ├── cards.txt
│   ├── claims.txt
│   ├── customers.txt
│   ├── logs.txt
│   └── users.txt
├── docs/
│   └── class_diagram.png
└── out/                    # compiled .class files (generated, git-ignored)
```

---

## 9. Suggested Demonstration Path

1. Start the app and show the welcome banner and the loaded-record counts.
2. Log in as `admin` → **6. Financial Analytics & Reports** → run the volume
   breakdown, then the payout-by-timeframe and per-officer reports.
3. As `admin`, exercise a filter: **Customer Management → 5. Search Customer by Name**
   and **7. List Dependents Covered by a PolicyHolder**.
4. Log out, log in as `officer1` → **1. Process & Update Claim Status**. First try an
   illegal `NEW → DONE` jump to show the business rule, then advance a claim
   correctly `NEW → PROCESSING`.
5. Log out, log in as `messi` → **1. View My Profile** to show **PLATINUM / 15%**.
   Repeat with `dangnn` (GOLD) and `saka` (SILVER) to contrast the tiers.
6. Show `data/logs.txt` growing with the audit trail from the actions above.
