package com.ecommerce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private static final String CHAT_MSGS_KEY = "chat:msgs:%d:%d";
    private static final String CHAT_CONV_KEY = "chat:conv:%d";
    private static final String CHAT_UNREAD_KEY = "chat:unread:%d";
    private static final long MSG_TTL_DAYS = 30;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public ChatService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private String msgKey(int uid1, int uid2) {
        int a = Math.min(uid1, uid2);
        int b = Math.max(uid1, uid2);
        return String.format(CHAT_MSGS_KEY, a, b);
    }

    private String convKey(int userId) {
        return String.format(CHAT_CONV_KEY, userId);
    }

    private String unreadKey(int userId) {
        return String.format(CHAT_UNREAD_KEY, userId);
    }

    public Map<String, Object> sendMessage(Integer fromUserId, String fromName, Integer toUserId, String content) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("id", UUID.randomUUID().toString().substring(0, 8));
        msg.put("from", fromUserId);
        msg.put("fromName", fromName);
        msg.put("to", toUserId);
        msg.put("content", content);
        msg.put("time", Instant.now().toEpochMilli());

        try {
            String json = objectMapper.writeValueAsString(msg);

            String msgsKey = msgKey(fromUserId, toUserId);
            redisTemplate.opsForList().rightPush(msgsKey, json);
            redisTemplate.expire(msgsKey, MSG_TTL_DAYS, TimeUnit.DAYS);

            long score = (long) msg.get("time");
            redisTemplate.opsForZSet().add(convKey(fromUserId), String.valueOf(toUserId), score);
            redisTemplate.opsForZSet().add(convKey(toUserId), String.valueOf(fromUserId), score);

            redisTemplate.opsForHash().increment(unreadKey(toUserId), String.valueOf(fromUserId), 1);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize chat message", e);
        } catch (Exception e) {
            log.warn("Redis error in sendMessage: {}", e.getMessage());
        }

        return msg;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getConversations(Integer userId) {
        try {
            Set<Object> members = redisTemplate.opsForZSet()
                    .reverseRange(convKey(userId), 0, -1);
            if (members == null || members.isEmpty()) {
                return Collections.emptyList();
            }

            List<Map<String, Object>> result = new ArrayList<>();
            for (Object member : members) {
                int otherId = Integer.parseInt(member.toString());
                if (otherId == userId) continue;

                Map<String, Object> conv = new LinkedHashMap<>();
                conv.put("userId", otherId);

                List<Object> raw = redisTemplate.opsForList()
                        .range(msgKey(userId, otherId), -1, -1);
                if (raw != null && !raw.isEmpty()) {
                    try {
                        Map<String, Object> lastMsg = objectMapper.readValue(
                                raw.get(0).toString(), Map.class);
                        conv.put("lastMessage", lastMsg.get("content"));
                        conv.put("lastTime", lastMsg.get("time"));
                        conv.put("lastFrom", lastMsg.get("from"));
                    } catch (JsonProcessingException ignored) {}
                }

                Object unreadObj = redisTemplate.opsForHash()
                        .get(unreadKey(userId), String.valueOf(otherId));
                conv.put("unread", unreadObj != null ? Integer.parseInt(unreadObj.toString()) : 0);

                result.add(conv);
            }
            return result;
        } catch (Exception e) {
            log.warn("Redis error in getConversations for userId={}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getMessages(Integer userA, Integer userB) {
        try {
            List<Object> raw = redisTemplate.opsForList()
                    .range(msgKey(userA, userB), 0, -1);
            if (raw == null || raw.isEmpty()) {
                return Collections.emptyList();
            }

            return raw.stream().map(r -> {
                try {
                    return (Map<String, Object>) objectMapper.readValue(r.toString(), Map.class);
                } catch (JsonProcessingException e) {
                    return Collections.<String, Object>emptyMap();
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Redis error in getMessages for userA={} userB={}: {}", userA, userB, e.getMessage());
            return Collections.emptyList();
        }
    }

    public void markRead(Integer userId, Integer otherUserId) {
        try {
            redisTemplate.opsForHash().delete(unreadKey(userId), String.valueOf(otherUserId));
        } catch (Exception e) {
            log.warn("Redis error in markRead for userId={} otherId={}: {}", userId, otherUserId, e.getMessage());
        }
    }

    public int getTotalUnread(Integer userId) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash()
                    .entries(unreadKey(userId));
            if (entries == null) return 0;
            return entries.values().stream()
                    .mapToInt(v -> Integer.parseInt(v.toString()))
                    .sum();
        } catch (Exception e) {
            log.warn("Redis error in getTotalUnread for userId={}: {}", userId, e.getMessage());
            return 0;
        }
    }
}
