# 🏠 Grand Horizon Student Hostel Management System

> A comprehensive, production-quality console application built with **Java + MySQL + JDBC**, adapted from a Hotel Management System design to manage a **student hostel** — rooms/beds, students, mess orders, and fee billing. Great for demonstrating **OOP** and **SQL/DBMS** skills in interviews.

---

## 📋 Table of Contents

1. [Project Overview](#-project-overview)
2. [Technology Stack](#-technology-stack)
3. [Project Structure](#-project-structure)
4. [Setup Guide](#-setup-guide)
5. [Features](#-features)
6. [OOP Concepts Used](#-oop-concepts-used)
7. [Database Design](#-database-design)
8. [How This Maps to the Hotel Version](#-how-this-maps-to-the-hotel-version)

---

## 🎯 Project Overview

**Grand Horizon Student Hostel Management System** lets hostel wardens/admin staff manage:

- **Room inventory** (4 types: Single, Double, Triple, Dormitory) with per-bed occupancy tracking
- **Student profiles** with ID proof, guardian details, course & year
- **Bed allocations** with double-booking prevention and full allocate/vacate lifecycle
- **Mess ordering** from a 23-item categorized menu (extra/ad-hoc orders beyond the standard mess plan)
- **Fee billing** with maintenance charge, discounts, and itemized receipts
- **Payments** via Cash, Card, UPI, or Net Banking

All data is stored permanently in **MySQL** using a normalized relational schema.

---

## 💻 Technology Stack

| Layer        | Technology                | Purpose                     |
|--------------|----------------------------|------------------------------|
| Language     | Java 17+                   | Core application logic       |
| Database     | MySQL 8.0+                 | Persistent data storage      |
| Connectivity | JDBC (mysql-connector-j)   | Java ↔ MySQL bridge          |
| Build        | Manual `javac` / IDE       | Compilation                  |

---

## 📁 Project Structure

```
HostelManagementSystem/
├── src/
│   ├── Main.java                      ← Console UI (entry point)
│   │
│   ├── model/
│   │   ├── Room.java                  ← Abstract base class
│   │   ├── SingleRoom.java            ← Extends Room (+60% fee)
│   │   ├── DoubleRoom.java            ← Extends Room (+30% fee)
│   │   ├── TripleRoom.java            ← Extends Room (+15% fee)
│   │   ├── DormitoryRoom.java         ← Extends Room (base fee)
│   │   ├── Student.java               ← Encapsulated entity
│   │   ├── Allocation.java            ← Aggregation of Student+Room
│   │   ├── MessItem.java              ← Mess menu item entity
│   │   ├── MessOrderItem.java         ← Line item (Composition)
│   │   ├── MessOrder.java             ← Composition + Collections
│   │   └── FeePayment.java            ← Payment with enum mode
│   │
│   ├── interfaces/
│   │   ├── Billable.java
│   │   ├── Searchable.java
│   │   └── Payable.java
│   │
│   ├── exception/
│   │   ├── RoomNotAvailableException.java   ← Unchecked
│   │   ├── InvalidAllocationException.java  ← Checked
│   │   └── InvalidPaymentException.java     ← Checked
│   │
│   ├── dao/
│   │   ├── StudentDAO.java            ← CRUD + LIKE + GROUP BY
│   │   ├── RoomDAO.java               ← CRUD + JOIN + BETWEEN + Factory
│   │   ├── AllocationDAO.java         ← CRUD + Transactions + overlap check
│   │   ├── MessDAO.java               ← CRUD + Junction table + Batch insert
│   │   └── PaymentDAO.java            ← CRUD + Analytics
│   │
│   ├── service/
│   │   ├── HostelService.java         ← Facade over Student/Room/Mess DAOs
│   │   ├── AllocationService.java     ← Business logic: allocate/vacate
│   │   └── FeeBillingService.java     ← Bill generation + payments (Payable)
│   │
│   └── util/
│       └── DBConnection.java          ← Singleton JDBC connection
│
├── database/
│   ├── schema.sql                     ← Tables, constraints, triggers, views, procedures
│   ├── sample_data.sql                ← Seed data (8 students, 12 rooms, 8 allocations...)
│   └── queries.sql                    ← 17 advanced SQL queries
│
└── README.md
```

---

## 🚀 Setup Guide

### Prerequisites
- Java 17+
- MySQL 8.0+
- `mysql-connector-j-8.x.x.jar` on your classpath

### Step 1 — Database Setup
```sql
source database/schema.sql
source database/sample_data.sql
```

### Step 2 — Configure JDBC
Edit `src/util/DBConnection.java`:
```java
private static final String DB_URL      = "jdbc:mysql://localhost:3306/hostel_management?useSSL=false&serverTimezone=UTC";
private static final String DB_USER     = "root";
private static final String DB_PASSWORD = "your_password_here";  // change this
```

### Step 3 — Compile & Run
```bash
# Compile (Linux/Mac use ':' as classpath separator, Windows uses ';')
javac -cp ".:mysql-connector-j-8.x.x.jar" -d out -sourcepath src src/Main.java

# Run
java -cp ".:out:mysql-connector-j-8.x.x.jar" Main
```

---

## ✨ Features

### 1. Room Management
Add / view / search rooms; filter by type or computed monthly fee range (BETWEEN); list rooms with free beds; delete rooms.

### 2. Student Management
Add / view / search / update / delete students with phone & email validation; student-count-by-course analytics.

### 3. Allocation (Booking) System
- Prevents double-booking a bed via an explicit occupancy check **and** a database trigger
- Full allocate → vacate lifecycle, wrapped in a **JDBC transaction** so the allocation row and the room's occupied-bed count always stay in sync
- History lookup by student, and by date range

### 4. Mess Ordering
23-item categorized menu (Breakfast, Lunch, Dinner, Snacks, Beverages, Desserts); ad-hoc orders per allocation using ArrayList + HashMap for O(1) duplicate-item merging; batch-inserted line items into a junction table.

### 5. Billing
Itemized fee receipt combining room charges (polymorphic per room type) + mess charges, plus a configurable maintenance levy and discount; payment status tracking.

### 6. Payments
Cash / Card / UPI / Net Banking, with an auto-generated transaction reference.

### 7. Analytics
Student count by course, revenue by room type, revenue by payment mode, top-selling mess items, unique room types (HashSet), and a runtime-polymorphism demo.

---

## 🎓 OOP Concepts Used

- **Encapsulation** — all model fields are `private`; setters validate (e.g. `Student.setPhone()` enforces a 10-digit number).
- **Inheritance** — `Room` (abstract) → `SingleRoom` / `DoubleRoom` / `TripleRoom` / `DormitoryRoom`.
- **Polymorphism** — `Room.calculateCharges()` is overridden per subtype; the same call on a `List<Room>` produces different monthly fees.
- **Abstraction** — `Room` is `abstract`; `Billable`, `Searchable<T>`, `Payable` are pure interfaces.
- **Composition vs Aggregation** — `MessOrder` *owns* its `MessOrderItem`s (composition); `Allocation` *references* an existing `Student` and `Room` (aggregation).
- **Custom Exceptions** — `RoomNotAvailableException` (unchecked), `InvalidAllocationException` / `InvalidPaymentException` (checked).
- **Collections** — `ArrayList` + `HashMap` in `MessOrder`, `HashSet` for unique room types.
- **Design Patterns** — Singleton (`DBConnection`), Facade (`HostelService`), DAO (all `dao.*` classes), Factory (`RoomDAO.buildRoom()`).

---

## 🗃️ Database Design

| Table              | Purpose                                       |
|--------------------|------------------------------------------------|
| `students`         | Guest/student registry                         |
| `rooms`             | Physical room inventory (all 4 types, 1 table) |
| `allocations`       | Bed allotments (links student + room)          |
| `mess_items`        | Hostel mess menu                                |
| `mess_orders`       | Ad-hoc mess order headers per allocation        |
| `mess_order_items`  | Junction table (order × item, M:N)              |
| `payments`          | Fee payment records per allocation              |

Key constraints: `PRIMARY KEY`, `FOREIGN KEY` (`ON DELETE RESTRICT`/`CASCADE`), `CHECK` (phone format, non-negative fees, date ordering), `UNIQUE` (roll number, room number, transaction reference), 3 **triggers** (auto-free room on vacate, prevent double-active-allocation, auto room-full status), 3 **views**, 2 **stored procedures**.

---

## 🔄 How This Maps to the Hotel Version

| Hotel Management System        | Hostel Management System            |
|--------------------------------|--------------------------------------|
| `Room` / `SingleRoom`... (nightly rate) | `Room` / `SingleRoom`... (**monthly** fee) |
| `Customer`                      | `Student` (adds roll no., course, year, guardian) |
| `Booking` (check-in/out dates)  | `Allocation` (bed allotment, allocation/vacate date) |
| `FoodItem` / `FoodOrder`        | `MessItem` / `MessOrder`             |
| `Payment` (GST 18%)             | `FeePayment` (5% maintenance levy instead of GST) |
| Nightly invoice                 | Monthly hostel fee receipt           |

The architecture — DAO pattern, transactional booking, PreparedStatements, Singleton connection, runtime polymorphism for pricing — carries over unchanged; only the domain vocabulary and a few business rules (monthly billing, bed-level occupancy, no double-active-allocation trigger) were adapted for a hostel setting.

---

*Built for B.Tech CSE placement portfolios — Java + MySQL + JDBC*
