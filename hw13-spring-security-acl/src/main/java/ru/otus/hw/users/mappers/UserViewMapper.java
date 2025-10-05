package ru.otus.hw.users.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import ru.otus.hw.users.dto.PageResponse;
import ru.otus.hw.users.dto.UserResponse;
import ru.otus.hw.users.model.User;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserViewMapper {

    UserResponse toResponse(User user);

    default PageResponse<UserResponse> toPageResponse(Page<User> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
