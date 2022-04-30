package com.example.webpos.db.mysql;

import com.example.webpos.db.PosDB;
import com.example.webpos.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.xdevapi.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

@Repository(value = "amazon")
public class Amazon implements PosDB {
    private List<Product> products = null;

    private Log logger = LogFactory.getLog(Amazon.class);

    private static final String SELECT_PRODUCTS_LIMIT = "SELECT `title`, `asin`,`imageURLHighRes` , `price` FROM `products` LIMIT ?,100";

    private static final String SELECT_PRODUCT_ASIN = "SELECT `title`, `asin`,`imageURLHighRes` , `price` FROM `products` where `asin` = ? LIMIT 1";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Override
    public List<Product> getProducts(int pageNum) {
        //首先是只处理数据库中前k个的情况
        //需要缓存
        //int k = 0;
        logger.info(String.format("getProducts(%d)", pageNum));
        products = jdbcTemplate.query(SELECT_PRODUCTS_LIMIT, new ProductRowMapper(), new Object[]{pageNum * 100});
//        logger.info(products.size());
        products.removeAll(Collections.singleton(null));
        return products;
    }

    @Override
    public Product getProduct(String productId) {
        //先在内存中寻找，找不到再去数据库
        //允许缓存
        logger.info(String.format("getProduct(%s)", productId));
        if (products != null) {
            for (Product p : products) {
                if (p.getId().equals(productId)) {
                    return p;
                }
            }
        }
        //找不到就在mysql中找
        Product product;
        try {
            product = jdbcTemplate.queryForObject(SELECT_PRODUCT_ASIN, new ProductRowMapper(), new Object[]{productId});
        } catch (EmptyResultDataAccessException e) {
            logger.warn(String.format("product id: %s faild to find", productId));
            return null;
        }
        return product;
    }

    @Override
    public double getTaxRate() {
        return 12;
    }

    @Override
    public double getDiscount() {
        return 10;
    }
}
