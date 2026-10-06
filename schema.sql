SET DEFINE OFF;
-- =====================================================================
--  Personal Expense Manager ("sixseven") – Oracle Schema (v4)
--  3-Member Academic Submission • Database Layer (Member 1)
-- =====================================================================

-- ─────────────────────────────────────────────────────────────────────
--  NORMALIZATION JUSTIFICATION (3NF) – Academic Viva Documentation
-- ─────────────────────────────────────────────────────────────────────
-- 1NF: All tables have single-column surrogate PKs; every column
--      stores one atomic value; no repeating groups exist.
-- 2NF: All PKs are single-column → partial dependencies are
--      structurally impossible; each non-key attribute is fully
--      dependent on the whole PK.
-- 3NF: No transitive dependencies:
--      • mode_name lives in PAYMENT_MODES, not in EXPENSES.
--      • category_name / category_type live in CATEGORIES, not inline.
--      • is_deleted and is_starred are facts about the expense row itself.
--      • DEBTS_LOANS separates borrowing/lending from daily expenses.
-- ─────────────────────────────────────────────────────────────────────

-- ── Safe teardown ────────────────────────────────────────────────────
BEGIN EXECUTE IMMEDIATE 'DROP TABLE DEBTS_LOANS CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE EXPENSES CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE CATEGORIES CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN EXECUTE IMMEDIATE 'DROP TABLE PAYMENT_MODES CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;
/

-- ── 1. CATEGORIES ───────────────────────────────────────────────────
CREATE TABLE CATEGORIES (
    category_id   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_name VARCHAR2(50) NOT NULL,
    category_type VARCHAR2(20) NOT NULL,
    CONSTRAINT uq_category_name UNIQUE (category_name),
    CONSTRAINT chk_cat_type     CHECK  (category_type IN ('EXPENSE','INCOME'))
);

-- ── 2. PAYMENT_MODES ────────────────────────────────────────────────
CREATE TABLE PAYMENT_MODES (
    payment_mode_id NUMBER PRIMARY KEY,
    mode_name       VARCHAR2(30) NOT NULL,
    CONSTRAINT uq_mode_name UNIQUE (mode_name)
);

-- ── 3. EXPENSES ─────────────────────────────────────────────────────
CREATE TABLE EXPENSES (
    expense_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    amount          NUMBER(10,2) NOT NULL,
    expense_date    DATE         NOT NULL,
    category_id     NUMBER       NOT NULL,
    payment_mode_id NUMBER,
    notes           VARCHAR2(255),
    is_deleted      NUMBER(1)    DEFAULT 0 NOT NULL,
    is_starred      NUMBER(1)    DEFAULT 0 NOT NULL,
    CONSTRAINT chk_amount       CHECK (amount > 0),
    CONSTRAINT chk_is_deleted   CHECK (is_deleted IN (0,1)),
    CONSTRAINT chk_is_starred   CHECK (is_starred IN (0,1)),
    CONSTRAINT fk_exp_category  FOREIGN KEY (category_id) REFERENCES CATEGORIES(category_id),
    CONSTRAINT fk_exp_mode      FOREIGN KEY (payment_mode_id) REFERENCES PAYMENT_MODES(payment_mode_id)
);

-- ── 4. DEBTS_LOANS ──────────────────────────────────────────────────
CREATE TABLE DEBTS_LOANS (
    debt_id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    person_name      VARCHAR2(100) NOT NULL,
    debt_type        VARCHAR2(20) NOT NULL,
    principal_amount NUMBER(10,2) NOT NULL,
    interest_rate    NUMBER(5,2) DEFAULT 0 NOT NULL,
    due_date         DATE NOT NULL,
    status           VARCHAR2(20) DEFAULT 'PENDING' NOT NULL,
    notes            VARCHAR2(255),
    CONSTRAINT chk_debt_type   CHECK (debt_type IN ('BORROWED', 'LENT')),
    CONSTRAINT chk_debt_amount CHECK (principal_amount > 0),
    CONSTRAINT chk_debt_status CHECK (status IN ('PENDING', 'REPAID', 'OVERDUE'))
);

