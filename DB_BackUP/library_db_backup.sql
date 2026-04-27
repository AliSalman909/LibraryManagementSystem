-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: library_db
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admins`
--

DROP TABLE IF EXISTS `admins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admins` (
  `user_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `employee_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `can_manage_users` tinyint(1) NOT NULL DEFAULT '1',
  `can_view_reports` tinyint(1) NOT NULL DEFAULT '1',
  `can_manage_catalog` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_admins_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admins`
--

LOCK TABLES `admins` WRITE;
/*!40000 ALTER TABLE `admins` DISABLE KEYS */;
INSERT INTO `admins` VALUES ('6NCWFN','Administrator','A-A4553B8F-66B',1,1,1);
/*!40000 ALTER TABLE `admins` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `book_copies`
--

DROP TABLE IF EXISTS `book_copies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book_copies` (
  `copy_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `book_id` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `isbn_code` varchar(96) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `copy_number` int NOT NULL,
  `is_available` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`copy_id`),
  UNIQUE KEY `uq_book_copies_isbn_code` (`isbn_code`),
  UNIQUE KEY `uq_book_copies_book_copy_number` (`book_id`,`copy_number`),
  CONSTRAINT `fk_book_copies_book` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_book_copies_copy_number_positive` CHECK ((`copy_number` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book_copies`
--

LOCK TABLES `book_copies` WRITE;
/*!40000 ALTER TABLE `book_copies` DISABLE KEYS */;
INSERT INTO `book_copies` VALUES ('0ce4f629-33d1-4aa4-a7ed-b66fff8b8417','CWQ25','COMPUT-INFO-8F7603-C015',15,1),('130dbc51-3667-4516-bf50-a009eec693c4','CWQ25','COMPUT-INFO-8F7603-C014',14,1),('222b15f6-b4a3-40e2-8aeb-0c0c60ba2c63','CWQ25','COMPUT-INFO-8F7603-C010',10,1),('23930f57-b29b-47b4-bc0b-1b8380998a39','M7HTU','OBJECT-PROG-905C13-C006',6,1),('24759a70-e64e-42c6-8fd7-e1d39a236bb2','M7HTU','OBJECT-PROG-905C13-C011',11,1),('2a87dd74-0c63-4208-988a-229e859346d5','N4AYX','PROGRA-PROG-B53832-C007',7,1),('2e76406e-4fab-4a85-bab5-8a2b6a4930f5','44VKB','NAMAL-NOVE-CED001-C002',2,1),('302ff7c3-27d8-46ac-99af-9d95c22722f2','44VKB','NAMAL-NOVE-CED001-C012',12,1),('36b84435-747e-44c9-81a6-d0ccdb917239','44VKB','NAMAL-NOVE-CED001-C009',9,1),('39c436b4-9f71-4d80-a6d8-d5576fd95b9d','M7HTU','OBJECT-PROG-905C13-C009',9,1),('3c2fd89d-b04f-44d7-a1df-89a482605487','CWQ25','COMPUT-INFO-8F7603-C003',3,1),('3d2e55d3-ffc0-4fe8-945e-d60a8b68e750','M7HTU','OBJECT-PROG-905C13-C012',12,1),('4107efb6-9902-4cb6-b86b-396217936e09','44VKB','NAMAL-NOVE-CED001-C001',1,1),('420c84a8-2677-49e9-947d-ef85d7d09c7d','N4AYX','PROGRA-PROG-B53832-C001',1,1),('5108d1ea-7f47-40ff-958d-5a619f41881b','SG6LK','LEARNI-PROG-704604-C004',4,1),('5272523d-51db-44b2-93df-6298cf54576a','CWQ25','COMPUT-INFO-8F7603-C011',11,1),('58d0d618-cdb9-4a20-9f78-16f292ea8284','CWQ25','COMPUT-INFO-8F7603-C004',4,1),('6a196e7e-dc9d-410a-be6d-0e273cd6d3e1','M7HTU','OBJECT-PROG-905C13-C002',2,1),('6b1db0fc-0ff7-4f2c-ba2e-a82935beda66','898WH','SPOKEN-LITE-B43ABC-C001',1,1),('6e211886-ca63-4bd6-84f8-65fede2fe70f','M7HTU','OBJECT-PROG-905C13-C004',4,1),('71baf178-3c33-438d-b853-fe497d4a1ae3','CWQ25','COMPUT-INFO-8F7603-C012',12,1),('741733e8-8d46-4f13-9183-729bce6dc8ed','N4AYX','PROGRA-PROG-B53832-C009',9,1),('7529b553-f9b8-423b-921c-a3ae39c6ea9c','SG6LK','LEARNI-PROG-704604-C003',3,1),('7ae55c47-1e52-41eb-bf2f-99c17a472802','N4AYX','PROGRA-PROG-B53832-C003',3,1),('7bcb5818-1764-42c8-8715-7af2ee034ee7','44VKB','NAMAL-NOVE-CED001-C010',10,1),('80d79e20-dfbc-45c2-ad2b-00076c0685d3','M7HTU','OBJECT-PROG-905C13-C007',7,1),('82b3073e-b947-416a-a2c2-52fce3f0dba2','N4AYX','PROGRA-PROG-B53832-C002',2,1),('85348450-7739-49d3-9ebd-8e4f31856f92','CWQ25','COMPUT-INFO-8F7603-C002',2,1),('868cdcc0-f947-4141-9260-56b2177be634','44VKB','NAMAL-NOVE-CED001-C013',13,1),('8763c0d5-849e-40c5-b9a3-d96184613d45','M7HTU','OBJECT-PROG-905C13-C008',8,1),('8c47a08a-319f-48ba-96c3-78e8c3f78df3','M7HTU','OBJECT-PROG-905C13-C010',10,1),('91f37172-733a-46d7-bd66-e8fa16db46cf','N4AYX','PROGRA-PROG-B53832-C010',10,1),('98d5356e-3b32-431d-87d9-86dd6c61a150','M7HTU','OBJECT-PROG-905C13-C005',5,1),('9bca5cea-cc59-4f27-bded-a6be97f24d63','CWQ25','COMPUT-INFO-8F7603-C005',5,1),('a1381033-8da2-4b39-8161-31964471e871','44VKB','NAMAL-NOVE-CED001-C014',14,1),('a4c618df-4615-4cb5-884f-efa6ea09004d','N4AYX','PROGRA-PROG-B53832-C008',8,1),('a512a4dd-7a47-46c9-bc56-5b4b7ecf9220','CWQ25','COMPUT-INFO-8F7603-C016',16,1),('a5a97bb8-fced-44ca-94ae-ab70a9287bd0','SG6LK','LEARNI-PROG-704604-C002',2,1),('a86185f1-b400-4bf7-8329-4f8baae59faf','M7HTU','OBJECT-PROG-905C13-C001',1,1),('ade9f948-ce24-4d5f-9885-43c018648663','CWQ25','COMPUT-INFO-8F7603-C008',8,1),('b2fa78c5-7c78-4864-83a0-dcfe6da84f6a','898WH','SPOKEN-LITE-B43ABC-C002',2,1),('b629997c-0d30-400c-9602-2e48aa4615d9','N4AYX','PROGRA-PROG-B53832-C004',4,1),('bbc39a5c-1673-44db-b6f9-dbfbac76d2e4','44VKB','NAMAL-NOVE-CED001-C005',5,1),('bbc3f28a-67ee-48f7-8fb7-8feb7896f53f','44VKB','NAMAL-NOVE-CED001-C007',7,1),('bca50a27-243c-4b3b-907e-feee5e6494a6','CWQ25','COMPUT-INFO-8F7603-C007',7,1),('bf1de516-0e6a-4aa6-b9c8-a036d8a5486d','CWQ25','COMPUT-INFO-8F7603-C001',1,1),('bfd56739-32a9-4a6a-946e-aeda9554909e','44VKB','NAMAL-NOVE-CED001-C004',4,1),('c40a5ca5-6e33-4f0c-8a2b-e5b000c14f60','44VKB','NAMAL-NOVE-CED001-C003',3,1),('c53c8481-8f99-47a7-9330-847c4b7158b1','CWQ25','COMPUT-INFO-8F7603-C006',6,1),('c9f5c4ab-d163-4b55-989c-ee49cac6113e','CWQ25','COMPUT-INFO-8F7603-C013',13,1),('cfbed5c6-4cac-4143-b846-64c835d04263','CWQ25','COMPUT-INFO-8F7603-C017',17,1),('ddabf545-0d7e-4c32-a7a8-3b56f4da7332','SG6LK','LEARNI-PROG-704604-C001',1,1),('e223be6d-d8d1-456f-ab20-0e5d7620c8c9','WUX93','MACHIN-PROG-5C8E34-C001',1,1),('eb43350f-6a13-4cf2-9174-e8eb9c7c7795','44VKB','NAMAL-NOVE-CED001-C008',8,1),('ec60e99b-054d-495a-973c-9c3a1e842e4a','SG6LK','LEARNI-PROG-704604-C005',5,1),('ee7211fa-2e29-43bb-9226-4cad96c66106','44VKB','NAMAL-NOVE-CED001-C006',6,1),('f29918fe-c5b6-4884-894b-035cebff9596','M7HTU','OBJECT-PROG-905C13-C003',3,1),('f4d98dc4-edd1-4078-b9c9-1d2698bab69d','44VKB','NAMAL-NOVE-CED001-C011',11,1),('f72c1498-d820-444d-8657-51202d39ef62','CWQ25','COMPUT-INFO-8F7603-C018',18,1),('f8b951de-0499-4560-a6a6-6f4657d6820a','CWQ25','COMPUT-INFO-8F7603-C009',9,1),('fde682c4-c85c-4172-91e2-3ca084e51dad','N4AYX','PROGRA-PROG-B53832-C005',5,1),('ff8f3c5f-432c-4033-a6e3-38bb7e8b8adc','N4AYX','PROGRA-PROG-B53832-C006',6,1);
/*!40000 ALTER TABLE `book_copies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `books`
--

DROP TABLE IF EXISTS `books`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `books` (
  `book_id` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `author` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `isbn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_copies` int NOT NULL,
  `available_copies` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `fine_per_day` decimal(10,2) NOT NULL,
  `fine_per_day_pkr` int NOT NULL,
  `max_borrow_days` int NOT NULL DEFAULT '28',
  PRIMARY KEY (`book_id`),
  UNIQUE KEY `uq_books_isbn` (`isbn`),
  UNIQUE KEY `uq_books_title_author_category` (`title`,`author`,`category`),
  CONSTRAINT `chk_books_available_le_total` CHECK ((`available_copies` <= `total_copies`)),
  CONSTRAINT `chk_books_available_nonnegative` CHECK ((`available_copies` >= 0)),
  CONSTRAINT `chk_books_total_copies_positive` CHECK ((`total_copies` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `books`
--

LOCK TABLES `books` WRITE;
/*!40000 ALTER TABLE `books` DISABLE KEYS */;
INSERT INTO `books` VALUES ('44VKB','Namal','Aisha Gul','NAMAL-NOVE-CED001','Novel',14,14,'2026-03-26 04:44:14.245847','2026-04-24 23:48:36.465389',50.00,50,28),('898WH','Spoken English','Manglier','SPOKEN-LITE-B43ABC','Literature',2,2,'2026-03-26 03:06:35.860176','2026-04-25 04:52:15.980384',50.00,50,28),('CWQ25','Computer Networks V2','Mikle Aradan MK','COMPUT-INFO-8F7603','Information Technology_',18,18,'2026-03-26 02:38:26.487278','2026-04-26 21:58:34.881592',150.00,150,14),('M7HTU','Object Oriented Programming','Hallen Walk','OBJECT-PROG-905C13','Programming',12,12,'2026-03-26 02:49:04.324517','2026-04-24 23:48:36.465389',50.00,50,28),('N4AYX','Programming Fundamentals','Hallen Walk','PROGRA-PROG-B53832','Programming',10,10,'2026-03-25 11:48:59.181845','2026-04-24 23:48:36.465389',50.00,50,28),('SG6LK','Learning Through AI','Hallen Walk','LEARNI-PROG-704604','Programming',5,5,'2026-03-25 11:50:16.274121','2026-04-24 23:48:36.465389',50.00,50,28),('WUX93','Machine Learning For Robotics','David Kelson','MACHIN-PROG-5C8E34','Programming',1,1,'2026-04-24 18:27:09.186261','2026-04-24 23:48:36.465389',500.00,500,28);
/*!40000 ALTER TABLE `books` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `borrow_records`
--

DROP TABLE IF EXISTS `borrow_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `borrow_records` (
  `record_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `book_id` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `copy_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `student_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `issued_by_librarian_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `issued_at` datetime(6) NOT NULL,
  `due_date` date NOT NULL,
  `returned_at` datetime(6) DEFAULT NULL,
  `last_renewed_at` datetime(6) DEFAULT NULL,
  `original_due_date` date DEFAULT NULL,
  `renew_count` int NOT NULL,
  `renew_request_pending` bit(1) NOT NULL,
  `renew_requested_at` datetime(6) DEFAULT NULL,
  `renew_requested_days` int DEFAULT NULL,
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uq_borrow_records_request` (`request_id`),
  KEY `fk_borrow_records_copy` (`copy_id`),
  KEY `fk_borrow_records_issued_by` (`issued_by_librarian_id`),
  KEY `idx_borrow_records_student_returned` (`student_id`,`returned_at`),
  KEY `idx_borrow_records_book_returned` (`book_id`,`returned_at`),
  CONSTRAINT `fk_borrow_records_book` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_borrow_records_copy` FOREIGN KEY (`copy_id`) REFERENCES `book_copies` (`copy_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_borrow_records_issued_by` FOREIGN KEY (`issued_by_librarian_id`) REFERENCES `librarians` (`user_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_borrow_records_request` FOREIGN KEY (`request_id`) REFERENCES `borrow_requests` (`request_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_borrow_records_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`user_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `borrow_records`
--

LOCK TABLES `borrow_records` WRITE;
/*!40000 ALTER TABLE `borrow_records` DISABLE KEYS */;
/*!40000 ALTER TABLE `borrow_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `borrow_requests`
--

DROP TABLE IF EXISTS `borrow_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `borrow_requests` (
  `request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `book_id` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `student_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `processed_by_librarian_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('APPROVED','CANCELLED','PENDING','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `requested_at` datetime(6) NOT NULL,
  `reviewed_at` datetime(6) DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `requested_duration_days` int NOT NULL DEFAULT '14',
  PRIMARY KEY (`request_id`),
  KEY `fk_borrow_requests_processed_by_librarian` (`processed_by_librarian_id`),
  KEY `idx_borrow_requests_student_status` (`student_id`,`status`),
  KEY `idx_borrow_requests_book_status` (`book_id`,`status`),
  KEY `idx_borrow_requests_requested_at` (`requested_at`),
  CONSTRAINT `fk_borrow_requests_book` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_borrow_requests_processed_by_librarian` FOREIGN KEY (`processed_by_librarian_id`) REFERENCES `librarians` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_borrow_requests_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_borrow_requests_duration` CHECK ((`requested_duration_days` in (7,14)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `borrow_requests`
--

LOCK TABLES `borrow_requests` WRITE;
/*!40000 ALTER TABLE `borrow_requests` DISABLE KEYS */;
/*!40000 ALTER TABLE `borrow_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `deletion_requests`
--

DROP TABLE IF EXISTS `deletion_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `deletion_requests` (
  `deletion_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `status` enum('approved','pending','rejected') COLLATE utf8mb4_unicode_ci NOT NULL,
  `requested_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `reviewed_at` datetime(6) DEFAULT NULL,
  `admin_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`deletion_id`),
  KEY `fk_deletion_requests_user` (`user_id`),
  CONSTRAINT `fk_deletion_requests_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `deletion_requests`
--

LOCK TABLES `deletion_requests` WRITE;
/*!40000 ALTER TABLE `deletion_requests` DISABLE KEYS */;
/*!40000 ALTER TABLE `deletion_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fines`
--

DROP TABLE IF EXISTS `fines`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fines` (
  `fine_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `days_late` int NOT NULL,
  `issued_at` datetime(6) NOT NULL,
  `notes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `status` enum('PAID','UNPAID','WAIVED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `record_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `resolved_by_librarian_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `student_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `waived_amount` decimal(10,2) NOT NULL,
  PRIMARY KEY (`fine_id`),
  UNIQUE KEY `UKpb7celoohrcfm1aflh3pbsynk` (`record_id`),
  KEY `FKmfi78yy2nxmi226cjbxl4wh5e` (`resolved_by_librarian_id`),
  KEY `FKsgxc21prt442x0tynbtaieyqk` (`student_id`),
  CONSTRAINT `FKmfi78yy2nxmi226cjbxl4wh5e` FOREIGN KEY (`resolved_by_librarian_id`) REFERENCES `librarians` (`user_id`),
  CONSTRAINT `FKqb1ke4ygikmvjr5w73x9y6ngl` FOREIGN KEY (`record_id`) REFERENCES `borrow_records` (`record_id`),
  CONSTRAINT `FKsgxc21prt442x0tynbtaieyqk` FOREIGN KEY (`student_id`) REFERENCES `students` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fines`
--

LOCK TABLES `fines` WRITE;
/*!40000 ALTER TABLE `fines` DISABLE KEYS */;
/*!40000 ALTER TABLE `fines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `librarians`
--

DROP TABLE IF EXISTS `librarians`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `librarians` (
  `user_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `employee_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `can_approve_borrowing` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_librarians_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `librarians`
--

LOCK TABLES `librarians` WRITE;
/*!40000 ALTER TABLE `librarians` DISABLE KEYS */;
INSERT INTO `librarians` VALUES ('7L3AWN','L-0FBD4A34-6FB',1),('AECA87','L-E055503C-A56',1),('GM56JU','L-F4AE7914-5A2',1),('PLLV3U','L-74A4C89F-9C1',1);
/*!40000 ALTER TABLE `librarians` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `notification_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `recipient_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `sender_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` enum('account_approved','account_rejected','account_suspended','deletion_approved','deletion_rejected','deletion_requested') COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `read_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`notification_id`),
  KEY `fk_notifications_recipient` (`recipient_id`),
  KEY `fk_notifications_sender` (`sender_id`),
  CONSTRAINT `fk_notifications_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_notifications_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES ('0b7d3443-8807-4318-b1d0-d13d659a41ab','DNZCTG','6NCWFN','account_approved','Account approved','Your library account (i222330@nu.edu.pk) is now active. You can sign in with your email and password.',0,'2026-03-25 16:41:30.697539',NULL),('4ceedc99-091f-4d13-afb8-0c6eba4a8e26','AECA87','6NCWFN','account_approved','Account approved','Your library account (i222337@nu.edu.pk) is now active. You can sign in with your email and password.',0,'2026-04-24 11:51:27.327916',NULL),('6757e539-43fa-42e1-ab81-b6fe98f0ed7a','4QRTDX','6NCWFN','account_approved','Account approved','Your library account (i222332@nu.edu.pk) is now active. You can sign in with your email and password.',0,'2026-03-25 16:41:36.004628',NULL),('b059f97f-e18a-4489-a216-a500f08b9156','PLLV3U','6NCWFN','account_approved','Account approved','Your library account (i222329@nu.edu.pk) is now active. You can sign in with your email and password.',0,'2026-03-25 16:41:27.948302',NULL),('b7dcb3c6-8d21-4db2-b574-e4b10f28cdeb','GM56JU','6NCWFN','account_approved','Account approved','Your library account (i222328@nu.edu.pk) is now active. You can sign in with your email and password.',0,'2026-03-25 16:41:25.910951',NULL),('fdf66d8d-0921-4897-9734-f94e8b1e805d','XEKLS9','6NCWFN','account_approved','Account approved','Your library account (i222331@nu.edu.pk) is now active. You can sign in with your email and password.',0,'2026-03-25 16:41:32.501105',NULL);
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `registration_requests`
--

DROP TABLE IF EXISTS `registration_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `registration_requests` (
  `request_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('approved','pending','rejected') COLLATE utf8mb4_unicode_ci NOT NULL,
  `submitted_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `reviewed_at` datetime(6) DEFAULT NULL,
  `rejection_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`request_id`),
  UNIQUE KEY `uq_reg_requests_user` (`user_id`),
  CONSTRAINT `fk_reg_requests_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `registration_requests`
--

LOCK TABLES `registration_requests` WRITE;
/*!40000 ALTER TABLE `registration_requests` DISABLE KEYS */;
INSERT INTO `registration_requests` VALUES ('15314369-49dd-41a5-87a1-ed71f8a461a6','4QRTDX','approved','2026-03-25 16:40:16.108665','2026-03-25 11:41:35.997340',NULL),('4137c44b-c152-4dc1-8019-871e7becc453','7L3AWN','pending','2026-04-26 23:42:47.061466',NULL,NULL),('5b0e0704-2428-498e-af26-891a4539c643','2BMFSR','pending','2026-04-26 23:41:36.502672',NULL,NULL),('68625c08-2219-4b90-bcb0-89724e447dc7','DNZCTG','approved','2026-03-25 16:04:02.787627','2026-03-25 11:41:30.687266',NULL),('9fabecc7-ddfd-42b9-9047-76eac5079946','6NCWFN','approved','2026-03-25 15:58:39.343128','2026-03-25 10:58:39.261871',NULL),('d574f2ac-0fa1-47c8-a9c5-d6c6266b5a4a','GM56JU','approved','2026-03-25 15:59:39.345629','2026-03-25 11:41:25.876942',NULL),('dfeeb82b-8123-4114-b965-946989ee8bdc','PLLV3U','approved','2026-03-25 16:00:33.264081','2026-03-25 11:41:27.939995',NULL),('e57d8ade-3e22-496c-b40b-967c3bb8ae2a','XEKLS9','approved','2026-03-25 16:38:25.256007','2026-03-25 11:41:32.489413',NULL),('f8a475d0-f917-4fd4-b6d2-7b260992b678','AECA87','approved','2026-04-24 11:50:48.969014','2026-04-24 06:51:27.308329',NULL);
/*!40000 ALTER TABLE `registration_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservations`
--

DROP TABLE IF EXISTS `reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservations` (
  `reservation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `fulfilled_at` datetime(6) DEFAULT NULL,
  `notified_at` datetime(6) DEFAULT NULL,
  `queue_position` int NOT NULL,
  `status` enum('CANCELLED','EXPIRED','FULFILLED','PENDING','READY') COLLATE utf8mb4_unicode_ci NOT NULL,
  `book_id` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `student_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `requested_duration_days` int NOT NULL,
  PRIMARY KEY (`reservation_id`),
  UNIQUE KEY `uq_reservation_student_book_active` (`student_id`,`book_id`,`status`),
  KEY `FKrsdd3ib3landfpmgoolccjakt` (`book_id`),
  CONSTRAINT `FKnlgg22885nfyspmen9jj0jcpp` FOREIGN KEY (`student_id`) REFERENCES `students` (`user_id`),
  CONSTRAINT `FKrsdd3ib3landfpmgoolccjakt` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations`
--

LOCK TABLES `reservations` WRITE;
/*!40000 ALTER TABLE `reservations` DISABLE KEYS */;
/*!40000 ALTER TABLE `reservations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students` (
  `user_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `student_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `program` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `enrollment_date` date DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `max_borrow_limit` int NOT NULL DEFAULT '3',
  `can_borrow` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_students_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_students_borrow_limit` CHECK (((`max_borrow_limit` > 0) and (`max_borrow_limit` <= 10)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `students`
--

LOCK TABLES `students` WRITE;
/*!40000 ALTER TABLE `students` DISABLE KEYS */;
INSERT INTO `students` VALUES ('2BMFSR','S-BAC47C2C-183','Cyber Security','2022-08-22','2006-06-24',3,1),('4QRTDX','S-18AF33BA-E03','Cyber Security','2022-08-27','2006-12-23',3,1),('DNZCTG','S-55D1E318-6AF','AI','2022-06-20','2004-02-28',3,1),('XEKLS9','S-B5B929F4-7B5','Computer Science','2022-07-23','2005-03-17',3,1);
/*!40000 ALTER TABLE `students` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_role` enum('ADMIN','LIBRARIAN','STUDENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `account_status` enum('active','deleted','deletion_pending','pending','rejected','suspended') COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `profile_picture` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `profile_picture_focal_x` double DEFAULT NULL,
  `profile_picture_focal_y` double DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `last_login_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uq_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('2BMFSR','Rehman Aslam','i222338@nu.edu.pk','E1:8AV3vq/mn0MrZWnCgmt2C3uAWu6RpR4MZYskZj73iXNMXuB2Syo=','STUDENT','pending','03110433521','/uploads/profiles/1.jpg',49.56,50.66,'2026-04-26 23:41:36.490222','2026-04-27 19:21:31.532596',NULL),('4QRTDX','Yaqoob','i222332@nu.edu.pk','E1:+zzYK9/PtNG9k2xUvN/Jm4Fp684ZOzpyiAWzNq4JAO2BShKgoJ8=','STUDENT','active','03110433560','/uploads/profiles/2.jpg',50,50,'2026-03-25 16:40:16.097432','2026-04-27 19:21:49.845162','2026-04-27 10:17:41.752870'),('6NCWFN','Mohammad Rohaan','i222327@nu.edu.pk','E1:xMdPZJRjxQC/rz2msHma+oGWoV2BYtuAaNVdWoE9I+Q/YT4E','ADMIN','active','03110433555','/uploads/profiles/3.jpg',50,50,'2026-03-25 15:58:39.325709','2026-04-27 19:22:56.505578','2026-04-27 14:22:56.462957'),('7L3AWN','Zain Asghar','i222387@nu.edu.pk','E1:H2uPGMFCV/UuNJUGUhC7oflOfbXDb9LFI6QhjJxrP3/e7SoZsF+rqg==','LIBRARIAN','pending','03232273594','/uploads/profiles/4.jpg',50,50,'2026-04-26 23:42:47.057016','2026-04-27 19:22:33.606158',NULL),('AECA87','Muhammad Farzeen Tareen Afzal','i222337@nu.edu.pk','E1:sDrmiolcb8aSss11zVqb3sVbTMQnqqPQNQYLJAlWb4RktxNPfsziig==','LIBRARIAN','active',NULL,NULL,NULL,NULL,'2026-04-24 11:50:48.961429','2026-04-27 02:21:51.402437','2026-04-26 21:21:51.398429'),('DNZCTG','Ahmed','i222330@nu.edu.pk','E1:7Ea++ZLl6UxlBSejMOOZB/0GdpMuDEBJ84DKzJ60P5bC/oBxmUc=','STUDENT','active','03110433558','/uploads/profiles/DNZCTG-607cb3af-99ee-4e35-b0d9-2b394772ca39.jpg',50,50,'2026-03-25 16:04:02.782913','2026-04-24 11:34:06.417691','2026-04-24 06:34:06.413400'),('GM56JU','Bilal','i222328@nu.edu.pk','E1:l1flE0KXSy3ExeJb/s3mZi99NCbwvZl8vHrfrp7PNqlK5NporH8JoA==','LIBRARIAN','active','03110433556','/uploads/profiles/GM56JU-255a16f6-6a1b-433a-9b7d-11422271a1b2.jpg',27.12,54.18,'2026-03-25 15:59:39.340954','2026-04-27 15:19:17.593805','2026-04-27 10:19:17.588489'),('PLLV3U','Ali','i222329@nu.edu.pk','E1:IwaVkiu3Qcisu3y1vcC19Zz31sakmiChhTjmd7I2PTf1PTFyfmVViQ==','LIBRARIAN','active','03110433557','/uploads/profiles/PLLV3U-f1a3b220-a565-4244-b6a9-40647159a552.jpg',50,50,'2026-03-25 16:00:33.260242','2026-04-24 10:59:06.561869','2026-04-24 05:59:06.559444'),('XEKLS9','Mikle','i222331@nu.edu.pk','E1:yp/ReGpx30GYHSw3GyDk7sW02GhKEqfoi2ntRtlJqVWv68l8JZ8=','STUDENT','active','03110433559','/uploads/profiles/XEKLS9-363f062a-24c7-48a6-b709-8c4fc90896b9.jpg',79.7,49.12,'2026-03-25 16:38:25.238050','2026-04-24 02:04:18.871833','2026-04-23 21:04:18.775937');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'library_db'
--

--
-- Dumping routines for database 'library_db'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-27 19:22:56
