package moe.studio.spring.boot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.annotation.Resource;

import moe.studio.spring.boot.entity.User;
import moe.studio.spring.boot.service.IUserService;

@RestController
@RequestMapping(value = "/users")
public class UserController {
    @Resource
    private IUserService userService;

    @RequestMapping(value = "/add/{id}/{name}/{address}")
    public User addUser(@PathVariable int id, @PathVariable String name,
                        @PathVariable String address) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setAddress(address);
        userService.saveUser(user);
        return user;
    }

    @RequestMapping(value = "/delete/{id}")
    public void deleteUser(@PathVariable int id) {
        userService.delete(id);
    }

    @RequestMapping(value = "/")
    public List<User> getUsers() {
        return userService.findAll();
    }

    @RequestMapping(value = "/{id}")
    public User getUser(@PathVariable int id) {
        return userService.findOne(id);
    }

    @RequestMapping(value = "/search/name/{name}")
    public List<User> getUserByName(@PathVariable String name) {
        return userService.findByName(name);
    }

}
