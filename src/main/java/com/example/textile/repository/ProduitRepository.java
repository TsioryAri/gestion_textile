package com.example.textile.repository;

import com.example.textile.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
    boolean existsByReference(String reference);
}