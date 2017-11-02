package moe.studio.spring.boot.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import moe.studio.spring.boot.entity.User;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {
    List<User> findByName(String name);
}
