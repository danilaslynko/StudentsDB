package ru.danya;

import java.sql.Date;

// POJO студента. попытка в ORM.
public class Student {
    private long id;
    private String name;
    private String surname;
    private String group;
    private Date dateOfBirth;

    // id в таблице ставим на автоинкремент, поэтому его нет смысла прописывать в конструкторе;
    // при получении записи из БД проще вставить его сеттером.
    public Student(String name, String surname, String group, Date dateOfBirth) {
        this.name = name;
        this.surname = surname;
        this.group = group;
        this.dateOfBirth = dateOfBirth;
    }

    public void setId(long id) {
        this.id = id;
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
