package org.beatengine.aibotdemo.repository;


import org.beatengine.aibotdemo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContaining(String name);

    List<Product> findByDescriptionContaining(String description);

    List<Product> findByNameContainingOrDescriptionContaining(String name, String description);
}
