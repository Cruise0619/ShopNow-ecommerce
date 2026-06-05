mysqldump: [Warning] Using a password on the command line interface can be insecure.
-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: ecommerce
-- ------------------------------------------------------
-- Server version	8.0.42

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
-- Table structure for table `addresses`
--

DROP TABLE IF EXISTS `addresses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `addresses` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `receiver` varchar(50) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `province` varchar(50) NOT NULL,
  `city` varchar(50) NOT NULL,
  `district` varchar(50) NOT NULL,
  `detail` varchar(200) NOT NULL,
  `is_default` tinyint(1) DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `addresses`
--

LOCK TABLES `addresses` WRITE;
/*!40000 ALTER TABLE `addresses` DISABLE KEYS */;
INSERT INTO `addresses` VALUES (20,2,'张三','13800000001','北京市','北京市','朝阳区','建国路100号',0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(21,2,'张三','13800000001','上海市','上海市','浦东新区','陆家嘴金融街88号',0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(22,3,'李四','13800000002','广东省','广州市','天河区','体育西路200号',0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(23,4,'王五','13800000003','浙江省','杭州市','西湖区','文三路300号',0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(24,5,'赵六','13800000004','四川省','成都市','武侯区','天府大道400号',0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(25,6,'钱七','13800000005','江苏省','南京市','鼓楼区','中山路500号',0,'2026-06-04 22:19:07','2026-06-04 22:19:07');
/*!40000 ALTER TABLE `addresses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `announcements`
--

DROP TABLE IF EXISTS `announcements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcements` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `content` text,
  `status` varchar(20) NOT NULL DEFAULT 'on',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcements`
--

LOCK TABLES `announcements` WRITE;
/*!40000 ALTER TABLE `announcements` DISABLE KEYS */;
INSERT INTO `announcements` VALUES (10,'618大促活动通知','618年中大促即将开始，全场商品低至5折，敬请期待！','on','2026-06-04 22:19:07','2026-06-04 22:19:07'),(11,'新用户专享优惠券','新注册用户可领取50元优惠券，满200元即可使用。','on','2026-06-04 22:19:07','2026-06-04 22:19:07'),(12,'春节物流安排','春节期间物流正常发货，部分偏远地区可能延迟1-2天。','on','2026-06-04 22:19:07','2026-06-04 22:19:07');
/*!40000 ALTER TABLE `announcements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `banners`
--

DROP TABLE IF EXISTS `banners`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `banners` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `image_url` varchar(500) NOT NULL,
  `link_url` varchar(500) DEFAULT '/products',
  `sort_order` int DEFAULT '0',
  `status` varchar(20) NOT NULL DEFAULT 'on',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `banners`
--

