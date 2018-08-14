package moe.studio.spring.boot.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.annotation.Resource;

import moe.studio.spring.boot.dao.UserJpaRepository;
import moe.studio.spring.boot.entity.User;

@Service
@Transactional
public class UserServiceImpl implements IUserService {
    @Resource
    private UserJpaRepository userJpaRepository;

    public List<User> findAll() {
        return userJpaRepository.findAll();
    }

    public List<User> findByName(String name) {
        return userJpaRepository.findByName(name);
    }

    public void saveUser(User user) {
        userJpaRepository.save(user);
    }

    @Cacheable("users")
    public User findOne(long id) {
        System.out.println("Cached Pages");
        return userJpaRepository.findOne(id);
    }

    public void delete(long id) {
        userJpaRepository.delete(id);
    }
}
