package com.internship.task1.product;

import jakarta.validation.constraints.NotNull;
import org.openapitools.api.ProductApi;
import org.openapitools.model.ProductDtoRead;
import org.openapitools.model.ProductDtoWrite;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@RestController
public class ProductController implements ProductApi {

    ProductService service;

    @Override
    public ResponseEntity<List<ProductDtoRead>> readAll(){
        return ResponseEntity.ok(service.readAll());
    }

    public ProductController(ProductService service) {
        this.service = service;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return ProductApi.super.getRequest();
    }

    @Override
    public ResponseEntity<ProductDtoRead> addProduct(ProductDtoWrite product) {
        ProductDtoRead response = service.save(product);
        return ResponseEntity.created(URI.create("/" + response.getId())).body(response);
    }

    @Override
    public ResponseEntity<Void> deleteProduct(Long productId) {
        return service.deleteProduct(productId);
    }

    @Override
    public ResponseEntity<List<ProductDtoRead>> findProductsByCategory(@NotNull String category) {
        return ResponseEntity.ok(service.findProductsByCategory(category));
    }

    @Override
    public ResponseEntity<ProductDtoRead> getProductById(Long productId) {
        return service.findProductById(productId);
    }

    @Override
    public ResponseEntity<Void> updateProduct(ProductDtoRead product) {
        return service.updateProduct(product);
    }

    @Override
    public ResponseEntity<Void> updateProductWithForm(Long productId, String name, Float price, String category, OffsetDateTime expiryDate) {
        return service.updateProduct(productId, name, price, category, expiryDate);
    }

}
