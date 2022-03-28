package com.example.webpos.model;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Component
@SessionScope
public class Cart implements Serializable {

    private List<Item> items = new ArrayList<>();

    public boolean addItem(Item item) {
        for (Item i : items) {
            if (Objects.equals(i.getProduct().getId(), item.getProduct().getId())) {
                if (i.getQuantity() + item.getQuantity() <= 0)
                    items.remove(i);
                else i.setQuantity(i.getQuantity() + item.getQuantity());
                return true;
            }
        }
        return items.add(item);
    }

    public double getTotal() {
        double total = 0;
        for (int i = 0; i < items.size(); i++) {
            total += items.get(i).getQuantity() * items.get(i).getProduct().getPrice();
        }
        return total;
    }

}
