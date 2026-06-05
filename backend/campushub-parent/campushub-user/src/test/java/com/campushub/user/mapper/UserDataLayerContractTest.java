package com.campushub.user.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campushub.user.entity.User;
import com.campushub.user.entity.UserVerification;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class UserDataLayerContractTest {

    @Test
    void userEntityMatchesCurrentSchema() throws NoSuchFieldException {
        TableName tableName = User.class.getAnnotation(TableName.class);

        assertNotNull(tableName);
        assertEquals("u_user", tableName.value());
        assertFieldType(User.class, "id", Long.class);
        assertFieldType(User.class, "studentId", String.class);
        assertFieldType(User.class, "password", String.class);
        assertFieldType(User.class, "nickname", String.class);
        assertFieldType(User.class, "avatarUrl", String.class);
        assertFieldType(User.class, "role", Integer.class);
        assertFieldType(User.class, "creditScore", Integer.class);
        assertFieldType(User.class, "status", Integer.class);
        assertFieldType(User.class, "createTime", OffsetDateTime.class);
        assertFieldType(User.class, "updateTime", OffsetDateTime.class);
        assertFieldType(User.class, "isDeleted", Boolean.class);
        assertNotNull(User.class.getDeclaredField("isDeleted").getAnnotation(TableLogic.class));
    }

    @Test
    void userVerificationEntityMatchesCurrentSchema() throws NoSuchFieldException {
        TableName tableName = UserVerification.class.getAnnotation(TableName.class);

        assertNotNull(tableName);
        assertEquals("t_user_verification", tableName.value());
        assertFieldType(UserVerification.class, "id", Long.class);
        assertFieldType(UserVerification.class, "userId", Long.class);
        assertFieldType(UserVerification.class, "reviewerId", Long.class);
        assertFieldType(UserVerification.class, "realName", String.class);
        assertFieldType(UserVerification.class, "studentCardImage", String.class);
        assertFieldType(UserVerification.class, "status", Integer.class);
        assertFieldType(UserVerification.class, "rejectReason", String.class);
        assertFieldType(UserVerification.class, "createTime", OffsetDateTime.class);
        assertFieldType(UserVerification.class, "updateTime", OffsetDateTime.class);
    }

    @Test
    void userMapperExposesB112Queries() throws NoSuchMethodException {
        Method selectActiveById = UserMapper.class.getMethod("selectActiveById", Long.class);
        Method selectActiveByStudentId = UserMapper.class.getMethod("selectActiveByStudentId", String.class);
        Method existsByStudentId = UserMapper.class.getMethod("existsByStudentId", String.class);

        assertEquals(User.class, selectActiveById.getReturnType());
        assertEquals(User.class, selectActiveByStudentId.getReturnType());
        assertEquals(int.class, existsByStudentId.getReturnType());
    }

    @Test
    void userVerificationMapperExposesB112Queries() throws NoSuchMethodException {
        Method selectLatestByUserId = UserVerificationMapper.class.getMethod("selectLatestByUserId", Long.class);
        Method selectPendingByUserId = UserVerificationMapper.class.getMethod("selectPendingByUserId", Long.class);
        Method selectPendingList = UserVerificationMapper.class.getMethod("selectPendingList");

        assertEquals(UserVerification.class, selectLatestByUserId.getReturnType());
        assertEquals(UserVerification.class, selectPendingByUserId.getReturnType());
        assertEquals(List.class, selectPendingList.getReturnType());
    }

    @Test
    void mapperXmlResourcesArePackagedUnderConfiguredLocation() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        assertNotNull(classLoader.getResource("mapper/user/UserMapper.xml"));
        assertNotNull(classLoader.getResource("mapper/user/UserVerificationMapper.xml"));
    }

    private static void assertFieldType(Class<?> type, String fieldName, Class<?> expectedType)
            throws NoSuchFieldException {
        Field field = type.getDeclaredField(fieldName);

        assertEquals(expectedType, field.getType());
    }
}