-- ── Reference Data ───────────────────────────────────────────────────
INSERT INTO CATEGORIES (category_name, category_type) VALUES ('Food & Dining',      'EXPENSE');
INSERT INTO CATEGORIES (category_name, category_type) VALUES ('Travel & Commute',   'EXPENSE');
INSERT INTO CATEGORIES (category_name, category_type) VALUES ('Bills & Utilities',  'EXPENSE');
INSERT INTO CATEGORIES (category_name, category_type) VALUES ('Shopping',           'EXPENSE');
INSERT INTO CATEGORIES (category_name, category_type) VALUES ('Entertainment',      'EXPENSE');
INSERT INTO CATEGORIES (category_name, category_type) VALUES ('Health',             'EXPENSE');
INSERT INTO CATEGORIES (category_name, category_type) VALUES ('Education',          'EXPENSE');
INSERT INTO CATEGORIES (category_name, category_type) VALUES ('Groceries',          'EXPENSE');
INSERT INTO CATEGORIES (category_name, category_type) VALUES ('Debt Repayment',     'EXPENSE');

INSERT INTO PAYMENT_MODES VALUES (1, 'UPI');
INSERT INTO PAYMENT_MODES VALUES (2, 'Cash');
INSERT INTO PAYMENT_MODES VALUES (3, 'Debit Card');
INSERT INTO PAYMENT_MODES VALUES (4, 'Credit Card');
INSERT INTO PAYMENT_MODES VALUES (5, 'Net Banking');

-- ═══════════════════════════════════════════════════════════════════
--  SEED EXPENSES — 2024 (Jan–Dec, ~Rs.15,000–25,000/month)
--  Realistic monthly spend: Rent, Groceries, Bills, Dining, Travel
--  category_id: 1=Food&Dining 2=Travel 3=Bills 4=Shopping
--               5=Entertainment 6=Health 7=Education 8=Groceries
--  payment_mode_id: 1=UPI 2=Cash 3=Debit 4=Credit 5=NetBanking
-- ═══════════════════════════════════════════════════════════════════

-- 2024 January
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (8000,  DATE '2024-01-01', 3, 5, 'Rent Jan 2024');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2800,  DATE '2024-01-08', 8, 1, 'Monthly groceries Jan');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1500,  DATE '2024-01-12', 3, 5, 'Electricity + internet bill');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (650,   DATE '2024-01-18', 1, 1, 'Dinner at local restaurant');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1200,  DATE '2024-01-25', 2, 2, 'Auto + metro commute Jan');

-- 2024 February
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (8000,  DATE '2024-02-01', 3, 5, 'Rent Feb 2024');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2600,  DATE '2024-02-07', 8, 1, 'Groceries Feb');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1400,  DATE '2024-02-10', 3, 5, 'Electricity bill Feb');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1800,  DATE '2024-02-14', 4, 4, 'Valentine gifts + dining');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (750,   DATE '2024-02-22', 2, 1, 'Cab fare Feb');

-- 2024 March
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (8000,  DATE '2024-03-01', 3, 5, 'Rent Mar 2024');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2900,  DATE '2024-03-06', 8, 1, 'Groceries + vegetables');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1350,  DATE '2024-03-11', 3, 5, 'Mar electricity');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2500,  DATE '2024-03-20', 2, 4, 'Holi trip bus + stay');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (480,   DATE '2024-03-28', 1, 1, 'Team lunch');

-- 2024 April
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (8000,  DATE '2024-04-01', 3, 5, 'Rent Apr 2024');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (3100,  DATE '2024-04-05', 8, 1, 'Monthly groceries Apr');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1600,  DATE '2024-04-10', 3, 5, 'Summer AC electricity spike');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (4200,  DATE '2024-04-18', 4, 4, 'Summer clothes haul');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (700,   DATE '2024-04-25', 5, 2, 'Weekend movie outing');

-- 2024 May
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (8000,  DATE '2024-05-01', 3, 5, 'Rent May 2024');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2750,  DATE '2024-05-07', 8, 1, 'Groceries May');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1800,  DATE '2024-05-12', 3, 5, 'May peak summer bill');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (6500,  DATE '2024-05-20', 2, 4, 'Summer vacation trip');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (550,   DATE '2024-05-28', 1, 1, 'Cafe outing');

