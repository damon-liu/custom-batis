package com.lhx.ssde.pojo;


import lombok.Builder;
import lombok.Data;

@Data
public class User {
    private int id;
    private String name;
    private Integer age;
    private String address;
}
