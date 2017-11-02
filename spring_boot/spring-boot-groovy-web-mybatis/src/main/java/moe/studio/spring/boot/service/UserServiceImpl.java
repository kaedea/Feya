package moe.studio.spring.boot.service;

import org.springframework.stereotype.Service;

import java.util.List;

import javax.annotation.Resource;

import moe.studio.spring.boot.domain.User;
import moe.studio.spring.boot.repository.UserDao;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserDao mUserDao;

    public List<User> findAll() {
        return mUserDao.findAll();
    }

    public List<User> findByName(String name) {
        return mUserDao.findByName(name);
    }

    public void saveUser(User user) {
        mUserDao.saveUser(user);
    }

    public User findById(long id) {
        return mUserDao.findById(id);
    }

    public void delete(long id) {
        mUserDao.delete(id);
    }
}
