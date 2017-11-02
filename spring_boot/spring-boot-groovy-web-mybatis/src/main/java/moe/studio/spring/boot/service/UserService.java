package moe.studio.spring.boot.service;

import java.util.List;

import moe.studio.spring.boot.domain.User;

public interface UserService {
    public List<User> findAll();

    public void saveUser(User book);

    public User findById(long id);

    public void delete(long id);

    public List<User> findByName(String name);

}
