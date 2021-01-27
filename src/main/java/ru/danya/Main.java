package ru.danya;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static DatabaseAccessProvider dbAccessProvider;
    private static Scanner input;

    public static void main(String[] args) {
        input = new Scanner(System.in);
        String url;
        String username;
        String password;
        boolean allowedToRewrite;

        System.out.println("Database should contain table 'students' with columns\n" +
                "'id', 'student_name', 'surname', 'academic_group', 'date_of_birth'.\n" +
                "If table doesn't exist, it will be created automatically\n" +
                "Enter DB URL:");
        url = input.next();
        System.out.println("Enter username:");
        username = input.next();
        System.out.println("Enter password:");
        password = input.next();
        System.out.println("Rewrite table 'students' if exists but doesn't match pattern? y/n, no by default:");
        allowedToRewrite = "y".compareToIgnoreCase(input.next()) == 0;

        try {
            dbAccessProvider =
                    new DatabaseAccessProvider(url, username, password, allowedToRewrite);
            System.out.println("Connected to database.");
            while (true) {
                if (chooseOps()) {
                    dbAccessProvider.close();
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private static boolean chooseOps() {
        System.out.println(
                "You can use following commands:\n" +
                        "'add' to get entry by it's unique number;\n" +
                        "'delete' to delete entry by it's unique number;\n" +
                        "'showall' to get all entries from database;\n" +
                        "'exit' to close the application."
        );

        String[] command = input.next().split(" ");
        switch (command[0]) {
            case "add": {
                addEntry();
                break;
            }
            case "delete": {
                deleteEntry();
                break;
            }
            case "showall": {
                showAllEntries();
                break;
            }
            case "exit": {
                System.out.println("Bye!");
                return true;
            }
            default: {
                System.out.println("Unknown command.");
                break;
            }
        }
        return false;
    }

    private static void showAllEntries() {
        try {
            List<Student> students = dbAccessProvider.getAllEntries();
            System.out.println("------------------------------------------------------------------------------------");
            students.forEach(System.out::println);
            System.out.println("------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void deleteEntry() {
        System.out.println("Enter student's digital unique number:");
        long id = input.nextLong();
        try {
            System.out.println(dbAccessProvider.deleteStudentById(id) + " rows affected");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void addEntry() {
        System.out.println("Enter student's name:");
        String name = input.next();
        System.out.println("Enter student's surname:");
        String surname = input.next();
        System.out.println("Enter student's group:");
        String group = input.next();

        boolean inputIsWrong = true;
        Date dateOfBirth = null;
        while (inputIsWrong) {
            System.out.println("Enter student's date of birth (yyyy-mm-dd):");
            try {
                dateOfBirth = Date.valueOf(input.next());
                inputIsWrong = false;
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid date format.");
            }
        }

        try {
            System.out.println(dbAccessProvider.addNewEntry(new Student(
                    name, surname, group, dateOfBirth)) + " rows affected");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
