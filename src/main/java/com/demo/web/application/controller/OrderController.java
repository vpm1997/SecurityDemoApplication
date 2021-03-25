package com.demo.web.application.controller;

import com.demo.web.application.dto.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@Slf4j
@RequestMapping("/orders")
@SessionAttributes("order")
public class OrderController {


    @GetMapping("/current")
    public String orderForm(Model model, @ModelAttribute Order order) {
        model.addAttribute("order", order);
        return "orderForm";
    }

    @PostMapping
    public String processOrder(Order order, SessionStatus sessionStatus){
       log.info("Order : {}",order);
       sessionStatus.setComplete();
        return "redirect:/";
    }

}
