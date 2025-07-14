-- Tạo bảng phòng ban
CREATE TABLE departments (
    department_id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_used BIT NOT NULL DEFAULT 1
);

-- Tạo bảng phòng họp
CREATE TABLE rooms (
    room_id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255),
    capacity INT,
    is_used BIT NOT NULL DEFAULT 1
);

-- Tạo bảng người dùng
CREATE TABLE users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    keycloak_id VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    firstname VARCHAR(100),
    lastname VARCHAR(100),
    dob DATE,
    email VARCHAR(100),
    department_id INT,
    FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

-- Tạo bảng lịch
CREATE TABLE schedules (
    schedule_id INT IDENTITY(1,1) PRIMARY KEY,
    title VARCHAR(255),
    type VARCHAR(20) CHECK (type IN ('ONLINE', 'OFFLINE')),
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    room_id INT,
    created_by INT NOT NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(room_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- Tạo bảng trung gian participants
CREATE TABLE participants (
    schedule_id INT,
    user_id INT,
    PRIMARY KEY (schedule_id, user_id),
    FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
