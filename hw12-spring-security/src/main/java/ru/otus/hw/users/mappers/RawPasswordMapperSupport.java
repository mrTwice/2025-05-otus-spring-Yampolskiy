package ru.otus.hw.users.mappers;


import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import ru.otus.hw.users.dto.AdminCreateUserRequest;
import ru.otus.hw.users.dto.UserRegistrationRequest;
import ru.otus.hw.users.model.User;

public final class RawPasswordMapperSupport {

    private RawPasswordMapperSupport() {

    }

    @AfterMapping
    static void copyRawPassword(UserRegistrationRequest src, @MappingTarget User target) {
        target.setPasswordHash(src.password());
    }

    @AfterMapping
    static void copyRawPassword(AdminCreateUserRequest src, @MappingTarget User target) {
        target.setPasswordHash(src.password());
    }
}
