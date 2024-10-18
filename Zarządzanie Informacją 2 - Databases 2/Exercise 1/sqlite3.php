<?php
$db = new SQLite3('my.db');

$db->exec("create table if not exists tabela (id integer PRIMARY KEY AUTOINCREMENT, liczba int, napis text)");
$db->exec("insert into tabela (liczba, napis) values (123, 'abc')");

$results = $db->query("SELECT * FROM tabela");

while($row = $results->fetchArray())
{
    echo$row['napis'] . ' - ' . $row['liczba'] . "\n";
}


