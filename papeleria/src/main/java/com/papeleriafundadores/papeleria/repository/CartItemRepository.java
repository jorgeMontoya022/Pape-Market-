package com.papeleriafundadores.papeleria.repository;

import com.papeleriafundadores.papeleria.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    Optional<CartItem> findItem(@Param("cartId") Long cartId, @Param("productId") long productId);

    @Modifying //Obligatorio para consultas que eliminan o actualizan datos con @Query
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void clearCart(@Param("cartId") Long cartId);

}
