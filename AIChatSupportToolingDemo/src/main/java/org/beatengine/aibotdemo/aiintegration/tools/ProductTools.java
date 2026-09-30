package org.beatengine.aibotdemo.aiintegration.tools;

import org.beatengine.aibotdemo.entity.Product;
import org.beatengine.aibotdemo.sharedservices.ProductService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The Annotation @JsonDeserialize(as = ProductRecord.class) causes to use only the data variables at serialisation.
 */
@Service
public class ProductTools {

    public static  <A, B> Collection<B> copyByCastConstructor(
            final Collection<A> collection,
            final Class<B> targetClass) {
        final Collection<B> outList = new ArrayList<>();
        try {
            for (A item : collection) {
                B newInstance = targetClass.getConstructor(item.getClass()).newInstance(item);
                outList.add(newInstance);
            }
        }
        catch (Exception e)
        {
            System.getLogger("copyByCastConstructor").log(System.Logger.Level.ERROR,
                    "copyByCastConstructor: Target class doesn't has a cast constructor "
                            + targetClass.getSimpleName() + "(A element) ");
        }
        return outList;
    }

    @Autowired
    ProductService serviceProduct;

    @Tool(description = "Search product by unique id.")
    public ProductRecord getProductById(final Long id)
    {
        if(serviceProduct == null)
        {
            System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.ERROR, "@Autowired" +
                    " ProductService serviceProduct is NULL!");
        }
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,
                "Tool getById: " + id);

        final Product result = serviceProduct.getById(id);
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,
                "Tool getById: " + result.getName());

        return new ProductRecord(result);
    }

    @Tool(description = "Search all products containing the given name.")
    public List<ProductRecord> getAllProductsByNameContains(final String name)
    {
        if(serviceProduct == null)
        {
            System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.ERROR, "@Autowired" +
                    " ProductService serviceProduct is NULL!");
        }
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,
                "Tool getAllByNameContains: " + name);
        final List<Product> result = serviceProduct.getAllByNameContains(name);

        final List<ProductRecord> productFields = new ArrayList<>(copyByCastConstructor(result, ProductRecord.class));

        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,
                "Tool getAllByNameContains: " + productFields.size() + " Results.");
        return productFields;
    }

    @Tool(description = "Search all products containing the given description.")
    public List<ProductRecord> getAllProductsByDescriptionContains(final String description)
    {
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "Tool getAllByDescriptionContains: " + description);
        final List<Product> result = serviceProduct.getAllByDescriptionContains(description);
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,
                "Tool getAllByDescriptionContains: " + result.size() + " Results.");
        return new ArrayList<>(copyByCastConstructor(result, ProductRecord.class));
    }

    @Tool(description = "Search all products containing the given name or the given description.")
    public List<ProductRecord> getAllProductsByNameOrDescriptionContains(final String name, final String description)
    {
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO, "Tool getAllByNameOrDescriptionContains: " + description + ", " + name);
        final List<Product> result = serviceProduct.getAllByNameOrDescriptionContains(name, description);
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,
                "Tool getAllProductsByNameOrDescriptionContains: " + result.size() + " Results.");
        return new ArrayList<>(copyByCastConstructor(result, ProductRecord.class));
    }

    /**
     *
     * <b>We need to pass the params flat because of problems with the object serialization (wrapped won't work)
     * {obj:{a,b,b}} fails and {a,b,c} would work with ProductRecord, but it fails very often. </b>
     * @return to AI with the result so AI will tell you about success.
     */
    @Tool(
            description = """
        Create or replace a Product.

        If id is null, create a new Product.
        If id is provided, replace/update the existing Product.
        Always respond with the id.
        """
    )
    public ProductRecord createProduct( final Long id, final String name, final String description, final Float basePrice) {
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,
                "Tool create: "+ String.valueOf(id).replace("null", "new ->") + ", " + name + ", " + description+ ", " + basePrice);
        final Product result = serviceProduct.create(new Product(id, name, description, basePrice));
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.INFO,
                "Tool created: " + result.getName());
        return new ProductRecord(result);
    }

    @Tool(
            description = """
        Remove a product by name and returns id.
        Always respond with the id.
        """
    )
    public Long removeProductByName(@ToolParam(description = "Product name not null.") final String name)
    {
        System.getLogger(this.getClass().getSimpleName()).log(System.Logger.Level.ERROR,
                "removeProductByName: Do not use this in production!");
        //todo ########### Do not use this in production deletes first found occurring product by name #################
        List<Product> allByNameContains = serviceProduct.getAllByNameContains(name);
        if(allByNameContains.isEmpty())
        {
            return null;
        }
        long firstId = allByNameContains.getFirst().getId();
        serviceProduct.deleteById(firstId);
        return firstId;
    }

    @Tool(
            description = """
        Remove a product by id and returns id.
        Only call this if your id comes from a previous tool call or directly from the user.
        Always respond with the id.
        """
    )
    public Long removeProductById(@ToolParam(description = "Product id not null.") final Long id)
    {
        serviceProduct.deleteById(id);
        return id;
    }
}
