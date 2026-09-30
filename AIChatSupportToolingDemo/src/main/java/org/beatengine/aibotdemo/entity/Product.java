package org.beatengine.aibotdemo.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.beatengine.aibotdemo.aiintegration.tools.ProductRecord;

import java.util.Objects;

@Entity
@JsonPropertyOrder({ "id", "name", "description", "basePrice" })
public class Product implements ProductFields{

    private @Id
    @GeneratedValue Long id;

    private String name;
    private String description;
    private float basePrice;

    public Product()
    {

    }

    /**
     * Cast constructor
     * @param productFields The variables of Product
     */
    public Product(ProductRecord productFields)
    {
        this.id = productFields.getId();
        this.name = productFields.getName();
        this.description = productFields.getDescription();
        this.basePrice = productFields.getBasePrice();

    }

    public Product(final String name, final String description, final float basePrice)
    {
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
    }

    public Product(final Long id, final String name, final String description, final float basePrice)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(float basePrice) {
        this.basePrice = basePrice;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Product product)) return false;
        return Float.compare(basePrice, product.basePrice) == 0
                && Objects.equals(id, product.id)
                && Objects.equals(name, product.name)
                && Objects.equals(description, product.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, basePrice);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", basePrice=" + basePrice +
                '}';
    }

    /**
     * The not found by id case.
     * */
    public static class ProductNotFoundException extends RuntimeException {

        public ProductNotFoundException(Long id) {
            super("Could not find product " + id);
        }
    }
}
