<?php
//phpinfo();

try {
    $conn = new PDO('mysql:host=localhost;dbname=lab2', 'root', '');
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $conn->exec("CREATE TABLE IF NOT EXISTS Animals (id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,animal_name VARCHAR(100) NOT NULL, age INT NOT NULL)");
    $conn->exec("DELETE FROM Animals");
    $animals = array('Pies', 'Kot', 'Papuga', 'Królik', 'Żółw',NULL, 'Mysz', 'Koń', 'Słoń', 'Lew', 'Paw');
    $conn->beginTransaction();
    $insertStatement = $conn->prepare("INSERT INTO Animals (animal_name, age) VALUES (?, ?)");

    foreach ($animals as $animal) {
        $insertStatement->execute(array($animal, rand()));
    }

    $conn->commit();
    $selectStatement = $conn->query("SELECT * FROM Animals");
    while ($row = $selectStatement->fetch()) {
        echo $row['animal_name'] . ' - ' . $row['age'] . "\n";
    }

} catch (PDOException $e) {
    print $e->getMessage();
    die();
}

?>
