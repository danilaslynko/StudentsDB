package ru.danya;

import java.io.Closeable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAccessProvider implements Closeable {

    private final Connection connection;
    private final PreparedStatement addEntryStatement;
    private final PreparedStatement deleteEntryStatement;
    private final PreparedStatement getAllEntriesStatement;

    public DatabaseAccessProvider(String url,
                                  String username,
                                  String password)
            throws SQLException {
        connection = DriverManager.getConnection(url, username, password);
        Statement statement = connection.createStatement();

        // проверяем таблицу на правильность, создаем ее, если ее нет.
        // если таблица существует, но ее структура не соответствует образцу, выкидываем исключение
        int tableMatchResult = tableMatches(statement);
        if (tableMatchResult == 0) {
            throw new SQLException("Table 'students' exist, but doesn't match pattern.\n" +
                    "Application will be closed to prevent data loss.");
        }
        if (tableMatchResult == -1) {
            statement.execute("create table students " +
                    "(" +
                    "id bigint not null auto_increment," +
                    "student_name varchar(32) not null," +
                    "surname varchar(32) not null," +
                    "academic_group varchar(16) not null," +
                    "date_of_birth date not null," +
                    "primary key (id)" +
                    ");");
        }
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

    // метод проверяет, есть ли в БД таблица students и соответствует ли она дефолтной структуре.
    // возвращает -1, если таблицы нет, 0, ее столбцы названы не так, как требуют наши скрипты,
    // 1, если таблица существует и соответствует шаблону.
    private int tableMatches(Statement statement) {
        List<String> checkList = new ArrayList<>(5);
        int matches = 1;
        checkList.add("id");
        checkList.add("student_name");
        checkList.add("surname");
        checkList.add("academic_group");
        checkList.add("date_of_birth");

        try (ResultSet columnsSet = statement.executeQuery("describe students;")) {
            while (columnsSet.next()) {
                if (!checkList.contains(columnsSet.getString("field"))) {
                    matches = 0;
                    break;
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1146) { // 1146 - код ошибки "таблица не существует", можем вернуть false
                matches = -1;
            }
        }

        return matches;
    }

    // добавляет запись о студенте в таблицу
    public int addNewEntry(Student student) throws SQLException {
        addEntryStatement.setString(1, student.getName());
        addEntryStatement.setString(2, student.getSurname());
        addEntryStatement.setString(3, student.getGroup());
        addEntryStatement.setDate(4, student.getDateOfBirth());

        return addEntryStatement.executeUpdate();
    }

    // удаляет запись о студенте
    public int deleteStudentById(long id) throws SQLException {
        deleteEntryStatement.setLong(1, id);

        return deleteEntryStatement.executeUpdate();
    }

    // вытаскивает из таблицы все записи о студентах
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

    @Override
    public void close() {
        try {
            connection.close();
            addEntryStatement.close();
            deleteEntryStatement.close();
            getAllEntriesStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
