package com.damon.jdbc.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private int id;
    private String name;
    private Integer age;
    private String address;
}
