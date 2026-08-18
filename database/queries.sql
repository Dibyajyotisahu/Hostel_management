-- =====================================================================
--  Grand Horizon Hostel Management System - Advanced SQL Query Bank
--  17 queries demonstrating JOINs, subqueries, GROUP BY/HAVING,
--  CASE expressions, window functions, and views.
-- =====================================================================
USE hostel_management;

-- 1. All currently active allocations with student & room info (INNER JOIN)
SELECT s.name, s.roll_number, r.room_number, r.room_type, a.bed_number, a.allocation_date
FROM allocations a
JOIN students s ON a.student_id = s.student_id
JOIN rooms r ON a.room_id = r.room_id
WHERE a.status = 'ACTIVE';

-- 2. Revenue by room type, including types with zero revenue (LEFT JOIN)
SELECT rt.room_type, COALESCE(SUM(p.amount), 0) AS revenue
FROM (SELECT DISTINCT room_type FROM rooms) rt
LEFT JOIN rooms r ON rt.room_type = r.room_type
LEFT JOIN allocations a ON r.room_id = a.room_id
LEFT JOIN payments p ON a.allocation_id = p.allocation_id
GROUP BY rt.room_type
ORDER BY revenue DESC;

-- 3. Students with more than one allocation record (GROUP BY + HAVING)
SELECT s.name, COUNT(*) AS allocation_count
FROM allocations a
JOIN students s ON a.student_id = s.student_id
GROUP BY s.name
HAVING COUNT(*) > 1;

-- 4. Rooms priced between Rs.3000 and Rs.6000 per month (BETWEEN, computed fee)
SELECT room_number, room_type,
       (base_monthly_fee * CASE room_type
            WHEN 'SINGLE' THEN 1.60 WHEN 'DOUBLE' THEN 1.30
            WHEN 'TRIPLE' THEN 1.15 ELSE 1.00 END) AS computed_fee
FROM rooms
HAVING computed_fee BETWEEN 3000 AND 6000
ORDER BY computed_fee;

-- 5. Students with fee dues below Rs.4000 paid so far (subquery + LEFT JOIN)
SELECT s.name, s.roll_number
FROM students s
WHERE s.student_id IN (
    SELECT a.student_id FROM allocations a
    LEFT JOIN payments p ON a.allocation_id = p.allocation_id
    GROUP BY a.student_id
    HAVING COALESCE(SUM(p.amount), 0) < 4000
);

-- 6. Overlap-free bed availability check for a given room/bed
SELECT COUNT(*) AS conflict_count FROM allocations
WHERE room_id = 3 AND bed_number = 1 AND status = 'ACTIVE';

-- 7. Payment revenue broken down by mode, with a human label (CASE + GROUP BY)
SELECT mode,
       CASE mode
           WHEN 'CASH' THEN 'Walk-in Payment'
           WHEN 'CARD' THEN 'Card Terminal'
           WHEN 'UPI' THEN 'UPI QR'
           ELSE 'Bank Transfer'
       END AS description,
       SUM(amount) AS total
FROM payments
GROUP BY mode
ORDER BY total DESC;

-- 8. Top 5 best-selling mess items overall (aggregate + junction table)
SELECT mi.name, SUM(moi.quantity) AS total_sold
FROM mess_order_items moi
JOIN mess_items mi ON moi.item_id = mi.item_id
GROUP BY mi.name
ORDER BY total_sold DESC
LIMIT 5;

-- 9. Allocations that started within a specific academic term (BETWEEN dates)
SELECT s.name, r.room_number, a.allocation_date
FROM allocations a
JOIN students s ON a.student_id = s.student_id
JOIN rooms r ON a.room_id = r.room_id
WHERE a.allocation_date BETWEEN '2025-07-01' AND '2025-08-31'
ORDER BY a.allocation_date;

-- 10. Student count per course (GROUP BY)
SELECT course, COUNT(*) AS total_students
FROM students
GROUP BY course
ORDER BY total_students DESC;

-- 11. Most-occupied room type by number of active allocations (GROUP BY + ORDER BY)
SELECT r.room_type, COUNT(*) AS active_allocations
FROM allocations a
JOIN rooms r ON a.room_id = r.room_id
WHERE a.status = 'ACTIVE'
GROUP BY r.room_type
ORDER BY active_allocations DESC
LIMIT 1;

-- 12. Rooms currently with free beds, sorted by most vacancies first
SELECT room_number, room_type, capacity, occupied_beds, (capacity - occupied_beds) AS free_beds
FROM rooms
WHERE occupied_beds < capacity AND status <> 'MAINTENANCE'
ORDER BY free_beds DESC;

-- 13. Total mess spend per student (JOIN across allocations + mess tables)
SELECT s.name, SUM(moi.quantity * moi.unit_price) AS total_mess_spend
FROM students s
JOIN allocations a ON s.student_id = a.student_id
JOIN mess_orders mo ON a.allocation_id = mo.allocation_id
JOIN mess_order_items moi ON mo.order_id = moi.order_id
GROUP BY s.name
ORDER BY total_mess_spend DESC;

-- 14. Ranking students by total fees paid using a window function
SELECT s.name, SUM(p.amount) AS total_paid,
       RANK() OVER (ORDER BY SUM(p.amount) DESC) AS payment_rank
FROM students s
JOIN allocations a ON s.student_id = a.student_id
JOIN payments p ON a.allocation_id = p.allocation_id
GROUP BY s.name;

-- 15. Using the vw_occupancy_summary view for a quick dashboard figure
SELECT * FROM vw_occupancy_summary;

-- 16. Using the vw_fee_summary view to find allocations with zero payments
SELECT * FROM vw_fee_summary WHERE total_paid = 0;

-- 17. Distinct room types currently available for allocation
SELECT DISTINCT room_type FROM rooms WHERE status = 'AVAILABLE';
