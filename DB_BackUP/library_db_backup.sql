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
INSERT INTO `book_copies` VALUES ('0ce4f629-33d1-4aa4-a7ed-b66fff8b8417','CWQ25','COMPUT-INFO-FD4502-C015',15,1),('130dbc51-3667-4516-bf50-a009eec693c4','CWQ25','COMPUT-INFO-FD4502-C014',14,1),('222b15f6-b4a3-40e2-8aeb-0c0c60ba2c63','CWQ25','COMPUT-INFO-FD4502-C010',10,1),('23930f57-b29b-47b4-bc0b-1b8380998a39','M7HTU','OBJECT-PROG-905C13-C006',6,1),('24759a70-e64e-42c6-8fd7-e1d39a236bb2','M7HTU','OBJECT-PROG-905C13-C011',11,1),('2a87dd74-0c63-4208-988a-229e859346d5','N4AYX','PROGRA-PROG-B53832-C007',7,1),('2e76406e-4fab-4a85-bab5-8a2b6a4930f5','44VKB','NAMAL-NOVE-CED001-C002',2,1),('302ff7c3-27d8-46ac-99af-9d95c22722f2','44VKB','NAMAL-NOVE-CED001-C012',12,1),('36b84435-747e-44c9-81a6-d0ccdb917239','44VKB','NAMAL-NOVE-CED001-C009',9,1),('39c436b4-9f71-4d80-a6d8-d5576fd95b9d','M7HTU','OBJECT-PROG-905C13-C009',9,1),('3c2fd89d-b04f-44d7-a1df-89a482605487','CWQ25','COMPUT-INFO-FD4502-C003',3,1),('3d2e55d3-ffc0-4fe8-945e-d60a8b68e750','M7HTU','OBJECT-PROG-905C13-C012',12,1),('4107efb6-9902-4cb6-b86b-396217936e09','44VKB','NAMAL-NOVE-CED001-C001',1,1),('420c84a8-2677-49e9-947d-ef85d7d09c7d','N4AYX','PROGRA-PROG-B53832-C001',1,1),('5108d1ea-7f47-40ff-958d-5a619f41881b','SG6LK','LEARNI-PROG-704604-C004',4,1),('5272523d-51db-44b2-93df-6298cf54576a','CWQ25','COMPUT-INFO-FD4502-C011',11,1),('58d0d618-cdb9-4a20-9f78-16f292ea8284','CWQ25','COMPUT-INFO-FD4502-C004',4,1),('6a196e7e-dc9d-410a-be6d-0e273cd6d3e1','M7HTU','OBJECT-PROG-905C13-C002',2,1),('6b1db0fc-0ff7-4f2c-ba2e-a82935beda66','898WH','SPOKEN-LITE-B43ABC-C001',1,0),('6e211886-ca63-4bd6-84f8-65fede2fe70f','M7HTU','OBJECT-PROG-905C13-C004',4,1),('71baf178-3c33-438d-b853-fe497d4a1ae3','CWQ25','COMPUT-INFO-FD4502-C012',12,1),('741733e8-8d46-4f13-9183-729bce6dc8ed','N4AYX','PROGRA-PROG-B53832-C009',9,1),('7529b553-f9b8-423b-921c-a3ae39c6ea9c','SG6LK','LEARNI-PROG-704604-C003',3,1),('7ae55c47-1e52-41eb-bf2f-99c17a472802','N4AYX','PROGRA-PROG-B53832-C003',3,1),('7bcb5818-1764-42c8-8715-7af2ee034ee7','44VKB','NAMAL-NOVE-CED001-C010',10,1),('80d79e20-dfbc-45c2-ad2b-00076c0685d3','M7HTU','OBJECT-PROG-905C13-C007',7,1),('82b3073e-b947-416a-a2c2-52fce3f0dba2','N4AYX','PROGRA-PROG-B53832-C002',2,1),('85348450-7739-49d3-9ebd-8e4f31856f92','CWQ25','COMPUT-INFO-FD4502-C002',2,0),('868cdcc0-f947-4141-9260-56b2177be634','44VKB','NAMAL-NOVE-CED001-C013',13,1),('8763c0d5-849e-40c5-b9a3-d96184613d45','M7HTU','OBJECT-PROG-905C13-C008',8,1),('8c47a08a-319f-48ba-96c3-78e8c3f78df3','M7HTU','OBJECT-PROG-905C13-C010',10,1),('91f37172-733a-46d7-bd66-e8fa16db46cf','N4AYX','PROGRA-PROG-B53832-C010',10,1),('96375a84-795f-4cd6-a4ef-eae1686188b3','L8JE4','DEVOPP-PARA-87EF49-C001',1,1),('98d5356e-3b32-431d-87d9-86dd6c61a150','M7HTU','OBJECT-PROG-905C13-C005',5,1),('9bca5cea-cc59-4f27-bded-a6be97f24d63','CWQ25','COMPUT-INFO-FD4502-C005',5,1),('a1381033-8da2-4b39-8161-31964471e871','44VKB','NAMAL-NOVE-CED001-C014',14,1),('a4c618df-4615-4cb5-884f-efa6ea09004d','N4AYX','PROGRA-PROG-B53832-C008',8,1),('a512a4dd-7a47-46c9-bc56-5b4b7ecf9220','CWQ25','COMPUT-INFO-FD4502-C016',16,1),('a5a97bb8-fced-44ca-94ae-ab70a9287bd0','SG6LK','LEARNI-PROG-704604-C002',2,1),('a86185f1-b400-4bf7-8329-4f8baae59faf','M7HTU','OBJECT-PROG-905C13-C001',1,0),('ade9f948-ce24-4d5f-9885-43c018648663','CWQ25','COMPUT-INFO-FD4502-C008',8,1),('b2fa78c5-7c78-4864-83a0-dcfe6da84f6a','898WH','SPOKEN-LITE-B43ABC-C002',2,1),('b629997c-0d30-400c-9602-2e48aa4615d9','N4AYX','PROGRA-PROG-B53832-C004',4,1),('bbc39a5c-1673-44db-b6f9-dbfbac76d2e4','44VKB','NAMAL-NOVE-CED001-C005',5,1),('bbc3f28a-67ee-48f7-8fb7-8feb7896f53f','44VKB','NAMAL-NOVE-CED001-C007',7,1),('bca50a27-243c-4b3b-907e-feee5e6494a6','CWQ25','COMPUT-INFO-FD4502-C007',7,1),('bf1de516-0e6a-4aa6-b9c8-a036d8a5486d','CWQ25','COMPUT-INFO-FD4502-C001',1,0),('bfd56739-32a9-4a6a-946e-aeda9554909e','44VKB','NAMAL-NOVE-CED001-C004',4,1),('c40a5ca5-6e33-4f0c-8a2b-e5b000c14f60','44VKB','NAMAL-NOVE-CED001-C003',3,1),('c53c8481-8f99-47a7-9330-847c4b7158b1','CWQ25','COMPUT-INFO-FD4502-C006',6,1),('c9f5c4ab-d163-4b55-989c-ee49cac6113e','CWQ25','COMPUT-INFO-FD4502-C013',13,1),('cfbed5c6-4cac-4143-b846-64c835d04263','CWQ25','COMPUT-INFO-FD4502-C017',17,1),('ddabf545-0d7e-4c32-a7a8-3b56f4da7332','SG6LK','LEARNI-PROG-704604-C001',1,1),('e223be6d-d8d1-456f-ab20-0e5d7620c8c9','WUX93','MACHIN-PROG-CF122D-C001',1,1),('eb43350f-6a13-4cf2-9174-e8eb9c7c7795','44VKB','NAMAL-NOVE-CED001-C008',8,1),('ec60e99b-054d-495a-973c-9c3a1e842e4a','SG6LK','LEARNI-PROG-704604-C005',5,1),('ee7211fa-2e29-43bb-9226-4cad96c66106','44VKB','NAMAL-NOVE-CED001-C006',6,1),('f29918fe-c5b6-4884-894b-035cebff9596','M7HTU','OBJECT-PROG-905C13-C003',3,1),('f4d98dc4-edd1-4078-b9c9-1d2698bab69d','44VKB','NAMAL-NOVE-CED001-C011',11,1),('f8b951de-0499-4560-a6a6-6f4657d6820a','CWQ25','COMPUT-INFO-FD4502-C009',9,1),('fde682c4-c85c-4172-91e2-3ca084e51dad','N4AYX','PROGRA-PROG-B53832-C005',5,1),('ff8f3c5f-432c-4033-a6e3-38bb7e8b8adc','N4AYX','PROGRA-PROG-B53832-C006',6,1);
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
INSERT INTO `books` VALUES ('44VKB','Namal','Aisha Gul','NAMAL-NOVE-CED001','Novel',14,14,'2026-03-26 04:44:14.245847','2026-04-29 01:43:49.309254',50.00,50,28),('898WH','Spoken English','Manglier','SPOKEN-LITE-B43ABC','Literature',2,1,'2026-03-26 03:06:35.860176','2026-04-28 23:06:48.845816',50.00,50,28),('CWQ25','Computer Networks V2','Mikle Aradan MK','COMPUT-INFO-FD4502','Information Technology_',17,15,'2026-03-26 02:38:26.487278','2026-04-29 00:57:11.174817',150.00,150,21),('L8JE4','DevOpps','Kalen Alex','DEVOPP-PARA-87EF49','Parallel Computing',1,1,'2026-04-28 18:41:14.385159','2026-04-29 00:45:11.052342',500.00,500,28),('M7HTU','Object Oriented Programming','Hallen Walk','OBJECT-PROG-905C13','Programming',12,11,'2026-03-26 02:49:04.324517','2026-04-29 01:15:56.067755',50.00,50,28),('N4AYX','Programming Fundamentals','Hallen Walk','PROGRA-PROG-B53832','Programming',10,10,'2026-03-25 11:48:59.181845','2026-04-29 01:44:30.545205',50.00,50,28),('SG6LK','Learning Through AI','Hallen Walk','LEARNI-PROG-704604','Programming',5,5,'2026-03-25 11:50:16.274121','2026-04-29 00:45:22.562049',50.00,50,28),('WUX93','Machine Learning For Robotics','David Kelson','MACHIN-PROG-CF122D','Programming',1,1,'2026-04-24 18:27:09.186261','2026-03-29 03:03:02.285422',500.00,500,28);
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
INSERT INTO `borrow_records` VALUES ('01a477ae-e1dd-4d90-9cec-62d76450ccfb','c91adf09-6a4a-447e-98bc-8ab71b96cb9f','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','DNZCTG','GM56JU','2026-03-24 19:04:17.019076','2026-04-15','2026-04-28 23:11:34.823531',NULL,NULL,0,_binary '\0',NULL,NULL),('15255e07-e982-4717-8d10-10bd2617e852','cc489c0f-4e23-453f-a5b9-5a852dcc3f96','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','DNZCTG','GM56JU','2026-03-29 02:32:12.373050','2026-04-26','2026-03-29 02:32:47.230091',NULL,NULL,0,_binary '\0',NULL,NULL),('2e7cb7c3-5371-4240-8e55-746db7013459','9066ee44-8a26-4cf3-8d21-61d4ea7b3986','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','XEKLS9','GM56JU','2026-03-29 02:33:52.016134','2026-04-26','2026-03-29 02:37:28.586927',NULL,NULL,0,_binary '\0',NULL,NULL),('315ec7a8-eb8d-47b8-bc2a-447bccf022ed','346f3704-3a76-4a1c-bada-99482eb1f606','L8JE4','96375a84-795f-4cd6-a4ef-eae1686188b3','DNZCTG','GM56JU','2026-04-28 22:35:08.187998','2026-05-27','2026-04-29 00:33:55.010512',NULL,NULL,0,_binary '\0',NULL,NULL),('3ec5a702-c908-4acf-80b7-ab4b0026564b','c950adf6-c2a2-49a4-8079-3216eac5e673','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','DNZCTG','GM56JU','2026-04-29 01:36:19.273530','2026-05-27','2026-04-29 01:42:47.107608',NULL,NULL,0,_binary '\0',NULL,NULL),('3efbe5a3-b341-48f9-8a07-afd0fecda2ad','6c35c3b2-a4ce-4141-9974-55609e460f9e','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','DNZCTG','GM56JU','2026-04-29 01:21:31.363553','2026-05-27','2026-04-29 01:30:30.285910',NULL,NULL,0,_binary '\0',NULL,NULL),('46e58c90-0f2f-453c-831d-84dfeb06e81f','2b28364b-21e8-40e4-98b1-c00710bc6691','SG6LK','ddabf545-0d7e-4c32-a7a8-3b56f4da7332','4QRTDX','GM56JU','2026-04-28 10:15:33.472859','2026-08-18','2026-04-29 00:12:11.499329','2026-04-28 10:53:20.376886','2026-05-26',3,_binary '\0',NULL,NULL),('477ee4e1-f812-4374-ac94-9a900c929c2f','fb80287f-3724-452a-b9e0-2803dff7c131','CWQ25','bf1de516-0e6a-4aa6-b9c8-a036d8a5486d','DNZCTG','GM56JU','2026-04-28 23:03:50.749261','2026-05-20','2026-04-29 00:33:51.171979',NULL,NULL,0,_binary '\0',NULL,NULL),('4abf9f42-2f08-497b-b0a0-bd1a80460f29','dcaf795f-f59a-4cc6-8af6-9a7ac9144748','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','DNZCTG','GM56JU','2026-03-29 02:12:23.458560','2026-04-26','2026-03-29 02:14:07.307609',NULL,NULL,0,_binary '\0',NULL,NULL),('4afa1d5e-b220-490c-b8aa-9dcdbc729207','314a6209-6982-40b2-97b3-70cb7b283eb9','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','DNZCTG','GM56JU','2026-04-29 02:05:16.933113','2026-05-27','2026-04-29 02:06:28.136271',NULL,NULL,0,_binary '\0',NULL,NULL),('52db0a3b-cc01-400b-8a60-05932917cb72','373cfe16-cd36-4564-b893-13625aa47762','M7HTU','a86185f1-b400-4bf7-8329-4f8baae59faf','DNZCTG','GM56JU','2026-04-29 01:15:56.105752','2026-05-27',NULL,NULL,NULL,0,_binary '\0',NULL,NULL),('5c8f4af2-5143-4cb3-8c76-6f5e29cf4ba9','83b6b946-a449-4dff-9832-00c3e8fbb15d','L8JE4','96375a84-795f-4cd6-a4ef-eae1686188b3','4QRTDX','GM56JU','2026-03-24 19:02:05.993882','2026-04-22','2026-04-28 22:58:49.491746','2026-03-24 19:12:18.426286','2026-04-01',3,_binary '\0',NULL,NULL),('7217b33d-1806-4bff-8ca5-b2c7ee970572','f1b4e76f-f009-4e6d-a322-8d210e0e76b7','N4AYX','420c84a8-2677-49e9-947d-ef85d7d09c7d','DNZCTG','GM56JU','2026-04-28 23:04:00.812322','2026-05-27','2026-04-29 00:33:58.145466',NULL,NULL,0,_binary '\0',NULL,NULL),('858efe3e-c725-41e9-9501-475709cc39e9','e94dbafc-a493-4ff8-b24a-4f1d9dccd372','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','DNZCTG','GM56JU','2026-03-29 03:02:04.135157','2026-04-26','2026-03-29 03:03:02.285422',NULL,NULL,0,_binary '\0',NULL,NULL),('8e712c4b-4abe-45e9-a96e-680e4b3dc7be','4c35de95-6c5f-4338-a51a-bf71a9b9c39c','CWQ25','bf1de516-0e6a-4aa6-b9c8-a036d8a5486d','DNZCTG','GM56JU','2026-04-29 00:34:39.375956','2026-05-20','2026-04-29 00:45:08.483289',NULL,NULL,0,_binary '\0',NULL,NULL),('92cf2ccc-73df-4e73-b534-7aa0ff87101d','7b424e0f-4aae-431d-a26c-e1456b129717','898WH','6b1db0fc-0ff7-4f2c-ba2e-a82935beda66','4QRTDX','GM56JU','2026-03-24 19:02:07.503448','2026-04-15',NULL,NULL,NULL,0,_binary '\0',NULL,NULL),('9615c3a2-ed72-48ec-a144-8f32e4c74b2f','c3542002-5e2c-4649-92a8-fe2c34bdee91','WUX93',NULL,'4QRTDX','GM56JU','2026-04-29 01:23:47.132701','2026-05-27','2026-04-29 01:24:34.675354',NULL,NULL,0,_binary '\0',NULL,NULL),('971dd11b-22c7-4241-8096-045ebda37cf4','21379c3c-c20d-43a5-9964-33e04830dbcc','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','4QRTDX','GM56JU','2026-04-29 01:44:56.936369','2026-05-27','2026-04-29 01:46:16.874607',NULL,NULL,0,_binary '\0',NULL,NULL),('99f77922-a1a7-4aa8-a964-ccf13435c000','a4cc09b8-374a-4a64-9d65-a602418763fb','N4AYX','420c84a8-2677-49e9-947d-ef85d7d09c7d','4QRTDX','GM56JU','2026-04-29 01:44:10.422161','2026-05-27','2026-04-29 01:44:30.545205',NULL,NULL,0,_binary '\0',NULL,NULL),('9be11d39-69ae-48d5-95e6-5f745864b9ad','801267b9-5cf8-4f7e-b985-cb060ce5e2e7','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','DNZCTG','GM56JU','2026-04-29 02:54:21.339610','2026-05-27','2026-04-29 02:55:48.594027',NULL,NULL,0,_binary '\0',NULL,NULL),('a60095dc-9924-4782-a1d7-5f476f9f8e2b','571ae7a5-561a-45a6-b6a8-3d2282774fb1','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','DNZCTG','GM56JU','2026-04-29 01:01:00.463367','2026-05-27','2026-04-29 01:19:13.937724',NULL,NULL,0,_binary '\0',NULL,NULL),('b4638585-46fa-4361-b199-fe82bdd9b70f','98b6ad11-03a3-4047-8a7f-cc41dc0f8c49','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','DNZCTG','GM56JU','2026-04-29 01:46:33.874808','2026-05-27','2026-04-29 01:59:49.935903',NULL,NULL,0,_binary '\0',NULL,NULL),('bf054094-c57f-4659-b1ed-bbe64db50403','07fa18ab-73d9-4b6c-8423-e869c2442065','898WH','b2fa78c5-7c78-4864-83a0-dcfe6da84f6a','DNZCTG','GM56JU','2026-03-24 19:04:18.844779','2026-04-22','2026-04-28 23:06:48.845816',NULL,NULL,0,_binary '\0',NULL,NULL),('ccd5f88f-39ce-4bfa-8f29-22e2b9ef7482','9c971026-e909-4711-b141-b14d09a982bf','L8JE4','96375a84-795f-4cd6-a4ef-eae1686188b3','DNZCTG','GM56JU','2026-04-29 00:34:43.625716','2026-05-27','2026-04-29 00:45:11.052342',NULL,NULL,0,_binary '\0',NULL,NULL),('ce17f591-7d09-4f4e-bcfb-22fe97368296','0310d4f7-5c8b-4c6a-a9e8-3b1637f8a5f5','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','4QRTDX','GM56JU','2026-04-29 02:00:04.172161','2026-05-27','2026-04-29 02:03:10.888433',NULL,NULL,0,_binary '\0',NULL,NULL),('d917ba1f-8f50-44f8-8f67-601b549a269f','313e46f5-a1b0-4333-a851-6afc01929f0f','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','DNZCTG','GM56JU','2026-04-29 01:43:07.885257','2026-05-27','2026-04-29 01:44:40.550798',NULL,NULL,0,_binary '\0',NULL,NULL),('dc754ee1-d034-4544-a894-ad86160d8b04','be8f99f8-08f3-4d9b-99f9-0bb52e0bd153','CWQ25','bf1de516-0e6a-4aa6-b9c8-a036d8a5486d','DNZCTG','GM56JU','2026-04-29 00:45:45.449955','2026-05-27',NULL,'2026-04-29 00:56:13.434175','2026-05-20',1,_binary '\0',NULL,NULL),('de4a6917-7624-4f48-a990-9f8fddae29dd','3e6ccddc-b282-40b0-b91f-170966f20a64','44VKB','4107efb6-9902-4cb6-b86b-396217936e09','4QRTDX','GM56JU','2026-04-29 00:43:33.651165','2026-06-10','2026-04-29 01:43:49.309254','2026-04-29 00:44:10.327813','2026-05-27',2,_binary '\0',NULL,NULL),('e0b0a95b-2940-430d-82f6-0ebbf8b2fe5b','ab6fe4ef-1252-4e82-aaeb-9db1bccb8cf5','CWQ25','85348450-7739-49d3-9ebd-8e4f31856f92','4QRTDX','GM56JU','2026-04-29 00:47:10.881685','2026-05-27',NULL,'2026-04-29 00:47:17.984245','2026-05-20',1,_binary '\0',NULL,NULL),('e56500c8-46b3-4e01-a16f-12b660c178b0','f8c4e364-4a65-4056-bbfe-11c0bc54125e','CWQ25','bf1de516-0e6a-4aa6-b9c8-a036d8a5486d','4QRTDX','GM56JU','2026-04-28 10:15:35.107005','2026-05-12','2026-04-28 19:57:10.883659',NULL,NULL,0,_binary '\0',NULL,NULL),('e6488e5f-317a-4708-a70f-61a2a250c144','e1a01604-f233-477b-9abf-117236c5f12f','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','4QRTDX','GM56JU','2026-04-29 02:56:05.008960','2026-05-27','2026-04-29 02:58:33.572543',NULL,NULL,0,_binary '\0',NULL,NULL),('ed8df639-0ae5-4835-9ec8-6a878431ca25','d6a12cd3-11c9-496e-bbd8-bdfcb64696de','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','XEKLS9','GM56JU','2026-04-29 02:59:11.009375','2026-05-27','2026-04-29 02:59:55.028677',NULL,NULL,0,_binary '\0',NULL,NULL),('f211472f-fd5c-4e56-94a7-2e58b916d1c2','d547b16a-374c-4a6f-ae09-0899f12d78fc','SG6LK','ddabf545-0d7e-4c32-a7a8-3b56f4da7332','DNZCTG','GM56JU','2026-04-29 00:34:44.849772','2026-05-27','2026-04-29 00:45:22.562049',NULL,NULL,0,_binary '\0',NULL,NULL),('faf58e08-af65-4523-96a4-91ba4d73224c','48d5941f-993f-4d8d-9393-d5f7f60e74bf','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','DNZCTG','GM56JU','2026-03-25 02:06:59.046776','2026-04-22','2026-03-25 02:07:28.844889',NULL,NULL,0,_binary '\0',NULL,NULL),('fc59740c-c4cd-43e0-8662-995160232fb6','af08b0ae-a721-4e60-b2e2-3222497ea61f','WUX93','e223be6d-d8d1-456f-ab20-0e5d7620c8c9','DNZCTG','GM56JU','2026-04-29 02:31:09.954896','2026-05-27','2026-04-29 02:31:45.663466',NULL,NULL,0,_binary '\0',NULL,NULL);
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
  CONSTRAINT `chk_borrow_requests_duration` CHECK ((`requested_duration_days` in (7,14,21,28)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `borrow_requests`
--

LOCK TABLES `borrow_requests` WRITE;
/*!40000 ALTER TABLE `borrow_requests` DISABLE KEYS */;
INSERT INTO `borrow_requests` VALUES ('0310d4f7-5c8b-4c6a-a9e8-3b1637f8a5f5','WUX93','4QRTDX','GM56JU','APPROVED','2026-04-29 01:59:29.158911','2026-04-29 02:00:04.166166','2026-05-27',28),('07fa18ab-73d9-4b6c-8423-e869c2442065','898WH','DNZCTG','GM56JU','APPROVED','2026-03-24 19:02:52.535892','2026-03-24 19:04:18.844779','2026-04-22',28),('0ba4415e-a4a8-47df-a2be-9f63a4c4e31e','L8JE4','DNZCTG','GM56JU','REJECTED','2026-04-29 00:45:37.357866','2026-04-29 00:46:05.284194',NULL,28),('16f05af7-19e1-48ad-b3c7-b1f49369d1f0','SG6LK','4QRTDX',NULL,'CANCELLED','2026-04-28 18:50:24.912129','2026-03-25 18:56:31.140627',NULL,28),('1ab1796a-ad82-44de-a2cb-c70876e9ba1e','L8JE4','4QRTDX',NULL,'REJECTED','2026-03-25 18:57:00.358115','2026-04-28 18:58:04.542735',NULL,28),('20595022-7e13-4a75-a2f9-43cab5db2238','44VKB','4QRTDX',NULL,'REJECTED','2026-03-28 09:48:19.321116','2026-04-28 15:07:39.908024',NULL,28),('21379c3c-c20d-43a5-9964-33e04830dbcc','WUX93','4QRTDX','GM56JU','APPROVED','2026-04-29 01:44:53.476056','2026-04-29 01:44:56.936369','2026-05-27',28),('242f5f2c-951c-4207-b254-f2e896570e42','SG6LK','4QRTDX',NULL,'CANCELLED','2026-04-28 18:16:15.431773','2026-04-28 18:16:30.630373',NULL,28),('26f65eb0-364c-4a81-994f-46a4b1f2a915','N4AYX','4QRTDX',NULL,'REJECTED','2026-03-28 09:48:31.756811','2026-04-28 15:07:39.908024',NULL,7),('2b28364b-21e8-40e4-98b1-c00710bc6691','SG6LK','4QRTDX','GM56JU','APPROVED','2026-04-28 10:15:16.007862','2026-04-28 10:15:33.471865','2026-05-26',28),('305377d8-3a72-4788-86a8-45d8183ec81c','CWQ25','DNZCTG','GM56JU','REJECTED','2026-04-28 23:00:21.313813','2026-04-28 23:00:35.808862',NULL,21),('313e46f5-a1b0-4333-a851-6afc01929f0f','WUX93','DNZCTG','GM56JU','APPROVED','2026-04-29 01:42:58.352591','2026-04-29 01:43:07.885257','2026-05-27',28),('314a6209-6982-40b2-97b3-70cb7b283eb9','WUX93','DNZCTG','GM56JU','APPROVED','2026-04-29 02:05:12.205039','2026-04-29 02:05:16.933113','2026-05-27',28),('346f3704-3a76-4a1c-bada-99482eb1f606','L8JE4','DNZCTG','GM56JU','APPROVED','2026-04-28 21:36:15.828936','2026-04-28 22:35:08.184991','2026-05-27',28),('373cfe16-cd36-4564-b893-13625aa47762','M7HTU','DNZCTG','GM56JU','APPROVED','2026-04-29 01:15:46.078835','2026-04-29 01:15:56.103750','2026-05-27',28),('3e6ccddc-b282-40b0-b91f-170966f20a64','44VKB','4QRTDX','GM56JU','APPROVED','2026-04-29 00:43:19.126245','2026-04-29 00:43:33.651165','2026-05-27',28),('48d5941f-993f-4d8d-9393-d5f7f60e74bf','WUX93','DNZCTG','GM56JU','APPROVED','2026-03-25 02:06:56.294887','2026-03-25 02:06:59.046776','2026-04-22',28),('4c35de95-6c5f-4338-a51a-bf71a9b9c39c','CWQ25','DNZCTG','GM56JU','APPROVED','2026-04-29 00:34:11.338596','2026-04-29 00:34:39.375956','2026-05-20',21),('4ef037b8-98ed-49a5-9e79-6042e005c4e5','SG6LK','DNZCTG','GM56JU','REJECTED','2026-04-29 00:45:39.498545','2026-04-29 00:46:07.021532',NULL,28),('571ae7a5-561a-45a6-b6a8-3d2282774fb1','WUX93','DNZCTG','GM56JU','APPROVED','2026-04-29 01:00:53.927349','2026-04-29 01:01:00.462368','2026-05-27',28),('6c35c3b2-a4ce-4141-9974-55609e460f9e','WUX93','DNZCTG','GM56JU','APPROVED','2026-04-29 01:20:40.972204','2026-04-29 01:21:31.363553','2026-05-27',28),('6f998f0e-5c3e-48fd-9371-1134ee42cf93','SG6LK','4QRTDX',NULL,'CANCELLED','2026-03-25 18:57:02.909066','2026-03-25 18:57:27.988596',NULL,28),('7b424e0f-4aae-431d-a26c-e1456b129717','898WH','4QRTDX','GM56JU','APPROVED','2026-03-24 19:00:48.665676','2026-03-24 19:02:07.503448','2026-04-15',21),('7caacf97-e157-47ac-a1da-ac2abfb8df47','CWQ25','4QRTDX','GM56JU','REJECTED','2026-04-28 22:52:35.225309','2026-04-28 22:52:38.143072',NULL,21),('7d6dc9b9-06c4-497e-a908-1f2604b0862e','L8JE4','4QRTDX',NULL,'CANCELLED','2026-04-28 18:50:19.960171','2026-03-25 18:56:50.479758',NULL,28),('801267b9-5cf8-4f7e-b985-cb060ce5e2e7','WUX93','DNZCTG','GM56JU','APPROVED','2026-04-29 02:54:18.120000','2026-04-29 02:54:21.339610','2026-05-27',28),('81aabdd6-256e-4a9f-805f-05a464272ef6','SG6LK','4QRTDX',NULL,'REJECTED','2026-03-28 09:48:26.438083','2026-04-28 15:07:39.908024',NULL,28),('833c8c00-63db-4023-8307-265a766a6183','L8JE4','DNZCTG','GM56JU','REJECTED','2026-04-28 23:03:21.266407','2026-04-28 23:03:49.625257',NULL,28),('83b6b946-a449-4dff-9832-00c3e8fbb15d','L8JE4','4QRTDX','GM56JU','APPROVED','2026-03-24 19:00:35.819033','2026-03-24 19:02:05.993882','2026-04-01',7),('8a9b2ce8-851b-48eb-8068-3c6db77288dd','WUX93','DNZCTG',NULL,'CANCELLED','2026-04-29 03:01:32.865790','2026-04-29 03:01:42.364187',NULL,28),('9066ee44-8a26-4cf3-8d21-61d4ea7b3986','WUX93','XEKLS9','GM56JU','APPROVED','2026-03-29 02:33:47.859693','2026-03-29 02:33:52.016134','2026-04-26',28),('98b6ad11-03a3-4047-8a7f-cc41dc0f8c49','WUX93','DNZCTG','GM56JU','APPROVED','2026-04-29 01:46:28.172722','2026-04-29 01:46:33.874808','2026-05-27',28),('9c971026-e909-4711-b141-b14d09a982bf','L8JE4','DNZCTG','GM56JU','APPROVED','2026-04-29 00:34:13.813520','2026-04-29 00:34:43.625716','2026-05-27',28),('a4cc09b8-374a-4a64-9d65-a602418763fb','N4AYX','4QRTDX','GM56JU','APPROVED','2026-04-29 01:44:02.416342','2026-04-29 01:44:10.422161','2026-05-27',28),('ab6fe4ef-1252-4e82-aaeb-9db1bccb8cf5','CWQ25','4QRTDX','GM56JU','APPROVED','2026-04-29 00:46:57.089633','2026-04-29 00:47:10.881685','2026-05-20',21),('af08b0ae-a721-4e60-b2e2-3222497ea61f','WUX93','DNZCTG','GM56JU','APPROVED','2026-04-29 02:30:57.337635','2026-04-29 02:31:09.954896','2026-05-27',28),('b9face46-ab1e-42f8-b8ca-9ff1c2fc0282','WUX93','4QRTDX','GM56JU','REJECTED','2026-04-29 02:31:05.571409','2026-04-29 02:31:13.968878',NULL,28),('be8f99f8-08f3-4d9b-99f9-0bb52e0bd153','CWQ25','DNZCTG','GM56JU','APPROVED','2026-04-29 00:45:35.935663','2026-04-29 00:45:45.449955','2026-05-20',21),('c3542002-5e2c-4649-92a8-fe2c34bdee91','WUX93','4QRTDX','GM56JU','APPROVED','2026-03-29 01:05:54.651213','2026-04-29 01:23:47.130684','2026-05-27',28),('c91adf09-6a4a-447e-98bc-8ab71b96cb9f','WUX93','DNZCTG','GM56JU','APPROVED','2026-03-24 19:02:49.608094','2026-03-24 19:04:17.019076','2026-04-15',21),('c950adf6-c2a2-49a4-8079-3216eac5e673','WUX93','DNZCTG','GM56JU','APPROVED','2026-04-29 01:36:08.053683','2026-04-29 01:36:19.273530','2026-05-27',28),('cc489c0f-4e23-453f-a5b9-5a852dcc3f96','WUX93','DNZCTG','GM56JU','APPROVED','2026-03-29 02:32:06.218092','2026-03-29 02:32:12.372051','2026-04-26',28),('d547b16a-374c-4a6f-ae09-0899f12d78fc','SG6LK','DNZCTG','GM56JU','APPROVED','2026-04-29 00:34:17.921303','2026-04-29 00:34:44.849772','2026-05-27',28),('d6a12cd3-11c9-496e-bbd8-bdfcb64696de','WUX93','XEKLS9','GM56JU','APPROVED','2026-04-29 02:55:38.867937','2026-04-29 02:59:11.007370','2026-05-27',28),('d79aa286-9ad2-431d-b0a7-5664954b1609','44VKB','4QRTDX',NULL,'REJECTED','2026-03-28 22:53:48.853348','2026-04-28 22:54:26.753549',NULL,28),('dcaf795f-f59a-4cc6-8af6-9a7ac9144748','WUX93','DNZCTG','GM56JU','APPROVED','2026-03-29 02:12:17.803866','2026-03-29 02:12:23.458560','2026-04-26',28),('e1a01604-f233-477b-9abf-117236c5f12f','WUX93','4QRTDX','GM56JU','APPROVED','2026-04-29 02:54:47.849893','2026-04-29 02:56:05.006955','2026-05-27',28),('e94dbafc-a493-4ff8-b24a-4f1d9dccd372','WUX93','DNZCTG','GM56JU','APPROVED','2026-03-29 03:02:01.592824','2026-03-29 03:02:04.135157','2026-04-26',28),('ea0ff4f7-aea4-45f3-8eec-67498da502ba','N4AYX','DNZCTG',NULL,'REJECTED','2026-03-24 19:02:57.284355','2026-04-28 19:13:04.541977',NULL,28),('f1b4e76f-f009-4e6d-a322-8d210e0e76b7','N4AYX','DNZCTG','GM56JU','APPROVED','2026-04-28 23:03:55.873666','2026-04-28 23:04:00.812322','2026-05-27',28),('f8c4e364-4a65-4056-bbfe-11c0bc54125e','CWQ25','4QRTDX','GM56JU','APPROVED','2026-04-28 10:15:18.219249','2026-04-28 10:15:35.107005','2026-05-12',14),('fb80287f-3724-452a-b9e0-2803dff7c131','CWQ25','DNZCTG','GM56JU','APPROVED','2026-04-28 23:03:19.273554','2026-04-28 23:03:50.749261','2026-05-20',21);
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
INSERT INTO `fines` VALUES ('39ded9a5-07e3-4d38-9f71-c3154fb0a57c',3000.00,6,'2026-04-28 22:58:49.491746',NULL,'2026-04-29 00:08:46.353735','WAIVED','5c8f4af2-5143-4cb3-8c76-6f5e29cf4ba9','GM56JU','4QRTDX',3000.00),('3c810c2d-17b4-4077-9c3b-8f4c5fdb7b94',300.00,6,'2026-04-28 23:06:48.845816',NULL,'2026-04-29 00:10:03.567175','PAID','bf054094-c57f-4659-b1ed-bbe64db50403','GM56JU','DNZCTG',200.00),('f3468538-84e5-4664-bcdb-ed54dbeec7c1',6500.00,13,'2026-04-28 23:11:34.823531',NULL,'2026-04-29 01:42:31.221313','PAID','01a477ae-e1dd-4d90-9cec-62d76450ccfb','GM56JU','DNZCTG',0.00);
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
INSERT INTO `librarians` VALUES ('3YGL5R','L-74E99108-D89',1),('AECA87','L-E055503C-A56',1),('GM56JU','L-F4AE7914-5A2',1),('PLLV3U','L-74A4C89F-9C1',1);
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
INSERT INTO `registration_requests` VALUES ('15314369-49dd-41a5-87a1-ed71f8a461a6','4QRTDX','approved','2026-03-25 16:40:16.108665','2026-03-25 11:41:35.997340',NULL),('68625c08-2219-4b90-bcb0-89724e447dc7','DNZCTG','approved','2026-03-25 16:04:02.787627','2026-03-25 11:41:30.687266',NULL),('8a5a9801-8aac-4d65-ad92-07aec9cf43c9','3YGL5R','pending','2026-04-27 22:21:34.407016',NULL,NULL),('9fabecc7-ddfd-42b9-9047-76eac5079946','6NCWFN','approved','2026-03-25 15:58:39.343128','2026-03-25 10:58:39.261871',NULL),('d574f2ac-0fa1-47c8-a9c5-d6c6266b5a4a','GM56JU','approved','2026-03-25 15:59:39.345629','2026-03-25 11:41:25.876942',NULL),('dfeeb82b-8123-4114-b965-946989ee8bdc','PLLV3U','approved','2026-03-25 16:00:33.264081','2026-03-25 11:41:27.939995',NULL),('e57d8ade-3e22-496c-b40b-967c3bb8ae2a','XEKLS9','approved','2026-03-25 16:38:25.256007','2026-03-25 11:41:32.489413',NULL),('f8a475d0-f917-4fd4-b6d2-7b260992b678','AECA87','approved','2026-04-24 11:50:48.969014','2026-04-24 06:51:27.308329',NULL);
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
  KEY `FKrsdd3ib3landfpmgoolccjakt` (`book_id`),
  KEY `idx_reservation_student_id` (`student_id`),
  KEY `idx_reservation_book_id` (`book_id`),
  KEY `idx_reservation_student_book_status` (`student_id`,`book_id`,`status`),
  CONSTRAINT `FKnlgg22885nfyspmen9jj0jcpp` FOREIGN KEY (`student_id`) REFERENCES `students` (`user_id`),
  CONSTRAINT `FKrsdd3ib3landfpmgoolccjakt` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations`
--

LOCK TABLES `reservations` WRITE;
/*!40000 ALTER TABLE `reservations` DISABLE KEYS */;
INSERT INTO `reservations` VALUES ('271df317-fcd0-44d6-9fcd-fca81ca6ed6d','2026-04-29 01:59:29.158911','2026-05-01 01:59:51.336726','2026-04-29 02:00:04.178161','2026-04-29 01:59:51.336726',4,'FULFILLED','WUX93','4QRTDX',28),('3fb863c6-10da-4ebd-9e51-9883b4b20b0f','2026-03-29 02:35:02.257715',NULL,NULL,NULL,1,'EXPIRED','WUX93','DNZCTG',28),('69223e43-a0a3-4ed4-9bb4-617851f9b4e9','2026-04-29 02:00:22.997497',NULL,NULL,NULL,5,'CANCELLED','WUX93','DNZCTG',28),('71b1dc74-4cb0-4e09-a45b-11bf14a07e24','2026-04-28 21:36:15.828936','2026-04-30 22:35:02.762563','2026-04-28 22:35:08.190991','2026-04-28 22:35:02.762563',1,'FULFILLED','L8JE4','DNZCTG',28),('765193b9-201e-4d7c-813c-b0074c5f9def','2026-03-29 03:02:11.559563',NULL,NULL,NULL,1,'EXPIRED','WUX93','4QRTDX',28),('7a54f006-9f71-4e6f-b925-96da50865415','2026-04-29 02:54:27.678053',NULL,NULL,NULL,1,'CANCELLED','WUX93','4QRTDX',28),('8cb66603-5123-48d0-8d55-ba4bad0212c9','2026-03-29 02:34:10.704828',NULL,NULL,NULL,1,'CANCELLED','WUX93','DNZCTG',28),('bf4df45c-2f74-441f-b9f4-a85f967d5c09','2026-04-29 02:54:47.849893','2026-05-03 02:55:49.822264','2026-04-29 02:56:05.010960','2026-04-29 02:55:49.822264',1,'FULFILLED','WUX93','4QRTDX',28),('c178cdda-c5d8-47ec-9a27-120ae0362caa','2026-04-29 02:55:38.867937','2026-05-03 02:58:37.353942','2026-04-29 02:59:11.012369','2026-04-29 02:58:37.353942',1,'FULFILLED','WUX93','XEKLS9',28),('c22902cf-9586-4a77-a536-49cce24ab040','2026-03-29 02:32:25.670853',NULL,NULL,NULL,1,'EXPIRED','WUX93','4QRTDX',28),('c3c371a9-e6d1-4e4d-b371-7f84f52eef81','2026-04-29 02:31:27.540281',NULL,NULL,NULL,1,'CANCELLED','WUX93','4QRTDX',28),('c88f30a3-07e6-48bc-b1bd-b3c9e763b650','2026-03-29 03:02:26.510761',NULL,NULL,NULL,2,'EXPIRED','WUX93','XEKLS9',28),('c9b39fa9-11bc-4d8e-9c47-bcba35200ef3','2026-03-29 02:12:26.625215',NULL,NULL,NULL,8,'CANCELLED','WUX93','4QRTDX',28),('d4b0180a-bfc6-46ba-89f2-49ff48acc266','2026-03-28 22:47:28.636007','2026-05-01 01:00:43.926621',NULL,'2026-04-29 01:00:43.926621',2,'CANCELLED','L8JE4','DNZCTG',28),('eb6ed3cb-b41f-4ae3-9797-7df690c988be','2026-04-29 02:56:20.835543',NULL,NULL,NULL,1,'CANCELLED','WUX93','DNZCTG',28);
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
INSERT INTO `students` VALUES ('4QRTDX','S-18AF33BA-E03','Cyber Security','2022-08-27','2006-12-23',3,1),('DNZCTG','S-55D1E318-6AF','AI','2022-06-20','2004-02-28',3,1),('XEKLS9','S-B5B929F4-7B5','Computer Science','2022-07-23','2005-03-17',3,1);
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
INSERT INTO `users` VALUES ('3YGL5R','Zain Azam','i22224920@nu.edu.pk','E1:Iqfa5tfhdzVRDvQs1kkGphKY0yg3RnFg3WwR+BqcbJyo+f30hYL7Xw==','LIBRARIAN','pending',NULL,NULL,NULL,NULL,'2026-04-27 22:21:34.392397','2026-04-27 22:21:34.392397',NULL),('4QRTDX','Yaqoob','i222332@nu.edu.pk','E1:+zzYK9/PtNG9k2xUvN/Jm4Fp684ZOzpyiAWzNq4JAO2BShKgoJ8=','STUDENT','active','03110433560','/uploads/profiles/2.jpg',50,50,'2026-03-25 16:40:16.097432','2026-04-29 08:01:19.965039','2026-04-29 03:01:19.963411'),('6NCWFN','Mohammad Rohaan','i222327@nu.edu.pk','E1:xMdPZJRjxQC/rz2msHma+oGWoV2BYtuAaNVdWoE9I+Q/YT4E','ADMIN','active','03110433555','/uploads/profiles/3.jpg',50,50,'2026-03-25 15:58:39.325709','2026-04-29 10:40:19.905630','2026-04-29 05:40:19.791975'),('AECA87','Muhammad Farzeen Tareen Afzal','i222337@nu.edu.pk','E1:sDrmiolcb8aSss11zVqb3sVbTMQnqqPQNQYLJAlWb4RktxNPfsziig==','LIBRARIAN','active',NULL,'/uploads/profiles/5.jpg',NULL,NULL,'2026-04-24 11:50:48.961429','2026-04-27 19:24:22.053117','2026-04-26 21:21:51.398429'),('DNZCTG','Ahmed','i222330@nu.edu.pk','E1:7Ea++ZLl6UxlBSejMOOZB/0GdpMuDEBJ84DKzJ60P5bC/oBxmUc=','STUDENT','active','03110433558','/uploads/profiles/6.jpg',50,50,'2026-03-25 16:04:02.782913','2026-04-29 07:58:21.133484','2026-04-29 02:58:21.130997'),('GM56JU','Bilal','i222328@nu.edu.pk','E1:l1flE0KXSy3ExeJb/s3mZi99NCbwvZl8vHrfrp7PNqlK5NporH8JoA==','LIBRARIAN','active','03110433556','/uploads/profiles/7.jpg',27.12,54.18,'2026-03-25 15:59:39.340954','2026-04-29 08:03:48.992192','2026-04-29 03:03:48.990349'),('PLLV3U','Ali','i222329@nu.edu.pk','E1:IwaVkiu3Qcisu3y1vcC19Zz31sakmiChhTjmd7I2PTf1PTFyfmVViQ==','LIBRARIAN','active','03110433557','/uploads/profiles/8.jpg',50,50,'2026-03-25 16:00:33.260242','2026-04-27 19:25:09.060645','2026-04-24 05:59:06.559444'),('XEKLS9','Mikle','i222331@nu.edu.pk','E1:yp/ReGpx30GYHSw3GyDk7sW02GhKEqfoi2ntRtlJqVWv68l8JZ8=','STUDENT','active','03110433559','/uploads/profiles/9.jpg',79.7,49.12,'2026-03-25 16:38:25.238050','2026-03-29 08:02:21.971919','2026-03-29 03:02:21.969723');
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

-- Dump completed on 2026-04-29 10:40:28
