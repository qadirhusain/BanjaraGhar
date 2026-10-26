package com.banjaraghar.backend.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderAddressResponse {

    private String name;
    private String phone;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String pincode;
}