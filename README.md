# SixSeven — Personal Expense Management System

<div align="center">

```
  ███████╗██╗██╗  ██╗███████╗███████╗██╗   ██╗███████╗███╗   ██╗
  ██╔════╝██║╚██╗██╔╝██╔════╝██╔════╝██║   ██║██╔════╝████╗  ██║
  ███████╗██║ ╚███╔╝ ███████╗█████╗  ██║   ██║█████╗  ██╔██╗ ██║
  ╚════██║██║ ██╔██╗ ╚════██║██╔══╝  ╚██╗ ██╔╝██╔══╝  ██║╚██╗██║
  ███████║██║██╔╝ ██╗███████║███████╗ ╚████╔╝ ███████╗██║ ╚████║
  ╚══════╝╚═╝╚═╝  ╚═╝╚══════╝╚══════╝  ╚═══╝  ╚══════╝╚═╝  ╚═══╝
```

**A Semester Academic Project — Desktop Personal Finance Management System**

![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square&logo=java)
![Oracle](https://img.shields.io/badge/Oracle-Database%20XE-red?style=flat-square&logo=oracle)
![Swing](https://img.shields.io/badge/UI-Java%20Swing%20%2B%20AWT-blue?style=flat-square)
![JDBC](https://img.shields.io/badge/DB-JDBC%20%2B%203NF%20Schema-green?style=flat-square)
![License](https://img.shields.io/badge/License-Academic%20Use-lightgrey?style=flat-square)

</div>

---

## Overview

**SixSeven** is a fully-featured desktop expense management application built entirely in **Java (Swing + AWT)**, backed by an **Oracle Database** via JDBC. The UI is themed after the Indian ₹500 currency note — a stone-grey and slate palette with the *Josefin Slab* typeface and an ancient Indian coin logo.

The project is structured as a **3-member modular academic submission**, with each member owning a distinct software layer: database/DAO, business logic/models, and desktop UI/dashboard.

| Property        | Detail                                          |
|-----------------|-------------------------------------------------|
| **App Name**    | SixSeven — Personal Expense System              |
| **Language**    | Java 17+                                        |
| **GUI**         | Java Swing + AWT (100% desktop, no web)         |
| **Database**    | Oracle Database 19c / 21c / XE                  |
| **DB Access**   | JDBC with `ojdbc8.jar`                          |
| **Theme**       | Indian ₹500 note — Stone Grey `#4E534E` Palette |
| **Typography**  | Josefin Slab (brand) · Arial (body text)        |
| **Login**       | Username: `sixseven` · Password: `12345`        |

---

## Key Features

### 🔐 Authentication
- Clean, card-style login panel with shake animation on invalid credentials
- Credential validation with `Access Granted` success feedback and auto-dismiss

### 📊 Dynamic Dashboard
- Real-time **Month-to-Date (MTD)** expense counter and transaction count metric cards
- Random casual peer greeting (e.g. *"Lessgooo! Stay on top of your spends."*)
- **Recent Transactions** snapshot — dynamically titled `Recent Transactions (N)`
- Single `View All Transactions ->` shortcut — no duplicate buttons

### 💳 Transaction Management
- Full **CRUD**: Add, Edit, Delete, Restore expenses via dialogs
- Star / Favourite toggle per record with visual indicator `(*)`
- Filter by **Month**, **Year**, **Category**, **Payment Mode** or live **Search**
- Calendar-based **Month/Year Picker** dialog
- **Export CSV** — exports currently filtered view to a `.csv` file

### 🗑️ Soft Delete & Trash Bin
- Soft deletion using `is_deleted = 1` column — records never permanently lost by accident
- Dedicated **Trash** tab with restore and permanent-delete actions

### 📈 Trends & Analytics
- **Annual view** — 12-month spending bar chart for a selected year
- **Monthly drill-down** — daily spending bars for any specific month
- Debt/loan balance overlay comparisons
- Sortable by total amount or category

### 💰 Debts & Loans Tracker
- Track **Borrowed** and **Lent** amounts with person name, principal, and interest rate
- Due date tracking with automatic **OVERDUE** status escalation
- **Mark as Repaid** — optionally logs repayment as an expense
- Overview cards: Total Borrowed, Total Lent, Net Balance

---

## System Architecture

```
expensemanager/
│
├── Main.java                        ← Entry point; DB seed + EDT launch
│
├── module_db_dao/                   ── MODULE 1 (Member 1) ──────────────
│   ├── DatabaseConnection.java      ← Oracle JDBC singleton + health check
│   ├── ExpenseDAO.java              ← CRUD + seedCompleteHistory()
│   ├── CategoryDAO.java             ← Category + PaymentMode lookups
│   └── DebtDAO.java                 ← Debt insert/update/status queries
│
├── module_core_logic/               ── MODULE 2 (Member 2) ──────────────
│   ├── ExpenseService.java          ← Central service; DB + in-memory fallback
│   ├── Expense.java                 ← Domain model
│   ├── Debt.java                    ← Debt domain model
│   ├── Category.java                ← Category model
│   └── PaymentMode.java             ← Payment mode model
│
├── module_ui/                       ── MODULE 3 (Member 3) ──────────────
│   ├── AquaTheme.java               ← Centralised colour palette & font factory
│   ├── LoginFrame.java              ← Login window
│   ├── MainDashboardFrame.java      ← Root JFrame; CardLayout routing
│   ├── SidebarPanel.java            ← WhatsApp-style vertical nav sidebar
│   ├── DashboardPanel.java          ← MTD metrics + recent transactions
│   ├── TransactionsPanel.java       ← Full CRUD table + filter bar + CSV
│   ├── TrendsChartPanel.java        ← Custom bar chart analytics
│   ├── DebtManagementPanel.java     ← Debts & loans table + actions
│   ├── DeletedHistoryPanel.java     ← Trash bin panel
│   ├── AddExpenseDialog.java        ← Add expense modal
│   ├── EditExpenseDialog.java       ← Edit expense modal
│   └── MonthYearPickerDialog.java   ← Calendar picker
│
├── lib/
│   └── ojdbc8.jar                   ← ⚠ Place Oracle JDBC driver here
│
├── schema.sql                       ← Full Oracle DDL + 2024–2026 seed data
├── build.bat                        ← Windows one-click build & run
└── build.sh                         ← Linux/macOS one-click build & run
```

### 3-Member Modular Division

| Member | Module | Responsibilities |
|--------|--------|-----------------|
| **Member 1** | `module_db_dao` | Oracle connection pooling, 3NF normalized schema design, DAO layer for all 4 tables, startup DB seeding with explicit `COMMIT`, CSV-safe PreparedStatements |
| **Member 2** | `module_core_logic` | Domain models (`Expense`, `Debt`, `Category`, `PaymentMode`), `ExpenseService` with DB/in-memory dual-path, MTD calculations, soft-delete logic, debt overdue escalation |
| **Member 3** | `module_ui` | Custom Swing component library, stone-grey `AquaTheme` design system, coin logo renderer, sidebar navigation, dynamic bar chart renderer, all dialog UIs |

---

## Database Schema

The schema follows **3rd Normal Form (3NF)**:
- **1NF** — Atomic values, single-column surrogate PKs, no repeating groups
- **2NF** — Single-column PKs eliminate partial dependencies structurally
- **3NF** — No transitive dependencies: `mode_name` lives in `PAYMENT_MODES`, `category_name` in `CATEGORIES`

### Tables

```sql
-- 1. CATEGORIES (lookup / reference)
CREATE TABLE CATEGORIES (
    category_id   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_name VARCHAR2(50) NOT NULL,
    category_type VARCHAR2(20) NOT NULL,          -- 'EXPENSE' | 'INCOME'
    CONSTRAINT uq_category_name UNIQUE (category_name),
    CONSTRAINT chk_cat_type CHECK (category_type IN ('EXPENSE','INCOME'))
);

-- 2. PAYMENT_MODES (lookup / reference)
CREATE TABLE PAYMENT_MODES (
    payment_mode_id NUMBER PRIMARY KEY,
    mode_name       VARCHAR2(30) NOT NULL,
    CONSTRAINT uq_mode_name UNIQUE (mode_name)
);

-- 3. EXPENSES (core fact table)
CREATE TABLE EXPENSES (
    expense_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    amount          NUMBER(10,2) NOT NULL,
    expense_date    DATE NOT NULL,
    category_id     NUMBER NOT NULL REFERENCES CATEGORIES(category_id),
    payment_mode_id NUMBER REFERENCES PAYMENT_MODES(payment_mode_id),
    notes           VARCHAR2(255),
    is_deleted      NUMBER(1) DEFAULT 0 NOT NULL,  -- soft delete flag
    is_starred      NUMBER(1) DEFAULT 0 NOT NULL   -- favourite flag
);

-- 4. DEBTS_LOANS
CREATE TABLE DEBTS_LOANS (
    debt_id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    person_name      VARCHAR2(100) NOT NULL,
    debt_type        VARCHAR2(20) NOT NULL,         -- 'BORROWED' | 'LENT'
    principal_amount NUMBER(10,2) NOT NULL,
    interest_rate    NUMBER(5,2) DEFAULT 0,
    due_date         DATE NOT NULL,
    status           VARCHAR2(20) DEFAULT 'PENDING',-- 'PENDING'|'REPAID'|'OVERDUE'
    notes            VARCHAR2(255)
);
```

### Seed Data

`schema.sql` contains **complete reference data** and **multi-year historical seed records** (2024 Jan–Dec, 2025 Jan–Dec, 2026 Jan–Oct) totalling 100+ INSERT statements across all expense categories.

The Java startup routine (`ExpenseDAO.seedCompleteHistory()`) provides an additional **25-record dataset** for 2024, 2025, and 2026 Jan–Sep with an explicit `COMMIT` guarantee.

---

## Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| **JDK** | 17 or 21 (LTS) | Older versions unsupported (uses switch expressions) |
| **Oracle Database** | 19c, 21c, or **XE** | XE (Express Edition) is free and recommended |
| **Oracle JDBC Driver** | `ojdbc8.jar` | Must be placed in `lib/` manually — see below |
| **OS** | Windows 10/11 · Linux · macOS | `build.bat` for Windows; `build.sh` for Unix |

### Download Links
- **JDK 21**: https://adoptium.net/
- **Oracle Database XE**: https://www.oracle.com/database/technologies/xe-downloads.html
- **ojdbc8.jar**: https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html  
  *(Direct: Maven Central → `com.oracle.database.jdbc:ojdbc8`)*

---

## Installation & Setup

### Step 1 — Clone / Download the Project

```bash
# If using Git:
git clone <your-repo-url>
cd expensemanager

# Or extract the ZIP and open a terminal in the project folder.
```

### Step 2 — Place `ojdbc8.jar`

```
expensemanager/
└── lib/
    └── ojdbc8.jar    ← copy here (replaces PUT_OJDBC8_JAR_HERE.txt)
```

> Without `ojdbc8.jar`, the application runs in **in-memory fallback mode** — all features work but data is not persisted between sessions.

### Step 3 — Configure Oracle Connection

Edit `config/DatabaseConnection.java` (lines 19–23):

```java
private static final String HOST     = "localhost";   // Oracle host
private static final String PORT     = "1521";         // default Oracle port
private static final String SID      = "XE";           // SID or SERVICE_NAME
private static final String USERNAME = "system";        // your Oracle username
private static final String PASSWORD = "12345";         // your Oracle password
```

### Step 4 — Create Schema & Load Seed Data

Open **SQL*Plus** or **SQL Developer** and run:

```sql
-- Connect to your Oracle instance first, then:
@schema.sql
```

This script will:
1. Drop and recreate all 4 tables (safe re-run)
2. Insert reference categories and payment modes
3. Insert 100+ historical expense records (2024 · 2025 · 2026)
4. Insert 14 diverse debt/loan sample records
5. Issue a final `COMMIT`

---

## Compilation & Execution

### Windows (Recommended)

```bat
cd "c:\path\to\expensemanager"
.\build.bat
```

`build.bat` compiles all modules and immediately launches the application.

### Manual Compilation (Windows / Linux / macOS)

```bash
# 1. Create output directory
mkdir -p out

# 2. Compile all source files
javac -encoding UTF-8 \
      -cp ".;lib/ojdbc8.jar" \
      -d out \
      module_db_dao/*.java \
      module_core_logic/*.java \
      module_ui/*.java \
      Main.java

# 3. Run the application
java -cp "out;lib/ojdbc8.jar" Main
```

> **Linux/macOS**: Replace `;` with `:` in the classpath separator:
> ```bash
> javac -cp ".:lib/ojdbc8.jar" -d out ...
> java  -cp "out:lib/ojdbc8.jar" Main
> ```

### Using `build.sh` (Linux / macOS)

```bash
chmod +x build.sh
./build.sh
```

---

## Login Credentials

```
Username : sixseven
Password : 12345
```

---

## Loading Demo Data (Offline / Fallback Mode)

When Oracle is unavailable, click the **`[Load Sample Data]`** button on the **Transactions** toolbar:

- Inserts **25 Oracle records** via `seedCompleteHistory()` (if DB is connected and records are missing)
- Loads **24 in-memory records** spanning 2024, 2025, and 2026 Jan–Sep

Then set the filter dropdowns to **All Years** + **All Months** to view the full historical dataset.

---

## Application Screenshots

| Screen | Description |
|--------|-------------|
| **Login** | Card-style login with coin logo, shake animation on failure |
| **Dashboard** | MTD metrics, peer greeting, recent transactions table |
| **Transactions** | Full CRUD table with multi-filter bar and CSV export |
| **Analytics** | Annual 12-bar and monthly 31-bar custom chart renderers |
| **Debts & Loans** | Overview cards + ledger table with repayment action |
| **Trash Bin** | Soft-deleted records with restore and hard-delete options |

---

## Color Palette

Inspired by the **Indian ₹500 currency note** stone-grey aesthetic:

| Token | Hex | Usage |
|-------|-----|-------|
| `DEEP_SLATE` | `#4A4D4A` | Sidebar background, login background |
| `STONE_PRIMARY` | `#545854` | Header accents, table headers |
| `ACTIVE_TAB_BG` | `#3A3C3A` | Active/hover nav items |
| `LIGHT_WASH` | `#ECEEEA` | Main app background |
| `CRISP_WHITE` | `#FFFFFF` | Cards, table rows, dialogs |
| `SUBTLE_BORDER` | `#D0D3CF` | Panel and table borders |
| `ACTION_TEAL` | `#4E534E` | Primary CTA buttons |
| `GOLD` | `#D4AF37` | Coin logo, brand label, active tab indicator |
| `DANGER_RED` | `#C0392B` | Delete actions, error states |
| `SUCCESS_GREEN` | `#27AE60` | DB connected indicator, success states |

---

## Project Structure Summary

```
expensemanager/
├── Main.java                    Entry point + background DB seed
├── build.bat                    Windows build & launch script
├── build.sh                     Unix/macOS build & launch script
├── schema.sql                   Oracle DDL + full 2024–2026 seed data
├── seed_records.sql             Conditional Oracle seed (25 records)
│
├── config/
│   └── DatabaseConnection.java  JDBC singleton + isDatabaseAvailable()
│
├── model/                       Standalone model classes (legacy)
│   ├── Expense.java
│   └── PaymentMode.java
│
├── module_db_dao/               Member 1 — Database Layer
│   ├── DatabaseConnection.java
│   ├── ExpenseDAO.java
│   ├── CategoryDAO.java
│   └── DebtDAO.java
│
├── module_core_logic/           Member 2 — Business Logic
│   ├── Expense.java
│   ├── Debt.java
│   ├── Category.java
│   ├── PaymentMode.java
│   └── ExpenseService.java
│
├── module_ui/                   Member 3 — Desktop GUI
│   ├── AquaTheme.java
│   ├── LoginFrame.java
│   ├── MainDashboardFrame.java
│   ├── SidebarPanel.java
│   ├── DashboardPanel.java
│   ├── TransactionsPanel.java
│   ├── TrendsChartPanel.java
│   ├── DebtManagementPanel.java
│   ├── DeletedHistoryPanel.java
│   ├── AddExpenseDialog.java
│   ├── EditExpenseDialog.java
│   └── MonthYearPickerDialog.java
│
├── lib/
│   └── ojdbc8.jar               ⚠ Not included — add manually
│
└── out/                         Compiled .class files (auto-generated)
```

---

## Academic Notes

### Normalization Evidence (Viva Reference)

| Normal Form | Evidence in Schema |
|-------------|-------------------|
| **1NF** | All columns store single atomic values. No repeating groups. Surrogate integer PKs throughout. |
| **2NF** | Every PK is single-column, making partial dependencies structurally impossible. |
| **3NF** | `mode_name` lives only in `PAYMENT_MODES`. `category_name` lives only in `CATEGORIES`. No transitive dependency exists in `EXPENSES`. |

### Design Patterns Used

| Pattern | Where Applied |
|---------|--------------|
| **DAO Pattern** | `ExpenseDAO`, `CategoryDAO`, `DebtDAO` — clean separation of SQL from business logic |
| **Service Layer** | `ExpenseService` — orchestrates DAO calls, fallback logic, and domain calculations |
| **Singleton** | `DatabaseConnection` — single utility class for connection management |
| **Strategy (Fallback)** | `ExpenseService` — DB path if Oracle connected, in-memory path if offline |
| **Observer (Callback)** | `Runnable` callbacks passed between panels to trigger cross-panel refreshes |
| **Template Method** | `seedCompleteHistory()` — guard → batch insert → explicit commit pattern |

---

## Team Members

| Member | Module | Primary Files |
|--------|--------|--------------|
| Member 1 | Database & DAO Layer | `schema.sql`, `DatabaseConnection.java`, `ExpenseDAO.java`, `CategoryDAO.java`, `DebtDAO.java` |
| Member 2 | Core Logic & Models | `ExpenseService.java`, `Expense.java`, `Debt.java`, `Category.java`, `PaymentMode.java` |
| Member 3 | Desktop GUI & Dashboard | `AquaTheme.java`, `MainDashboardFrame.java`, `SidebarPanel.java`, `DashboardPanel.java`, `TrendsChartPanel.java` |

---

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `ojdbc8.jar not on classpath` | `lib/ojdbc8.jar` missing | Download and place `ojdbc8.jar` in `lib/` directory |
| Only 3 records appear | ojdbc8 missing → offline mode | Click `[Load Sample Data]` on Transactions toolbar |
| `ORA-12541: TNS no listener` | Oracle DB not running | Start Oracle service: `net start OracleServiceXE` |
| `ORA-01017: invalid credentials` | Wrong username/password | Update `DatabaseConnection.java` constants |
| `Debts & Loans` opens Trash | CardLayout bug (fixed) | Ensure `MainDashboardFrame` uses single canonical card key `"Debts & Loans"` |
| Square boxes `▯` in greeting | Emoji not supported by AWT | Fixed — all greetings use plain ASCII text only |
| Compilation error `&&` | PowerShell syntax | Use `;` not `&&` in PowerShell, or use `build.bat` |

---

<div align="center">

**SixSeven** · Academic Semester Project · Java Desktop Application  
Built with Java Swing + AWT + JDBC + Oracle Database

*"Track every rupee. Own your future."*

</div>
