-- =====================================================================
--  Grand Horizon Student Hostel Management System - Schema
-- =====================================================================

DROP DATABASE IF EXISTS hostel_management;
CREATE DATABASE hostel_management;
USE hostel_management;

-- ---------------------------------------------------------------------
-- Table: students
-- ---------------------------------------------------------------------
CREATE TABLE students (
    student_id      INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    roll_number     VARCHAR(30)  NOT NULL UNIQUE,
    course          VARCHAR(60)  NOT NULL,
    year            INT NOT NULL CHECK (year BETWEEN 1 AND 5),
    phone           VARCHAR(10)  NOT NULL UNIQUE CHECK (phone REGEXP '^[0-9]{10}$'),
    email           VARCHAR(100) NOT NULL,
    guardian_name   VARCHAR(100),
    guardian_phone  VARCHAR(10)  CHECK (guardian_phone REGEXP '^[0-9]{10}$'),
    address         VARCHAR(255),
    id_proof_type   ENUM('AADHAR','PASSPORT','VOTER_ID','DRIVING_LICENSE') NOT NULL,
    id_proof_number VARCHAR(50) NOT NULL,
    admission_date  DATE NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Table: rooms  (single physical table backing Room + subclasses)
-- ---------------------------------------------------------------------
CREATE TABLE rooms (
    room_id           INT AUTO_INCREMENT PRIMARY KEY,
    room_number       VARCHAR(15) NOT NULL UNIQUE,
    room_type         ENUM('SINGLE','DOUBLE','TRIPLE','DORMITORY') NOT NULL,
    block             VARCHAR(30) NOT NULL,
    floor             INT NOT NULL CHECK (floor >= 0),
    capacity          INT NOT NULL CHECK (capacity > 0),
    base_monthly_fee  DECIMAL(10,2) NOT NULL CHECK (base_monthly_fee >= 0),
    occupied_beds     INT NOT NULL DEFAULT 0 CHECK (occupied_beds >= 0),
    status            ENUM('AVAILABLE','FULL','MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    feature_flag      INT NOT NULL DEFAULT 0,   -- bool flag or locker count depending on type
    CHECK (occupied_beds <= capacity)
);

-- ---------------------------------------------------------------------
-- Table: allocations  (links students to rooms/beds)
-- ---------------------------------------------------------------------
CREATE TABLE allocations (
    allocation_id   INT AUTO_INCREMENT PRIMARY KEY,
    student_id      INT NOT NULL,
    room_id         INT NOT NULL,
    bed_number      INT NOT NULL CHECK (bed_number > 0),
    allocation_date DATE NOT NULL,
    vacate_date     DATE,
    academic_year   VARCHAR(15) NOT NULL,
    status          ENUM('ACTIVE','VACATED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_alloc_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE RESTRICT,
    CONSTRAINT fk_alloc_room    FOREIGN KEY (room_id)    REFERENCES rooms(room_id)    ON DELETE RESTRICT,
    CONSTRAINT chk_dates CHECK (vacate_date IS NULL OR vacate_date >= allocation_date)
);

-- ---------------------------------------------------------------------
-- Table: mess_items (hostel mess menu)
-- ---------------------------------------------------------------------
CREATE TABLE mess_items (
    item_id     INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(80) NOT NULL,
    category    ENUM('BREAKFAST','LUNCH','DINNER','SNACKS','BEVERAGES','DESSERTS') NOT NULL,
    price       DECIMAL(8,2) NOT NULL CHECK (price >= 0)
);

-- ---------------------------------------------------------------------
-- Table: mess_orders (header - extra/ad-hoc orders per allocation)
-- ---------------------------------------------------------------------
CREATE TABLE mess_orders (
    order_id       INT AUTO_INCREMENT PRIMARY KEY,
    allocation_id  INT NOT NULL,
    order_date     DATE NOT NULL,
    CONSTRAINT fk_order_alloc FOREIGN KEY (allocation_id) REFERENCES allocations(allocation_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Table: mess_order_items (junction table: orders <-> items, M:N)
-- ---------------------------------------------------------------------
CREATE TABLE mess_order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id      INT NOT NULL,
    item_id       INT NOT NULL,
    quantity      INT NOT NULL CHECK (quantity > 0),
    unit_price    DECIMAL(8,2) NOT NULL,     -- price at time of order (historical accuracy)
    CONSTRAINT fk_moi_order FOREIGN KEY (order_id) REFERENCES mess_orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_moi_item  FOREIGN KEY (item_id)  REFERENCES mess_items(item_id)   ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- Table: payments
-- ---------------------------------------------------------------------
CREATE TABLE payments (
    payment_id            INT AUTO_INCREMENT PRIMARY KEY,
    allocation_id         INT NOT NULL,
    amount                DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    mode                  ENUM('CASH','CARD','UPI','NETBANKING') NOT NULL,
    transaction_reference VARCHAR(40) NOT NULL UNIQUE,
    payment_date          DATE NOT NULL,
    CONSTRAINT fk_payment_alloc FOREIGN KEY (allocation_id) REFERENCES allocations(allocation_id) ON DELETE RESTRICT
);

-- =====================================================================
-- TRIGGERS
-- =====================================================================

DELIMITER //

-- When an allocation is marked VACATED, free the room automatically.
CREATE TRIGGER trg_allocation_vacated
AFTER UPDATE ON allocations
FOR EACH ROW
BEGIN
    IF NEW.status = 'VACATED' AND OLD.status != 'VACATED' THEN
        UPDATE rooms
        SET occupied_beds = GREATEST(occupied_beds - 1, 0),
            status = 'AVAILABLE'
        WHERE room_id = NEW.room_id;
    END IF;
END //

-- Prevent allocating a student who already has an ACTIVE allocation elsewhere.
CREATE TRIGGER trg_prevent_double_allocation
BEFORE INSERT ON allocations
FOR EACH ROW
BEGIN
    DECLARE active_count INT;
    SELECT COUNT(*) INTO active_count
    FROM allocations
    WHERE student_id = NEW.student_id AND status = 'ACTIVE';

    IF active_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Student already has an active bed allocation.';
    END IF;
END //

-- Automatically mark a room FULL when its last free bed is taken.
CREATE TRIGGER trg_room_full_check
BEFORE UPDATE ON rooms
FOR EACH ROW
BEGIN
    IF NEW.occupied_beds >= NEW.capacity THEN
        SET NEW.status = 'FULL';
    ELSEIF NEW.status = 'FULL' AND NEW.occupied_beds < NEW.capacity THEN
        SET NEW.status = 'AVAILABLE';
    END IF;
END //

DELIMITER ;

-- =====================================================================
-- VIEWS
-- =====================================================================

-- Consolidated view of active allocations with student & room info.
CREATE VIEW vw_active_allocations AS
SELECT a.allocation_id, s.name AS student_name, s.roll_number, r.room_number,
       r.room_type, a.bed_number, a.allocation_date, a.academic_year
FROM allocations a
JOIN students s ON a.student_id = s.student_id
JOIN rooms r ON a.room_id = r.room_id
WHERE a.status = 'ACTIVE';

-- Fee collection summary per allocation.
CREATE VIEW vw_fee_summary AS
SELECT a.allocation_id, s.name AS student_name, r.room_number,
       COALESCE(SUM(p.amount), 0) AS total_paid, COUNT(p.payment_id) AS payment_count
FROM allocations a
JOIN students s ON a.student_id = s.student_id
JOIN rooms r ON a.room_id = r.room_id
LEFT JOIN payments p ON a.allocation_id = p.allocation_id
GROUP BY a.allocation_id, s.name, r.room_number;

-- Occupancy summary per room type.
CREATE VIEW vw_occupancy_summary AS
SELECT room_type, COUNT(*) AS total_rooms, SUM(capacity) AS total_beds,
       SUM(occupied_beds) AS beds_occupied, SUM(capacity - occupied_beds) AS beds_free
FROM rooms
GROUP BY room_type;

-- =====================================================================
-- STORED PROCEDURES
-- =====================================================================

DELIMITER //

-- Checks a student out: vacates the bed and records a final payment in one call.
CREATE PROCEDURE sp_checkout(
    IN p_allocation_id INT,
    IN p_amount DECIMAL(10,2),
    IN p_mode VARCHAR(20),
    OUT p_success BOOLEAN
)
BEGIN
    DECLARE v_room_id INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_success = FALSE;
    END;

    START TRANSACTION;

    SELECT room_id INTO v_room_id FROM allocations
    WHERE allocation_id = p_allocation_id AND status = 'ACTIVE';

    IF v_room_id IS NOT NULL THEN
        INSERT INTO payments (allocation_id, amount, mode, transaction_reference, payment_date)
        VALUES (p_allocation_id, p_amount, p_mode,
                CONCAT('HMS-', p_allocation_id, '-', FLOOR(RAND() * 90000) + 10000), CURDATE());

        UPDATE allocations SET status = 'VACATED', vacate_date = CURDATE()
        WHERE allocation_id = p_allocation_id;

        SET p_success = TRUE;
        COMMIT;
    ELSE
        SET p_success = FALSE;
        ROLLBACK;
    END IF;
END //

-- Returns the number of students currently allocated to a given block.
CREATE PROCEDURE sp_block_occupancy(IN p_block VARCHAR(30))
BEGIN
    SELECT r.block, COUNT(*) AS students_housed
    FROM allocations a
    JOIN rooms r ON a.room_id = r.room_id
    WHERE a.status = 'ACTIVE' AND r.block = p_block
    GROUP BY r.block;
END //

DELIMITER ;

-- =====================================================================
-- INDEXES for common lookups
-- =====================================================================
CREATE INDEX idx_students_roll ON students(roll_number);
CREATE INDEX idx_rooms_type ON rooms(room_type);
CREATE INDEX idx_allocations_student ON allocations(student_id);
CREATE INDEX idx_allocations_room ON allocations(room_id);
CREATE INDEX idx_payments_alloc ON payments(allocation_id);
