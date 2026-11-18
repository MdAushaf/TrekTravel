-- =====================================================================
-- TrekTravel Database Schema & Seed Data
-- Tagline: Climb Higher. Travel Smarter.
-- Target RDBMS: MySQL 8.0+ (InnoDB, UTF-8 utf8mb4)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS trektravel CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE trektravel;

-- Disable foreign key checks for safe schema recreation
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS batches;
DROP TABLE IF EXISTS treks;
DROP TABLE IF EXISTS trekker_profiles;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- 1. USERS TABLE
-- Core accounts for Trekkers, Trek Organizers, and System Admins
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role ENUM('TREKKER', 'ORGANIZER', 'ADMIN') NOT NULL,
    status ENUM('ACTIVE', 'BLOCKED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role),
    INDEX idx_user_status (status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. TREKKER_PROFILES TABLE
-- Trekker personal & emergency details, fitness profile, and Govt-ID verification
-- ---------------------------------------------------------------------
CREATE TABLE trekker_profiles (
    profile_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    date_of_birth DATE NOT NULL,
    city VARCHAR(100) NOT NULL,
    emergency_contact_name VARCHAR(100) NOT NULL,
    emergency_contact_phone VARCHAR(20) NOT NULL,
    fitness_level ENUM('BEGINNER', 'INTERMEDIATE', 'EXPERIENCED') NOT NULL,
    medical_notes VARCHAR(255) NULL,
    id_proof_file VARCHAR(100) NOT NULL,
    verification_status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    admin_remarks VARCHAR(255) NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL,
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_profile_status (verification_status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. TREKS TABLE
-- Trek listings curated and managed by Organizers; require Admin approval
-- ---------------------------------------------------------------------
CREATE TABLE treks (
    trek_id INT AUTO_INCREMENT PRIMARY KEY,
    organizer_id INT NOT NULL,
    name VARCHAR(150) NOT NULL,
    state VARCHAR(100) NOT NULL,
    region VARCHAR(100) NOT NULL,
    base_village VARCHAR(100) NOT NULL,
    difficulty ENUM('EASY', 'MODERATE', 'DIFFICULT') NOT NULL,
    duration_days INT NOT NULL,
    max_altitude_m INT NOT NULL,
    trek_distance_km DECIMAL(5, 1) NOT NULL,
    best_season VARCHAR(80) NOT NULL,
    description TEXT NOT NULL,
    highlights VARCHAR(255) NOT NULL,
    inclusions VARCHAR(255) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trek_organizer FOREIGN KEY (organizer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_trek_duration CHECK (duration_days >= 1 AND duration_days <= 30),
    CONSTRAINT chk_trek_altitude CHECK (max_altitude_m >= 0),
    CONSTRAINT chk_trek_distance CHECK (trek_distance_km >= 0.0),
    INDEX idx_trek_state_status (state, status),
    INDEX idx_trek_organizer (organizer_id),
    INDEX idx_trek_status (status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4. BATCHES TABLE
-- Specific scheduled departure dates, pricing, verified trekker discount, and slots
-- ---------------------------------------------------------------------
CREATE TABLE batches (
    batch_id INT AUTO_INCREMENT PRIMARY KEY,
    trek_id INT NOT NULL,
    start_date DATE NOT NULL,
    price_per_person DECIMAL(10, 2) NOT NULL,
    verified_discount_percent INT NOT NULL DEFAULT 0,
    total_slots INT NOT NULL DEFAULT 15,
    status ENUM('OPEN', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    CONSTRAINT fk_batch_trek FOREIGN KEY (trek_id) REFERENCES treks(trek_id) ON DELETE CASCADE,
    CONSTRAINT chk_batch_discount CHECK (verified_discount_percent >= 0 AND verified_discount_percent <= 50),
    CONSTRAINT chk_batch_slots CHECK (total_slots >= 1 AND total_slots <= 50),
    CONSTRAINT chk_batch_price CHECK (price_per_person >= 0.00),
    INDEX idx_batch_trek (trek_id),
    INDEX idx_batch_start_date (start_date),
    INDEX idx_batch_status (status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 5. BOOKINGS TABLE
-- Trek slot reservations with snapshot pricing and locked inventory
-- ---------------------------------------------------------------------
CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    batch_id INT NOT NULL,
    num_participants INT NOT NULL,
    price_per_person DECIMAL(10, 2) NOT NULL,
    discount_percent INT NOT NULL DEFAULT 0,
    total_amount DECIMAL(12, 2) NOT NULL,
    status ENUM('CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_batch FOREIGN KEY (batch_id) REFERENCES batches(batch_id) ON DELETE CASCADE,
    CONSTRAINT chk_booking_participants CHECK (num_participants >= 1 AND num_participants <= 10),
    CONSTRAINT chk_booking_amount CHECK (total_amount >= 0.00),
    INDEX idx_booking_batch_status (batch_id, status),
    INDEX idx_booking_user (user_id),
    INDEX idx_booking_status (status)
) ENGINE=InnoDB;

-- =====================================================================
-- SAMPLE SEED DATA
-- Passwords:
-- Admin:      Admin@123
-- Organizers: Organizer@123
-- Trekkers:   Trekker@123
-- Salt:       d8578edf8458ce06fbc5bb76a58c5ca4
-- =====================================================================

-- 1. Insert Users
INSERT INTO users (id, name, email, password_hash, salt, phone, role, status) VALUES
(1, 'System Administrator', 'admin@trektravel.com',     'pbkdf2:120000:Y6mKOU46SeMG5UsTWoD2OA==:jQBF3C1RJxfv063gyy2yxa8sHA+O+N+uP9TeyZGEYks=', '', '9876543210', 'ADMIN',     'ACTIVE'),
(2, 'Himalayan Trails Co.', 'organizer@trektravel.com', 'pbkdf2:120000:c7BQFd4wEZ0peIhYCt2VlQ==:5ODzROd1ZVxU5a2G6T7O1p6ncsU1U4MlrTrhYexcxXo=', '', '9876543211', 'ORGANIZER', 'ACTIVE'),
(3, 'Summit Adventures',    'organizer2@trektravel.com','pbkdf2:120000:c7BQFd4wEZ0peIhYCt2VlQ==:5ODzROd1ZVxU5a2G6T7O1p6ncsU1U4MlrTrhYexcxXo=', '', '9876543212', 'ORGANIZER', 'ACTIVE'),
(4, 'Rahul Sharma',         'trekker@trektravel.com',   'pbkdf2:120000:hOAJXBSG7fjy5RFirBE4PQ==:cLUewSCJXrECbHHK0LBcxzBzjFVwFc+JVwUVpy1Tz1U=', '', '9876543213', 'TREKKER',   'ACTIVE'),
(5, 'Priya Patel',          'newbie@trektravel.com',    'pbkdf2:120000:hOAJXBSG7fjy5RFirBE4PQ==:cLUewSCJXrECbHHK0LBcxzBzjFVwFc+JVwUVpy1Tz1U=', '', '9876543214', 'TREKKER',   'ACTIVE'),
(6, 'Amit Verma',           'fresh@trektravel.com',     'pbkdf2:120000:hOAJXBSG7fjy5RFirBE4PQ==:cLUewSCJXrECbHHK0LBcxzBzjFVwFc+JVwUVpy1Tz1U=', '', '9876543215', 'TREKKER',   'ACTIVE');

-- 2. Insert Trekker Profiles
-- User 4 has an APPROVED profile (unlocks Verified Trekker discounts)
INSERT INTO trekker_profiles (profile_id, user_id, date_of_birth, city, emergency_contact_name, emergency_contact_phone, fitness_level, medical_notes, id_proof_file, verification_status, admin_remarks, submitted_at, verified_at) VALUES
(1, 4, '1998-05-15', 'Dehradun', 'Sunil Sharma (Father)', '9876543299', 'EXPERIENCED', 'No known allergies or medical issues.', 'sample_id_rahul.pdf', 'APPROVED', 'Government Aadhaar Verified successfully.', NOW(), NOW()),
-- User 5 has a PENDING profile (under Admin review)
(2, 5, '2002-11-20', 'Pune', 'Kavita Patel (Mother)', '9876543288', 'INTERMEDIATE', 'Mild exercise-induced asthma; carries personal inhaler.', 'sample_id_priya.jpg', 'PENDING', NULL, NOW(), NULL);

-- 3. Insert Treks (7 APPROVED, 1 PENDING)
INSERT INTO treks (trek_id, organizer_id, name, state, region, base_village, difficulty, duration_days, max_altitude_m, trek_distance_km, best_season, description, highlights, inclusions, status) VALUES
(1, 2, 'Kedarkantha Winter Trek', 'Uttarakhand', 'Garhwal Himalaya', 'Sankri', 'EASY', 6, 3810, 20.0, 'December to April', 
 'Kedarkantha is India’s most beloved winter snow trek. Rising from the scenic hamlet of Sankri, the trail winds through dense pine forests, frozen Juda Ka Talab lake, and culminates in a dramatic 360-degree summit sunrise over the Swargarohini and Bandarpoonch peaks.',
 'Juda Ka Talab campsite, 360-degree summit panorama, stunning snow trails, dense pine forests',
 'Certified Trek Leader, microspikes & gaiters, all meals (veg), tent stay, forest permits & insurance', 'APPROVED'),

(2, 2, 'Hampta Pass & Chandratal', 'Himachal Pradesh', 'Pir Panjal Range', 'Jobra', 'MODERATE', 5, 4270, 25.0, 'June to October',
 'A breathtaking crossover trek that connects the lush green alpine meadows of Kullu Valley to the stark, arid, dramatic moonscapes of Spiti Valley. Features an excursion to the turquoise holy waters of high-altitude Chandratal Lake.',
 'Dramatic valley crossover, Jobra pine forests, Shea Goru river crossing, Chandratal Lake visit',
 'Experienced mountain guide, camping equipment, nutritious mountain meals, Spiti transfer, permits', 'APPROVED'),

(3, 2, 'Triund Ridge Weekend Trek', 'Himachal Pradesh', 'Kangra Valley', 'McLeod Ganj', 'EASY', 2, 2850, 9.0, 'All Year Round',
 'The crown jewel of Dharamshala, Triund offers an accessible weekend ridge trek right under the imposing granite walls of the Dhauladhar range on one side and the expansive Kangra valley on the other.',
 'Close-up view of Dhauladhar snowline, sunset over Kangra valley, star-filled night camping',
 'Camp stay on ridge, dinner & breakfast, experienced local trek escort, environmental fees', 'APPROVED'),

(4, 2, 'Valley of Flowers & Hemkund Sahib', 'Uttarakhand', 'Chamoli Garhwal', 'Govindghat', 'MODERATE', 6, 4389, 38.0, 'July to September',
 'A UNESCO World Heritage National Park blooming with over 500 species of rare sub-alpine wildflowers, medicinal herbs, and exotic butterflies, paired with the pilgrimage ascent to the crystalline high-altitude lake of Hemkund Sahib.',
 'UNESCO World Heritage flora, Pushpawati river gorge, glacial Hemkund Sahib lake, Brahmakamal blossoms',
 'Guesthouse accommodation, all vegetarian meals, certified guides, National Park entry entry fee', 'APPROVED'),

(5, 3, 'Kheerganga Hot Springs Trek', 'Himachal Pradesh', 'Parvati Valley', 'Barshaini', 'EASY', 2, 2960, 12.0, 'March to November',
 'A refreshing trek following the gushing Parvati River through pine woods, passing charming wooden villages of Nakthan and Rudranag waterfalls, ending with a soothing dip in the natural sulfur hot springs at Kheerganga.',
 'Natural geothermal sulfur bath, Rudranag waterfall, scenic Parvati valley views, cozy alpine camping',
 'Dome tent stay, dinner and mountain breakfast, trek marshal, campfire, local eco-cess', 'APPROVED'),

(6, 3, 'Sandakphu Phalut Trek', 'West Bengal', 'Singalila Ridge', 'Manebhanjan', 'MODERATE', 6, 3636, 46.0, 'October to April',
 'The only trek in the world offering views of four of the five highest peaks on Earth: Everest, Kanchenjunga, Lhotse, and Makalu, formed in the sacred shape of the Sleeping Buddha.',
 'Sleeping Buddha panorama, 4 highest 8000m peaks, Singalila National Park, red panda habitat',
 'Traditional tea-house stays, all meals, West Bengal forest entry permits, experienced ridge guide', 'APPROVED'),

(7, 3, 'Har Ki Dun Valley of Gods', 'Uttarakhand', 'Rawai Valley', 'Sankri', 'MODERATE', 7, 3566, 47.0, 'April to June, September to December',
 'A mythical cradle-shaped valley steeped in Mahabharata folklore along the Supin River, surrounded by towering peaks of Jaundhar Glacier and Swargarohini. Rich in centuries-old wooden architecture and alpine fauna.',
 'Supin river trail, ancient Osla village temple, Swargarohini glacier view, pristine pine forests',
 'Tent and lodge stays, all meals, technical equipment, wilderness first-aid kit, certified trek lead', 'APPROVED'),

(8, 3, 'Dodital Lake & Darwa Top', 'Uttarakhand', 'Uttarkashi', 'Sangamchatti', 'DIFFICULT', 5, 3024, 22.0, 'May to November',
 'Believed to be the sacred birthplace of Lord Ganesha, Dodital is a tranquil emerald-green lake cradled by thick oak and rhododendron forests, famous for Himalayan Golden Trout and steep ridge ascent to Darwa Pass.',
 'Sacred emerald lake, Darwa Pass alpine meadow, Golden Trout spotting, snow-capped Bandarpoonch views',
 'All camping gear, meals, fishing permit coordination, local guide, porter support', 'PENDING');

-- 4. Insert Batches (2 to 3 future departure batches per approved trek)
-- Start dates relative to today using DATE_ADD(CURDATE(), INTERVAL X DAY)
INSERT INTO batches (batch_id, trek_id, start_date, price_per_person, verified_discount_percent, total_slots, status) VALUES
-- Kedarkantha (Trek 1)
(1, 1, DATE_ADD(CURDATE(), INTERVAL 14 DAY), 8500.00, 15, 20, 'OPEN'),
(2, 1, DATE_ADD(CURDATE(), INTERVAL 28 DAY), 8500.00, 15, 20, 'OPEN'),
(3, 1, DATE_ADD(CURDATE(), INTERVAL 42 DAY), 9200.00, 10, 18, 'OPEN'),

-- Hampta Pass (Trek 2)
(4, 2, DATE_ADD(CURDATE(), INTERVAL 20 DAY), 10500.00, 20, 16, 'OPEN'),
(5, 2, DATE_ADD(CURDATE(), INTERVAL 35 DAY), 10500.00, 20, 16, 'OPEN'),

-- Triund Ridge (Trek 3)
(6, 3, DATE_ADD(CURDATE(), INTERVAL 7 DAY),  2200.00, 10, 25, 'OPEN'),
(7, 3, DATE_ADD(CURDATE(), INTERVAL 14 DAY), 2200.00, 10, 25, 'OPEN'),
(8, 3, DATE_ADD(CURDATE(), INTERVAL 21 DAY), 2500.00, 10, 25, 'OPEN'),

-- Valley of Flowers (Trek 4)
(9, 4, DATE_ADD(CURDATE(), INTERVAL 25 DAY), 11800.00, 15, 18, 'OPEN'),
(10, 4, DATE_ADD(CURDATE(), INTERVAL 40 DAY), 11800.00, 15, 18, 'OPEN'),

-- Kheerganga (Trek 5)
(11, 5, DATE_ADD(CURDATE(), INTERVAL 10 DAY), 2800.00, 10, 24, 'OPEN'),
(12, 5, DATE_ADD(CURDATE(), INTERVAL 24 DAY), 2800.00, 10, 24, 'OPEN'),

-- Sandakphu Phalut (Trek 6)
(13, 6, DATE_ADD(CURDATE(), INTERVAL 18 DAY), 12500.00, 15, 15, 'OPEN'),
(14, 6, DATE_ADD(CURDATE(), INTERVAL 32 DAY), 12500.00, 15, 15, 'OPEN'),

-- Har Ki Dun (Trek 7)
(15, 7, DATE_ADD(CURDATE(), INTERVAL 22 DAY), 11200.00, 20, 18, 'OPEN'),
(16, 7, DATE_ADD(CURDATE(), INTERVAL 38 DAY), 11200.00, 20, 18, 'OPEN'),

-- Dodital Lake (Trek 8 - Pending trek with upcoming batch)
(17, 8, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 9800.00, 15, 16, 'OPEN');

-- 5. Insert Sample Confirmed Booking
-- Rahul Sharma (User 4, Verified Trekker with 15% discount) on Kedarkantha Batch 1:
-- Price: 8500 * 2 participants = 17000. 15% discount = 2550. Final Total = 14450.
INSERT INTO bookings (booking_id, user_id, batch_id, num_participants, price_per_person, discount_percent, total_amount, status) VALUES
(1, 4, 1, 2, 8500.00, 15, 14450.00, 'CONFIRMED');
