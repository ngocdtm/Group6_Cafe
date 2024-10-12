package com.coffee.controller;

import com.coffee.constants.CafeConstants;
import com.coffee.service.ProductService;
import com.coffee.utils.CafeUtils;
import com.coffee.wrapper.ProductWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    @Autowired
    ProductService productService;

    private final Path fileStorageLocation;

    public ProductController(@Value("${app.file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @GetMapping("/images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String saveImage(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    @Operation(
            summary = "Add a new product",
            description = "Endpoint to add a new product to the category."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/add")
    public ResponseEntity<String> addProduct(@RequestParam(value = "files", required = false) List<MultipartFile> files,
                                             @RequestParam("name") String name,
                                             @RequestParam("categoryId") Integer categoryId,
                                             @RequestParam("description") String description,
                                             @RequestParam("price") Integer price,
                                             @RequestParam("originalPrice") Integer originalPrice){
        try{
            return productService.addProduct(files, name, categoryId, description, price, originalPrice);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Get all product",
            description = "Endpoint to get all product."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/get")
    public ResponseEntity<List<ProductWrapper>> getAllProduct(){
        try{
            return productService.getAllProduct();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Update a product",
            description = "Endpoint to update a product including its images."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/update")
    public ResponseEntity<String> updateProduct(
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam("id") Integer id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = false) Integer price,
            @RequestParam(value = "originalPrice", required = false) Integer originalPrice,
            @RequestParam(value = "deletedImageIds", required = false) List<Integer> deletedImageIds) {
        try {
            return productService.updateProduct(files, id, name, categoryId, description, price, originalPrice, deletedImageIds);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Delete product image",
            description = "Endpoint to delete a specific product image."
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/deleteImage/{productId}/{imageId}")
    public ResponseEntity<String> deleteProductImage(@PathVariable Integer productId, @PathVariable Integer imageId){
        try{
            return productService.deleteProductImage(productId, imageId);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Delete a product",
            description = "Endpoint to delete a product."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Integer id){
        try{
            return productService.deleteProduct(id);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Update a product status",
            description = "Endpoint to update a product status."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/updateStatus")
    public ResponseEntity<String> updateStatus(@RequestBody Map<String,String> requestMap){
        try{
            return productService.updateStatus(requestMap);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Get a product by category",
            description = "Endpoint to get a product by category."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/getByCategory/{id}")
    public ResponseEntity<List<ProductWrapper>> getByCategory(@PathVariable Integer id){
        try{
            return productService.getByCategory(id);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(
            summary = "Get a product by id",
            description = "Endpoint to get a product by id."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/getById/{id}")
    public ResponseEntity<ProductWrapper> getById(@PathVariable Integer id){
        try{
            return productService.getById(id);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ProductWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}