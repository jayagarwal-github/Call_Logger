# 📞 Android Call Logger App

An Android app that reads device call logs and sends them to a remote PHP + MySQL backend. It also includes a simple web interface to view logs using HTML + JavaScript.

---

## ✨ Features

- ✅ Read device call logs (incoming, outgoing, missed)
- ✅ Send logs to a remote server (PHP backend)
- ✅ Store logs in a MySQL database (via XAMPP)
- ✅ View logs in a responsive HTML table
- ✅ Filter by call type (Incoming, Outgoing, Missed)
- ✅ Sort logs by date, type, number, or duration

---

## 📱 Android App

### 🔧 Tech Used
- Kotlin + Jetpack Compose
- Retrofit for HTTP networking
- ContentResolver for accessing call logs
- Material 3 UI
- Permission handling

### 📦 Required Permissions
- `READ_CALL_LOG`
- `READ_PHONE_STATE`
- `READ_SMS` *(to access device number on some devices)*

---

## 🖥️ Backend Setup (PHP + MySQL)

### 1. 📦 Tools Required
- [XAMPP](https://www.apachefriends.org/)
- MySQL database
- PHP 8+

### 2. 📂 Files

- `call_log.php`: Accepts logs from Android app and inserts them into DB
- `log_call_fetch.php`: Returns all logs as JSON

### 3. 🗄️ MySQL Table

```sql
CREATE TABLE call_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date DATE,
    time TIME,
    type VARCHAR(20),
    device_number VARCHAR(20),
    client_number VARCHAR(20),
    ring_duration INT,
    call_duration INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
---

## 🌐 Web Dashboard

### 📄 File: `index.html`
A simple page to view the stored call logs from the database.

- ✅ Shows all logs in a table  
- ✅ Filters by call type  
- ✅ Sorts by any column  

---

### 🔗 How to Run

1. Place `index.html` in your project or open it directly in the browser  
2. Ensure XAMPP Apache server is running  
3. Logs are loaded from: `http://localhost/log_call_fetch.php`

---

## 🚀 How to Use

1. ✅ Install and run the Android app on your device  
2. ✅ Tap **“Read Logs”** to load logs from the device  
3. ✅ Tap **“Send Logs”** to upload to the backend  
4. ✅ Open `index.html` in a browser to view logs

---

