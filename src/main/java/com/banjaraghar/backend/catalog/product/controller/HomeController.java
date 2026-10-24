package com.banjaraghar.backend.catalog.product.controller;

import com.banjaraghar.backend.catalog.product.dto.response.HomeResponse;
import com.banjaraghar.backend.catalog.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    @GetMapping
    public HomeResponse getHome() {
        return productService.getHome();
    }
}