package org.beatengine.aibotdemo.sharedservices;

import org.beatengine.aibotdemo.entity.Product;
import org.beatengine.aibotdemo.repository.ProductRepository;
import org.beatengine.aibotdemo.websocket.RestWebsocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import org.beatengine.aibotdemo.restcontroller.ProductsController;

/**
 * <h5>This service is used for:</h5>
 *  <div> - regular application procedures over the Controller </div>
 *  <div> - AI-model-tools </div>
 */
@Service
public class ProductService {

    private final ProductRepository repository;

    ProductService(final ProductRepository repository) {
        this.repository = repository;
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    public List<Product> all() {
        return repository.findAll();
    }
    // end::get-aggregate-root[]


    public List<Product> getAllByNameContains(final String name)
    {
        return repository.findByNameContaining(name);
    }


    public List<Product> getAllByDescriptionContains(final String description)
    {
        return repository.findByDescriptionContaining(description);
    }


    public List<Product> getAllByNameOrDescriptionContains(final String name, final String description)
    {
        return repository.findByNameContainingOrDescriptionContaining(name, description);
    }


    public Product create(Product n) {
        final Product save = repository.save(n);

        //Tell the frontend over websocket to reload products
        RestWebsocketHandler.getInstance().triggerRestUpdateEvent(ProductsController.API_ROOT);

        return save;
    }

    // Single item

    public Product getById(final Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new Product.ProductNotFoundException(id));
    }

    public Product replaceById(final Product n,final Long id) {
        /* Save or replace */
        return repository.findById(id)
                .map(product -> {
                    product.setName(n.getName());
                    product.setDescription(n.getDescription());
                    product.setBasePrice(n.getBasePrice());
                    //Tell the frontend over websocket to reload products
                    RestWebsocketHandler.getInstance().triggerRestUpdateEvent(ProductsController.API_ROOT);

                    return repository.save(product);
                })
                .orElseGet(() -> {
                    //Tell the frontend over websocket to reload products
                    RestWebsocketHandler.getInstance().triggerRestUpdateEvent(ProductsController.API_ROOT);

                    return repository.save(n);
                });
    }

    public void deleteById(final Long id) {
        repository.deleteById(id);
        //Tell the frontend over websocket to reload products
        RestWebsocketHandler.getInstance().triggerRestUpdateEvent(ProductsController.API_ROOT);
    }

    public Long count()
    {
        return repository.count();
    }


}
