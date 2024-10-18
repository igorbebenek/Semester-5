package org.example;
import java.util.Random;
import java.sql.*;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/lab3", "root", "")) {
            System.out.println("Połączono");

            Statement stmt = conn.createStatement();

            stmt.execute("CREATE TABLE IF NOT EXISTS tabela ("+ "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " + "liczba INT NOT NULL, "
                    + "tekst VARCHAR(255) NOT NULL)");

            stmt.executeUpdate("DELETE FROM tabela");

            String[] texts = {"Pies", "Kot", "Papuga", "Królik", "Żółw", "Mysz", "Koń", "Słoń", "Lew", "Paw"};
            PreparedStatement ps = conn.prepareStatement("INSERT INTO tabela (liczba, tekst) VALUES (?, ?)");
            Random random = new Random();

            for (String text : texts) {
                ps.setInt(1, random.nextInt(100));
                ps.setString(2, text);
                ps.executeUpdate();
            }
            System.out.println("Wartości z tablicy dodane");

            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM tabela");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " - " + rs.getInt("liczba") + " - " + rs.getString("tekst"));
            }

        } catch (SQLException e) {
            System.err.println("Nie udało się połączyć z bazą danych lub wykonać operacji.");
            System.err.println(e.getMessage());
        }
    }
}
