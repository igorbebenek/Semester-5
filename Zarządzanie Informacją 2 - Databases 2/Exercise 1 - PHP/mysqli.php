<?php
$db = new mysqli("localhost", "root", "", "example_db");

if ($db->connect_errno) {
    die("Failed to connect to MySQL: " . $db->connect_error);
}

$db->query("CREATE TABLE IF NOT EXISTS tabela (liczba INT, napis TEXT)");

$db->query("INSERT INTO tabela (liczba, napis) VALUES (123, 'abc')");

$res = $db->query("SELECT * FROM tabela");

while ($row = $res->fetch_array()) {
    echo $row['napis'] . "\n";
}
?>
