-- =====================================================================
--  Grand Horizon Student Hostel Management System - Sample Data
-- =====================================================================
USE hostel_management;

-- ---------------------------------------------------------------------
-- Rooms (12 rooms across types and blocks)
-- feature_flag: SINGLE/DOUBLE/TRIPLE = 1(yes)/0(no) for their bool feature;
--               DORMITORY = number of lockers
-- ---------------------------------------------------------------------
INSERT INTO rooms (room_number, room_type, block, floor, capacity, base_monthly_fee, occupied_beds, status, feature_flag) VALUES
('A-101', 'SINGLE',    'A-Block', 1, 1, 5000.00, 0, 'AVAILABLE', 1),
('A-102', 'SINGLE',    'A-Block', 1, 1, 5000.00, 0, 'AVAILABLE', 0),
('A-201', 'DOUBLE',    'A-Block', 2, 2, 3500.00, 0, 'AVAILABLE', 1),
('A-202', 'DOUBLE',    'A-Block', 2, 2, 3500.00, 0, 'AVAILABLE', 0),
('B-101', 'DOUBLE',    'B-Block', 1, 2, 3500.00, 0, 'AVAILABLE', 1),
('B-201', 'TRIPLE',    'B-Block', 2, 3, 2800.00, 0, 'AVAILABLE', 1),
('B-202', 'TRIPLE',    'B-Block', 2, 3, 2800.00, 0, 'AVAILABLE', 0),
('C-101', 'TRIPLE',    'C-Block', 1, 3, 2800.00, 0, 'AVAILABLE', 1),
('C-201', 'DORMITORY', 'C-Block', 2, 6, 1800.00, 0, 'AVAILABLE', 6),
('C-202', 'DORMITORY', 'C-Block', 2, 6, 1800.00, 0, 'AVAILABLE', 6),
('D-101', 'DORMITORY', 'D-Block', 1, 6, 1800.00, 0, 'AVAILABLE', 8),
('D-102', 'SINGLE',    'D-Block', 1, 1, 5500.00, 0, 'MAINTENANCE', 1);

-- ---------------------------------------------------------------------
-- Students (8 students)
-- ---------------------------------------------------------------------
INSERT INTO students (name, roll_number, course, year, phone, email, guardian_name, guardian_phone,
    address, id_proof_type, id_proof_number, admission_date) VALUES
('Aditya Sharma', 'CSE2026001', 'B.Tech CSE', 2, '9876543210', 'aditya.sharma@gmail.com',
    'Ramesh Sharma', '9876500001', '12 MG Road, Mumbai', 'AADHAR', '1234-5678-9012', '2025-07-15'),
('Priya Nair', 'CSE2026002', 'B.Tech CSE', 2, '9876543211', 'priya.nair@gmail.com',
    'Suresh Nair', '9876500002', '45 Marine Drive, Kochi', 'AADHAR', '2234-5678-9012', '2025-07-15'),
('Rohan Verma', 'ECE2025010', 'B.Tech ECE', 3, '9876543212', 'rohan.verma@gmail.com',
    'Anil Verma', '9876500003', '78 Sector 21, Chandigarh', 'AADHAR', '3234-5678-9012', '2024-07-20'),
('Sneha Reddy', 'ME2026005', 'B.Tech Mechanical', 1, '9876543213', 'sneha.reddy@gmail.com',
    'Krishna Reddy', '9876500004', '9 Banjara Hills, Hyderabad', 'AADHAR', '4234-5678-9012', '2026-07-10'),
('Karan Mehta', 'CSE2025020', 'B.Tech CSE', 3, '9876543214', 'karan.mehta@gmail.com',
    'Vijay Mehta', '9876500005', '22 Satellite Road, Ahmedabad', 'AADHAR', '5234-5678-9012', '2024-07-18'),
('Ananya Das', 'CE2026008', 'B.Tech Civil', 1, '9876543215', 'ananya.das@gmail.com',
    'Bimal Das', '9876500006', '5 Salt Lake, Kolkata', 'AADHAR', '6234-5678-9012', '2026-07-12'),
('Vikram Singh', 'EEE2025015', 'B.Tech EEE', 3, '9876543216', 'vikram.singh@gmail.com',
    'Harpal Singh', '9876500007', '31 Civil Lines, Jaipur', 'AADHAR', '7234-5678-9012', '2024-07-22'),
('Meera Iyer', 'CSE2026003', 'B.Tech CSE', 2, '9876543217', 'meera.iyer@gmail.com',
    'Ganesh Iyer', '9876500008', '14 Anna Nagar, Chennai', 'AADHAR', '8234-5678-9012', '2025-07-16');

