<?php
$mysqli = new mysqli("investorsentiment.ydns.eu", "root", "projecth", "investorsentiment");
if ($mysqli->connect_errno)
    die("Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
$query = "SELECT `date`, `sentiment`, `volume` FROM `scores` WHERE `stock` = ? ORDER BY `date` DESC";
$stmt = $mysqli->stmt_init();
if (!$stmt->prepare($query))
	die("Statement preparing failed: (" . $stmt->errno . ") " . $stmt->error);
if (!$stmt->bind_param("s", $stock))
	die("Parameter binding failed: (" . $stmt->errno . ") " . $stmt->error);;
$stock = $_GET["stock"];
if (!$stmt->execute())
	die("Statement execution failed: (" . $stmt->errno . ") " . $stmt->error);;
if (!($result = $stmt->get_result()))
	die("Result fetching failed: (" . $stmt->errno . ") " . $stmt->error);
print json_encode($result->fetch_all(MYSQLI_ASSOC));
$stmt->close();
$mysqli->close();