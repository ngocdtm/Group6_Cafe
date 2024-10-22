package com.coffee.wrapper;

import com.coffee.entity.Bill;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BillWrapper {
    private Integer id;
    private String uuid;
    private String name;
    private String email;
    private String phoneNumber;
    private String paymentMethod;
    private Integer total;
    private Integer discount;
    private Integer totalAfterDiscount;
    private String status;
    private LocalDateTime orderDate;
    private List<BillItemWrapper> items;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Integer getTotalAfterDiscount() {
        return totalAfterDiscount;
    }

    public void setTotalAfterDiscount(Integer totalAfterDiscount) {
        this.totalAfterDiscount = totalAfterDiscount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public List<BillItemWrapper> getItems() {
        return items;
    }

    public void setItems(List<BillItemWrapper> items) {
        this.items = items;
    }

    // Constructors, getters, and setters

    public static BillWrapper fromBill(Bill bill) {
        BillWrapper dto = new BillWrapper();
        dto.setId(bill.getId());
        dto.setUuid(bill.getUuid());
        dto.setName(bill.getName());
        dto.setEmail(bill.getEmail());
        dto.setPhoneNumber(bill.getPhoneNumber());
        dto.setPaymentMethod(bill.getPaymentMethod());
        dto.setTotal(bill.getTotal());
        dto.setDiscount(bill.getDiscount());
        dto.setTotalAfterDiscount(bill.getTotalAfterDiscount());
        dto.setStatus(bill.getStatus());
        dto.setOrderDate(bill.getOrderDate());
        dto.setItems(bill.getBillItems().stream().map(BillItemWrapper::fromBillItem).collect(Collectors.toList()));
        return dto;
    }
}