-- 2024 June
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (8000,  DATE '2024-06-01', 3, 5, 'Rent Jun 2024');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2850,  DATE '2024-06-06', 8, 1, 'Groceries Jun');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1700,  DATE '2024-06-11', 3, 5, 'Monsoon electricity + maintenance');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (900,   DATE '2024-06-18', 6, 1, 'Medical checkup + medicines');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1100,  DATE '2024-06-26', 2, 2, 'Fuel + toll for weekend trip');

-- 2024 July
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (8000,  DATE '2024-07-01', 3, 5, 'Rent Jul 2024');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2700,  DATE '2024-07-08', 8, 1, 'Monthly groceries Jul');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1500,  DATE '2024-07-13', 3, 5, 'Jul electricity');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1800,  DATE '2024-07-20', 7, 3, 'Online course subscription');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (620,   DATE '2024-07-27', 1, 1, 'Lunch with friends');

-- 2024 August
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (8000,  DATE '2024-08-01', 3, 5, 'Rent Aug 2024');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2900,  DATE '2024-08-06', 8, 1, 'Groceries Aug');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1350,  DATE '2024-08-10', 3, 5, 'Aug electricity');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (3500,  DATE '2024-08-15', 4, 4, 'Independence day shopping');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (750,   DATE '2024-08-23', 5, 2, 'OTT + gaming subscriptions');

-- 2024 September
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (8000,  DATE '2024-09-01', 3, 5, 'Rent Sep 2024');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2800,  DATE '2024-09-05', 8, 1, 'Groceries Sep');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1400,  DATE '2024-09-10', 3, 5, 'Sep utility bills');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (5000,  DATE '2024-09-17', 4, 4, 'Pre-festive wardrobe');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (850,   DATE '2024-09-25', 1, 1, 'Ganesh Chaturthi feast');

-- 2024 October
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (8000,  DATE '2024-10-01', 3, 5, 'Rent Oct 2024');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (3000,  DATE '2024-10-06', 8, 1, 'Festive grocery run');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1450,  DATE '2024-10-10', 3, 5, 'Oct electricity');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (7200,  DATE '2024-10-15', 4, 4, 'Navratri + Dussehra shopping');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (900,   DATE '2024-10-26', 2, 2, 'Diwali travel fuel');

-- 2024 November
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (8000,  DATE '2024-11-01', 3, 5, 'Rent Nov 2024');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2700,  DATE '2024-11-05', 8, 1, 'Groceries Nov');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1200,  DATE '2024-11-08', 3, 5, 'Nov electricity');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (8500,  DATE '2024-11-15', 4, 4, 'Diwali sale + Big Billion Day');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1600,  DATE '2024-11-25', 2, 3, 'Road trip long weekend');

-- 2024 December
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (8000,  DATE '2024-12-01', 3, 5, 'Rent Dec 2024');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (3200,  DATE '2024-12-06', 8, 1, 'Holiday grocery stock-up');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1300,  DATE '2024-12-10', 3, 5, 'Dec electricity');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (9000,  DATE '2024-12-20', 4, 4, 'Christmas + year-end gifts');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (3000,  DATE '2024-12-31', 5, 4, 'New Year Eve dinner + party');

-- ═══════════════════════════════════════════════════════════════════
--  SEED EXPENSES — 2025 (Jan–Dec, one per category per month)
--  category_id: 1=Food&Dining 2=Travel 3=Bills 4=Shopping
--               5=Entertainment 6=Health 7=Education 8=Groceries
--  payment_mode_id: 1=UPI 2=Cash 3=Debit 4=Credit 5=NetBanking
-- ═══════════════════════════════════════════════════════════════════

-- 2025 January
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (450,  DATE '2025-01-05', 1, 1, 'Dinner at restaurant');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1200, DATE '2025-01-10', 3, 5, 'Electricity bill Jan');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (3500, DATE '2025-01-15', 4, 4, 'Winter shopping');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (850,  DATE '2025-01-20', 8, 1, 'Monthly groceries');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (300,  DATE '2025-01-25', 2, 2, 'Cab fare');

-- 2025 February
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (620,  DATE '2025-02-03', 1, 1, 'Valentine dinner');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1100, DATE '2025-02-08', 3, 5, 'Water & internet bill');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (980,  DATE '2025-02-14', 4, 3, 'Gift shopping');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (750,  DATE '2025-02-18', 8, 1, 'Grocery run');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (500,  DATE '2025-02-22', 5, 2, 'Movie night');

