package com.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ecommerce.entity.Address;
import com.ecommerce.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressMapper addressMapper;

    public List<Address> list(Integer userId) {
        QueryWrapper<Address> qw = new QueryWrapper<Address>()
                .eq("user_id", userId)
                .orderByDesc("is_default")
                .orderByDesc("created_at");
        return addressMapper.selectList(qw);
    }

    @Transactional
    public Address create(Address address) {
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            clearDefaults(address.getUserId());
        }
        addressMapper.insert(address);
        return address;
    }

    @Transactional
    public void update(Integer userId, Integer id, Address data) {
        Address addr = addressMapper.selectById(id);
        if (addr == null || !addr.getUserId().equals(userId)) return;
        if (Boolean.TRUE.equals(data.getIsDefault())) {
            clearDefaults(userId);
        }
        data.setId(id);
        addressMapper.updateById(data);
    }

    public void remove(Integer userId, Integer id) {
        Address addr = addressMapper.selectById(id);
        if (addr != null && addr.getUserId().equals(userId)) {
            addressMapper.deleteById(id);
        }
    }

    @Transactional
    public void setDefault(Integer userId, Integer id) {
        clearDefaults(userId);
        Address addr = addressMapper.selectById(id);
        if (addr != null && addr.getUserId().equals(userId)) {
            addr.setIsDefault(true);
            addressMapper.updateById(addr);
        }
    }

    private void clearDefaults(Integer userId) {
        List<Address> defaults = addressMapper.selectList(
                new QueryWrapper<Address>().eq("user_id", userId).eq("is_default", true));
        for (Address a : defaults) {
            a.setIsDefault(false);
            addressMapper.updateById(a);
        }
    }
}
