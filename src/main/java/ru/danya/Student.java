package ru.danya;

import java.sql.Date;

public class Student {
    private long id;
    private String name;
    private String surname;
    private String group;
    private Date dateOfBirth;

    public Student(long id, String name, String surname, String group, Date dateOfBirth) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.group = group;
        this.dateOfBirth = dateOfBirth;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getGroup() {
        return group;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    @Override
    public String toString() {
        return String.format(
                "%-5d %-32s %-32s %-16s %-12s",
                id,
                name,
                surname,
                group,
                dateOfBirth.toString()
        );
    }
}
