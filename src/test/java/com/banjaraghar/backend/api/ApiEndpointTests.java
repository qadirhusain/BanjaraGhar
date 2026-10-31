package com.banjaraghar.backend.api;

import com.banjaraghar.backend.auth.controller.AuthController;
import com.banjaraghar.backend.auth.dto.request.RegisterRequest;
import com.banjaraghar.backend.auth.service.AuthService;
import com.banjaraghar.backend.auth.service.SocialAuthService;
import com.banjaraghar.backend.catalog.category.controller.CategoryController;
import com.banjaraghar.backend.catalog.category.service.CategoryService;
import com.banjaraghar.backend.catalog.product.controller.ProductController;
import com.banjaraghar.backend.catalog.product.service.ProductService;
import com.banjaraghar.backend.common.response.MessageResponse;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Validator;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Lightweight HTTP contract tests: these exercise request routing, parameter binding,
 * bean validation, and controller-to-service delegation without a database or gateways.
 */
class ApiEndpointTests {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    private AuthService authService;
    private CategoryService categoryService;
    private ProductService productService;
    private MockMvc authMvc;
    private MockMvc catalogMvc;

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        SocialAuthService socialAuthService = mock(SocialAuthService.class);
        categoryService = mock(CategoryService.class);
        productService = mock(ProductService.class);

        authMvc = standaloneSetup(new AuthController(authService, socialAuthService))
                .setValidator((Validator) VALIDATOR_FACTORY.getValidator())
                .build();
        catalogMvc = standaloneSetup(new CategoryController(categoryService), new ProductController(productService))
                .build();
    }

    @Test
    void registerEndpointAcceptsValidRequestAndDelegates() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new MessageResponse("Verification code sent"));

        authMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"fullName":"Test Customer","email":"customer@example.com","password":"safe-pass-123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Verification code sent"));

        verify(authService).register(argThat(request ->
                "customer@example.com".equals(request.getEmail()) && "Test Customer".equals(request.getFullName())));
    }

    @Test
    void registerEndpointRejectsInvalidRequestBeforeCallingService() throws Exception {
        authMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"fullName":"","email":"not-an-email","password":"short"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void publicCategoryEndpointReturnsServiceResults() throws Exception {
        when(categoryService.listActive()).thenReturn(List.of());

        catalogMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(categoryService).listActive();
    }

    @Test
    void productSearchBindsQueryParametersAndUsesDefaults() throws Exception {
        catalogMvc.perform(get("/api/products")
                        .param("q", "cotton")
                        .param("minPrice", "1000")
                        .param("inStock", "true")
                        .param("size", "6"))
                .andExpect(status().isOk());

        verify(productService).search(eq("cotton"), isNull(), eq(1000L), isNull(), eq(true),
                eq(com.banjaraghar.backend.catalog.product.enums.ProductSort.NEWEST), eq(0), eq(6));
    }
}
