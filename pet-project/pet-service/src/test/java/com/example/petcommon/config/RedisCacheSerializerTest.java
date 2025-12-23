package com.example.petcommon.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import com.example.petpojo.vo.PetsDetailsVo;
class RedisCacheSerializerTest {

    @Test
    void genericJacksonSerializerShouldDeserializeToOriginalTypeWithTypeInfo() {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        PetsDetailsVo value = new PetsDetailsVo();
        value.setPid(1);
        value.setName("旺财");

        byte[] bytes = serializer.serialize(value);
        Object restored = serializer.deserialize(bytes);

        assertNotNull(restored);
        assertInstanceOf(PetsDetailsVo.class, restored);
        PetsDetailsVo restoredVo = (PetsDetailsVo) restored;
        assertEquals(1, restoredVo.getPid());
        assertEquals("旺财", restoredVo.getName());
    }
}
