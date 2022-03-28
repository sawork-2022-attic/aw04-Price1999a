package com.example.webpos.web;

import com.example.webpos.biz.PosService;
import com.example.webpos.model.Cart;
import com.example.webpos.model.Item;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.logging.Log;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class PosController {

    @Autowired
    private HttpSession session;

    private Log logger = LogFactory.getLog(PosController.class);

    private PosService posService;

    private Cart cart;

    @Autowired
    public void setCart(Cart cart) {
        this.cart = cart;
    }

    @Autowired
    public void setPosService(PosService posService) {
        this.posService = posService;
    }

    @GetMapping("/")
    public String pos(Model model) {
        getCart();
        model.addAttribute("taxRate", posService.getTaxRate());
        logger.info(posService.getTaxRate() + " " + posService.getDiscount());
        model.addAttribute("discount", posService.getDiscount());
        double total = cart.getTotal()
                * ((100 - posService.getDiscount()) / 100)
                * ((100 + posService.getTaxRate()) / 100);
        total = (double) Math.round(total * 100) / 100;
        model.addAttribute("total", total);
        logger.info(total);
        logger.info(posService.products().size());
        model.addAttribute("products", posService.products());
        model.addAttribute("cart", cart);
        return "index";
    }

    @GetMapping("/test")
    public String posTest(Model model) {
        getCart();
        if (cart.getItems().size() == 0) {
            posService.add(cart, "1", 1);
            session.setAttribute("cart", cart);
        }
        model.addAttribute("taxRate", posService.getTaxRate());
        logger.info(posService.getTaxRate() + " " + posService.getDiscount());
        model.addAttribute("discount", posService.getDiscount());
        model.addAttribute("total", cart.getTotal()
                * ((100 - posService.getDiscount()) / 100)
                * ((100 + posService.getTaxRate()) / 100));
        logger.info(cart.getTotal()
                * ((100 - posService.getDiscount()) / 100)
                * ((100 + posService.getTaxRate()) / 100));
        model.addAttribute("products", posService.products());
        model.addAttribute("cart", cart);
        return "index";
    }

    @GetMapping("/add")
    public String addByGet(@RequestParam(name = "pid") String pid, Model model) {
        getCart();
        posService.add(cart, pid, 1);
        session.setAttribute("cart", cart);
        return "redirect:/";
    }

    @GetMapping("/inc")
    public String incByGet(@RequestParam(name = "pid") String pid, Model model) {
        getCart();
        posService.add(cart, pid, 1);
        session.setAttribute("cart", cart);
        return "redirect:/";
    }

    @GetMapping("/dec")
    public String decByGet(@RequestParam(name = "pid") String pid, Model model) {
        getCart();
        posService.add(cart, pid, -1);
        session.setAttribute("cart", cart);
        return "redirect:/";
    }

    @GetMapping("/delete")
    public String deleteByGet(@RequestParam(name = "pid") String pid, Model model) {
        getCart();
        posService.add(cart, pid, Integer.MIN_VALUE);
        session.setAttribute("cart", cart);
        return "redirect:/";
    }

    @GetMapping("/cancel")
    public String cancelByGet(Model model) {
        getCart();
        posService.cancelCart(cart);
        session.setAttribute("cart", cart);
        return "redirect:/";
    }

    @GetMapping("/charge")
    public String chargeByGet(Model model) {
        getCart();
        posService.checkout(cart);
        session.setAttribute("cart", cart);
        return "redirect:/";
    }


    private void getCart() {
        logger.info(session.getId());
        Cart cart1 = (Cart) session.getAttribute("cart");
        setCart(cart1 == null ? new Cart() : cart1);
    }

    // clear all cache using cache manager
//    @GetMapping(value = "/clearCache")
//    public void clearCache() {
//        for (String name : cacheManager.getCacheNames()) {
//            cacheManager.getCache(name).clear();
//        }
//    }
}
