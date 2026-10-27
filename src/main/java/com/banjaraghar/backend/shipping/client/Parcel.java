package com.banjaraghar.backend.shipping.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Parcel {

    private double weightKg;
    private double lengthCm;
    private double breadthCm;
    private double heightCm;
}
