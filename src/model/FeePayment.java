package model;

import java.time.LocalDate;

/** Records a fee payment made against a hostel room allocation. */
public class FeePayment {

    public enum Mode { CASH, CARD, UPI, NETBANKING }

    private int paymentId;
    private int allocationId;
    private double amount;
    private Mode mode;
    private String transactionReference;
    private LocalDate paymentDate;

    public FeePayment(int paymentId, int allocationId, double amount, Mode mode,
                       String transactionReference, LocalDate paymentDate) {
        this.paymentId = paymentId;
        this.allocationId = allocationId;
        setAmount(amount);
        this.mode = mode;
        this.transactionReference = transactionReference;
        this.paymentDate = paymentDate;
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getAllocationId() { return allocationId; }
    public void setAllocationId(int allocationId) { this.allocationId = allocationId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        this.amount = amount;
    }

    public Mode getMode() { return mode; }
    public void setMode(Mode mode) { this.mode = mode; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    @Override
    public String toString() {
        return String.format("[Payment #%d] Allocation #%d - Rs.%.2f via %s (Ref: %s) on %s",
                paymentId, allocationId, amount, mode, transactionReference, paymentDate);
    }
}
