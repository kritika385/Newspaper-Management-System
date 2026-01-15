package com.example._jan.billingboard;

public class BillingBean {
    private String mobile;
    private String dateTo;
    private float amount;
    private String status;

    public BillingBean(String mobile, String dateTo, float amount, String status) {
        this.mobile = mobile;
        this.dateTo = dateTo;
        this.amount = amount;
        this.status = status;
    }

    public String getMobile() { return mobile; }
    public String getDateTo() { return dateTo; }
    public float getAmount() { return amount; }
    public String getStatus() { return status; }
}