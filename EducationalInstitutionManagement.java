import java.sql.*;
import java.util.*;

// Абстрактный класс для людей
abstract class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public abstract void displayRole();

    @Override
    public String toString() {
        return "Person{name='" + name + "', age=" + age + "}";
    }
    //2
    public abstract String getMajor();

    public abstract String getSubject();
}

// Интерфейс для работы с людьми
interface PersonRepository {
    void savePerson(Person person);
    List<Person> findAllPersons();
    void updatePersonAge(String name, int newAge);
    void deletePerson(String name);
}

// Конкретный класс для студента
class Student extends Person {
    private String major;

    public Student(String name, int age, String major) {
        super(name, age);
        this.major = major;
    }

    @Override
    public void displayRole() {
        System.out.println("I am a Student majoring in " + major + ".");
    }

    @Override
    public String toString() {
        return super.toString() + ", major='" + major + "'}";
    }

    @Override
    public String getMajor() {
        return "";
    }

    @Override
    public String getSubject() {
        return "";
    }
}

// Конкретный класс для преподавателя
class Teacher extends Person {
    private String subject;

    public Teacher(String name, int age, String subject) {
        super(name, age);
        this.subject = subject;
    }

    @Override
    public void displayRole() {
        System.out.println("I am a Teacher, and I teach " + subject + ".");
    }

    @Override
    public String toString() {
        return super.toString() + ", subject='" + subject + "'}";
    }

    @Override
    public String getMajor() {
        return "";
    }

    @Override
    public String getSubject() {
        return "";
    }
}

// Фабрика для создания людей
class PersonFactory {
    public static Person createPerson(String type, String name, int age, String info) {
        switch (type) {
            case "Student":
                return new Student(name, age, info);
            case "Teacher":
                return new Teacher(name, age, info);
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }
}

// Класс для работы с базой данных
class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/education_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "0000";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

// Репозиторий для работы с людьми в базе данных
class PersonRepositoryImpl implements PersonRepository {
    @Override
    public void savePerson(Person person) {
        String sql = "INSERT INTO Person (name, age, type, major_or_subject) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, person.getName());
            statement.setInt(2, person.getAge());
            statement.setString(3, person.getClass().getSimpleName());
            if (person instanceof Student) {
                statement.setString(4, ((Student) person).getMajor());
            } else if (person instanceof Teacher) {
                statement.setString(4, ((Teacher) person).getSubject());
            }
            statement.executeUpdate();
            System.out.println("Person saved: " + person.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Person> findAllPersons() {
        String sql = "SELECT * FROM Person";
        List<Person> persons = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                String type = resultSet.getString("type");
                String majorOrSubject = resultSet.getString("major_or_subject");

                Person person = PersonFactory.createPerson(type, name, age, majorOrSubject);
                persons.add(person);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return persons;
    }

    @Override
    public void updatePersonAge(String name, int newAge) {
        String sql = "UPDATE Person SET age = ? WHERE name = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, newAge);
            statement.setString(2, name);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Person updated: " + name + ", new age: " + newAge);
            } else {
                System.out.println("Person not found: " + name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deletePerson(String name) {
        String sql = "DELETE FROM Person WHERE name = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Person deleted: " + name);
            } else {
                System.out.println("Person not found: " + name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

// Главный класс программы
public class EducationalInstitutionManagement {
    public static void main(String[] args) {
        PersonRepository personRepository = new PersonRepositoryImpl();

        // Создаем студентов и преподавателей
        Person student1 = new Student("Alice", 20, "Computer Science");
        Person teacher1 = new Teacher("Dr. Smith", 45, "Mathematics");

        // Сохраняем студентов и преподавателей в базе данных
        personRepository.savePerson(student1);
        personRepository.savePerson(teacher1);

        // Читаем всех студентов и преподавателей из базы данных
        System.out.println("Persons in database:");
        personRepository.findAllPersons().forEach(person -> {
            System.out.println(person);
            person.displayRole(); // Отображаем роль человека
        });

        // Обновляем возраст человека
        personRepository.updatePersonAge("Alice", 21);

        // Удаляем человека
        personRepository.deletePerson("Dr. Smith");

        // Читаем всех людей после изменений
        System.out.println("Persons in database after updates:");
        personRepository.findAllPersons().forEach(person -> {
            System.out.println(person);
            person.displayRole(); // Отображаем роль человека
        });
    }
}
