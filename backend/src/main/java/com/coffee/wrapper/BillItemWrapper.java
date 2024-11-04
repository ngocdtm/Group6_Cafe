package com.coffee.wrapper;

import com.coffee.entity.BillItem;
import java.util.List;

public class BillItemWrapper {

    private Integer id;
    private Integer originalProductId;
    private String productName;
    private String productDescription;
    private String productCategory;
    private Integer originalPrice;
    private Integer quantity;
    private Integer price;
    private List<String> productImages;

    // Constructors, getters, and setters


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOriginalProductId() {
        return originalProductId;
    }

    public void setOriginalProductId(Integer originalProductId) {
        this.originalProductId = originalProductId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public Integer getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Integer originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public List<String> getProductImages() {
        return productImages;
    }

    public void setProductImages(List<String> productImagesAtTimeOfOrder) {
        this.productImages = productImagesAtTimeOfOrder;
    }

    public static BillItemWrapper fromBillItem(BillItem item) {
        BillItemWrapper dto = new BillItemWrapper();
        dto.setId(item.getId());
        dto.setOriginalProductId(item.getOriginalProductId());
        dto.setProductName(item.getProductName());
        dto.setProductDescription(item.getProductDescription());
        dto.setProductCategory(item.getProductCategory());
        dto.setOriginalPrice(item.getOriginalPrice());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setProductImages(item.getProductImages());
        return dto;
    }
}