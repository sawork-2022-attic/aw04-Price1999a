package com.example.webpos.biz;

import com.example.webpos.db.JD;
import com.example.webpos.db.PosDB;
import com.example.webpos.model.Cart;
import com.example.webpos.model.Item;
import com.example.webpos.model.Product;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class PosServiceImp implements PosService, Serializable {

    private PosDB posDB;
    private Log logger = LogFactory.getLog(PosServiceImp.class);
    private int lastPageNum = 0;

    @Autowired
    @Resource(name = "amazon")
    public void setPosDB(PosDB posDB) {
        this.posDB = posDB;
    }


    @Override
    public Product randomProduct() {
        return products(lastPageNum).get(ThreadLocalRandom.current().nextInt(0, products(lastPageNum).size()));
    }

    @Override
    public void checkout(Cart cart) {
//        double total = cart.getTotal()
//                * ((100 - getDiscount()) / 100)
//                * ((100 + getTaxRate()) / 100);
//        total = (double) Math.round(total * 100) / 100;
        logger.info(String.format("checkout--total: %f", getTotal(cart)));
        cart.getItems().clear();
    }

    @Override
    public double getTotal(Cart cart) {
        double total = cart.getTotal()
                * ((100 - getDiscount()) / 100)
                * ((100 + getTaxRate()) / 100);
        total = (double) Math.round(total * 100) / 100;
        return total;
    }

    public void cancelCart(Cart cart) {
        cart.getItems().clear();
    }

    @Override
    public Cart add(Cart cart, Product product, int amount) {
        return add(cart, product.getId(), amount);
    }

    @Override
    public Cart add(Cart cart, String productId, int amount) {

        Product product = posDB.getProduct(productId);
        if (product == null) return cart;

        cart.addItem(new Item(product, amount));
        return cart;
    }

    @Override
    //@Cacheable(value = "products")
    public List<Product> products(int pageNum) {
        lastPageNum = pageNum;
        return posDB.getProducts(pageNum);
    }

    @Override
    public double getTaxRate() {
        return posDB.getTaxRate();
    }

    @Override
    public double getDiscount() {
        return posDB.getDiscount();
    }
}
