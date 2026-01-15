package com.example._jan.billcalculation;

public class BillBean {
    private String mobile;
    private String dateTo;
    private float amount;

    public BillBean() {}

    public BillBean(String mobile, String dateTo, float amount) {
        this.mobile = mobile;
        this.dateTo = dateTo;
        this.amount = amount;
    }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getDateTo() { return dateTo; }
    public void setDateTo(String dateTo) { this.dateTo = dateTo; }

    public float getAmount() { return amount; }
    public void setAmount(float amount) { this.amount = amount; }
}