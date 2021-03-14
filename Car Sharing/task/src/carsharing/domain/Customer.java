package carsharing.domain;

public class Customer {
    private int id;
    private String name;
    private int rentedCarID;

    public Customer(int id, String name, int rentedCarID) {
        this.id = id;
        this.name = name;
        this.rentedCarID = rentedCarID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRentedCarID() {
        return rentedCarID;
    }

    public void setRentedCarID(int rentedCarID) {
        this.rentedCarID = rentedCarID;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", rentedCarID=" + rentedCarID +
                '}';
    }
}
