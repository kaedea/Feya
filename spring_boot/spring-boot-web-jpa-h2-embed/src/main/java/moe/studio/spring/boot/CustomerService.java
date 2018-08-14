package moe.studio.spring.boot;

public interface CustomerService {
    Customer getCustomerById(long id);

    Iterable<Customer> getAllCustomers();

    void save(Customer customer);
}
