package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.entity.Address;
import com.ecommerce.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public ApiResponse<List<Address>> list(HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        return ApiResponse.ok(addressService.list(userId));
    }

    @PostMapping
    public ApiResponse<Address> create(@RequestBody Address address, HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        address.setUserId(userId);
        return ApiResponse.ok(addressService.create(address));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Integer id, @RequestBody Address data,
                                  HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        addressService.update(userId, id, data);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> remove(@PathVariable Integer id, HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        addressService.remove(userId, id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/default")
    public ApiResponse<?> setDefault(@PathVariable Integer id, HttpServletRequest req) {
        Integer userId = (Integer) req.getAttribute("userId");
        addressService.setDefault(userId, id);
        return ApiResponse.ok(null);
    }
}
