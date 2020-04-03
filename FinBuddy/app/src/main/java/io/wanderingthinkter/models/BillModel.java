package io.wanderingthinkter.models;

import com.google.firebase.Timestamp;

import java.util.List;

public class BillModel {
    private String id;
    private String billName;
    private Timestamp billDate;
    private Double totalBill;
    private Integer itemCount;
    private String userId;
    private List<BillItem> items;

    public BillModel() {
    }

    public BillModel(String billName, Timestamp billDate, Double totalBill, Integer itemCount, String userId, List<BillItem> items) {
        this.billName = billName;
        this.billDate = billDate;
        this.totalBill = totalBill;
        this.itemCount = itemCount;
        this.userId = userId;
        this.items = items;
        this.id = String.valueOf(System.currentTimeMillis());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBillName() {
        return billName;
    }

    public void setBillName(String billName) {
        this.billName = billName;
    }

    public Timestamp getBillDate() {
        return billDate;
    }

    public void setBillDate(Timestamp billDate) {
        this.billDate = billDate;
    }

    public Double getTotalBill() {
        return totalBill;
    }

    public void setTotalBill(Double totalBill) {
        this.totalBill = totalBill;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<BillItem> getItems() {
        return items;
    }

    public void setItems(List<BillItem> items) {
        this.items = items;
    }
}
