package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByName(String name);
}
