package com.coffee.wrapper;

import com.coffee.entity.BillItem;

import java.util.List;
import java.util.stream.Collectors;

public class BillItemWrapper {

    private Integer id;
    private Integer productId;  // Thêm productId
    private String productName;
    private Integer quantity;
    private Integer price;
    private List<ProductImageWrapper> images; // Thêm images


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public List<ProductImageWrapper> getImages() {
        return images;
    }

    public void setImages(List<ProductImageWrapper> images) {
        this.images = images;
    }

    // Constructors, getters, and setters

    public static BillItemWrapper fromBillItem(BillItem item) {
        BillItemWrapper dto = new BillItemWrapper();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());

        // Map images
        if (item.getProduct().getImages() != null) {
            dto.setImages(item.getProduct().getImages().stream()
                    .map(image -> new ProductImageWrapper(image.getId(), image.getImagePath()))
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
