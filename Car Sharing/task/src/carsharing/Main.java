package carsharing;

import carsharing.UI.UserInterface;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        new UserInterface(scanner, args[1]).start();

    }
}