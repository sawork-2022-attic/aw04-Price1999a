package com.example.webpos.db;

import com.example.webpos.model.Cart;
import com.example.webpos.model.Product;
import com.example.webpos.web.PosController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository(value = "jd")
public class JD implements PosDB {


    private List<Product> products = null;

    private Log logger = LogFactory.getLog(JD.class);

    @Override
    public List<Product> getProducts(int pageNum) {
        try {
            if (products == null)
                products = parseJD("Java");
        } catch (IOException e) {
            logger.info(e);
            products = new ArrayList<>();
        }
        return products;
    }

    public double getTaxRate() {
        return 12;
    }

    @Override
    public double getDiscount() {
        return 0;
    }

    @Override
    public Product getProduct(String productId) {
        for (Product p : getProducts(0)) {
            if (p.getId().equals(productId)) {
                return p;
            }
        }
        return null;
    }

    @Cacheable(value = "jd")
    public List<Product> parseJD(String keyword) throws IOException {
        List<Product> list = new ArrayList<>();
        //获取请求https://search.jd.com/Search?keyword=java
        String url = "https://search.jd.com/Search?keyword=" + keyword;
        //解析网页
        Document document = Jsoup.parse(new URL(url), 10000);
        //所有js的方法都能用
        Element element = document.getElementById("J_goodsList");

        if (element == null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            list.add(new Product("13284888", "offline Java从入门到精通（第6版）（软件开发视频大讲堂） Java入门经典", 39.9, "https://img13.360buyimg.com/n1/s200x200_jfs/t1/186038/9/7947/120952/60bdd993E41eea7e2/48ab930455d7381b.jpg"));
            return list;
        }

        //获取所有li标签
        Elements elements = element.getElementsByTag("li");
//        System.out.println(element.html());
        int tmp = 1;
        //获取元素的内容
        for (Element el : elements
        ) {
            //关于图片特别多的网站，所有图片都是延迟加载的
            String id = el.attr("data-spu");
            String img = "https:".concat(el.getElementsByTag("img").eq(0).attr("data-lazy-img"));
            String price = el.getElementsByAttribute("data-price").text();
            String title = el.getElementsByClass("p-name").eq(0).text();
            if (title.indexOf("，") >= 0)
                title = title.substring(0, title.indexOf("，"));

            if (Objects.equals(id, "")) {
                continue;
                //id = String.valueOf(tmp++);
            }
            Product product = new Product(id, title, Double.parseDouble(price), img);
            //System.out.println(product);
            list.add(product);
        }
        return list;
    }

}
