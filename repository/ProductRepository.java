/*
 * Copyright © Progmasters (QTC Kft.), 2018.
 * All rights reserved. No part or the whole of this Teaching Material (TM) may be reproduced, copied, distributed,
 * publicly performed, disseminated to the public, adapted or transmitted in any form or by any means, including
 * photocopying, recording, or other electronic or mechanical methods, without the prior written permission of QTC Kft.
 * This TM may only be used for the purposes of teaching exclusively by QTC Kft. and studying exclusively by QTC Kft.’s
 * students and for no other purposes by any parties other than QTC Kft.
 * This TM shall be kept confidential and shall not be made public or made available or disclosed to any unauthorized person.
 * Any dispute or claim arising out of the breach of these provisions shall be governed by and construed in accordance with the laws of Hungary.
 */

package hu.progmasters.webshop.repository;

import hu.progmasters.webshop.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p " +
            "FROM Product p " +
            "WHERE p.name = :name " +
            "AND p.isDeleted = false " +
            "AND p.vendor = :vendor"
            )
    Product findByNameAndVendor(String name, String vendor);

    @Query("SELECT p " +
            "FROM Product p " +
            "WHERE p.productCategory.id = :productCategoryId " +
            "AND p.isDeleted = false")
    List<Product> findAllByProductCategoryId(Long productCategoryId);

    @Query("SELECT p " +
            "FROM Product p " +
            "WHERE lower(p.name) " +
            "LIKE lower(concat('%', :name, '%'))" +
            "AND p.isDeleted = false")
    List<Product> findAllByName(String name);

    @Query("SELECT p " +
            "FROM Product p " +
            "WHERE p.promotionStatus = 'ON_PROMOTION'" +
            "AND p.isDeleted = false")
    Optional<List<Product>> findAllByPromotion();

    @Query("SELECT p " +
            "FROM Product p " +
            "WHERE p.isDeleted = false")
    List<Product> findAllNonDeletedProducts();
}
