package org.example.jpa;

import jakarta.persistence.*;
import java.util.List;
import java.util.Random;

@Entity
class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String familyName;
    private Integer age;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("xd");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        for (int i = 0; i < 5; i++) {
            Person person = new Person();
            person.setFirstName("Imie" + i);
            person.setFamilyName("Nazwisko" + i);
            person.setAge(new Random().nextInt(50) + 1);
            em.persist(person);
        }
        em.getTransaction().commit();

        List<Person> persons = em.createQuery("SELECT p FROM Person p", Person.class).getResultList();
        for (Person p : persons) {
            System.out.println(p.getFirstName() + " " + p.getFamilyName() + ", Wiek: " + p.getAge());
        }

        em.getTransaction().begin();
        for (Person p : persons) {
            if (p.getAge() < 18) {
                p.setAge(18);
                em.merge(p);
            }
        }
        em.getTransaction().commit();

        persons = em.createQuery("SELECT p FROM Person p", Person.class).getResultList();
        for (Person p : persons) {
            System.out.println(p.getFirstName() + " " + p.getFamilyName() + ", Wiek: " + p.getAge());
        }

        long count = (long) em.createQuery("SELECT COUNT(p) FROM Person p WHERE p.age > 25").getSingleResult();
        System.out.println("Liczba osób z wiekiem większym niż 25: " + count);
    }
}
