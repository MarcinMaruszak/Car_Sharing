package carsharing;

import carsharing.domain.Car;
import carsharing.domain.Company;
import carsharing.domain.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class H2Connection {
    private final String JDBCDriver = "org.h2.Driver";
    private String dbUrl = "jdbc:h2:./src/carsharing/db/";


    public H2Connection(String dbUrl) {
        this.dbUrl = this.dbUrl.concat(dbUrl);
    }

    public void connect() {
        try {
            Class.forName(JDBCDriver);
            createCompanyTable();
            createCarTable();
            createCustomerTable();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createCompanyTable() {
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            connection.setAutoCommit(true);
            Statement statement = connection.createStatement();
            String sql = "CREATE TABLE company(" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "name VARCHAR(255) UNIQUE NOT NULL" +
                    ");";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCarTable() {
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            connection.setAutoCommit(true);
            Statement statement = connection.createStatement();
            String sql = "CREATE TABLE car(" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "name VARCHAR(255) UNIQUE NOT NULL," +
                    "company_id INT NOT NULL," +
                    "CONSTRAINT fk_company FOREIGN KEY (company_id)" +
                    "REFERENCES company (id)" +
                    ");";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCustomerTable() {
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            connection.setAutoCommit(true);
            Statement statement = connection.createStatement();
            String sql = "CREATE TABLE customer(" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "name VARCHAR(255) NOT NULL UNIQUE," +
                    "rented_car_id INT," +
                    "CONSTRAINT fk_car FOREIGN KEY (rented_car_id)" +
                    "REFERENCES car (id)" +
                    ");";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public List<Company> getAllCompanies() {
        List<Company> list = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            Statement statement = connection.createStatement();
            String sql = "SELECT * FROM company";
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                list.add(new Company(id, name));
            }
            statement.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return list;
    }

    public void addCompany(String companyName) {
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            String sql = "INSERT INTO company (name) VALUES (?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, companyName);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            System.out.println("The company was created!");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void addCarr(Company company, String carName) {
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            String sql = "INSERT INTO car (name, company_id) VALUES (?, ?)";
            PreparedStatement pr = connection.prepareStatement(sql);
            pr.setString(1, carName);
            pr.setInt(2, company.getId());
            pr.executeUpdate();
            pr.close();
            System.out.println("The car was added!");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public List<Car> getCarsByCompany(int id) {
        List<Car> cars = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            String sql = "SELECT * FROM car WHERE company_id = ?";
            PreparedStatement pr = connection.prepareStatement(sql);
            pr.setInt(1, id);
            pr.execute();
            ResultSet rs = pr.getResultSet();
            while (rs.next()) {
                int carID = rs.getInt("id");
                String name = rs.getString("name");
                int companyID = rs.getInt("company_id");
                cars.add(new Car(carID, name, companyID));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return cars;
    }

    public List<Car> getCarsByCompanyAndAvailable(int id) {
        List<Car> cars = new ArrayList<>();

        getCarsByCompany(id).forEach(System.out::println);
        getAllCustomers().forEach(System.out::println);

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            String sql = "SELECT * FROM car WHERE company_id = ? AND " +
                    "id NOT IN (SELECT rented_car_id FROM customer WHERE rented_car_id IS NOT NULL)";
            PreparedStatement pr = connection.prepareStatement(sql);
            pr.setInt(1, id);
            pr.execute();
            ResultSet rs = pr.getResultSet();
            while (rs.next()) {
                int carID = rs.getInt("id");
                String name = rs.getString("name");
                int companyID = rs.getInt("company_id");
                cars.add(new Car(carID, name, companyID));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return cars;
    }

    public void addCustomer(String customer) {
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            String sql = "INSERT INTO customer (name) VAlUES (?)";
            PreparedStatement pr = connection.prepareStatement(sql);
            pr.setString(1, customer);
            pr.executeUpdate();
            pr.close();
            System.out.println("The customer was added!");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            String sql = "SELECT * FROM customer";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int carID = resultSet.getInt("rented_car_id");
                Customer customer = new Customer(id, name, carID);
                customers.add(customer);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return customers;
    }

    public void rentCar(Car car, Customer customer) {
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            connection.setAutoCommit(false);
            String sql = "UPDATE customer SET rented_car_id = ? WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, car.getId());
            ps.setInt(2, customer.getId());
            ps.executeUpdate();
            connection.commit();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Optional<Car> getCarByID(int rentedCarID) {
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            String sql = "SELECT * FROM car WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, rentedCarID);
            ps.execute();
            ResultSet rs = ps.getResultSet();
            if (rs.next()) {
                int carID = rs.getInt("id");
                String name = rs.getString("name");
                int companyID = rs.getInt("company_id");
                return Optional.of(new Car(carID, name, companyID));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Company> getCompanyByID(int companyID) {
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            String sql = "SELECT * FROM company WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, companyID);
            ps.execute();
            ResultSet rs = ps.getResultSet();
            if (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                return Optional.of(new Company(id , name));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return Optional.empty();
    }

    public void returnCar(Customer customer) {
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            String sql = "UPDATE customer SET rented_car_id = NULL WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, customer.getId());
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