-- 2025 March
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (380,  DATE '2025-03-02', 1, 1, 'Team lunch');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1350, DATE '2025-03-07', 3, 5, 'March electricity bill');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2200, DATE '2025-03-15', 2, 4, 'Weekend trip fuel');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (900,  DATE '2025-03-20', 8, 1, 'Groceries & vegetables');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (650,  DATE '2025-03-28', 6, 1, 'Medical checkup');

-- 2025 April
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (520,  DATE '2025-04-04', 1, 1, 'Biryani + cold drinks');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1200, DATE '2025-04-09', 3, 5, 'April electricity');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (3800, DATE '2025-04-13', 4, 4, 'Summer clothes shopping');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (850,  DATE '2025-04-18', 8, 1, 'Monthly groceries');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1500, DATE '2025-04-25', 7, 3, 'Course fee April');

-- 2025 May
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (460,  DATE '2025-05-02', 1, 1, 'Cafe brunch');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1400, DATE '2025-05-08', 3, 5, 'AC electricity spike');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2100, DATE '2025-05-12', 2, 3, 'Flight tickets');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (920,  DATE '2025-05-20', 8, 1, 'Groceries');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (700,  DATE '2025-05-27', 5, 2, 'Concert tickets');

-- 2025 June
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (550,  DATE '2025-06-03', 1, 1, 'Pizza night');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1550, DATE '2025-06-09', 3, 5, 'Heavy monsoon AC bill');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (4500, DATE '2025-06-15', 4, 4, 'Monsoon shopping haul');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (800,  DATE '2025-06-22', 8, 1, 'Monthly vegetables & dal');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1200, DATE '2025-06-28', 7, 3, 'Online course purchase');

-- 2025 July
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (490,  DATE '2025-07-04', 1, 1, 'Lunch with colleagues');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1300, DATE '2025-07-10', 3, 5, 'July electricity');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1800, DATE '2025-07-16', 2, 3, 'Metro + cab travel');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (870,  DATE '2025-07-22', 8, 1, 'Groceries');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (950,  DATE '2025-07-30', 6, 1, 'Dentist visit');

-- 2025 August
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (600,  DATE '2025-08-03', 1, 1, 'Independence day special');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1250, DATE '2025-08-09', 3, 5, 'August bills');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2700, DATE '2025-08-14', 4, 4, 'Festive shopping');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (910,  DATE '2025-08-20', 8, 1, 'Monthly groceries');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (800,  DATE '2025-08-27', 5, 2, 'OTT subscriptions');

-- 2025 September
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (530,  DATE '2025-09-02', 1, 1, 'Thali lunch');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1300, DATE '2025-09-07', 3, 5, 'Sep electricity');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (3200, DATE '2025-09-14', 4, 4, 'Phone accessories');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (860,  DATE '2025-09-19', 8, 1, 'Groceries');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1100, DATE '2025-09-26', 7, 3, 'Exam registration');

-- 2025 October
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (720,  DATE '2025-10-04', 1, 1, 'Dussehra feast');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1400, DATE '2025-10-08', 3, 5, 'Oct electricity');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (5500, DATE '2025-10-15', 4, 4, 'Navratri shopping spree');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (940,  DATE '2025-10-22', 8, 1, 'Groceries for the month');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (600,  DATE '2025-10-29', 5, 2, 'Halloween event');

-- 2025 November
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (480,  DATE '2025-11-03', 1, 1, 'Diwali sweets order');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1200, DATE '2025-11-08', 3, 5, 'Nov electricity');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (6800, DATE '2025-11-15', 4, 4, 'Diwali sale shopping');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (880,  DATE '2025-11-22', 8, 1, 'Groceries');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1500, DATE '2025-11-28', 2, 3, 'Long weekend road trip fuel');

-- 2025 December
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (900,  DATE '2025-12-03', 1, 1, 'Christmas lunch');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1300, DATE '2025-12-08', 3, 5, 'Dec electricity');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (7200, DATE '2025-12-20', 4, 4, 'Year-end gift shopping');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (950,  DATE '2025-12-24', 8, 1, 'Holiday groceries');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2500, DATE '2025-12-31', 5, 4, 'New Year Eve party');

