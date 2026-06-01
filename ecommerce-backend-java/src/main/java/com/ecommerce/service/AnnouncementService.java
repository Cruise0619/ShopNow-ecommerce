package com.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ecommerce.entity.Announcement;
import com.ecommerce.mapper.AnnouncementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    public List<Announcement> listActive() {
        return announcementMapper.selectList(new QueryWrapper<Announcement>()
                .eq("status", "on")
                .orderByDesc("created_at")
                .last("LIMIT 5"));
    }

    public List<Announcement> listAll() {
        return announcementMapper.selectList(new QueryWrapper<Announcement>().orderByDesc("created_at"));
    }
}
