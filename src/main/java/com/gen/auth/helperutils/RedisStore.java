package com.gen.auth.helperutils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class RedisStore {
    private final StringRedisTemplate redisTemplate;
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    public void update(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }
    public boolean delete(String key) {
        Boolean deleted = redisTemplate.delete(key);
        return Boolean.TRUE.equals(deleted);
    }
    public boolean exists(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
    public void set(String key, String value, long expiryMillis) {
        redisTemplate.opsForValue().set(key,value,Duration.ofMillis(expiryMillis));
    }
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key,value);
    }

    public long getTtl(String key) {
        Long ttl = redisTemplate.getExpire(key);
        return ttl == null ? -2 : ttl;
    }
    //login count
    public long increment(String key) {
        Long value = redisTemplate
                .opsForValue()
                .increment(key);
        return value == null ? 0 : value;
    }
    public long increment(
            String key,
            long amount
    ) {
        Long value = redisTemplate
                .opsForValue()
                .increment(key, amount);
        return value == null ? 0 : value;
    }
    public long decrement(String key) {
        Long value = redisTemplate
                .opsForValue()
                .decrement(key);
        return value == null ? 0 : value;
    }
    public long decrement(
            String key,
            long amount
    ) {
        Long value = redisTemplate
                .opsForValue()
                .decrement(key, amount);
        return value == null ? 0 : value;
    }
    public boolean setIfAbsent(
            String key,
            String value
    ) {
        Boolean result = redisTemplate
                .opsForValue()
                .setIfAbsent(key, value);
        return Boolean.TRUE.equals(result);
    }
    public boolean setIfAbsent(
            String key,
            String value,
            long millis
    ) {
        Boolean result = redisTemplate
                .opsForValue()
                .setIfAbsent(
                        key,
                        value,
                        Duration.ofMillis(millis)
                );
        return Boolean.TRUE.equals(result);
    }
    public void hSet(
            String key,
            String field,
            String value
    ) {
        redisTemplate.opsForHash()
                .put(key, field, value);
    }
    public String hGet(
            String key,
            String field
    ) {
        Object value = redisTemplate.opsForHash()
                .get(key, field);
        return value == null
                ? null
                : value.toString();
    }
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash()
                .entries(key);
    }
    public boolean hDelete(
            String key,
            String field
    ) {
        Long result = redisTemplate.opsForHash()
                .delete(key, field);
        return result != null && result > 0;
    }
    public boolean hExists(
            String key,
            String field
    ) {
        Boolean result = redisTemplate.opsForHash()
                .hasKey(key, field);
        return Boolean.TRUE.equals(result);
    }
    public long listPush(
            String key,
            String value
    ) {
        Long result = redisTemplate.opsForList()
                .rightPush(key, value);
        return result == null ? 0 : result;
    }
    public List<String> listGet(
            String key,
            long start,
            long end
    ) {
        return redisTemplate.opsForList()
                .range(key, start, end);
    }
    public List<String> listGetAll(String key) {
        return redisTemplate.opsForList()
                .range(key, 0, -1);
    }
    public String listPop(String key) {
        return redisTemplate.opsForList()
                .leftPop(key);
    }
    public long listSize(String key) {
        Long size = redisTemplate.opsForList()
                .size(key);
        return size == null ? 0 : size;
    }

    public void deleteUserKeys(String pattern) {

        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();

        redisTemplate.execute((RedisCallback<Void>) connection -> {

            try (Cursor<byte[]> cursor = connection.scan(options)) {

                List<byte[]> keys = new ArrayList<>();

                while (cursor.hasNext()) {
                    keys.add(cursor.next());

                    // Delete in batches
                    if (keys.size() >= 100) {
                        connection.del(keys.toArray(new byte[0][]));
                        keys.clear();
                    }
                }

                // Delete remaining keys
                if (!keys.isEmpty()) {
                    connection.del(keys.toArray(new byte[0][]));
                }
            }

            return null;
        });
    }

}