-- ═══════════════════════════════════════════════════════════════════
--  SEED EXPENSES — 2026 (Jan–Oct)
-- ═══════════════════════════════════════════════════════════════════

-- 2026 January
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (550,  DATE '2026-01-05', 1, 1, 'New year restaurant lunch');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1100, DATE '2026-01-10', 3, 5, 'Jan electricity + broadband');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (4200, DATE '2026-01-15', 4, 4, 'Winter clearance sale');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (900,  DATE '2026-01-20', 8, 1, 'Monthly groceries');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (350,  DATE '2026-01-27', 2, 2, 'Auto rickshaw commute');

-- 2026 February
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (680,  DATE '2026-02-07', 1, 1, 'Brunch outing');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1150, DATE '2026-02-12', 3, 5, 'Feb utility bills');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1900, DATE '2026-02-14', 4, 3, 'Valentine gifts');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (850,  DATE '2026-02-20', 8, 1, 'Groceries & fruits');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (700,  DATE '2026-02-25', 6, 1, 'Pharmacy medicines');

-- 2026 March
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (420,  DATE '2026-03-04', 1, 1, 'Quick lunch');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1400, DATE '2026-03-10', 3, 5, 'Mar electricity');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (3100, DATE '2026-03-16', 2, 4, 'Holi travel + stay');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (920,  DATE '2026-03-22', 8, 1, 'Groceries');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (850,  DATE '2026-03-28', 7, 3, 'Online subscription renewal');

-- 2026 April
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (580,  DATE '2026-04-03', 1, 1, 'Team outing dinner');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1500, DATE '2026-04-08', 3, 5, 'April heavy summer bill');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (4900, DATE '2026-04-14', 4, 4, 'Summer wardrobe refresh');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (870,  DATE '2026-04-20', 8, 1, 'Monthly groceries');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2000, DATE '2026-04-26', 7, 3, 'Semester course fee');

-- 2026 May
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (500,  DATE '2026-05-05', 1, 1, 'Cafe hangout');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1700, DATE '2026-05-11', 3, 5, 'May peak AC bill');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (5500, DATE '2026-05-15', 2, 4, 'Summer vacation trip');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (930,  DATE '2026-05-21', 8, 1, 'Groceries');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1200, DATE '2026-05-28', 5, 2, 'Gaming event + tickets');

-- 2026 June
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (610,  DATE '2026-06-04', 1, 1, 'Monsoon comfort food');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1600, DATE '2026-06-10', 3, 5, 'Jun electricity + maintenance');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (3400, DATE '2026-06-16', 4, 4, 'Monsoon outerwear');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (890,  DATE '2026-06-22', 8, 1, 'Groceries');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (600,  DATE '2026-06-29', 5, 1, 'Movie + popcorn');

-- 2026 July
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (470,  DATE '2026-07-03', 1, 1, 'Quick takeout');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1350, DATE '2026-07-09', 3, 5, 'Jul electricity');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2000, DATE '2026-07-15', 2, 3, 'Travel + hotel weekend');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (880,  DATE '2026-07-21', 8, 1, 'Monthly groceries');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1100, DATE '2026-07-28', 6, 1, 'Eye checkup + glasses');

-- 2026 August
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (730,  DATE '2026-08-04', 1, 1, 'Independence day lunch');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1250, DATE '2026-08-10', 3, 5, 'Aug electricity');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (3700, DATE '2026-08-15', 4, 4, 'Raksha Bandhan shopping');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (950,  DATE '2026-08-20', 8, 1, 'Groceries');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (900,  DATE '2026-08-27', 5, 2, 'Streaming + music subs');

-- 2026 September
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (660,  DATE '2026-09-03', 1, 1, 'Ganesh Chaturthi sweets');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1300, DATE '2026-09-09', 3, 5, 'Sep utility bills');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (2800, DATE '2026-09-14', 4, 4, 'Pre-festive shopping');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (900,  DATE '2026-09-20', 8, 1, 'Groceries & snacks');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1400, DATE '2026-09-27', 7, 3, 'Certification exam fee');

