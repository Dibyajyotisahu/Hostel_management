package model;

import java.time.LocalDate;

/**
 * Represents allotment of a Student to a bed in a Room for an academic period.
 * Aggregation: Allocation references Student and Room which exist independently.
 */
public class Allocation {

    public static final String ACTIVE = "ACTIVE";
    public static final String VACATED = "VACATED";
    public static final String CANCELLED = "CANCELLED";

    private int allocationId;
    private Student student;
    private Room room;
    private int bedNumber;
    private LocalDate allocationDate;
    private LocalDate vacateDate;   // null while active
    private String academicYear;    // e.g. "2026-2027"
    private String status;

    public Allocation(int allocationId, Student student, Room room, int bedNumber,
                       LocalDate allocationDate, LocalDate vacateDate, String academicYear, String status) {
        this.allocationId = allocationId;
        this.student = student;
        this.room = room;
        this.bedNumber = bedNumber;
        this.allocationDate = allocationDate;
        this.vacateDate = vacateDate;
        this.academicYear = academicYear;
        this.status = status;
    }

    public long monthsStayed(LocalDate asOf) {
        LocalDate end = (vacateDate != null) ? vacateDate : asOf;
        long months = java.time.temporal.ChronoUnit.MONTHS.between(allocationDate, end);
        return Math.max(months, 1); // minimum 1 month billed
    }

    // ----- Getters / Setters -----
    public int getAllocationId() { return allocationId; }
    public void setAllocationId(int allocationId) { this.allocationId = allocationId; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public int getBedNumber() { return bedNumber; }
    public void setBedNumber(int bedNumber) { this.bedNumber = bedNumber; }

    public LocalDate getAllocationDate() { return allocationDate; }
    public void setAllocationDate(LocalDate allocationDate) { this.allocationDate = allocationDate; }

    public LocalDate getVacateDate() { return vacateDate; }
    public void setVacateDate(LocalDate vacateDate) { this.vacateDate = vacateDate; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("[Allocation #%d] %s -> Room %s Bed %d | %s to %s | %s",
                allocationId, student.getName(), room.getRoomNumber(), bedNumber,
                allocationDate, (vacateDate == null ? "present" : vacateDate), status);
    }
}
