import dao.MessDAO;
import exception.InvalidAllocationException;
import exception.InvalidPaymentException;
import exception.RoomNotAvailableException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import model.*;
import service.AllocationService;
import service.FeeBillingService;
import service.HostelService;
import util.DBConnection;

/**
 * Console entry point for the soa university Student Hostel Management System.
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final HostelService hostelService = new HostelService();
    private static final AllocationService allocationService = new AllocationService();
    private static final FeeBillingService billingService = new FeeBillingService();
    private static final MessDAO messDAO = new MessDAO();

    public static void main(String[] args) {
        printBanner();
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ");
            try {
                switch (choice) {
                    case 1: roomMenu(); break;
                    case 2: studentMenu(); break;
                    case 3: allocationMenu(); break;
                    case 4: messMenu(); break;
                    case 5: billingMenu(); break;
                    case 6: analyticsMenu(); break;
                    case 7:
                        running = false;
                        System.out.println("Thank you for using  Soa Student Hostel Management System!");
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (RoomNotAvailableException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input: " + e.getMessage());
            }
        }
        DBConnection.close();
        sc.close();
    }

    // ================= MENUS =================

    private static void printBanner() {
        System.out.println("+========================================================+");
        System.out.println("|       Soa Student Hostel Management System     |");
        System.out.println("|      Technology : Java + MySQL + JDBC                   |");
        System.out.println("+========================================================+");
    }

    private static void printMenu() {
        System.out.println("\n=========================================================");
        System.out.println("        *  soa university HOSTEL MANAGEMENT  *");
        System.out.println("=========================================================");
        System.out.println("   1. Room Management");
        System.out.println("   2. Student Management");
        System.out.println("   3. Allocation Management");
        System.out.println("   4. Mess Menu & Orders");
        System.out.println("   5. Billing & Payments");
        System.out.println("   6. Analytics & Reports");
        System.out.println("   7. Exit");
    }

    // ---------- ROOM MENU ----------
    private static void roomMenu() throws SQLException {
        System.out.println("\n-- Room Management --");
        System.out.println("1. Add Room  2. View All Rooms  3. Rooms With Free Beds");
        System.out.println("4. Search by Type  5. Search by Fee Range  6. Delete Room  0. Back");
        int c = readInt("Choice: ");
        switch (c) {
            case 1: addRoom(); break;
            case 2: hostelService.listRooms().forEach(System.out::println); break;
            case 3: hostelService.listRoomsWithFreeBeds().forEach(System.out::println); break;
            case 4: {
                String type = readString("Room type (SINGLE/DOUBLE/TRIPLE/DORMITORY): ").toUpperCase();
                hostelService.listRoomsByType(type).forEach(System.out::println);
                break;
            }
            case 5: {
                double min = readDouble("Min fee: ");
                double max = readDouble("Max fee: ");
                hostelService.listRoomsByFeeRange(min, max).forEach(System.out::println);
                break;
            }
            case 6: {
                int id = readInt("Room ID to delete: ");
                System.out.println(hostelService.deleteRoom(id) ? "Deleted." : "Not found.");
                break;
            }
            default: break;
        }
    }

    private static void addRoom() throws SQLException {
        String number = readString("Room Number: ");
        String block = readString("Block (e.g. A-Block): ");
        int floor = readInt("Floor: ");
        double baseFee = readDouble("Base Monthly Fee (Rs.): ");
        String type = readString("Type (SINGLE/DOUBLE/TRIPLE/DORMITORY): ").toUpperCase();

        Room room;
        switch (type) {
            case "SINGLE":
                room = new SingleRoom(0, number, block, floor, baseFee, "AVAILABLE",
                        readBoolean("Attached bathroom? (y/n): "));
                break;
            case "DOUBLE":
                room = new DoubleRoom(0, number, block, floor, baseFee, "AVAILABLE",
                        readBoolean("Has balcony? (y/n): "));
                break;
            case "TRIPLE":
                room = new TripleRoom(0, number, block, floor, baseFee, "AVAILABLE",
                        readBoolean("Has study lounge? (y/n): "));
                break;
            case "DORMITORY":
                room = new DormitoryRoom(0, number, block, floor, baseFee, "AVAILABLE",
                        readInt("Number of lockers: "));
                break;
            default:
                System.out.println("Unknown room type.");
                return;
        }
        int id = hostelService.addRoom(room);
        System.out.println("Room added with ID: " + id);
    }

    // ---------- STUDENT MENU ----------
    private static void studentMenu() throws SQLException {
        System.out.println("\n-- Student Management --");
        System.out.println("1. Add Student  2. View All  3. Search  4. Update  5. Delete  0. Back");
        int c = readInt("Choice: ");
        switch (c) {
            case 1: addStudent(); break;
            case 2: hostelService.listStudents().forEach(System.out::println); break;
            case 3: {
                String kw = readString("Search keyword (name/roll/phone): ");
                hostelService.searchStudents(kw).forEach(System.out::println);
                break;
            }
            case 4: updateStudent(); break;
            case 5: {
                int id = readInt("Student ID to delete: ");
                System.out.println(hostelService.deleteStudent(id) ? "Deleted." : "Not found.");
                break;
            }
            default: break;
        }
    }

    private static void addStudent() throws SQLException {
        String name = readString("Name: ");
        String roll = readString("Roll Number: ");
        String course = readString("Course: ");
        int year = readInt("Year of study (1-5): ");
        String phone = readString("Phone (10 digits): ");
        String email = readString("Email: ");
        String guardianName = readString("Guardian Name: ");
        String guardianPhone = readString("Guardian Phone (10 digits): ");
        String address = readString("Address: ");
        String idType = readString("ID Proof Type (AADHAR/PASSPORT/VOTER_ID/DRIVING_LICENSE): ");
        String idNumber = readString("ID Proof Number: ");

        Student s = new Student(0, name, roll, course, year, phone, email,
                guardianName, guardianPhone, address, idType, idNumber, LocalDate.now());
        int id = hostelService.addStudent(s);
        System.out.println("Student added with ID: " + id);
    }

    private static void updateStudent() throws SQLException {
        int id = readInt("Student ID to update: ");
        Student existing = hostelService.getStudent(id);
        if (existing == null) {
            System.out.println("Student not found.");
            return;
        }
        System.out.println("Current: " + existing);
        existing.setPhone(readString("New Phone (10 digits): "));
        existing.setEmail(readString("New Email: "));
        existing.setAddress(readString("New Address: "));
        System.out.println(hostelService.updateStudent(existing) ? "Updated." : "Update failed.");
    }

    // ---------- ALLOCATION MENU ----------
    private static void allocationMenu() throws SQLException {
        System.out.println("\n-- Allocation Management --");
        System.out.println("1. Allocate Bed  2. Vacate Bed  3. View All  4. History by Student");
        System.out.println("5. By Date Range  0. Back");
        int c = readInt("Choice: ");
        switch (c) {
            case 1: allocateBed(); break;
            case 2: {
                int allocId = readInt("Allocation ID to vacate: ");
                boolean ok = allocationService.vacateBed(allocId, LocalDate.now());
                System.out.println(ok ? "Bed vacated." : "Allocation not found or already vacated.");
                break;
            }
            case 3: allocationService.listAllAllocations().forEach(System.out::println); break;
            case 4: {
                int sid = readInt("Student ID: ");
                allocationService.getAllocationHistory(sid).forEach(System.out::println);
                break;
            }
            case 5: {
                LocalDate start = readDate("Start date (YYYY-MM-DD): ");
                LocalDate end = readDate("End date (YYYY-MM-DD): ");
                allocationService.getAllocationsBetween(start, end).forEach(System.out::println);
                break;
            }
            default: break;
        }
    }

    private static void allocateBed() {
        try {
            int studentId = readInt("Student ID: ");
            int roomId = readInt("Room ID: ");
            int bedNumber = readInt("Bed Number: ");
            String academicYear = readString("Academic Year (e.g. 2026-2027): ");
            int allocId = allocationService.allocateBed(studentId, roomId, bedNumber,
                    LocalDate.now(), academicYear);
            System.out.println("Allocation successful. Allocation ID: " + allocId);
        } catch (InvalidAllocationException | RoomNotAvailableException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // ---------- MESS MENU ----------
    private static void messMenu() throws SQLException {
        System.out.println("\n-- Mess Menu & Orders --");
        System.out.println("1. View Full Menu  2. View by Category  3. Place Order  0. Back");
        int c = readInt("Choice: ");
        switch (c) {
            case 1: hostelService.listMessItems().forEach(System.out::println); break;
            case 2: {
                String cat = readString("Category (BREAKFAST/LUNCH/DINNER/SNACKS/BEVERAGES/DESSERTS): ").toUpperCase();
                hostelService.listMessItemsByCategory(cat).forEach(System.out::println);
                break;
            }
            case 3: placeMessOrder(); break;
            default: break;
        }
    }

    private static void placeMessOrder() throws SQLException {
        int allocationId = readInt("Allocation ID: ");
        MessOrder order = new MessOrder(0, allocationId, LocalDate.now());
        boolean adding = true;
        while (adding) {
            int itemId = readInt("Item ID to add (0 to finish): ");
            if (itemId == 0) { adding = false; break; }
            MessItem item = messDAO.findItemById(itemId);
            if (item == null) {
                System.out.println("Item not found.");
                continue;
            }
            int qty = readInt("Quantity: ");
            order.addItem(item, qty);
        }
        if (order.getOrderItems().isEmpty()) {
            System.out.println("No items added. Order cancelled.");
            return;
        }
        int orderId = messDAO.saveOrder(order);
        System.out.println("Order placed. Order ID: " + orderId +
                " | Total: Rs." + String.format("%.2f", order.calculateCharges()));
    }

    // ---------- BILLING MENU ----------
    private static void billingMenu() throws SQLException {
        System.out.println("\n-- Billing & Payments --");
        System.out.println("1. Generate Bill  2. Record Payment  0. Back");
        int c = readInt("Choice: ");
        switch (c) {
            case 1: {
                int allocId = readInt("Allocation ID: ");
                double discount = readDouble("Discount % (0 if none): ");
                System.out.println(billingService.generateBill(allocId, discount));
                break;
            }
            case 2: recordPayment(); break;
            default: break;
        }
    }

    private static void recordPayment() {
        try {
            int allocId = readInt("Allocation ID: ");
            double amount = readDouble("Amount (Rs.): ");
            String mode = readString("Mode (CASH/CARD/UPI/NETBANKING): ");
            billingService.processPayment(allocId, mode, amount);
        } catch (InvalidPaymentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // ---------- ANALYTICS MENU ----------
    private static void analyticsMenu() throws SQLException {
        System.out.println("\n-- Analytics & Reports --");
        System.out.println("1. Student Count by Course   2. Revenue by Room Type");
        System.out.println("3. Revenue by Payment Mode   4. Top-Selling Mess Items");
        System.out.println("5. Unique Room Types         6. Polymorphism Demo   0. Back");
        int c = readInt("Choice: ");
        switch (c) {
            case 1: hostelService.printStudentCountByCourse(); break;
            case 2: hostelService.printRevenueByRoomType(); break;
            case 3: billingService.printRevenueByMode(); break;
            case 4: {
                int limit = readInt("Top N items: ");
                hostelService.printTopSellingMessItems(limit);
                break;
            }
            case 5: {
                Set<String> types = hostelService.getUniqueRoomTypes();
                System.out.println("  Unique room types: " + types);
                break;
            }
            case 6: {
                List<Room> rooms = hostelService.listRooms();
                hostelService.printPolymorphismDemo(rooms);
                break;
            }
            default: break;
        }
    }

    // ================= INPUT HELPERS =================

    private static int readInt(String prompt) {
        System.out.print(prompt);
        while (!sc.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            sc.next();
        }
        int val = sc.nextInt();
        sc.nextLine();
        return val;
    }

    private static double readDouble(String prompt) {
        System.out.print(prompt);
        while (!sc.hasNextDouble()) {
            System.out.print("Please enter a valid number: ");
            sc.next();
        }
        double val = sc.nextDouble();
        sc.nextLine();
        return val;
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static boolean readBoolean(String prompt) {
        System.out.print(prompt);
        String v = sc.nextLine().trim().toLowerCase();
        return v.startsWith("y");
    }

    private static LocalDate readDate(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return LocalDate.parse(sc.nextLine().trim());
            } catch (Exception e) {
                System.out.print("Invalid date. Use YYYY-MM-DD: ");
            }
        }
    }
}
