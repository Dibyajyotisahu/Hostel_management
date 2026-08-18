package service;

import dao.AllocationDAO;
import dao.RoomDAO;
import dao.StudentDAO;
import exception.InvalidAllocationException;
import exception.RoomNotAvailableException;
import model.Allocation;
import model.Room;
import model.Student;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Business logic layer for allocating and vacating hostel beds.
 * Delegates persistence to AllocationDAO, but enforces domain rules here.
 */
public class AllocationService {

    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    /**
     * Allocates a bed to a student after validating the student and room exist,
     * the room has a free bed, and the chosen bed number isn't already taken.
     */
    public int allocateBed(int studentId, int roomId, int bedNumber, LocalDate allocationDate,
                            String academicYear) throws InvalidAllocationException, RoomNotAvailableException, SQLException {

        if (allocationDate == null) {
            throw new InvalidAllocationException("Allocation date cannot be null.");
        }

        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new InvalidAllocationException("Student ID " + studentId + " does not exist.");
        }

        Room room = roomDAO.findById(roomId);
        if (room == null) {
            throw new InvalidAllocationException("Room ID " + roomId + " does not exist.");
        }

        if (bedNumber < 1 || bedNumber > room.getCapacity()) {
            throw new InvalidAllocationException("Bed number must be between 1 and " + room.getCapacity() + ".");
        }

        if ("MAINTENANCE".equals(room.getStatus())) {
            throw new RoomNotAvailableException("Room " + room.getRoomNumber() + " is under maintenance.");
        }

        if (!room.hasFreeBed()) {
            throw new RoomNotAvailableException("Room " + room.getRoomNumber() + " has no free beds.");
        }

        if (allocationDAO.isBedOccupied(roomId, bedNumber)) {
            throw new RoomNotAvailableException(
                    "Bed " + bedNumber + " in Room " + room.getRoomNumber() + " is already occupied.");
        }

        return allocationDAO.allocate(studentId, roomId, bedNumber, allocationDate, academicYear);
    }

    public boolean vacateBed(int allocationId, LocalDate vacateDate) throws SQLException {
        return allocationDAO.vacate(allocationId, vacateDate);
    }

    public Allocation getAllocation(int allocationId) throws SQLException {
        return allocationDAO.findById(allocationId);
    }

    public List<Allocation> listAllAllocations() throws SQLException {
        return allocationDAO.findAll();
    }

    public List<Allocation> getAllocationHistory(int studentId) throws SQLException {
        return allocationDAO.findByStudent(studentId);
    }

    public List<Allocation> getAllocationsBetween(LocalDate start, LocalDate end) throws SQLException {
        return allocationDAO.findByDateRange(start, end);
    }

    public List<Allocation> searchAllocations(String keyword) throws SQLException {
        return allocationDAO.searchByKeyword(keyword);
    }
}
