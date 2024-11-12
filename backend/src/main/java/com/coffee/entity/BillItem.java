package com.coffee.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "bill_item")
public class BillItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    // Removed direct product reference, instead storing snapshot data
    @Column(name = "product_id", nullable = false)
    private Integer originalProductId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_description")
    private String productDescription;

    @Column(name = "product_category")
    private String productCategory;

    @Column(name = "original_price")
    private Integer originalPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price_at_time_of_order", nullable = false)
    private Integer price;

    // New column to store product images at time of order
    @ElementCollection
    @CollectionTable(name = "bill_item_product_images", joinColumns = @JoinColumn(name = "bill_item_id"))
    @Column(name = "image_path")
    private List<String> productImages = new ArrayList<>();

    // Method to capture product snapshot during order creation
    public static BillItem createFromProduct(Product product, Integer quantity, Integer priceAtTimeOfOrder) {
        BillItem billItem = new BillItem();
        billItem.setOriginalProductId(product.getId());
        billItem.setProductName(product.getName());
        billItem.setProductDescription(product.getDescription());
        billItem.setProductCategory(product.getCategory().getName()); // Assuming Category has a getName() method
        billItem.setOriginalPrice(product.getOriginalPrice());
        billItem.setQuantity(quantity);
        billItem.setPrice(priceAtTimeOfOrder);

        // Capture product images at time of order
        billItem.setProductImages(
                product.getImages().stream()
                        .map(ProductImage::getImagePath)
                        .collect(Collectors.toList())
        );

        return billItem;
    }
}