<?php
$mysqli = mysqli_connect("localhost", "root", "", "example2_db");

if (!$mysqli) {
    die("Failed to connect to MySQL: " . mysqli_connect_error());
}

mysqli_query($mysqli, "CREATE TABLE IF NOT EXISTS tabela (liczba INT, napis TEXT)");

mysqli_query($mysqli, "INSERT INTO tabela (liczba, napis) VALUES (123, 'abc')");

$res = mysqli_query($mysqli, "SELECT * FROM tabela");

while ($row = mysqli_fetch_array($res)) {
    echo $row['napis'] . "\n";
}
?>
