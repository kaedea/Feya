package moe.studio.spring.boot.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    long id;
    String name;
    String address;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return this.id + "," + this.name + "," + this.address;
    }
}
