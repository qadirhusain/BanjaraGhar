package com.banjaraghar.backend.user.controller;

import com.banjaraghar.backend.user.dto.request.AddressRequest;
import com.banjaraghar.backend.user.dto.response.AddressResponse;
import com.banjaraghar.backend.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users/me/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public List<AddressResponse> list(@AuthenticationPrincipal UUID userId) {
        return addressService.list(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(@AuthenticationPrincipal UUID userId,
                                  @Valid @RequestBody AddressRequest request) {
        return addressService.create(userId, request);
    }

    @PutMapping("/{id}")
    public AddressResponse update(@AuthenticationPrincipal UUID userId, @PathVariable UUID id,
                                  @Valid @RequestBody AddressRequest request) {
        return addressService.update(userId, id, request);
    }

    @PutMapping("/{id}/default")
    public AddressResponse setDefault(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return addressService.setDefault(userId, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        addressService.delete(userId, id);
    }
}