-- 2026 October (current month seed)
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes,is_starred) VALUES (450, DATE '2026-10-01', 1, 1, 'Lunch with team', 1);
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (1200, DATE '2026-10-02', 3, 5, 'Electricity Bill October');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (150,  DATE '2026-10-03', 2, 2, 'Cab fare');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (870,  DATE '2026-10-03', 8, 1, 'Groceries top-up');
INSERT INTO EXPENSES (amount,expense_date,category_id,payment_mode_id,notes) VALUES (3200, DATE '2026-10-03', 4, 4, 'Dussehra shopping');

-- ═══════════════════════════════════════════════════════════════════
--  SEED DEBTS & LOANS — 2025 and 2026
-- ═══════════════════════════════════════════════════════════════════

-- 2025 debts
INSERT INTO DEBTS_LOANS (person_name,debt_type,principal_amount,interest_rate,due_date,status,notes)
VALUES ('Rahul Sharma','BORROWED',5000,2.5,DATE '2025-03-31','REPAID','Personal emergency loan Q1 2025');

INSERT INTO DEBTS_LOANS (person_name,debt_type,principal_amount,interest_rate,due_date,status,notes)
VALUES ('Priya Mehta','LENT',3000,0,DATE '2025-05-15','REPAID','Helped with college fees');

INSERT INTO DEBTS_LOANS (person_name,debt_type,principal_amount,interest_rate,due_date,status,notes)
VALUES ('Vikram Singh','BORROWED',8000,3.0,DATE '2025-07-31','REPAID','Laptop purchase loan');

INSERT INTO DEBTS_LOANS (person_name,debt_type,principal_amount,interest_rate,due_date,status,notes)
VALUES ('Ananya Roy','LENT',2000,0,DATE '2025-09-30','REPAID','Textbook money');

INSERT INTO DEBTS_LOANS (person_name,debt_type,principal_amount,interest_rate,due_date,status,notes)
VALUES ('Kabir Das','BORROWED',4500,2.0,DATE '2025-11-30','REPAID','Rent advance for Nov 2025');

INSERT INTO DEBTS_LOANS (person_name,debt_type,principal_amount,interest_rate,due_date,status,notes)
VALUES ('Sneha Patel','LENT',1500,0,DATE '2025-12-25','OVERDUE','Christmas shopping advance');

-- 2026 debts
INSERT INTO DEBTS_LOANS (person_name,debt_type,principal_amount,interest_rate,due_date,status,notes)
VALUES ('Rohan Kapoor','BORROWED',6000,2.0,DATE '2026-03-31','REPAID','Course registration fee 2026');

INSERT INTO DEBTS_LOANS (person_name,debt_type,principal_amount,interest_rate,due_date,status,notes)
VALUES ('Meera Iyer','LENT',2500,0,DATE '2026-05-20','REPAID','Lent for medical expenses');

INSERT INTO DEBTS_LOANS (person_name,debt_type,principal_amount,interest_rate,due_date,status,notes)
VALUES ('Arjun Nair','BORROWED',7500,3.5,DATE '2026-07-31','REPAID','Home renovation contribution');

INSERT INTO DEBTS_LOANS (person_name,debt_type,principal_amount,interest_rate,due_date,status,notes)
VALUES ('Divya Sharma','LENT',3500,0,DATE '2026-09-15','OVERDUE','Festival advance not returned');

-- Active pending debts (due soon in Oct 2026)
INSERT INTO DEBTS_LOANS (person_name,debt_type,principal_amount,interest_rate,due_date,status,notes)
VALUES ('Rahul Sharma','BORROWED',2500,2.5,DATE '2026-10-15','PENDING','Emergency cash for rent Oct');

INSERT INTO DEBTS_LOANS (person_name,debt_type,principal_amount,interest_rate,due_date,status,notes)
VALUES ('Ananya Roy','LENT',1500,0,DATE '2026-10-01','OVERDUE','Textbook purchase overdue');

INSERT INTO DEBTS_LOANS (person_name,debt_type,principal_amount,interest_rate,due_date,status,notes)
VALUES ('Kiran Bhat','LENT',4000,1.5,DATE '2026-11-30','PENDING','Startup seed money lent');

INSERT INTO DEBTS_LOANS (person_name,debt_type,principal_amount,interest_rate,due_date,status,notes)
VALUES ('Nisha Gupta','BORROWED',3000,0,DATE '2026-12-31','PENDING','End of year expenses loan');

COMMIT;
