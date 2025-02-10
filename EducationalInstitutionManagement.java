import java.sql.*;
import java.util.*;

public class EducationalInstitutionManagement {
    // Абстрактный класс Person (для студентов и преподавателей)
    abstract static class Person {
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

        public abstract void displayRole(); // Абстрактный метод для роли человека (студент/преподаватель)

        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + "}";
        }
    }

    // Конкретный класс Student (Студент)
    static class Student extends Person {
        private String major;

        public Student(String name, int age, String major) {
            super(name, age);
            this.major = major;
        }

        public String getMajor() {
            return major;
        }

        public void setMajor(String major) {
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
    }

    // Конкретный класс Teacher (Преподаватель)
    static class Teacher extends Person {
        private String subject;

        public Teacher(String name, int age, String subject) {
            super(name, age);
            this.subject = subject;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
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
    }

    // Класс для работы с учебным заведением (Institution)
    static class Institution {
        private String name;
        private String address;

        public Institution(String name, String address) {
            this.name = name;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return "Institution{name='" + name + "', address='" + address + "'}";
        }
    }

    // Класс для подключения к базе данных
    static class DatabaseConnection {
        private static final String URL = "jdbc:postgresql://localhost:5432/education_db"; // URL базы
        private static final String USER = "postgres"; // Имя пользователя
        private static final String PASSWORD = "0000"; // Пароль

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }

    // Класс для работы с таблицами в базе данных (Students, Teachers, Institutions)
    static class EducationRepository {
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

                    Person person = null;
                    if ("Student".equals(type)) {
                        person = new Student(name, age, majorOrSubject);
                    } else if ("Teacher".equals(type)) {
                        person = new Teacher(name, age, majorOrSubject);
                    } else {
                        throw new IllegalStateException("Unknown person type: " + type);
                    }

                    persons.add(person);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return persons;
        }

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

    // Главный метод
    public static void main(String[] args) {
        EducationRepository repository = new EducationRepository();

        // Создаем студентов и преподавателей
        Person student1 = new Student("Alice", 20, "Computer Science");
        Person teacher1 = new Teacher("Dr. Smith", 45, "Mathematics");

        // Сохраняем студентов и преподавателей в базе данных
        repository.savePerson(student1);
        repository.savePerson(teacher1);

        // Читаем всех студентов и преподавателей из базы данных
        System.out.println("Persons in database:");
        repository.findAllPersons().forEach(person -> {
            System.out.println(person);
            person.displayRole(); // Отображаем роль человека
        });

        // Обновляем возраст человека
        repository.updatePersonAge("Alice", 21);

        // Удаляем человека
        repository.deletePerson("Dr. Smith");

        // Читаем всех людей после изменений
        System.out.println("Persons in database after updates:");
        repository.findAllPersons().forEach(person -> {
            System.out.println(person);
            person.displayRole(); // Отображаем роль человека
        });
    }
}
