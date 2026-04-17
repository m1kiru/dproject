package com.m1kiru.aboutCredit;

public class CalculationRecord {
    public long timestamp;
    public String type;          // 🏦 КРЕДИТ / 💸 МИКРОЗАЙМ / 🏠 ИПОТЕКА
    public double amount;
    public double monthlyPayment;
    public double overpayment;
    public double apr;
    public String termInfo;      // "24 мес." или "30 дн."

    public CalculationRecord(long timestamp, String type, double amount,
                             double monthlyPayment, double overpayment,
                             double apr, String termInfo) {
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
        this.monthlyPayment = monthlyPayment;
        this.overpayment = overpayment;
        this.apr = apr;
        this.termInfo = termInfo;
    }
}