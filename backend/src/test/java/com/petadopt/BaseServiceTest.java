package com.petadopt;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest {

    @BeforeAll
    static void initMybatisPlus() {
        try {
            Class<?> clazz = Wrappers.class;
            Field field = clazz.getDeclaredField("mybatisConfiguration");
            field.setAccessible(true);
            field.set(null, new MybatisConfiguration());
        } catch (Exception e) {
        }
    }

    protected <S extends ServiceImpl<M, T>, M, T> void setBaseMapper(S service, M mapper) {
        try {
            Field field = ServiceImpl.class.getDeclaredField("baseMapper");
            field.setAccessible(true);
            field.set(service, mapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
