package com.banjaraghar.backend.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** The three values Razorpay's checkout returns to the website or app after a successful payment. */
@Getter
@Setter
public class VerifyPaymentRequest {

    @NotBlank(message = "Razorpay order id is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay payment id is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay signature is required")
    private String razorpaySignature;
}