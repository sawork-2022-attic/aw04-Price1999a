package com.example.webpos.db;

import com.example.webpos.model.Cart;
import com.example.webpos.model.Product;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface PosDB {

    @Cacheable(cacheNames = "amazon_products", key = "#pageNum")
    public List<Product> getProducts(int pageNum);

    @Cacheable(cacheNames = "amazon_productFromID", key = "#productId")
    public Product getProduct(String productId);

    public double getTaxRate();

    public double getDiscount();

}
