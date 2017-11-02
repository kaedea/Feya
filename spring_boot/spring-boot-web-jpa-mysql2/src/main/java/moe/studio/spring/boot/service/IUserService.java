package moe.studio.spring.boot.service;

import java.util.List;

import moe.studio.spring.boot.entity.User;

public interface IUserService {
    List<User> findAll();

    void saveUser(User user);

    User findOne(long id);

    void delete(long id);

    List<User> findByName(String name);

}
