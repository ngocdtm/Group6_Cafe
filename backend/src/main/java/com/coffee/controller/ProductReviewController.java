package com.coffee.controller;

import com.coffee.service.ProductReviewService;
import com.coffee.wrapper.ProductRatingWrapper;
import com.coffee.wrapper.ProductReviewWrapper;
import com.coffee.wrapper.ProductWrapper;
import com.coffee.wrapper.ReviewWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@Slf4j
public class ProductReviewController {

    @Autowired
    private ProductReviewService reviewService;

    private final Path fileStorageLocation;

    public ProductReviewController(@Value("${app.file.review-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Operation(summary = "Create product review with images")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createReview(
            @ModelAttribute ReviewWrapper reviewDTO,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        return reviewService.createReview(reviewDTO, images);
    }

    @Operation(summary = "Get product reviews")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductReviewWrapper>> getProductReviews(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return reviewService.getProductReviews(productId, page, size);
    }

    @Operation(summary = "Get user reviews")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/user")
    public ResponseEntity<List<ProductReviewWrapper>> getUserReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return reviewService.getUserReviews(page, size);
    }

    @Operation(summary = "Get reviews by bill ID")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/bill/{billId}")
    public ResponseEntity<List<ProductReviewWrapper>> getReviewsByBillId(@PathVariable Integer billId) {
        return reviewService.getReviewsByBillId(billId);
    }

    @Operation(summary = "Check if product in bill is reviewed")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/check/{billId}/{productId}")
    public ResponseEntity<Boolean> isProductReviewed(
            @PathVariable Integer billId,
            @PathVariable Integer productId) {
        return reviewService.isProductReviewed(billId, productId);
    }

    @Operation(
            summary = "Get a product rating & comment by id",
            description = "Endpoint to get a product rating & comment by id."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/rating/{productId}")
    public ResponseEntity<ProductRatingWrapper> getProductRating(@PathVariable Integer productId) {
        try {
            return reviewService.getProductRating(productId);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ProductRatingWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Value("${app.file.review-dir}")
    private String uploadDir;

    @GetMapping("/images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                // Determine the media type of the file
                String contentType = determineContentType(filename);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            default:
                return "application/octet-stream";
        }
    }
}
