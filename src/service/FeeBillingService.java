package service;

import dao.AllocationDAO;
import dao.MessDAO;
import dao.PaymentDAO;
import exception.InvalidPaymentException;
import interfaces.Payable;
import model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Business logic for hostel fee billing and payment processing.
 * Implements Payable to standardize how payments are handled across the app.
 */
public class FeeBillingService implements Payable {

    private static final double MAINTENANCE_CHARGE_PERCENT = 5.0; // flat hostel maintenance levy

    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final MessDAO messDAO = new MessDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    /** Generates a complete, itemized fee receipt for the given allocation. */
    public String generateBill(int allocationId, double discountPercent) throws SQLException {
        Allocation allocation = allocationDAO.findById(allocationId);
        if (allocation == null) return "Allocation not found.";

        Student student = allocation.getStudent();
        Room room = allocation.getRoom();

        long months = allocation.monthsStayed(LocalDate.now());
        double roomCharges = room.calculateCharges() * months;

        List<MessOrder> messOrders = messDAO.findOrdersByAllocation(allocationId);
        double messCharges = 0;
        StringBuilder messSection = new StringBuilder();
        for (MessOrder order : messOrders) {
            messSection.append(order.generateBillSummary());
            messCharges += order.calculateCharges();
        }

        double grossTotal = roomCharges + messCharges;
        double maintenanceCharge = grossTotal * (MAINTENANCE_CHARGE_PERCENT / 100.0);
        double discount = grossTotal * (discountPercent / 100.0);
        double totalDue = grossTotal + maintenanceCharge - discount;
        double alreadyPaid = paymentDAO.getTotalPaid(allocationId);
        double balance = totalDue - alreadyPaid;

        StringBuilder bill = new StringBuilder();
        bill.append("=====================================================================\n");
        bill.append("               GRAND HORIZON STUDENT HOSTEL\n");
        bill.append("         123 University Road, Bhubaneswar - 751001\n");
        bill.append("              Tel: +91-674-123-4567\n");
        bill.append("=====================================================================\n");
        bill.append("                    HOSTEL FEE RECEIPT\n");
        bill.append("=====================================================================\n");
        bill.append(String.format("  Allocation ID : #%d%n", allocationId));
        bill.append(String.format("  Student Name  : %s%n", student.getName()));
        bill.append(String.format("  Roll Number   : %s%n", student.getRollNumber()));
        bill.append(String.format("  Course/Year   : %s, Year %d%n", student.getCourse(), student.getYear()));
        bill.append(String.format("  ID Proof      : %s - %s%n", student.getIdProofType(), student.getIdProofNumber()));
        bill.append("---------------------------------------------------------------------\n");
        bill.append("  ROOM DETAILS\n");
        bill.append(String.format("  %s Room %s (%s) - Rs.%.2f/month%n",
                room.getRoomTypeName(), room.getRoomNumber(), room.getBlock(), room.calculateCharges()));
        bill.append(String.format("  Allocated On  : %s%n", allocation.getAllocationDate()));
        bill.append(String.format("  Bed Number    : %d%n", allocation.getBedNumber()));
        bill.append(String.format("  Months Billed : %d%n", months));
        bill.append(String.format("  Features      : %s%n", room.getSpecialFeatures()));
        bill.append("---------------------------------------------------------------------\n");
        bill.append("  ROOM CHARGES\n");
        bill.append(String.format("  %s Room @ Rs.%.2f x %d months        Rs.%,.2f%n",
                room.getRoomTypeName(), room.calculateCharges(), months, roomCharges));
        bill.append("---------------------------------------------------------------------\n");
        if (!messOrders.isEmpty()) {
            bill.append("  MESS / EXTRA ORDER CHARGES\n");
            bill.append(messSection);
            bill.append("---------------------------------------------------------------------\n");
        }
        bill.append(String.format("  Room Charges                              Rs.%,.2f%n", roomCharges));
        bill.append(String.format("  Mess Charges                              Rs.%,.2f%n", messCharges));
        bill.append(String.format("  Gross Total                               Rs.%,.2f%n", grossTotal));
        bill.append(String.format("  Maintenance Charge (%.0f%%)                 Rs.%,.2f%n",
                MAINTENANCE_CHARGE_PERCENT, maintenanceCharge));
        bill.append(String.format("  Discount (%.1f%%)                          Rs.%,.2f%n", discountPercent, discount));
        bill.append("=====================================================================\n");
        bill.append(String.format("  TOTAL AMOUNT DUE                          Rs.%,.2f%n", totalDue));
        bill.append(String.format("  Already Paid                              Rs.%,.2f%n", alreadyPaid));
        bill.append(String.format("  BALANCE                                   Rs.%,.2f%n", balance));
        bill.append("=====================================================================\n");
        bill.append("        Thank you for staying at Grand Horizon Hostel!\n");
        bill.append("=====================================================================\n");
        return bill.toString();
    }

    @Override
    public void processPayment(int allocationId, String mode, double amount)
            throws InvalidPaymentException, SQLException {
        if (amount <= 0) {
            throw new InvalidPaymentException("Payment amount must be positive.");
        }
        FeePayment.Mode paymentMode;
        try {
            paymentMode = FeePayment.Mode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidPaymentException("Invalid payment mode: " + mode +
                    ". Valid modes: CASH, CARD, UPI, NETBANKING.");
        }

        String txnRef = "HMS-" + allocationId + "-" + (10000 + (int) (Math.random() * 89999));
        FeePayment payment = new FeePayment(0, allocationId, amount, paymentMode, txnRef, LocalDate.now());
        paymentDAO.savePayment(payment);
        System.out.println("Payment recorded successfully. Transaction Ref: " + txnRef);
    }

    @Override
    public boolean isFullyPaid(int allocationId) throws SQLException {
        // Simplified check: compares paid amount to a freshly generated bill without discount.
        double paid = paymentDAO.getTotalPaid(allocationId);
        Allocation allocation = allocationDAO.findById(allocationId);
        if (allocation == null) return false;
        long months = allocation.monthsStayed(LocalDate.now());
        double dueEstimate = allocation.getRoom().calculateCharges() * months * 1.05; // + maintenance
        return paid >= dueEstimate;
    }

    public void printRevenueByMode() throws SQLException { paymentDAO.printRevenueByMode(); }
    public double getRevenueBetween(LocalDate start, LocalDate end) throws SQLException {
        return paymentDAO.getRevenueBetween(start, end);
    }
}