-- ---------------------------------------------------------------------
-- Mess Menu (23 items across 6 categories)
-- ---------------------------------------------------------------------
INSERT INTO mess_items (name, category, price) VALUES
('Poha', 'BREAKFAST', 40.00),
('Idli Sambhar', 'BREAKFAST', 50.00),
('Aloo Paratha', 'BREAKFAST', 60.00),
('Bread Omelette', 'BREAKFAST', 45.00),
('Masala Dosa', 'BREAKFAST', 65.00),
('Veg Thali', 'LUNCH', 90.00),
('Chicken Curry Meal', 'LUNCH', 150.00),
('Veg Dum Biryani', 'LUNCH', 130.00),
('Rajma Chawal', 'LUNCH', 80.00),
('Chicken Biryani', 'DINNER', 160.00),
('Paneer Butter Masala Meal', 'DINNER', 140.00),
('Dal Tadka with Rice', 'DINNER', 85.00),
('Grilled Chicken', 'DINNER', 180.00),
('French Fries', 'SNACKS', 60.00),
('Veg Sandwich', 'SNACKS', 50.00),
('Samosa (2 pcs)', 'SNACKS', 30.00),
('Chicken Roll', 'SNACKS', 90.00),
('Cold Coffee', 'BEVERAGES', 70.00),
('Masala Chai', 'BEVERAGES', 20.00),
('Fresh Lime Soda', 'BEVERAGES', 40.00),
('Chocolate Lava Cake', 'DESSERTS', 90.00),
('Gulab Jamun (2 pcs)', 'DESSERTS', 50.00),
('Ice Cream Scoop', 'DESSERTS', 60.00);

-- ---------------------------------------------------------------------
-- Allocations (8 active/vacated allocations)
-- ---------------------------------------------------------------------
INSERT INTO allocations (student_id, room_id, bed_number, allocation_date, vacate_date, academic_year, status) VALUES
(1, 3, 1, '2025-07-16', NULL, '2025-2026', 'ACTIVE'),
(2, 3, 2, '2025-07-16', NULL, '2025-2026', 'ACTIVE'),
(3, 6, 1, '2024-07-21', NULL, '2025-2026', 'ACTIVE'),
(4, 9, 1, '2026-07-11', NULL, '2026-2027', 'ACTIVE'),
(5, 1, 1, '2024-07-19', NULL, '2025-2026', 'ACTIVE'),
(6, 9, 2, '2026-07-13', NULL, '2026-2027', 'ACTIVE'),
(7, 6, 2, '2024-07-23', NULL, '2025-2026', 'ACTIVE'),
(8, 2, 1, '2025-07-17', '2026-05-30', '2025-2026', 'VACATED');

-- NOTE: occupied_beds is denormalized on the rooms table and is normally
-- maintained by AllocationDAO.allocate()/vacate() inside a transaction.
-- Sync it here to match the seeded allocations above.
UPDATE rooms SET occupied_beds = 2 WHERE room_id = 3;   -- A-201 DOUBLE (Aditya + Priya)
UPDATE rooms SET occupied_beds = 1 WHERE room_id = 6;   -- B-201 TRIPLE (Rohan)
UPDATE rooms SET occupied_beds = 2 WHERE room_id = 9;   -- C-201 DORMITORY (Sneha + Ananya)
UPDATE rooms SET occupied_beds = 1 WHERE room_id = 1;   -- A-101 SINGLE (Karan)
UPDATE rooms SET occupied_beds = 2 WHERE room_id = 6;   -- B-201 (Rohan + Vikram) -- corrected below

-- Fix: Rohan(alloc3, bed1) and Vikram(alloc7, bed2) are both in room_id 6 => 2 occupied
UPDATE rooms SET occupied_beds = 2 WHERE room_id = 6;

-- ---------------------------------------------------------------------
-- Mess Orders (extra ad-hoc orders beyond standard mess plan)
-- ---------------------------------------------------------------------
INSERT INTO mess_orders (allocation_id, order_date) VALUES
(1, '2026-08-05'),
(1, '2026-08-10'),
(3, '2026-08-07');

INSERT INTO mess_order_items (order_id, item_id, quantity, unit_price) VALUES
(1, 13, 1, 180.00),  -- Grilled Chicken x1
(1, 8,  2, 130.00),  -- Veg Dum Biryani x2
(1, 18, 3, 70.00),   -- Cold Coffee x3
(1, 21, 2, 90.00),   -- Chocolate Lava Cake x2
(2, 10, 1, 160.00),  -- Chicken Biryani x1
(2, 14, 2, 60.00),   -- French Fries x2
(3, 17, 1, 90.00),   -- Chicken Roll x1
(3, 19, 2, 20.00);   -- Masala Chai x2

-- ---------------------------------------------------------------------
-- Payments (2 sample fee payments)
-- ---------------------------------------------------------------------
INSERT INTO payments (allocation_id, amount, mode, transaction_reference, payment_date) VALUES
(1, 4550.00, 'UPI', 'HMS-1-34521', '2026-08-16'),
(3, 3220.00, 'CASH', 'HMS-3-67890', '2026-08-01');
