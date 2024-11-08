<?php
require_once __DIR__ . '/vendor/autoload.php';

use Doctrine\DBAL\DriverManager;
use Doctrine\DBAL\Schema\Table;
$params = ['dbname' => 'lab4', 'user' => 'root', 'password' => '', 'host' => 'localhost', 'port' => 3306, 'driver' => 'pdo_mysql'];

$conn = DriverManager::getConnection($params);

$queryBuilder = $conn->createQueryBuilder();
$queryBuilder->select('customerName', 'creditLimit')->from('customers')->where('country = :country')->setParameter('country', 'USA')->orderBy('creditLimit', 'DESC');

$results = $queryBuilder->executeQuery()->fetchAllAssociative();

echo "Posortowane malejaco wartosci limitu kredytowego:\n";
foreach ($results as $row) {
    echo $row['customerName'] . " - " . $row['creditLimit'] . "\n";
}

$queryBuilder = $conn->createQueryBuilder();
$queryBuilder
    ->select('c.customerNumber', 'c.customerName', 'e.firstName', 'e.lastName')->from('customers', 'c')->leftJoin('c', 'employees', 'e',
        'c.salesRepEmployeeNumber = e.employeeNumber');

$results = $queryBuilder->executeQuery()->fetchAllAssociative();

echo "Klienci i opiekunowie handlowi:\n";
foreach ($results as $row) {
    echo $row['customerNumber'] . " - " . $row['customerName'] . " - " . $row['firstName'] . " " . $row['lastName'] . "\n";
}

$schemaManager = $conn->createSchemaManager();
$table = new Table('nowa_tabela');
$table->addColumn('id', 'integer', ['autoincrement' => true]);
$table->addColumn('napis', 'string', ['length' => 255]);
$table->addColumn('liczba', 'integer');
$table->setPrimaryKey(['id']);
$schemaManager->createTable($table);

$conn->insert('nowa_tabela', ['napis' => 'Wpis jeden', 'liczba' => 999]);
$conn->insert('nowa_tabela', ['napis' => 'Wpis dwa', 'liczba' => 888]);

