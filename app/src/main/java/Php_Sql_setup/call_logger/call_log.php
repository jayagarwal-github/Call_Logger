<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

$conn = new mysqli("127.0.0.1", "root", "", "call_logger_db", 3308);
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["status" => "error", "message" => "DB connection failed"]);
    exit;
}

$input = json_decode(file_get_contents("php://input"), true);

if (!is_array($input)) {
    echo json_encode(["status" => "error", "message" => "Invalid JSON format"]);
    exit;
}

$stmt = $conn->prepare("INSERT INTO call_logs (date, time, type, device_number, client_number, ring_duration, call_duration) VALUES (?, ?, ?, ?, ?, ?, ?)");

if (!$stmt) {
    echo json_encode(["status" => "error", "message" => "Prepare failed: " . $conn->error]);
    exit;
}

foreach ($input as $log) {
    if (
        !isset($log['date'], $log['time'], $log['type'], $log['device_number'],
        $log['client_number'], $log['ring_duration'], $log['call_duration'])
    ) {
        continue;
    }

    $stmt->bind_param(
        "ssssssi",
        $log['date'],
        $log['time'],
        $log['type'],
        $log['device_number'],
        $log['client_number'],
        $log['ring_duration'],
        $log['call_duration']
    );
    $stmt->execute();
}

$stmt->close();
$conn->close();

echo json_encode(["status" => "success", "message" => "Logs inserted"]);
?>
