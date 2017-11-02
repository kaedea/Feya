package moe.studio.spring.boot.repository;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

import moe.studio.spring.boot.domain.User;

@Repository
public interface UserDao {
    @Select("SELECT * FROM USER")
    List<User> findAll();

    User findById(long id);

    List<User> findByName(String name);

    void saveUser(User book);

    void delete(long id);
}
