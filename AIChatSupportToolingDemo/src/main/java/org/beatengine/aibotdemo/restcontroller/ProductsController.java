package org.beatengine.aibotdemo.restcontroller;

import org.beatengine.aibotdemo.entity.Product;
import org.beatengine.aibotdemo.sharedservices.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST: <code>/api/products<code/>
 *
 */
@RestController
public class ProductsController {

    public final static String API_ROOT = "/api/products";

    @Autowired
    private ProductService serviceProducts;


    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping(API_ROOT)
    List<Product> all() {
        return serviceProducts.all();
    }
    // end::get-aggregate-root[]

    @PostMapping(API_ROOT)
    Product create(@RequestBody Product n) {
        return serviceProducts.create(n);
    }

    // Single item

    @GetMapping(API_ROOT+"/{id}")
    Product getById(@PathVariable Long id) {

        return serviceProducts.getById(id);
    }

    @PutMapping(API_ROOT+"/{id}")
    Product replaceById(@RequestBody Product n, @PathVariable Long id) {
        /* Save or replace */
        final Product product = serviceProducts.replaceById(n, id);
        return product;
    }

    @DeleteMapping(API_ROOT+"/{id}")
    void deleteById(@PathVariable Long id) {
        serviceProducts.deleteById(id);
    }

    @RestControllerAdvice
    static class EmployeeNotFoundAdvice {

        @ExceptionHandler(Product.ProductNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        String productNotFoundHandler(Product.ProductNotFoundException ex) {
            return ex.getMessage();
        }
    }

}

