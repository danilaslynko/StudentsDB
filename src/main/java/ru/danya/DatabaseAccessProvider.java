package ru.danya;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAccessProvider implements Closeable {

    private Connection connection;
    private PreparedStatement addEntryStatement;
    private PreparedStatement deleteEntryStatement;
    private PreparedStatement getAllEntriesStatement;
    private boolean allowedToRewrite;

    public DatabaseAccessProvider(String url,
                                  String username,
                                  String password,
                                  boolean allowedToRewrite) throws SQLException {
        this.allowedToRewrite = allowedToRewrite;
        connection = DriverManager.getConnection(url, username, password);

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
        prepareTable();
    }

    private void prepareTable() throws SQLException {
        Statement statement = connection.createStatement();
        // проверяем таблицу на правильность, создаем ее, если ее нет.
        // если таблица существует, но ее структура не соответствует образцу, выкидываем исключение
        int tableMatchResult = tableMatches(statement);
        if (tableMatchResult == 0) {
            if (this.allowedToRewrite) {
                statement.execute("drop table students;");
                tableMatchResult = -1;
            } else {
                close();

                throw new SQLException("Database connection was closed.");
            }
        }
        if (tableMatchResult == -1) {
            statement.execute("create table students " +
                   "(id bigint not null auto_increment," +
                   "student_name varchar(32) not null," +
                   "surname varchar(32) not null," +
                   "academic_group varchar(16) not null," +
                   "date_of_birth date not null," +
                   "primary key (id));"
            );
        }
        statement.close();
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
            Student studentFromDB = new Student(
                    studentsResultSet.getString("student_name"),
                    studentsResultSet.getString("surname"),
                    studentsResultSet.getString("academic_group"),
                    studentsResultSet.getDate("date_of_birth"));
            studentFromDB.setId(studentsResultSet.getLong("id"));

            students.add(studentFromDB);
        }

        studentsResultSet.close();
        return students;
    }

    @Override
    public void close() {
        try {
            disconnect(connection);
            disconnect(addEntryStatement);
            disconnect(deleteEntryStatement);
            disconnect(getAllEntriesStatement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnect(AutoCloseable closeable)
            throws Exception {
        if (closeable != null) {
            closeable.close();
            closeable = null;
        }
        else throw new IllegalStateException("Connection or statement is already closed");
    }
}
