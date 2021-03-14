package carsharing.UI;

import carsharing.H2Connection;
import carsharing.domain.Car;
import carsharing.domain.Company;
import carsharing.domain.Customer;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserInterface {
    private Scanner scanner;
    private H2Connection h2Connection;

    public UserInterface(Scanner scanner, String dbName) {
        this.scanner = scanner;
        this.h2Connection = new H2Connection(dbName);
    }

    public void start() {
        h2Connection.connect();
        label:
        while (true) {
            System.out.println("\n1. Log in as a manager\n" +
                    "2. Log in as a customer\n" +
                    "3. Create a customer\n" +
                    "0. Exit");
            String input = scanner.nextLine();
            switch (input) {
                case "1":
                    managerUI();
                    break;
                case "2":
                    printCustomers();
                    break;
                case "3":
                    createCustomer();
                    break;
                case "0":
                    break label;
            }
        }
    }

    private void printCustomers() {
        List<Customer> customers = h2Connection.getAllCustomers();
        if (!customers.isEmpty()) {
            System.out.println("\nChoose a customer:");
            for (int i = 0; i < customers.size(); i++) {
                System.out.println((i + 1) + ". " + customers.get(i).getName());
            }
            System.out.println("0. Back");
            int input = Integer.parseInt(scanner.nextLine());
            if (input <= customers.size() && input > 0) {
                Customer customer = customers.get(input - 1);
                customerUI(customer);
            }
        } else {
            System.out.println("The customer list is empty!");
        }
    }

    private void customerUI(Customer customer) {
        label:
        while (true) {
            System.out.println("\n1. Rent a car\n" +
                    "2. Return a rented car\n" +
                    "3. My rented car\n" +
                    "0. Back");
            String input = scanner.nextLine();
            switch (input) {
                case "1":
                    rentCar(customer);
                    break;
                case "2":
                    returnCar(customer);
                    break;
                case "3":
                    myRentedCar(customer);
                    break;
                case "0":
                    break label;
            }
        }
    }

    private void myRentedCar(Customer customer) {
        Optional<Car> carOptional = h2Connection.getCarByID(customer.getRentedCarID());

        if (carOptional.isPresent()) {
            Optional<Company> companyOptional = h2Connection.getCompanyByID(carOptional.get().getCompanyID());
            System.out.println("Your rented car:\n" +
                    carOptional.get().getName() + "\n" +
                    "Company:\n" +
                    companyOptional.get().getName());
        } else {
            System.out.println("You didn't rent a car!");
        }
    }

    private void returnCar(Customer customer) {
        Optional<Car> carOptional = h2Connection.getCarByID(customer.getRentedCarID());
        if (carOptional.isPresent()) {
            h2Connection.returnCar(customer);
            customer.setRentedCarID(0);
            System.out.println("\nYou've returned a rented car!");
        } else {
            System.out.println("You didn't rent a car!");
        }
    }

    private void rentCar(Customer customer) {
        if (customer.getRentedCarID() != 0) {
            System.out.println("You've already rented a car!");
        } else {
            Optional<Company> company = companiesList();
            if (company.isPresent()) {
                List<Car> cars = getAvailableCars(company.get());
                printCars(cars);
                int input = Integer.parseInt(scanner.nextLine());
                if (input >= 0 && input <= cars.size()) {
                    Car car = cars.get(input - 1);
                    h2Connection.rentCar(car, customer);
                    customer.setRentedCarID(car.getId());
                    System.out.println("\nYou rented '" + car.getName() + "'");
                }
            } else {
                System.out.println("The company list is empty!");
            }
        }

    }

    private void managerUI() {
        label:
        while (true) {
            System.out.println("\n1. Company list\n" +
                    "2. Create a company\n" +
                    "0. Back");
            String input = scanner.nextLine();
            switch (input) {
                case "1":
                    Optional<Company> company = companiesList();
                    if (company.isPresent()) {
                        managerCompanyUI(company.get());
                    } else {
                        System.out.println("The company list is empty!");
                    }
                    break;
                case "2":
                    createCompany();
                    break;
                case "0":
                    break label;
            }
        }
    }

    private void createCompany() {
        System.out.println("\nEnter the company name:");
        String company = scanner.nextLine();
        h2Connection.addCompany(company);
    }

    private Optional<Company> companiesList() {
        List<Company> companies = h2Connection.getAllCompanies();
        if (!companies.isEmpty()) {
            System.out.println("\nCompany list:");
            for (int i = 0; i < companies.size(); i++) {
                System.out.println((i + 1) + ". " + companies.get(i).getName());
            }
            System.out.println("0. Back");
            int input = Integer.parseInt(scanner.nextLine());
            if (input <= companies.size() && input > 0) {
                return Optional.of(companies.get(input - 1));
            }
        }
        return Optional.empty();
    }

    private void managerCompanyUI(Company company) {
        System.out.print("\n" + company.getName() + " company:");
        label:
        while (true) {
            System.out.println("\n1. Car list\n" +
                    "2. Create a car\n" +
                    "0. Back\n");
            String input = scanner.nextLine();
            switch (input) {
                case "1":
                    printCars(getCars(company));
                    break;
                case "2":
                    addCar(company);
                    break;
                case "0":
                    break label;
            }
        }
    }

    private void addCar(Company company) {
        System.out.println("\nEnter the car name:");
        String carName = scanner.nextLine();
        if (!carName.isBlank()) {
            h2Connection.addCarr(company, carName);
        }
    }

    private List<Car> getAvailableCars(Company company) {
        return h2Connection.getCarsByCompanyAndAvailable(company.getId());
    }

    private List<Car> getCars(Company company) {
        return h2Connection.getCarsByCompany(company.getId());
    }

    public void printCars(List<Car> cars) {
        if (!cars.isEmpty()) {
            System.out.println("\nCars list:");
            for (int i = 0; i < cars.size(); i++) {
                System.out.println((i + 1) + ". " + cars.get(i).getName());
            }
        } else {
            System.out.println("The car list is empty!");
        }
    }


    private void createCustomer() {
        System.out.println("\nEnter the customer name:");
        String customer = scanner.nextLine();
        h2Connection.addCustomer(customer);
    }

}
