package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.entity.User;
import ru.practicum.exception.ConcurrentNameException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(NewUserRequest newUserRequest) {
        log.info("MAIN SERVICE LOG: create user; name check");
        if (userRepository.existsByName(newUserRequest.getName())) {
            log.info("MAIN SERVICE LOG: name check failed");
            throw new ConcurrentNameException("could not execute statement; SQL [n/a]; constraint [uq_email];" +
                    " nested exception is org.hibernate.exception.ConstraintViolationException:" +
                    " could not execute statement");
        }
        User actual = userMapper.toUser(newUserRequest);
        userRepository.save(actual);
        log.info("MAIN SERVICE LOG: user id " + actual.getId() + " created");
        return userMapper.toUserDto(actual);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> get(List<Long> ids, Integer from, Integer size) {
        log.info("MAIN SERVICE LOG: get users list");
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        if (Optional.ofNullable(ids).isEmpty() || ids.isEmpty()) {
            List<UserDto> resultList = userMapper.toUserDtoList(userRepository.findAll(pageRequest).toList());
            log.info("MAIN SERVICE LOG: list of all users formed");
            return resultList;
        }
        List<UserDto> resultList = userMapper.toUserDtoList(userRepository.findAllById(ids));
        log.info("MAIN SERVICE LOG: list of users formed");
        return resultList;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("MAIN SERVICE LOG: delete user id " + id);
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User with id=" + id + " was not found");
        }
        userRepository.deleteById(id);
        log.info("MAIN SERVICE LOG: user id " + id + " removed");
    }
}
