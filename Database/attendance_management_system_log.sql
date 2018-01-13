-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: attendance_management_system
-- ------------------------------------------------------
-- Server version	5.6.37

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `log`
--

DROP TABLE IF EXISTS `log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `log` (
  `ldate` date NOT NULL,
  `ltime` time NOT NULL,
  `eno` varchar(10) NOT NULL,
  `operation` varchar(100) NOT NULL,
  PRIMARY KEY (`ldate`,`ltime`,`eno`),
  KEY `logeno_idx` (`eno`),
  CONSTRAINT `logeno` FOREIGN KEY (`eno`) REFERENCES `employee` (`eno`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `log`
--

LOCK TABLES `log` WRITE;
/*!40000 ALTER TABLE `log` DISABLE KEYS */;
INSERT INTO `log` VALUES ('2018-01-12','17:29:45','10002','login'),('2018-01-12','17:29:59','10002','modify_user'),('2018-01-12','17:30:20','10002','submit_trip'),('2018-01-12','17:31:10','10002','submit_trip'),('2018-01-12','17:46:22','10003','login'),('2018-01-12','19:42:40','10002','login'),('2018-01-12','19:44:40','10002','login'),('2018-01-12','19:48:29','10003','login'),('2018-01-12','19:56:15','10003','login'),('2018-01-12','20:16:52','10002','login'),('2018-01-12','20:17:04','10002','modify_user'),('2018-01-12','20:20:09','10002','login'),('2018-01-12','20:20:47','10002','login'),('2018-01-12','20:21:12','10002','submit_leave'),('2018-01-12','21:10:21','10003','login'),('2018-01-12','21:10:23','10003','login_in'),('2018-01-12','21:11:01','10003','login'),('2018-01-12','21:11:02','10003','login_in'),('2018-01-12','21:13:29','10003','login'),('2018-01-12','21:13:30','10003','login_in'),('2018-01-12','21:14:57','10003','login'),('2018-01-12','21:14:59','10003','login_in'),('2018-01-12','21:16:04','10002','login'),('2018-01-12','21:17:13','10002','submit_trip'),('2018-01-12','21:17:49','10003','login'),('2018-01-12','21:18:02','10003','modify_user'),('2018-01-12','21:18:44','10004','login'),('2018-01-12','21:19:04','10004','submit_trip'),('2018-01-12','21:19:49','10004','submit_leave'),('2018-01-12','21:20:44','10005','login'),('2018-01-12','21:20:47','10005','login_in'),('2018-01-12','21:21:53','10005','login'),('2018-01-12','21:21:55','10005','login_in'),('2018-01-12','21:23:30','10005','login'),('2018-01-12','21:23:31','10005','login_in'),('2018-01-12','21:24:30','10005','login'),('2018-01-12','21:24:37','10005','modify_user'),('2018-01-12','21:24:41','10005','login_in'),('2018-01-12','21:27:58','10005','login'),('2018-01-12','21:27:59','10005','login_in'),('2018-01-12','21:28:53','10005','login'),('2018-01-12','21:28:54','10005','login_in'),('2018-01-12','21:29:55','10005','approve_leave'),('2018-01-12','21:31:14','10005','login'),('2018-01-12','21:31:15','10005','login_in'),('2018-01-12','21:32:40','10002','login'),('2018-01-12','21:33:16','10002','submit_trip'),('2018-01-12','21:33:50','10002','submit_leave'),('2018-01-12','21:34:10','10002','submit_trip'),('2018-01-12','21:34:15','10002','login_in'),('2018-01-12','21:35:00','10002','approve_leave'),('2018-01-12','21:35:59','10002','login'),('2018-01-12','21:36:00','10002','login_in'),('2018-01-12','21:36:42','10002','reject_leave'),('2018-01-12','21:43:02','10002','login'),('2018-01-12','21:47:49','10002','login'),('2018-01-12','21:48:15','10002','submit_leave'),('2018-01-12','21:49:08','10002','submit_trip'),('2018-01-12','21:49:26','10003','login'),('2018-01-12','21:49:28','10003','login_in'),('2018-01-12','21:49:51','10003','login'),('2018-01-12','21:49:52','10003','login_in'),('2018-01-12','21:54:43','10003','approve_leave'),('2018-01-12','21:59:39','10002','login'),('2018-01-12','21:59:41','10002','login_in'),('2018-01-12','21:59:51','10003','login'),('2018-01-12','21:59:54','10003','login_in'),('2018-01-12','22:00:17','10003','approve_leave'),('2018-01-12','22:00:36','10002','login'),('2018-01-13','12:40:00','10000','login'),('2018-01-13','12:44:10','10001','login'),('2018-01-13','12:44:54','10001','login'),('2018-01-13','12:44:59','10001','checkin'),('2018-01-13','12:45:03','10001','checkout'),('2018-01-13','12:45:42','10000','login'),('2018-01-13','13:12:55','10008','login'),('2018-01-13','13:13:05','10008','checkin'),('2018-01-13','13:15:26','10008','login'),('2018-01-13','13:19:01','10008','login'),('2018-01-13','13:30:09','10006','login'),('2018-01-13','13:30:14','10006','checkin'),('2018-01-13','13:31:13','10006','submit_trip'),('2018-01-13','13:32:01','10005','login'),('2018-01-13','13:32:03','10005','login_in'),('2018-01-13','13:37:50','10005','login'),('2018-01-13','13:37:51','10005','login_in'),('2018-01-13','13:45:56','10007','login'),('2018-01-13','13:46:00','10007','checkin'),('2018-01-13','13:46:14','10004','login'),('2018-01-13','13:46:33','10004','checkin'),('2018-01-13','13:46:37','10004','checkout'),('2018-01-13','13:47:05','10004','submit_leave'),('2018-01-13','13:47:16','10002','login'),('2018-01-13','13:47:17','10002','login_in'),('2018-01-13','13:47:49','10005','login'),('2018-01-13','13:47:50','10005','login_in');
/*!40000 ALTER TABLE `log` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-01-13 13:54:51
