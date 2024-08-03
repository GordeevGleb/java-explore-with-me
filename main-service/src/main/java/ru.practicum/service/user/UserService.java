package ru.practicum.service.user;

import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(NewUserRequest newUserRequest);

    List<UserDto> get(List<Long> ids, Integer from, Integer size);

    void delete(Long id);
}
