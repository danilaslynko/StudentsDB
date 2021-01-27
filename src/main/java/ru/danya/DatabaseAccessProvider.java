package ru.danya;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAccessProvider {

    private Connection connection;
    private PreparedStatement addEntryStatement;
    private PreparedStatement deleteEntryStatement;
    private PreparedStatement getAllEntriesStatement;

    public DatabaseAccessProvider(String url,
                                  String username,
                                  String password)
            throws SQLException {
        connection = DriverManager.getConnection(url, username, password);

        Statement statement = connection.createStatement();
        statement.execute(
                "create table if not exists students " +
                        "(" +
                        "id bigint not null auto_increment," +
                        "student_name varchar(32) not null," +
                        "surname varchar(32) not null," +
                        "academic_group varchar(16) not null," +
                        "date_of_birth date not null," +
                        "primary key (id)" +
                        ");"
        );
        statement.close();

        addEntryStatement = connection.prepareStatement(
                "insert into students (student_name, surname, academic_group, date_of_birth) " +
                "values (?, ?, ?, ?);"
        );
        deleteEntryStatement = connection.prepareStatement(
                "delete from students where id=?;"
        );
        getAllEntriesStatement = connection.prepareStatement(
                "select * from students;"
        );
    }

    public int addNewEntry(Student student) throws SQLException {
        addEntryStatement.setString(1, student.getName());
        addEntryStatement.setString(2, student.getSurname());
        addEntryStatement.setString(3, student.getGroup());
        addEntryStatement.setDate(4, student.getDateOfBirth());

        return addEntryStatement.executeUpdate();
    }

    public int deleteStudentById(long id) throws SQLException {
        deleteEntryStatement.setLong(1, id);

        return deleteEntryStatement.executeUpdate();
    }

    public List<Student> getAllEntries() throws SQLException {
        List<Student> students = new ArrayList<>();
        ResultSet studentsResultSet = getAllEntriesStatement.executeQuery();

        while (studentsResultSet.next()) {
            students.add(new Student(
                    studentsResultSet.getLong("id"),
                    studentsResultSet.getString("student_name"),
                    studentsResultSet.getString("surname"),
                    studentsResultSet.getString("academic_group"),
                    studentsResultSet.getDate("date_of_birth")
            ));
        }

        studentsResultSet.close();
        return students;
    }
}
