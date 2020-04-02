package io.wanderingthinkter.models;

public class BillItem {
    private String itemName;
    private Integer qty;
    private Double price;

    public BillItem() {
    }

    public BillItem(String itemName, Integer qty, Double price) {
        this.itemName = itemName;
        this.qty = qty;
        this.price = price;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
