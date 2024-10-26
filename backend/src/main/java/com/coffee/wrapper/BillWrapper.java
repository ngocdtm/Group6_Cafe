package com.coffee.wrapper;

import com.coffee.entity.Bill;
import com.coffee.enums.OrderStatus;
import com.coffee.enums.OrderType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BillWrapper {
    private Integer id;
    private String uuid;
    private String customerName;
    private String customerPhone;
    private String paymentMethod;
    private String shippingAddress;
    private Integer total;
    private Integer discount;
    private Integer totalAfterDiscount;
    private OrderStatus orderStatus;
    private OrderType orderType;
    private String orderDate;
    private String lastUpdatedDate;
    private String createdByUser;
    private List<BillItemWrapper> billItems;

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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
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

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(String lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(String createdByUser) {
        this.createdByUser = createdByUser;
    }

    public List<BillItemWrapper> getBillItems() {
        return billItems;
    }

    public void setBillItems(List<BillItemWrapper> billItems) {
        this.billItems = billItems;
    }

    // Constructors, getters, and setters

    public static BillWrapper fromBill(Bill bill) {
        BillWrapper dto = new BillWrapper();
        dto.setId(bill.getId());
        dto.setUuid(bill.getUuid());
        dto.setCustomerName(bill.getCustomerName());
        dto.setCustomerPhone(bill.getCustomerPhone());
        dto.setPaymentMethod(bill.getPaymentMethod());
        dto.setShippingAddress(bill.getShippingAddress());
        dto.setTotal(bill.getTotal());
        dto.setDiscount(bill.getDiscount());
        dto.setTotalAfterDiscount(bill.getTotalAfterDiscount());
        dto.setOrderStatus(bill.getOrderStatus());
        dto.setOrderType(bill.getOrderType());
        dto.setOrderDate(String.valueOf(bill.getOrderDate()));
        dto.setCreatedByUser(bill.getCreatedByUser());
        dto.setBillItems(bill.getBillItems().stream().map(BillItemWrapper::fromBillItem).collect(Collectors.toList()));
        return dto;
    }
}
