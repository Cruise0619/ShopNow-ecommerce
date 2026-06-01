package com.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.entity.Message;
import com.ecommerce.entity.User;
import com.ecommerce.mapper.MessageMapper;
import com.ecommerce.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    private Integer cachedAdminId;

    public Integer getAdminId() {
        if (cachedAdminId == null) {
            User admin = userMapper.selectOne(new QueryWrapper<User>().eq("role", "admin").last("LIMIT 1"));
            if (admin != null) {
                cachedAdminId = admin.getId();
            }
        }
        return cachedAdminId;
    }

    public Message send(Integer fromUserId, Integer toUserId, String content) {
        Message msg = new Message();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setContent(content);
        msg.setCreatedAt(new Date());
        msg.setUpdatedAt(new Date());
        messageMapper.insert(msg);
        return msg;
    }

    public List<Message> getConversation(Integer userId) {
        Integer adminId = getAdminId();
        QueryWrapper<Message> qw = new QueryWrapper<Message>()
                .and(w -> w
                    .eq("from_user_id", userId).eq("to_user_id", adminId)
                    .or()
                    .eq("from_user_id", adminId).eq("to_user_id", userId)
                )
                .orderByAsc("created_at");
        return messageMapper.selectList(qw);
    }

    public Map<String, Object> adminConversations(int page, int pageSize) {
        Integer adminId = getAdminId();
        QueryWrapper<Message> qw = new QueryWrapper<Message>()
                .eq("to_user_id", adminId)
                .or().eq("from_user_id", adminId)
                .orderByDesc("created_at");
        Page<Message> result = messageMapper.selectPage(new Page<>(page, pageSize), qw);

        // Collect distinct user IDs
        Set<Integer> userIds = new LinkedHashSet<>();
        for (Message m : result.getRecords()) {
            Integer uid = m.getFromUserId().equals(adminId) ? m.getToUserId() : m.getFromUserId();
            userIds.add(uid);
        }

        List<Map<String, Object>> conversations = new ArrayList<>();
        for (Integer uid : userIds) {
            User user = userMapper.selectById(uid);
            Message lastMsg = getLastMessage(uid);
            long unread = countUnread(uid);
            Map<String, Object> conv = new HashMap<>();
            conv.put("userId", uid);
            conv.put("username", user != null ? user.getUsername() : "用户#" + uid);
            conv.put("lastMessage", lastMsg != null ? lastMsg.getContent() : "");
            conv.put("lastTime", lastMsg != null ? lastMsg.getCreatedAt() : null);
            conv.put("unread", unread);
            conversations.add(conv);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", conversations);
        data.put("total", result.getTotal());
        return data;
    }

    private Message getLastMessage(Integer userId) {
        Integer adminId = getAdminId();
        QueryWrapper<Message> qw = new QueryWrapper<Message>()
                .and(w -> w
                    .eq("from_user_id", userId).eq("to_user_id", adminId)
                    .or()
                    .eq("from_user_id", adminId).eq("to_user_id", userId)
                )
                .orderByDesc("created_at")
                .last("LIMIT 1");
        return messageMapper.selectOne(qw);
    }

    private long countUnread(Integer userId) {
        Integer adminId = getAdminId();
        return messageMapper.selectCount(new QueryWrapper<Message>()
                .eq("from_user_id", userId)
                .eq("to_user_id", adminId));
    }
}
