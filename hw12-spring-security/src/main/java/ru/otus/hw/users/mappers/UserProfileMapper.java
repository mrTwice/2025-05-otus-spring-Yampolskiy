package ru.otus.hw.users.mappers;

import org.mapstruct.*;
import ru.otus.hw.users.dto.UserProfileUpdateRequest;
import ru.otus.hw.users.model.User;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserProfileMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void applyProfileUpdate(UserProfileUpdateRequest request, @MappingTarget User user);
}
