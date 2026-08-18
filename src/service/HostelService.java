package service;

import dao.MessDAO;
import dao.RoomDAO;
import dao.StudentDAO;
import model.MessItem;
import model.Room;
import model.Student;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Facade providing a simplified API over StudentDAO, RoomDAO and MessDAO
 * for straightforward CRUD/lookups. Business rules for allocation and
 * billing live in their own dedicated services.
 */
public class HostelService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final MessDAO messDAO = new MessDAO();

    // ----- Student management -----
    public int addStudent(Student s) throws SQLException { return studentDAO.insert(s); }
    public boolean updateStudent(Student s) throws SQLException { return studentDAO.update(s); }
    public boolean deleteStudent(int studentId) throws SQLException { return studentDAO.delete(studentId); }
    public Student getStudent(int studentId) throws SQLException { return studentDAO.findById(studentId); }
    public List<Student> listStudents() throws SQLException { return studentDAO.findAll(); }
    public List<Student> searchStudents(String keyword) throws SQLException { return studentDAO.searchByKeyword(keyword); }
    public void printStudentCountByCourse() throws SQLException { studentDAO.printStudentCountByCourse(); }

    // ----- Room management -----
    public int addRoom(Room r) throws SQLException { return roomDAO.insert(r); }
    public boolean deleteRoom(int roomId) throws SQLException { return roomDAO.delete(roomId); }
    public Room getRoom(int roomId) throws SQLException { return roomDAO.findById(roomId); }
    public List<Room> listRooms() throws SQLException { return roomDAO.findAll(); }
    public List<Room> listRoomsByType(String type) throws SQLException { return roomDAO.findByType(type); }
    public List<Room> listRoomsWithFreeBeds() throws SQLException { return roomDAO.findRoomsWithFreeBeds(); }
    public List<Room> listRoomsByFeeRange(double min, double max) throws SQLException { return roomDAO.findByFeeRange(min, max); }
    public List<Room> searchRooms(String keyword) throws SQLException { return roomDAO.searchByKeyword(keyword); }
    public Set<String> getUniqueRoomTypes() throws SQLException { return roomDAO.getUniqueRoomTypes(); }
    public void printRevenueByRoomType() throws SQLException { roomDAO.printRevenueByRoomType(); }

    // ----- Mess menu -----
    public List<MessItem> listMessItems() throws SQLException { return messDAO.findAllItems(); }
    public List<MessItem> listMessItemsByCategory(String category) throws SQLException { return messDAO.findItemsByCategory(category); }
    public void printTopSellingMessItems(int limit) throws SQLException { messDAO.printTopSellingItems(limit); }

    /** Demonstrates runtime polymorphism: same calculateCharges() call, different result per room subtype. */
    public void printPolymorphismDemo(List<Room> rooms) {
        System.out.println("  -- Runtime Polymorphism Demo --");
        System.out.println("  Same method call -> different results per room type:\n");
        for (Room r : rooms) {
            System.out.printf("  %-10s -> calculateCharges() = Rs.%.2f/month%n",
                    r.getRoomTypeName(), r.calculateCharges());
        }
    }
}
