package sample.web

import groovy.transform.CompileStatic
import moe.studio.spring.boot.User
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@CompileStatic
@RestController
class UserController {

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    User getUser() {
        User user = new User()
        user.id = 100L
        user.name = "User"
        return user
    }
}
