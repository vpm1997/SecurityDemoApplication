package com.demo.web.application.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class Order {

    @ManyToOne
    private User user;
    private String name;
    private String street;
    private String city;
    private String state;
    private String zip;
    private String ccNumber;
    private String ccExpiration;
    private String ccCVV;
    private List<Taco> tacos = new ArrayList<>();

    public void addTaco(Taco taco){
        this.tacos.add(taco);
    }





}
