package moe.studio.spring.boot;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/customer")
public class CustomController {
    @Resource
    private CustomerService service;

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public @ResponseBody Response get(@RequestParam(value = "id", defaultValue = "0") long id) {
        Response response = new Response();
        if (id == 0) {
            response.data = service.getAllCustomers();
        } else {
            response.data = service.getCustomerById(id);
        }
        response.code = 0;
        response.msg = "success";
        return response;
    }

    @RequestMapping("/put")
    public String save(@RequestParam("name") String name) {
        service.save(new Customer(String.valueOf(name), "Default"));
        return "Done";
    }

    public static class Response {
        public int code = 0;
        public String msg;
        public Object data;
    }
}
