package com.example.webpos.db.mysql;

import com.example.webpos.model.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductRowMapper implements RowMapper<Product> {
    @Autowired
    private ObjectMapper objectMapper;

    private Log logger = LogFactory.getLog(ProductRowMapper.class);

    //    SELECT `title`, `asin`,`imageURLHighRes` , `price`
    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
        if (objectMapper == null) objectMapper = new ObjectMapper();
        Product product = new Product();
        product.setId(rs.getString("asin"));
        product.setName(rs.getString("title"));
        String price = rs.getString("price");
        double dp = -1;
        if (Objects.equals(price, "")) dp = 12.34;
        else {
            price = price.substring(1, price.length());
            try {
                dp = Double.parseDouble(price);
            } catch (NumberFormatException n) {
                dp = 43.21;
            }
        }
        product.setPrice(dp);
        String[] list;
        String urls = rs.getString("imageURLHighRes");
        //logger.info(urls);
        try {
            list = objectMapper.readValue(urls, String[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        if (list.length == 0) return null;//没图就这样处理
//        logger.info(list[0]);
        product.setImage(list[0]);
        return product;
    }
}
