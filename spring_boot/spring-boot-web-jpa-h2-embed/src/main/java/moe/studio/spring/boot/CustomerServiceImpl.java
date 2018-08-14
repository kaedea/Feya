package moe.studio.spring.boot;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("customerService")
public class CustomerServiceImpl implements CustomerService {

    @Resource
    private CustomerRepository repository;

    @Override
    public Customer getCustomerById(long id) {
        return repository.findOne(id);
    }

    @Override
    public Iterable<Customer> getAllCustomers() {
        return repository.findAll();
    }

    @Override
    public void save(Customer customer) {
        repository.save(customer);
    }
}
