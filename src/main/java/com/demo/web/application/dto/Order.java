package com.demo.web.application.dto;

import lombok.Data;

import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.List;

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
