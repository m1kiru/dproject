package com.m1kiru.aboutCredit;

import java.util.ArrayList;
import java.util.List;

class CreditCalculator {

    private boolean microTermInDays = true; // true = дни, false = месяцы для микрозайма
    public enum LoanType {CREDIT, MICROLOAN, MORTGAGE}

    public enum PaymentType {ANNUITY, DIFFERENTIATED}

    private double principal;
    private double annualInterestRate;
    private int termMonths;
    private LoanType loanType;
    private PaymentType paymentType;

    private double downPayment = 0;
    private double processingFee = 0;
    private double insuranceRate = 0;
    private double dailyInterestRate = 0;

    public CreditCalculator(double principal, double annualInterestRate, int termMonths,
                            LoanType loanType, PaymentType paymentType) {
        this.principal = principal;
        this.annualInterestRate = annualInterestRate;
        this.termMonths = termMonths;
        this.loanType = loanType;
        this.paymentType = paymentType;
    }

    public void setDownPayment(double downPayment) {
        this.downPayment = downPayment;
    }

    public void setProcessingFee(double processingFee) {
        this.processingFee = processingFee;
    }

    public void setInsuranceRate(double insuranceRate) {
        this.insuranceRate = insuranceRate;
    }

    public void setDailyInterestRate(double dailyInterestRate) {
        this.dailyInterestRate = dailyInterestRate;
    }

    public int getTermMonths() {
        return termMonths;
    }

    // проверка
    public boolean isValid() {
        double eff = getEffectivePrincipal();
        return eff > 0 && termMonths > 0 && termMonths <= 360 &&
                (annualInterestRate >= 0 || dailyInterestRate >= 0) &&
                downPayment >= 0 && downPayment < principal;
    }

    public double getEffectivePrincipal() {
        return principal - downPayment;
    }

    public double getMonthlyRate() {
        if (loanType == LoanType.MICROLOAN && dailyInterestRate > 0) {
            return dailyInterestRate * 30.44 / 100; // Конвертация дневной в месячную
        }
        return annualInterestRate / 100 / 12;
    }

    // рассчёт
    public double getMonthlyPayment() {
        double p = getEffectivePrincipal();
        double r = getMonthlyRate();
        int n = termMonths;

        if (p <= 0) return 0;
        if (r == 0) return p / n;

        double factor = Math.pow(1 + r, n);
        return (p * r * factor) / (factor - 1);
    }

    public double getTotalPayment() {
        return getMonthlyPayment() * termMonths + processingFee;
    }

    public double getTotalOverpayment() {
        double insuranceCost = getEffectivePrincipal() * (insuranceRate / 100) * (termMonths / 12.0);
        return getTotalPayment() - getEffectivePrincipal() + insuranceCost;
    }

    public double getEffectiveAnnualRate() {
        double total = getTotalPayment();
        double p = getEffectivePrincipal();
        if (p <= 0 || total <= p) return 0;
        return (Math.pow(total / p, 12.0 / termMonths) - 1) * 100;
    }

    // график...
    public static class PaymentInfo {
        public final int month;
        public final double payment;
        public final double principalPart;
        public final double interestPart;
        public final double remainingBalance;

        public PaymentInfo(int month, double payment, double principalPart, double interestPart, double remainingBalance) {
            this.month = month;
            this.payment = payment;
            this.principalPart = principalPart;
            this.interestPart = interestPart;
            this.remainingBalance = remainingBalance;
        }
    }

    public List<PaymentInfo> generateSchedule() {
        List<PaymentInfo> schedule = new ArrayList<>();
        double balance = getEffectivePrincipal();
        double r = getMonthlyRate();
        if (balance <= 0 || r <= 0) return schedule;

        double monthly = getMonthlyPayment();
        for (int i = 1; i <= termMonths; i++) {
            double interest = balance * r;
            double principalPart = monthly - interest;
            balance -= principalPart;
            schedule.add(new PaymentInfo(i, monthly, Math.max(0, principalPart), interest, Math.max(0, balance)));
        }
        return schedule;
    }
    public double getDailyPayment() {
        if (loanType != LoanType.MICROLOAN) {
            return 0;
        }

        double p = getEffectivePrincipal();
        double dailyRate = dailyInterestRate / 100;
        int days = termMonths; // микрозаймов дни

        if (p <= 0) return 0;
        if (dailyRate == 0) return p / days;

        // Формула аннуитета для дневных платежей
        double factor = Math.pow(1 + dailyRate, days);
        return (p * dailyRate * factor) / (factor - 1);
    }

    public double getTotalPaymentDaily() {
        if (loanType == LoanType.MICROLOAN) {
            return getDailyPayment() * termMonths + processingFee;
        }
        return getTotalPayment();
    }
}