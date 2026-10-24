package com.banjaraghar.backend.user.service;

import com.banjaraghar.backend.exception.BadRequestException;
import com.banjaraghar.backend.exception.ResourceNotFoundException;
import com.banjaraghar.backend.user.dto.request.AddressRequest;
import com.banjaraghar.backend.user.dto.response.AddressResponse;
import com.banjaraghar.backend.user.entity.Address;
import com.banjaraghar.backend.user.entity.User;
import com.banjaraghar.backend.user.repo.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private static final int MAX_ADDRESSES = 10;

    private final AddressRepository addressRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<AddressResponse> list(UUID userId) {
        return addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AddressResponse create(UUID userId, AddressRequest request) {
        User user = userService.getActiveUser(userId);

        long count = addressRepository.countByUserId(userId);
        if (count >= MAX_ADDRESSES) {
            throw new BadRequestException("You can save up to " + MAX_ADDRESSES + " addresses");
        }

        Address address = new Address();
        address.setUser(user);
        apply(address, request);
        address.setDefaultAddress(count == 0);   // the first address is the default
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(UUID userId, UUID addressId, AddressRequest request) {
        Address address = getOwnedAddress(userId, addressId);
        apply(address, request);
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse setDefault(UUID userId, UUID addressId) {
        addressRepository.clearDefaultForUser(userId);

        Address address = getOwnedAddress(userId, addressId);
        address.setDefaultAddress(true);
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public void delete(UUID userId, UUID addressId) {
        Address address = getOwnedAddress(userId, addressId);
        boolean wasDefault = address.isDefaultAddress();
        addressRepository.delete(address);

        if (wasDefault) {
            // The query below flushes the delete first, so the one-default-per-user rule is never broken
            addressRepository.findFirstByUserIdOrderByCreatedAtDesc(userId).ifPresent(next -> {
                next.setDefaultAddress(true);
                addressRepository.save(next);
            });
        }
    }

    private Address getOwnedAddress(UUID userId, UUID addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    private void apply(Address address, AddressRequest request) {
        address.setName(request.getName().trim());
        address.setPhone(request.getPhone().trim());
        address.setLine1(request.getLine1().trim());
        address.setLine2(blankToNull(request.getLine2()));
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPincode(request.getPincode().trim());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getName(),
                address.getPhone(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.isDefaultAddress());
    }
}
