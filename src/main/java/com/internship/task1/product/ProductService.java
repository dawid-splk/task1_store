package com.internship.task1.product;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.openapitools.model.CategoryEnum;
import org.openapitools.model.ProductDtoRead;
import org.openapitools.model.ProductDtoWrite;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private ProductRepository repository;
    private ProductMapper mapper;
    private KafkaTemplate<String, Object> kafkaTemplate;

//    public ProductService() {
//    }

    public ProductService(ProductRepository repository, ProductMapper mapper, KafkaTemplate<String, Object> kafkaTemplate) {
        this.repository = repository;
        this.mapper = mapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "store_status")
    void listener(ConsumerRecord<String, Float> record) {
        long id = Long.parseLong(record.key());
        Product productToUpdate = repository.findProductById(id).orElseThrow(() -> new IllegalStateException());
        productToUpdate.setQuantity(record.value());     //TODO float w sygnaturze metody i brak castowania
        repository.save(productToUpdate);
    }


    ProductDtoRead save(ProductDtoWrite product) {
        Product productToAdd = mapper.fromDtoWriteToProduct(product, -1L, 0.0F);
        repository.save(productToAdd);

        kafkaTemplate.send("store_control", String.valueOf(productToAdd.getId()), null);

        return mapper.toDtoRead(productToAdd);
    }

    ResponseEntity<Void> deleteProduct(Long id) {
        Optional<Product> productOptional = repository.findProductById(id);
        if(productOptional.isPresent()){
            productOptional.ifPresent(repository::delete);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public List<ProductDtoRead> findProductsByCategory(String category) {
        return repository.findAllByCategory(CategoryEnum.fromValue(category))
                .stream().map(mapper::toDtoRead)
                .collect(Collectors.toList());
    }

    ResponseEntity<ProductDtoRead> findProductById(Long id) {
        Optional<Product> productOptional = repository.findProductById(id);
        if(productOptional.isPresent()){
            return ResponseEntity.ok(mapper.toDtoRead(productOptional.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    ResponseEntity<Void> updateProduct(Long productId, ProductDtoWrite productDtoWrite) {
        Optional<Product> productOptional = repository.findProductById(productId);

        if(productOptional.isPresent()){
            repository.save(mapper.fromDtoWriteToProduct(productDtoWrite, productOptional.get().getId(), productOptional.get().getQuantity()));
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    ResponseEntity<Void> updateProduct(Long productId, String name, Float price, String category, @RequestParam("expiryDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime expiryDate) {
        Optional<Product> productOptional = repository.findProductById(productId);

        if(productOptional.isPresent()){
            Product toUpdate = productOptional.get();
            if(name != null) {
                toUpdate.setName(name);
            }
            if(price != null) {
                toUpdate.setPrice(price);
            }
            if(category != null) {
                toUpdate.setCategory(CategoryEnum.fromValue(category));
            }
            if(expiryDate != null) {
                toUpdate.setExpiryDate(expiryDate.toLocalDateTime());
            }
            repository.save(toUpdate);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public List<ProductDtoRead> readAll() {
        return repository.findAll().stream().map(mapper::toDtoRead).collect(Collectors.toList());
    }
}
