package model;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Encapsulated Student entity. All fields are private with validating setters.
 */
public class Student {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private int studentId;
    private String name;
    private String rollNumber;
    private String course;
    private int year;             // year of study: 1-5
    private String phone;
    private String email;
    private String guardianName;
    private String guardianPhone;
    private String address;
    private String idProofType;   // AADHAR, PASSPORT, VOTER_ID, DRIVING_LICENSE
    private String idProofNumber;
    private LocalDate admissionDate;

    public Student(int studentId, String name, String rollNumber, String course, int year,
                    String phone, String email, String guardianName, String guardianPhone,
                    String address, String idProofType, String idProofNumber, LocalDate admissionDate) {
        this.studentId = studentId;
        setName(name);
        this.rollNumber = rollNumber;
        this.course = course;
        setYear(year);
        setPhone(phone);
        setEmail(email);
        this.guardianName = guardianName;
        setGuardianPhone(guardianPhone);
        this.address = address;
        this.idProofType = idProofType;
        this.idProofNumber = idProofNumber;
        this.admissionDate = admissionDate;
    }

    // ----- Getters / Setters with validation (Encapsulation) -----
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Student name cannot be empty.");
        this.name = name;
    }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public int getYear() { return year; }
    public void setYear(int year) {
        if (year < 1 || year > 5)
            throw new IllegalArgumentException("Year of study must be between 1 and 5.");
        this.year = year;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) {
        if (phone == null || !phone.matches("\\d{10}"))
            throw new IllegalArgumentException("Phone must be a 10-digit number.");
        this.phone = phone;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches())
            throw new IllegalArgumentException("Invalid email format.");
        this.email = email;
    }

    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }

    public String getGuardianPhone() { return guardianPhone; }
    public void setGuardianPhone(String guardianPhone) {
        if (guardianPhone == null || !guardianPhone.matches("\\d{10}"))
            throw new IllegalArgumentException("Guardian phone must be a 10-digit number.");
        this.guardianPhone = guardianPhone;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getIdProofType() { return idProofType; }
    public void setIdProofType(String idProofType) { this.idProofType = idProofType; }

    public String getIdProofNumber() { return idProofNumber; }
    public void setIdProofNumber(String idProofNumber) { this.idProofNumber = idProofNumber; }

    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }

    @Override
    public String toString() {
        return String.format("[%d] %s (Roll: %s) - %s Year %d | Ph: %s | %s: %s",
                studentId, name, rollNumber, course, year, phone, idProofType, idProofNumber);
    }
}