LOCK TABLES `banners` WRITE;
/*!40000 ALTER TABLE `banners` DISABLE KEYS */;
INSERT INTO `banners` VALUES (10,'618年中大促','/uploads/1780592299419-1613.png','/products',1,'on','2026-06-04 22:19:07','2026-06-05 00:58:20'),(11,'iPhone 15 Pro','/uploads/1780592293768-8837.png','/products/1',2,'on','2026-06-04 22:19:07','2026-06-05 00:58:14'),(12,'春季焕新','/uploads/1780592288606-8544.png','/products',3,'on','2026-06-04 22:19:07','2026-06-05 00:58:09');
/*!40000 ALTER TABLE `banners` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `sku_spec` varchar(100) DEFAULT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `selected` tinyint(1) DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
INSERT INTO `cart_items` VALUES (37,2,157,NULL,3,1,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(38,4,188,NULL,1,1,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(39,2,179,NULL,2,1,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(40,3,167,NULL,1,1,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(41,2,163,NULL,2,1,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(42,2,167,NULL,1,1,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(43,4,153,NULL,3,1,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(44,3,170,NULL,2,1,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(45,3,189,NULL,2,1,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(46,4,173,NULL,1,1,'2026-06-04 22:19:09','2026-06-04 22:19:09');
/*!40000 ALTER TABLE `cart_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `parent_id` int DEFAULT '0',
  `sort_order` int DEFAULT '0',
  `icon` varchar(100) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=83 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (64,'手机数码',0,1,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(65,'电脑办公',0,2,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(66,'服饰鞋包',0,3,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(67,'食品生鲜',0,4,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(68,'家居家装',0,5,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(69,'美妆护肤',0,6,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(70,'运动户外',0,7,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(71,'图书文具',0,8,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(72,'键盘鼠标',65,1,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(73,'女装',66,2,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(74,'男装',66,3,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(75,'休闲零食',67,4,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(76,'生鲜水果',67,5,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(77,'家具',68,6,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(78,'灯具',68,7,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(79,'面部护理',69,8,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(80,'彩妆',69,9,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(81,'跑步鞋',70,10,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(82,'健身器材',70,11,NULL,'2026-06-04 22:19:07','2026-06-04 22:19:07');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupons`
--

DROP TABLE IF EXISTS `coupons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupons` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `type` varchar(20) NOT NULL,
  `value` decimal(10,2) NOT NULL,
  `min_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `total` int NOT NULL DEFAULT '0',
  `used` int NOT NULL DEFAULT '0',
  `status` varchar(20) NOT NULL DEFAULT 'active',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupons`
--

LOCK TABLES `coupons` WRITE;
/*!40000 ALTER TABLE `coupons` DISABLE KEYS */;
INSERT INTO `coupons` VALUES (13,'新人50元券','fixed',50.00,200.00,'2026-06-04 22:19:08','2026-07-04 22:19:08',1000,0,'active','2026-06-04 22:19:07','2026-06-04 22:19:07'),(14,'满300减30','fixed',30.00,300.00,'2026-06-04 22:19:08','2026-06-19 22:19:08',500,0,'active','2026-06-04 22:19:07','2026-06-04 22:19:07'),(15,'满500减60','fixed',60.00,500.00,'2026-06-04 22:19:08','2026-06-11 22:19:08',300,0,'active','2026-06-04 22:19:07','2026-06-04 22:19:07'),(16,'全场9折券','percent',10.00,100.00,'2026-06-04 22:19:08','2026-06-24 22:19:08',800,0,'active','2026-06-04 22:19:07','2026-06-04 22:19:07');
/*!40000 ALTER TABLE `coupons` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `favorites`
--

DROP TABLE IF EXISTS `favorites`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorites` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=125 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorites`
--

LOCK TABLES `favorites` WRITE;
/*!40000 ALTER TABLE `favorites` DISABLE KEYS */;
INSERT INTO `favorites` VALUES (95,3,171,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(96,4,174,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(97,4,165,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(98,5,145,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(99,5,176,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(100,6,179,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(101,4,185,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(102,5,155,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(103,4,184,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(104,2,157,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(105,6,167,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(106,4,184,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(107,2,176,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(108,3,188,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(109,5,180,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(110,5,153,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(111,6,184,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(112,4,178,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(113,2,169,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(114,4,160,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(115,2,192,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(116,4,180,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(117,3,161,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(118,2,180,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(119,6,173,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(120,5,183,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(121,4,168,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(122,2,172,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(123,2,165,'2026-06-04 22:19:09','2026-06-04 22:19:09'),(124,6,179,'2026-06-04 22:19:09','2026-06-04 22:19:09');
/*!40000 ALTER TABLE `favorites` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feedbacks`
--

DROP TABLE IF EXISTS `feedbacks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedbacks` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `content` text NOT NULL,
  `reply` text,
  `status` varchar(20) NOT NULL DEFAULT 'pending',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedbacks`
--

LOCK TABLES `feedbacks` WRITE;
/*!40000 ALTER TABLE `feedbacks` DISABLE KEYS */;
INSERT INTO `feedbacks` VALUES (10,2,'希望能增加更多支付方式',NULL,'pending','2026-06-04 22:19:09','2026-06-04 22:19:09'),(11,3,'APP什么时候上线？',NULL,'pending','2026-06-04 22:19:09','2026-06-04 22:19:09'),(12,4,'商品质量很好，会继续支持',NULL,'pending','2026-06-04 22:19:09','2026-06-04 22:19:09');
/*!40000 ALTER TABLE `feedbacks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flash_sales`
--

DROP TABLE IF EXISTS `flash_sales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flash_sales` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `flash_price` decimal(10,2) NOT NULL,
  `stock` int NOT NULL DEFAULT '0',
  `sold` int NOT NULL DEFAULT '0',
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'active',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flash_sales`
--

LOCK TABLES `flash_sales` WRITE;
/*!40000 ALTER TABLE `flash_sales` DISABLE KEYS */;
INSERT INTO `flash_sales` VALUES (24,174,3954.95,47,10,'2026-06-04 22:19:09','2026-06-05 00:19:09','active','2026-06-04 22:19:09','2026-06-04 22:19:09'),(25,155,5106.96,36,5,'2026-06-04 22:19:09','2026-06-05 00:19:09','active','2026-06-04 22:19:09','2026-06-04 22:19:09'),(26,150,657.71,99,9,'2026-06-04 22:19:09','2026-06-05 00:19:09','active','2026-06-04 22:19:09','2026-06-04 22:19:09'),(27,168,6704.31,80,12,'2026-06-04 22:19:09','2026-06-05 22:19:09','active','2026-06-04 22:19:09','2026-06-04 22:19:09'),(28,145,2016.33,30,9,'2026-06-04 22:19:09','2026-06-05 22:19:09','active','2026-06-04 22:19:09','2026-06-04 22:19:09'),(29,178,4153.32,33,5,'2026-06-04 22:19:09','2026-06-05 22:19:09','active','2026-06-04 22:19:09','2026-06-04 22:19:09');
/*!40000 ALTER TABLE `flash_sales` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `id` int NOT NULL AUTO_INCREMENT,
  `from_user_id` int NOT NULL,
  `to_user_id` int NOT NULL,
  `content` text,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES (1,2,2,'订单什么时候发货？','2026-06-01 13:21:30','2026-06-01 13:21:30'),(2,2,2,'马上','2026-06-01 13:21:39','2026-06-01 13:21:39'),(3,23,23,'订单什么时候发货？','2026-06-01 14:45:46','2026-06-01 14:45:46'),(4,23,23,'很快','2026-06-01 14:45:54','2026-06-01 14:45:54'),(5,23,23,'henkuai','2026-06-02 14:39:55','2026-06-02 14:39:55'),(6,23,23,'你好','2026-06-02 14:40:05','2026-06-02 14:40:05'),(7,23,23,'你好','2026-06-02 14:40:10','2026-06-02 14:40:10'),(8,23,23,'什么时候发货','2026-06-02 14:45:25','2026-06-02 14:45:25'),(9,23,23,'请稍等，我帮您查询一下。','2026-06-02 14:45:31','2026-06-02 14:45:31'),(10,23,23,'如有其他问题，随时联系我们。','2026-06-02 14:50:21','2026-06-02 14:50:21'),(11,23,23,'你好','2026-06-02 14:50:42','2026-06-02 14:50:42'),(12,23,23,'sb','2026-06-02 14:54:59','2026-06-02 14:54:59'),(13,23,23,'666','2026-06-02 14:55:17','2026-06-02 14:55:17'),(14,34,23,'Hello, when will my order ship?','2026-06-02 15:03:05','2026-06-02 15:03:05'),(15,23,34,'Your order will ship within 24 hours.','2026-06-02 15:03:11','2026-06-02 15:03:11'),(16,23,34,'您好，请问有什么可以帮您？','2026-06-03 23:01:23','2026-06-03 23:01:23'),(17,23,34,'请稍等，我帮您查询一下。','2026-06-03 23:01:34','2026-06-03 23:01:34'),(18,34,23,'sb','2026-06-03 23:01:54','2026-06-03 23:01:54'),(19,23,34,'请稍等，我帮您查询一下。','2026-06-03 23:02:12','2026-06-03 23:02:12');
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `product_id` int NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `product_snapshot` json DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=199 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (154,109,154,295.00,1,'{\"name\": \"ThinkPad X1 Carbon 商务笔记本\", \"images\": [\"https://placehold.co/600x600?text=ThinkPad+X1+Carbon+商务笔记本\"]}','2026-06-04 22:19:07','2026-06-04 22:19:07'),(155,110,175,9111.00,2,'{\"name\": \"专业跑步鞋\", \"images\": [\"https://placehold.co/600x600?text=专业跑步鞋\"]}','2026-06-04 22:19:07','2026-06-04 22:19:07'),(156,111,183,3482.00,1,'{\"name\": \"便携蓝牙音箱\", \"images\": [\"https://placehold.co/600x600?text=便携蓝牙音箱\"]}','2026-06-04 22:19:07','2026-06-04 22:19:07'),(157,112,145,3871.00,2,'{\"name\": \"iPhone 15 Pro Max 旗舰手机\", \"images\": [\"https://placehold.co/600x600?text=iPhone+15+Pro+Max+旗舰手机\"]}','2026-06-04 22:19:07','2026-06-04 22:19:07'),(158,113,178,7564.00,2,'{\"name\": \"20kg哑铃套装\", \"images\": [\"https://placehold.co/600x600?text=20kg哑铃套装\"]}','2026-06-04 22:19:07','2026-06-04 22:19:07'),(159,114,164,825.00,2,'{\"name\": \"有机绿茶\", \"images\": [\"https://placehold.co/600x600?text=有机绿茶\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(160,115,189,2982.00,1,'{\"name\": \"大号游戏鼠标垫\", \"images\": [\"https://placehold.co/600x600?text=大号游戏鼠标垫\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(161,116,150,1094.00,1,'{\"name\": \"Logitech MX Master 3S 无线鼠标\", \"images\": [\"https://placehold.co/600x600?text=Logitech+MX+Master+3S+无线鼠标\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(162,117,154,295.00,2,'{\"name\": \"ThinkPad X1 Carbon 商务笔记本\", \"images\": [\"https://placehold.co/600x600?text=ThinkPad+X1+Carbon+商务笔记本\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(163,118,190,6217.00,1,'{\"name\": \"桌面收纳套装\", \"images\": [\"https://placehold.co/600x600?text=桌面收纳套装\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(164,119,183,3482.00,3,'{\"name\": \"便携蓝牙音箱\", \"images\": [\"https://placehold.co/600x600?text=便携蓝牙音箱\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(165,120,168,9227.00,1,'{\"name\": \"北欧简约书桌\", \"images\": [\"https://placehold.co/600x600?text=北欧简约书桌\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(166,121,164,825.00,2,'{\"name\": \"有机绿茶\", \"images\": [\"https://placehold.co/600x600?text=有机绿茶\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(167,122,159,6117.00,1,'{\"name\": \"女士羊绒大衣\", \"images\": [\"https://placehold.co/600x600?text=女士羊绒大衣\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(168,123,149,4801.00,3,'{\"name\": \"Sony WH-1000XM5 头戴式降噪耳机\", \"images\": [\"https://placehold.co/600x600?text=Sony+WH-1000XM5+头戴式降噪耳机\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(169,124,145,3871.00,3,'{\"name\": \"iPhone 15 Pro Max 旗舰手机\", \"images\": [\"https://placehold.co/600x600?text=iPhone+15+Pro+Max+旗舰手机\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(170,124,150,1094.00,1,'{\"name\": \"Logitech MX Master 3S 无线鼠标\", \"images\": [\"https://placehold.co/600x600?text=Logitech+MX+Master+3S+无线鼠标\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(171,125,153,5775.00,3,'{\"name\": \"Dell XPS 15 笔记本电脑\", \"images\": [\"https://placehold.co/600x600?text=Dell+XPS+15+笔记本电脑\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(172,125,171,7742.00,1,'{\"name\": \"玻尿酸精华液\", \"images\": [\"https://placehold.co/600x600?text=玻尿酸精华液\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(173,126,176,8057.00,2,'{\"name\": \"优质瑜伽垫\", \"images\": [\"https://placehold.co/600x600?text=优质瑜伽垫\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(174,126,188,7139.00,2,'{\"name\": \"1080p高清摄像头\", \"images\": [\"https://placehold.co/600x600?text=1080p高清摄像头\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(175,127,192,2998.00,2,'{\"name\": \"专业吹风机\", \"images\": [\"https://placehold.co/600x600?text=专业吹风机\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(176,127,151,444.00,1,'{\"name\": \"iPad Air M2 平板电脑\", \"images\": [\"https://placehold.co/600x600?text=iPad+Air+M2+平板电脑\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(177,128,158,1386.00,1,'{\"name\": \"7合1 USB-C扩展坞\", \"images\": [\"https://placehold.co/600x600?text=7合1+USB-C扩展坞\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(178,128,186,6528.00,1,'{\"name\": \"手机保护壳\", \"images\": [\"https://placehold.co/600x600?text=手机保护壳\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(179,129,182,9287.00,3,'{\"name\": \"书法练习字帖\", \"images\": [\"https://placehold.co/600x600?text=书法练习字帖\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(180,129,169,4141.00,1,'{\"name\": \"记忆棉枕头\", \"images\": [\"https://placehold.co/600x600?text=记忆棉枕头\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(181,130,161,4863.00,2,'{\"name\": \"超轻跑步鞋\", \"images\": [\"https://placehold.co/600x600?text=超轻跑步鞋\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(182,130,165,9958.00,2,'{\"name\": \"挪威新鲜三文鱼\", \"images\": [\"https://placehold.co/600x600?text=挪威新鲜三文鱼\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(183,131,192,2998.00,2,'{\"name\": \"专业吹风机\", \"images\": [\"https://placehold.co/600x600?text=专业吹风机\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(184,131,165,9958.00,2,'{\"name\": \"挪威新鲜三文鱼\", \"images\": [\"https://placehold.co/600x600?text=挪威新鲜三文鱼\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(185,132,175,9111.00,2,'{\"name\": \"专业跑步鞋\", \"images\": [\"https://placehold.co/600x600?text=专业跑步鞋\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(186,132,161,4863.00,1,'{\"name\": \"超轻跑步鞋\", \"images\": [\"https://placehold.co/600x600?text=超轻跑步鞋\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(187,133,149,4801.00,2,'{\"name\": \"Sony WH-1000XM5 头戴式降噪耳机\", \"images\": [\"https://placehold.co/600x600?text=Sony+WH-1000XM5+头戴式降噪耳机\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(188,133,161,4863.00,1,'{\"name\": \"超轻跑步鞋\", \"images\": [\"https://placehold.co/600x600?text=超轻跑步鞋\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(189,134,192,2998.00,3,'{\"name\": \"专业吹风机\", \"images\": [\"https://placehold.co/600x600?text=专业吹风机\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(190,134,178,7564.00,2,'{\"name\": \"20kg哑铃套装\", \"images\": [\"https://placehold.co/600x600?text=20kg哑铃套装\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(191,135,171,7742.00,2,'{\"name\": \"玻尿酸精华液\", \"images\": [\"https://placehold.co/600x600?text=玻尿酸精华液\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(192,135,176,8057.00,2,'{\"name\": \"优质瑜伽垫\", \"images\": [\"https://placehold.co/600x600?text=优质瑜伽垫\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(193,136,170,5840.00,3,'{\"name\": \"智能LED灯泡\", \"images\": [\"https://placehold.co/600x600?text=智能LED灯泡\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(194,136,173,9328.00,2,'{\"name\": \"哑光唇膏套装\", \"images\": [\"https://placehold.co/600x600?text=哑光唇膏套装\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(195,137,186,6528.00,2,'{\"name\": \"手机保护壳\", \"images\": [\"https://placehold.co/600x600?text=手机保护壳\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(196,137,168,9227.00,1,'{\"name\": \"北欧简约书桌\", \"images\": [\"https://placehold.co/600x600?text=北欧简约书桌\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(197,138,179,862.00,1,'{\"name\": \"2024年度畅销小说\", \"images\": [\"https://placehold.co/600x600?text=2024年度畅销小说\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08'),(198,138,166,2491.00,3,'{\"name\": \"澳洲牛排\", \"images\": [\"https://placehold.co/600x600?text=澳洲牛排\"]}','2026-06-04 22:19:08','2026-06-04 22:19:08');
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL,
  `user_id` int NOT NULL,
  `address_snapshot` json DEFAULT NULL,
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `discount_amount` decimal(10,2) DEFAULT '0.00',
  `coupon_id` int DEFAULT NULL,
  `payment_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `status` varchar(30) NOT NULL DEFAULT 'pending_payment',
  `payment_method` varchar(20) DEFAULT NULL,
  `payment_time` datetime DEFAULT NULL,
  `shipping_time` datetime DEFAULT NULL,
  `receive_time` datetime DEFAULT NULL,
  `tracking_no` varchar(100) DEFAULT NULL,
  `pay_token` varchar(64) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `shipping_company` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_no` (`order_no`),
  UNIQUE KEY `idx_pay_token` (`pay_token`)
) ENGINE=InnoDB AUTO_INCREMENT=139 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (109,'EC20260604221907OAB1D6',5,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',885.00,0.00,NULL,860.00,'pending_payment','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-05 22:19:08','2026-06-04 22:19:07',NULL),(110,'EC20260604221907VCEQVJ',4,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',18222.00,0.00,NULL,18208.00,'pending_payment','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-06 22:19:08','2026-06-04 22:19:07',NULL),(111,'EC20260604221907ZG07AJ',5,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',6964.00,0.00,NULL,6947.00,'pending_shipment','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-07 22:19:08','2026-06-04 22:19:07',NULL),(112,'EC202606042219074WVH4M',2,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',7742.00,0.00,NULL,7734.00,'pending_shipment','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-08 22:19:08','2026-06-04 22:19:07',NULL),(113,'EC20260604221907JQ81MB',3,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',7564.00,0.00,NULL,7538.00,'shipped','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-09 22:19:08','2026-06-04 22:19:07',NULL),(114,'EC2026060422190865I5K2',2,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',825.00,0.00,NULL,796.00,'shipped','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-10 22:19:08','2026-06-04 22:19:08',NULL),(115,'EC20260604221908ADREH1',3,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',2982.00,0.00,NULL,2967.00,'shipped','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-11 22:19:08','2026-06-04 22:19:08',NULL),(116,'EC20260604221908TTCX6K',6,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',1094.00,0.00,NULL,1094.00,'completed','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-12 22:19:08','2026-06-04 22:19:08',NULL),(117,'EC20260604221908MR3GD4',5,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',590.00,0.00,NULL,572.00,'completed','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-13 22:19:08','2026-06-04 22:19:08',NULL),(118,'EC20260604221908AAN1EO',4,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',18651.00,0.00,NULL,18626.00,'completed','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-14 22:19:08','2026-06-04 22:19:08',NULL),(119,'EC2026060422190856E4X7',3,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',10446.00,0.00,NULL,10424.00,'cancelled','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-15 22:19:08','2026-06-04 22:19:08',NULL),(120,'EC202606042219083KJ1NZ',3,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',18454.00,0.00,NULL,18427.00,'pending_payment','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-16 22:19:08','2026-06-04 22:19:08',NULL),(121,'EC202606042219084CCEM4',3,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',1650.00,0.00,NULL,1640.00,'pending_shipment','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-17 22:19:08','2026-06-04 22:19:08',NULL),(122,'EC20260604221908E78COE',3,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',18351.00,0.00,NULL,18347.00,'shipped','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-18 22:19:08','2026-06-04 22:19:08',NULL),(123,'EC20260604221908I7LXNA',3,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',14403.00,0.00,NULL,14374.00,'completed','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-19 22:19:08','2026-06-04 22:19:08',NULL),(124,'EC202606042219086KNR6V',2,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',9930.00,0.00,NULL,9912.00,'pending_payment','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-20 22:19:08','2026-06-04 22:19:08',NULL),(125,'EC20260604221908JXJKO8',4,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',13517.00,0.00,NULL,13498.00,'pending_shipment','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-21 22:19:08','2026-06-04 22:19:08',NULL),(126,'EC20260604221908RKOCEU',3,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',29474.00,0.00,NULL,29472.00,'shipped','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-22 22:19:08','2026-06-04 22:19:08',NULL),(127,'EC20260604221908DAIUCV',6,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',9438.00,0.00,NULL,9427.00,'completed','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-23 22:19:08','2026-06-04 22:19:08',NULL),(128,'EC20260604221908R1IQQQ',6,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',15828.00,0.00,NULL,15821.00,'cancelled','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-24 22:19:08','2026-06-04 22:19:08',NULL),(129,'EC202606042219081X8T90',4,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',40284.00,0.00,NULL,40282.00,'pending_payment','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-25 22:19:08','2026-06-04 22:19:08',NULL),(130,'EC20260604221908KW6E1A',3,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',19684.00,0.00,NULL,19672.00,'pending_payment','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-26 22:19:08','2026-06-04 22:19:08',NULL),(131,'EC202606042219083GFYLG',2,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',25912.00,0.00,NULL,25907.00,'shipped','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-27 22:19:08','2026-06-04 22:19:08',NULL),(132,'EC20260604221908DOE7SJ',6,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',37059.00,0.00,NULL,37055.00,'completed','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-28 22:19:08','2026-06-04 22:19:08',NULL),(133,'EC20260604221908IN6N8T',5,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',14527.00,0.00,NULL,14498.00,'completed','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-29 22:19:09','2026-06-04 22:19:08',NULL),(134,'EC20260604221908LIYMLQ',3,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',13560.00,0.00,NULL,13554.00,'pending_shipment','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-30 22:19:09','2026-06-04 22:19:08',NULL),(135,'EC20260604221908UP1XPM',6,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',15799.00,0.00,NULL,15788.00,'shipped','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-05-31 22:19:09','2026-06-04 22:19:08',NULL),(136,'EC20260604221908N2PEQS',2,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',26848.00,0.00,NULL,26828.00,'completed','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-01 22:19:09','2026-06-04 22:19:08',NULL),(137,'EC202606042219080KGL5Q',5,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',40737.00,0.00,NULL,40713.00,'pending_payment','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-02 22:19:09','2026-06-04 22:19:08',NULL),(138,'EC202606042219082PJ1BT',5,'{\"city\": \"北京市\", \"phone\": \"13800000000\", \"detail\": \"测试地址\", \"district\": \"朝阳区\", \"province\": \"北京市\", \"receiver\": \"收货人\"}',3353.00,0.00,NULL,3352.00,'completed','alipay',NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-03 22:19:09','2026-06-04 22:19:08',NULL);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `description` text,
  `detail` text,
  `category_id` int DEFAULT NULL,
  `price` decimal(10,2) NOT NULL DEFAULT '0.00',
  `original_price` decimal(10,2) DEFAULT NULL,
  `stock` int NOT NULL DEFAULT '0',
  `images` json DEFAULT NULL,
  `specs` json DEFAULT NULL,
  `tags` varchar(500) DEFAULT '[]',
  `status` varchar(20) NOT NULL DEFAULT 'on',
  `sales` int DEFAULT '0',
  `is_new` tinyint(1) DEFAULT '0',
  `is_hot` tinyint(1) DEFAULT '0',
  `is_promotion` tinyint(1) DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=193 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (145,'iPhone 15 Pro Max 旗舰手机','高品质iPhone 15 Pro Max 旗舰手机','<p>这是一款高品质的<strong>iPhone 15 Pro Max 旗舰手机</strong>，精选优质材料，严格品控，给您最好的体验。</p>',1,3871.00,5734.00,651,'[\"https://placehold.co/600x600?text=iPhone+15+Pro+Max+旗舰手机\", \"/uploads/1780591875505-3246.png\", \"/uploads/1780591879450-9512.png\", \"/uploads/1780591958958-3861.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',4155,1,0,0,'2026-06-04 22:19:07','2026-06-05 00:52:39'),(146,'Samsung Galaxy S24 Ultra 旗舰手机','高品质Samsung Galaxy S24 Ultra 旗舰手机','<p>这是一款高品质的<strong>Samsung Galaxy S24 Ultra 旗舰手机</strong>，精选优质材料，严格品控，给您最好的体验。</p>',1,2463.00,3188.00,315,'[\"https://placehold.co/600x600?text=Samsung+Galaxy+S24+Ultra+旗舰手机\", \"/uploads/1780591962553-7027.png\", \"/uploads/1780591964370-3847.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',4014,1,0,0,'2026-06-04 22:19:07','2026-06-05 00:52:44'),(147,'MacBook Pro 16 M3 笔记本电脑','高品质MacBook Pro 16 M3 笔记本电脑','<p>这是一款高品质的<strong>MacBook Pro 16 M3 笔记本电脑</strong>，精选优质材料，严格品控，给您最好的体验。</p>',2,3331.00,6294.00,107,'[\"https://placehold.co/600x600?text=MacBook+Pro+16+M3+笔记本电脑\", \"/uploads/1780592007676-7761.png\", \"/uploads/1780592014295-6498.png\", \"/uploads/1780592016350-1945.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',510,1,0,0,'2026-06-04 22:19:07','2026-06-05 00:53:36'),(148,'AirPods Pro 2 蓝牙耳机','高品质AirPods Pro 2 蓝牙耳机','<p>这是一款高品质的<strong>AirPods Pro 2 蓝牙耳机</strong>，精选优质材料，严格品控，给您最好的体验。</p>',1,1052.00,3698.00,127,'[\"https://placehold.co/600x600?text=AirPods+Pro+2+蓝牙耳机\", \"/uploads/1780592019330-481.png\", \"/uploads/1780592021538-4230.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',815,1,0,0,'2026-06-04 22:19:07','2026-06-05 00:53:42'),(149,'Sony WH-1000XM5 头戴式降噪耳机','高品质Sony WH-1000XM5 头戴式降噪耳机','<p>这是一款高品质的<strong>Sony WH-1000XM5 头戴式降噪耳机</strong>，精选优质材料，严格品控，给您最好的体验。</p>',1,4801.00,7623.00,806,'[\"https://placehold.co/600x600?text=Sony+WH-1000XM5+头戴式降噪耳机\", \"/uploads/1780592025119-3727.png\", \"/uploads/1780592027942-8576.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',4214,1,0,0,'2026-06-04 22:19:07','2026-06-05 00:53:48'),(150,'Logitech MX Master 3S 无线鼠标','高品质Logitech MX Master 3S 无线鼠标','<p>这是一款高品质的<strong>Logitech MX Master 3S 无线鼠标</strong>，精选优质材料，严格品控，给您最好的体验。</p>',2,1094.00,3232.00,486,'[\"https://placehold.co/600x600?text=Logitech+MX+Master+3S+无线鼠标\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',4669,1,1,0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(151,'iPad Air M2 平板电脑','高品质iPad Air M2 平板电脑','<p>这是一款高品质的<strong>iPad Air M2 平板电脑</strong>，精选优质材料，严格品控，给您最好的体验。</p>',1,444.00,2193.00,625,'[\"https://placehold.co/600x600?text=iPad+Air+M2+平板电脑\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',4492,1,1,0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(152,'Xiaomi 14 Pro 智能手机','高品质Xiaomi 14 Pro 智能手机','<p>这是一款高品质的<strong>Xiaomi 14 Pro 智能手机</strong>，精选优质材料，严格品控，给您最好的体验。</p>',1,3976.00,6028.00,359,'[\"https://placehold.co/600x600?text=Xiaomi+14+Pro+智能手机\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',4357,1,1,0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(153,'Dell XPS 15 笔记本电脑','高品质Dell XPS 15 笔记本电脑','<p>这是一款高品质的<strong>Dell XPS 15 笔记本电脑</strong>，精选优质材料，严格品控，给您最好的体验。</p>',2,5775.00,7425.00,809,'[\"https://placehold.co/600x600?text=Dell+XPS+15+笔记本电脑\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',1889,1,1,0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(154,'ThinkPad X1 Carbon 商务笔记本','高品质ThinkPad X1 Carbon 商务笔记本','<p>这是一款高品质的<strong>ThinkPad X1 Carbon 商务笔记本</strong>，精选优质材料，严格品控，给您最好的体验。</p>',2,295.00,1098.00,731,'[\"https://placehold.co/600x600?text=ThinkPad+X1+Carbon+商务笔记本\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',3755,1,1,0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(155,'RGB机械键盘','高品质RGB机械键盘','<p>这是一款高品质的<strong>RGB机械键盘</strong>，精选优质材料，严格品控，给您最好的体验。</p>',2,9469.00,9782.00,703,'[\"https://placehold.co/600x600?text=RGB机械键盘\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',4409,0,1,0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(156,'27英寸4K显示器','高品质27英寸4K显示器','<p>这是一款高品质的<strong>27英寸4K显示器</strong>，精选优质材料，严格品控，给您最好的体验。</p>',2,7163.00,9161.00,358,'[\"https://placehold.co/600x600?text=27英寸4K显示器\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',3547,0,1,0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(157,'无线鼠标','高品质无线鼠标','<p>这是一款高品质的<strong>无线鼠标</strong>，精选优质材料，严格品控，给您最好的体验。</p>',2,1350.00,4181.00,289,'[\"https://placehold.co/600x600?text=无线鼠标\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',2242,0,1,0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(158,'7合1 USB-C扩展坞','高品质7合1 USB-C扩展坞','<p>这是一款高品质的<strong>7合1 USB-C扩展坞</strong>，精选优质材料，严格品控，给您最好的体验。</p>',2,1386.00,2675.00,902,'[\"https://placehold.co/600x600?text=7合1+USB-C扩展坞\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',1750,0,1,0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(159,'女士羊绒大衣','高品质女士羊绒大衣','<p>这是一款高品质的<strong>女士羊绒大衣</strong>，精选优质材料，严格品控，给您最好的体验。</p>',5,6117.00,8129.00,557,'[\"https://placehold.co/600x600?text=女士羊绒大衣\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',835,0,1,0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(160,'男士商务西装套装','高品质男士商务西装套装','<p>这是一款高品质的<strong>男士商务西装套装</strong>，精选优质材料，严格品控，给您最好的体验。</p>',5,5377.00,6377.00,52,'[\"https://placehold.co/600x600?text=男士商务西装套装\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',3657,0,0,0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(161,'超轻跑步鞋','高品质超轻跑步鞋','<p>这是一款高品质的<strong>超轻跑步鞋</strong>，精选优质材料，严格品控，给您最好的体验。</p>',10,4863.00,6406.00,621,'[\"https://placehold.co/600x600?text=超轻跑步鞋\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',3359,0,0,0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(162,'优质纯棉T恤','高品质优质纯棉T恤','<p>这是一款高品质的<strong>优质纯棉T恤</strong>，精选优质材料，严格品控，给您最好的体验。</p>',5,4984.00,6721.00,691,'[\"https://placehold.co/600x600?text=优质纯棉T恤\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',2452,0,0,0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(163,'混合坚果礼盒','高品质混合坚果礼盒','<p>这是一款高品质的<strong>混合坚果礼盒</strong>，精选优质材料，严格品控，给您最好的体验。</p>',7,1285.00,4155.00,407,'[\"https://placehold.co/600x600?text=混合坚果礼盒\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',3249,0,0,0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(164,'有机绿茶','高品质有机绿茶','<p>这是一款高品质的<strong>有机绿茶</strong>，精选优质材料，严格品控，给您最好的体验。</p>',7,825.00,2453.00,692,'[\"https://placehold.co/600x600?text=有机绿茶\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',854,0,0,0,'2026-06-04 22:19:07','2026-06-04 22:19:07'),(165,'挪威新鲜三文鱼','高品质挪威新鲜三文鱼','<p>这是一款高品质的<strong>挪威新鲜三文鱼</strong>，精选优质材料，严格品控，给您最好的体验。</p>',13,9958.00,10275.00,886,'[\"https://placehold.co/600x600?text=挪威新鲜三文鱼\", \"/uploads/1780592041854-7920.png\", \"/uploads/1780592044561-3569.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',1832,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:54:05'),(166,'澳洲牛排','高品质澳洲牛排','<p>这是一款高品质的<strong>澳洲牛排</strong>，精选优质材料，严格品控，给您最好的体验。</p>',13,2491.00,3198.00,518,'[\"https://placehold.co/600x600?text=澳洲牛排\", \"/uploads/1780592046264-3166.png\", \"/uploads/1780592047872-8760.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',3827,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:54:08'),(167,'北欧落地灯','高品质北欧落地灯','<p>这是一款高品质的<strong>北欧落地灯</strong>，精选优质材料，严格品控，给您最好的体验。</p>',5,8935.00,9138.00,805,'[\"https://placehold.co/600x600?text=北欧落地灯\", \"/uploads/1780592050478-3164.png\", \"/uploads/1780592052619-287.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',1282,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:54:13'),(168,'北欧简约书桌','高品质北欧简约书桌','<p>这是一款高品质的<strong>北欧简约书桌</strong>，精选优质材料，严格品控，给您最好的体验。</p>',5,9227.00,9975.00,944,'[\"https://placehold.co/600x600?text=北欧简约书桌\", \"/uploads/1780592082802-9083.png\", \"/uploads/1780592084574-8914.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',2864,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:54:45'),(169,'记忆棉枕头','高品质记忆棉枕头','<p>这是一款高品质的<strong>记忆棉枕头</strong>，精选优质材料，严格品控，给您最好的体验。</p>',5,4141.00,6519.00,740,'[\"https://placehold.co/600x600?text=记忆棉枕头\", \"/uploads/1780592087481-1904.png\", \"/uploads/1780592090033-3899.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',4434,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:54:50'),(170,'智能LED灯泡','高品质智能LED灯泡','<p>这是一款高品质的<strong>智能LED灯泡</strong>，精选优质材料，严格品控，给您最好的体验。</p>',5,5840.00,5844.00,212,'[\"https://placehold.co/600x600?text=智能LED灯泡\", \"/uploads/1780592091950-7942.png\", \"/uploads/1780592093454-1875.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',1963,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:54:53'),(171,'玻尿酸精华液','高品质玻尿酸精华液','<p>这是一款高品质的<strong>玻尿酸精华液</strong>，精选优质材料，严格品控，给您最好的体验。</p>',6,7742.00,9884.00,604,'[\"https://placehold.co/600x600?text=玻尿酸精华液\", \"/uploads/1780592097044-6787.png\", \"/uploads/1780592098833-7220.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',1499,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:54:59'),(172,'维生素C亮肤面霜','高品质维生素C亮肤面霜','<p>这是一款高品质的<strong>维生素C亮肤面霜</strong>，精选优质材料，严格品控，给您最好的体验。</p>',6,4958.00,7001.00,279,'[\"https://placehold.co/600x600?text=维生素C亮肤面霜\", \"/uploads/1780592100326-2860.png\", \"/uploads/1780592101613-4146.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',1237,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:55:02'),(173,'哑光唇膏套装','高品质哑光唇膏套装','<p>这是一款高品质的<strong>哑光唇膏套装</strong>，精选优质材料，严格品控，给您最好的体验。</p>',9,9328.00,11850.00,539,'[\"https://placehold.co/600x600?text=哑光唇膏套装\", \"/uploads/1780592105097-3881.png\", \"/uploads/1780592106742-9116.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',3060,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:55:07'),(174,'抗皱眼霜','高品质抗皱眼霜','<p>这是一款高品质的<strong>抗皱眼霜</strong>，精选优质材料，严格品控，给您最好的体验。</p>',6,7071.00,9083.00,140,'[\"https://placehold.co/600x600?text=抗皱眼霜\", \"/uploads/1780592108489-5999.png\", \"/uploads/1780592109843-6639.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',2121,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:55:10'),(175,'专业跑步鞋','高品质专业跑步鞋','<p>这是一款高品质的<strong>专业跑步鞋</strong>，精选优质材料，严格品控，给您最好的体验。</p>',10,9111.00,11599.00,947,'[\"https://placehold.co/600x600?text=专业跑步鞋\", \"/uploads/1780592112888-7095.png\", \"/uploads/1780592114547-8569.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',2707,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:55:15'),(176,'优质瑜伽垫','高品质优质瑜伽垫','<p>这是一款高品质的<strong>优质瑜伽垫</strong>，精选优质材料，严格品控，给您最好的体验。</p>',10,8057.00,9828.00,194,'[\"https://placehold.co/600x600?text=优质瑜伽垫\", \"/uploads/1780592116947-9418.png\", \"/uploads/1780592118049-1802.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',1060,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:55:18'),(177,'弹力带套装','高品质弹力带套装','<p>这是一款高品质的<strong>弹力带套装</strong>，精选优质材料，严格品控，给您最好的体验。</p>',11,2011.00,4658.00,789,'[\"https://placehold.co/600x600?text=弹力带套装\", \"/uploads/1780592120450-7879.png\", \"/uploads/1780592122173-7791.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',580,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:55:22'),(178,'20kg哑铃套装','高品质20kg哑铃套装','<p>这是一款高品质的<strong>20kg哑铃套装</strong>，精选优质材料，严格品控，给您最好的体验。</p>',12,7564.00,9027.00,819,'[\"https://placehold.co/600x600?text=20kg哑铃套装\", \"/uploads/1780592123999-8086.png\", \"/uploads/1780592125789-736.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',973,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:55:26'),(179,'2024年度畅销小说','高品质2024年度畅销小说','<p>这是一款高品质的<strong>2024年度畅销小说</strong>，精选优质材料，严格品控，给您最好的体验。</p>',12,862.00,1010.00,858,'[\"https://placehold.co/600x600?text=2024年度畅销小说\", \"/uploads/1780592128235-522.png\", \"/uploads/1780592131172-1364.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',3370,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:55:31'),(180,'Python编程入门','高品质Python编程入门','<p>这是一款高品质的<strong>Python编程入门</strong>，精选优质材料，严格品控，给您最好的体验。</p>',12,7925.00,9084.00,936,'[\"https://placehold.co/600x600?text=Python编程入门\", \"/uploads/1780592133268-5506.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',3828,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:55:33'),(181,'素描艺术套装','高品质素描艺术套装','<p>这是一款高品质的<strong>素描艺术套装</strong>，精选优质材料，严格品控，给您最好的体验。</p>',12,926.00,2282.00,419,'[\"https://placehold.co/600x600?text=素描艺术套装\", \"/uploads/1780592136303-1024.png\", \"/uploads/1780592139206-8445.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',2442,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:55:39'),(182,'书法练习字帖','高品质书法练习字帖','<p>这是一款高品质的<strong>书法练习字帖</strong>，精选优质材料，严格品控，给您最好的体验。</p>',12,9287.00,10326.00,186,'[\"https://placehold.co/600x600?text=书法练习字帖\", \"/uploads/1780592146500-1466.png\", \"/uploads/1780592152636-3500.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',2868,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:55:53'),(183,'便携蓝牙音箱','高品质便携蓝牙音箱','<p>这是一款高品质的<strong>便携蓝牙音箱</strong>，精选优质材料，严格品控，给您最好的体验。</p>',1,3482.00,3879.00,531,'[\"https://placehold.co/600x600?text=便携蓝牙音箱\", \"/uploads/1780592154918-5148.png\", \"/uploads/1780592158741-4704.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',3644,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:55:59'),(184,'20000mAh充电宝','高品质20000mAh充电宝','<p>这是一款高品质的<strong>20000mAh充电宝</strong>，精选优质材料，严格品控，给您最好的体验。</p>',1,6472.00,9402.00,196,'[\"https://placehold.co/600x600?text=20000mAh充电宝\", \"/uploads/1780592160185-3127.png\", \"/uploads/1780592162218-6160.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',1018,0,0,0,'2026-06-04 22:19:07','2026-06-05 00:56:02'),(185,'无线充电支架','高品质无线充电支架','<p>这是一款高品质的<strong>无线充电支架</strong>，精选优质材料，严格品控，给您最好的体验。</p>',1,8341.00,10367.00,946,'[\"https://placehold.co/600x600?text=无线充电支架\", \"/uploads/1780592165332-1862.png\", \"/uploads/1780592167777-1711.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',1075,0,0,1,'2026-06-04 22:19:07','2026-06-05 00:56:08'),(186,'手机保护壳','高品质手机保护壳','<p>这是一款高品质的<strong>手机保护壳</strong>，精选优质材料，严格品控，给您最好的体验。</p>',1,6528.00,9086.00,770,'[\"https://placehold.co/600x600?text=手机保护壳\", \"/uploads/1780592170322-2401.png\", \"/uploads/1780592172314-7988.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',1861,0,0,1,'2026-06-04 22:19:07','2026-06-05 00:56:12'),(187,'铝合金笔记本支架','高品质铝合金笔记本支架','<p>这是一款高品质的<strong>铝合金笔记本支架</strong>，精选优质材料，严格品控，给您最好的体验。</p>',2,8495.00,9637.00,974,'[\"https://placehold.co/600x600?text=铝合金笔记本支架\", \"/uploads/1780592176875-4252.png\", \"/uploads/1780592179353-2094.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',1522,0,0,1,'2026-06-04 22:19:07','2026-06-05 00:56:19'),(188,'1080p高清摄像头','高品质1080p高清摄像头','<p>这是一款高品质的<strong>1080p高清摄像头</strong>，精选优质材料，严格品控，给您最好的体验。</p>',2,7139.00,9248.00,770,'[\"https://placehold.co/600x600?text=1080p高清摄像头\", \"/uploads/1780592181646-2847.png\", \"/uploads/1780592183517-4993.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',1982,0,0,1,'2026-06-04 22:19:07','2026-06-05 00:56:24'),(189,'大号游戏鼠标垫','高品质大号游戏鼠标垫','<p>这是一款高品质的<strong>大号游戏鼠标垫</strong>，精选优质材料，严格品控，给您最好的体验。</p>',2,2982.00,3211.00,142,'[\"https://placehold.co/600x600?text=大号游戏鼠标垫\", \"/uploads/1780592193609-9953.png\", \"/uploads/1780592196083-4429.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',14,0,0,1,'2026-06-04 22:19:07','2026-06-05 00:56:36'),(190,'桌面收纳套装','高品质桌面收纳套装','<p>这是一款高品质的<strong>桌面收纳套装</strong>，精选优质材料，严格品控，给您最好的体验。</p>',2,6217.00,8321.00,227,'[\"https://placehold.co/600x600?text=桌面收纳套装\", \"/uploads/1780592214049-9094.png\", \"/uploads/1780592215961-3286.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',911,0,0,1,'2026-06-04 22:19:07','2026-06-05 00:56:56'),(191,'电动牙刷','高品质电动牙刷','<p>这是一款高品质的<strong>电动牙刷</strong>，精选优质材料，严格品控，给您最好的体验。</p>',3,4322.00,4418.00,224,'[\"https://placehold.co/600x600?text=电动牙刷\", \"/uploads/1780592240830-7282.png\", \"/uploads/1780592243040-7853.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',504,0,0,1,'2026-06-04 22:19:07','2026-06-05 00:57:23'),(192,'专业吹风机','高品质专业吹风机','<p>这是一款高品质的<strong>专业吹风机</strong>，精选优质材料，严格品控，给您最好的体验。</p>',3,2998.00,3626.00,855,'[\"https://placehold.co/600x600?text=专业吹风机\", \"/uploads/1780592236660-4464.png\", \"/uploads/1780592238334-79.png\"]','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"白色\", \"蓝色\"]}, {\"name\": \"规格\", \"values\": [\"标准版\", \"Pro版\"]}]','[]','on',2678,0,0,1,'2026-06-04 22:19:07','2026-06-05 00:57:18');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `order_id` int DEFAULT NULL,
  `rating` int NOT NULL DEFAULT '5',
  `content` text,
  `images` json DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=203 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reviews`
--

LOCK TABLES `reviews` WRITE;
/*!40000 ALTER TABLE `reviews` DISABLE KEYS */;
INSERT INTO `reviews` VALUES (153,74,157,NULL,4,'使用了一段时间，感觉很满意。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(154,86,161,NULL,3,'性价比很高，推荐购买。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(155,79,163,NULL,4,'使用了一段时间，感觉很满意。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(156,72,192,NULL,3,'很好用，质量不错！',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(157,74,179,NULL,5,'很好用，质量不错！',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(158,73,158,NULL,5,'很好用，质量不错！',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(159,84,167,NULL,3,'很好用，质量不错！',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(160,76,176,NULL,3,'很好用，质量不错！',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(161,74,158,NULL,5,'很好用，质量不错！',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(162,88,187,NULL,4,'使用了一段时间，感觉很满意。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(163,86,151,NULL,5,'性价比很高，推荐购买。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(164,69,186,NULL,4,'很好用，质量不错！',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(165,77,164,NULL,3,'很好用，质量不错！',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(166,70,149,NULL,5,'性价比很高，推荐购买。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(167,70,184,NULL,3,'使用了一段时间，感觉很满意。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(168,80,154,NULL,5,'使用了一段时间，感觉很满意。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(169,79,185,NULL,4,'物流很快，包装完好。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(170,88,183,NULL,5,'很好用，质量不错！',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(171,79,190,NULL,3,'性价比很高，推荐购买。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(172,84,157,NULL,4,'颜色和图片一致，非常好看。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(173,83,176,NULL,3,'颜色和图片一致，非常好看。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(174,70,175,NULL,5,'性价比很高，推荐购买。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(175,82,157,NULL,4,'性价比很高，推荐购买。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(176,86,176,NULL,4,'颜色和图片一致，非常好看。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(177,76,192,NULL,5,'物流很快，包装完好。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(178,74,168,NULL,3,'性价比很高，推荐购买。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(179,69,170,NULL,3,'很好用，质量不错！',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(180,72,175,NULL,5,'颜色和图片一致，非常好看。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(181,83,172,NULL,5,'物流很快，包装完好。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(182,72,192,NULL,3,'物流很快，包装完好。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(183,74,154,NULL,4,'性价比很高，推荐购买。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(184,75,167,NULL,5,'很好用，质量不错！',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(185,71,153,NULL,3,'物流很快，包装完好。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(186,70,147,NULL,4,'物流很快，包装完好。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(187,72,160,NULL,5,'使用了一段时间，感觉很满意。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(188,77,164,NULL,3,'性价比很高，推荐购买。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(189,89,170,NULL,3,'很好用，质量不错！',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(190,75,189,NULL,3,'性价比很高，推荐购买。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(191,73,146,NULL,3,'物流很快，包装完好。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(192,80,146,NULL,5,'物流很快，包装完好。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(193,81,150,NULL,5,'颜色和图片一致，非常好看。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(194,89,171,NULL,4,'物流很快，包装完好。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(195,76,165,NULL,3,'颜色和图片一致，非常好看。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(196,69,159,NULL,4,'颜色和图片一致，非常好看。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(197,73,160,NULL,5,'物流很快，包装完好。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(198,82,185,NULL,5,'性价比很高，推荐购买。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(199,75,155,NULL,3,'很好用，质量不错！',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(200,74,166,NULL,3,'物流很快，包装完好。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(201,78,157,NULL,5,'颜色和图片一致，非常好看。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08'),(202,69,181,NULL,5,'使用了一段时间，感觉很满意。',NULL,'2026-06-04 22:19:08','2026-06-04 22:19:08');
/*!40000 ALTER TABLE `reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) DEFAULT '',
  `password_hash` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT '',
  `role` varchar(20) NOT NULL DEFAULT 'user',
  `status` varchar(20) NOT NULL DEFAULT 'active',
  `avatar` varchar(255) DEFAULT '/default-avatar.png',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `uid` varchar(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uid` (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=90 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (69,'admin','admin@shop.com','$2a$10$sDVFD.OoaN/.z05wUa39gefN7U5DahX9i9/fBcAiLsw8j5MFv/G0a','13800000001','admin','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(70,'user1','user1@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000001','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(71,'user2','user2@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000002','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(72,'user3','user3@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000003','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(73,'user4','user4@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000004','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(74,'user5','user5@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000005','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(75,'user6','user6@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000006','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(76,'user7','user7@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000007','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(77,'user8','user8@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000008','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(78,'user9','user9@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000009','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(79,'user10','user10@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000010','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(80,'user11','user11@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000011','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(81,'user12','user12@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000012','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(82,'user13','user13@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000013','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(83,'user14','user14@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000014','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(84,'user15','user15@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000015','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(85,'user16','user16@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000016','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(86,'user17','user17@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000017','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(87,'user18','user18@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000018','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(88,'user19','user19@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000019','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL),(89,'user20','user20@test.com','$2a$10$TBVHdyw4VZab7uWy1wTaQeJtVB85zjngO1D4Exefxnkg1cbuTZy9m','13800000020','user','active','/default-avatar.png','2026-06-04 22:19:07','2026-06-04 22:19:07',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'ecommerce'
--

--
-- Dumping routines for database 'ecommerce'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-05  1:00:53
