package moe.studio.spring.boot.web;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import javax.annotation.Resource;

import moe.studio.spring.boot.service.UserService;
import moe.studio.spring.boot.domain.User;

@RestController
@RequestMapping(value = "/users")
public class UserController {
    @Resource
    private UserService userService;

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
    public void deleteBook(@PathVariable int id) {
        userService.delete(id);
    }

    @RequestMapping(value = "/")
    public List<User> getBooks() {
        return userService.findAll();
    }

    @RequestMapping(value = "/{id}")
    public User getUser(@PathVariable int id) {
        User user = userService.findById(id);
        return user;
    }

    @RequestMapping(value = "/search/name/{name}")
    public List<User> getBookByName(@PathVariable String name) {
        List<User> users = userService.findByName(name);
        return users;
    }

}
