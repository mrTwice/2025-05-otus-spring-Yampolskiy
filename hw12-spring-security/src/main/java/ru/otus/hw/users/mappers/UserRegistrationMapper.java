package ru.otus.hw.users.mappers;

import org.mapstruct.*;
import ru.otus.hw.users.dto.UserRegistrationRequest;
import ru.otus.hw.users.model.User;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = RawPasswordMapperSupport.class
)
public interface UserRegistrationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User fromRegistration(UserRegistrationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateFromRegistration(UserRegistrationRequest request, @MappingTarget User user);
}
