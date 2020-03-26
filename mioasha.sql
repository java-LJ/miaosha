/*
SQLyog Ultimate v12.09 (64 bit)
MySQL - 5.7.26 : Database - miaosha
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`miaosha` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `miaosha`;

/*Table structure for table `goods` */

DROP TABLE IF EXISTS `goods`;

CREATE TABLE `goods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `goods_name` varchar(16) DEFAULT NULL,
  `goods_title` varchar(64) DEFAULT NULL,
  `goods_img` varchar(64) DEFAULT NULL,
  `goods_detail` longtext,
  `goods_price` decimal(10,2) DEFAULT '0.00',
  `goods_stock` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

/*Data for the table `goods` */

insert  into `goods`(`id`,`goods_name`,`goods_title`,`goods_img`,`goods_detail`,`goods_price`,`goods_stock`) values (1,'iphoneX','Apple iPhone X 64G 银色','/img/iphonex.png','Aphone (A1865)','5000.00',100),(2,'华为Meta 9','华为Meta 9','/img/meta10.png','Meta 9','5200.00',50),(3,'oppo R9','oppo R9','/img/iphonex.png','oppo ','3000.00',10),(4,'小米8','小米8','/img/mi6.png','小米8','2000.00',100),(5,'小米8-1','小米8-1','/img/iphonex.png','小米8','10000.00',20),(6,'锤子','锤子','/img/iphone8.png','锤子','20000.00',30);

/*Table structure for table `miaosha_goods` */

DROP TABLE IF EXISTS `miaosha_goods`;

CREATE TABLE `miaosha_goods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `goods_id` bigint(20) DEFAULT NULL,
  `miaosha_price` decimal(10,2) DEFAULT '0.00',
  `stock_count` int(11) DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

/*Data for the table `miaosha_goods` */

insert  into `miaosha_goods`(`id`,`goods_id`,`miaosha_price`,`stock_count`,`start_date`,`end_date`) values (1,1,'0.01',8,'2020-02-16 16:06:12','2020-02-29 15:50:59'),(2,2,'0.01',7,'2020-02-21 11:10:12','2020-03-11 15:51:31'),(3,3,'0.09',5,'2019-05-30 11:10:12','2019-05-30 19:50:15'),(4,4,'0.04',10,'2019-05-22 19:52:54','2019-05-30 19:52:58'),(5,5,'1.00',10,'2019-05-25 19:58:48','2019-05-30 19:58:51'),(6,6,'0.07',5,'2019-05-29 19:59:19','2019-05-30 19:59:26');

/*Table structure for table `miaosha_order` */

DROP TABLE IF EXISTS `miaosha_order`;

CREATE TABLE `miaosha_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  `goods_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `u_uid_gid` (`user_id`,`goods_id`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=369 DEFAULT CHARSET=utf8;

/*Data for the table `miaosha_order` */

insert  into `miaosha_order`(`id`,`user_id`,`order_id`,`goods_id`) values (368,15200000000,617,1);

/*Table structure for table `miaosha_user` */

DROP TABLE IF EXISTS `miaosha_user`;

CREATE TABLE `miaosha_user` (
  `id` bigint(20) NOT NULL,
  `nickname` varchar(255) NOT NULL,
  `password` varchar(32) DEFAULT NULL,
  `salt` varchar(45) DEFAULT NULL,
  `head` varchar(128) DEFAULT NULL,
  `register_date` datetime DEFAULT NULL,
  `last_login_date` datetime DEFAULT NULL,
  `login_count` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Data for the table `miaosha_user` */

insert  into `miaosha_user`(`id`,`nickname`,`password`,`salt`,`head`,`register_date`,`last_login_date`,`login_count`) values (15200000000,'user0','01aec3b9e7c87b34fc50d54fa7933027','d1a814e67cb6751fe050843bb7ef69a3',NULL,'2020-02-21 14:49:00',NULL,1),(15200000001,'user1','f4308c7c6a645ea9b8e77b40b000d4a1','4e3dbb72a7f92ac452d4983f8c7b8c2b',NULL,'2020-02-21 14:49:00',NULL,1),(15200000002,'user2','aefb48036517a60683bf55b8ad3a4b78','1524b894702deedb38e6e98ca681c8f7',NULL,'2020-02-21 14:49:00',NULL,1),(15200000003,'user3','db9c3b4425597aa4796c188fecdb8640','2d237abe5a2f85e0edfbe51482a20486',NULL,'2020-02-21 14:49:00',NULL,1),(15200000004,'user4','0e17b103f1b1364db6418f605c8869cd','5b41d1039c38adaed0cf06cba0bf21f3',NULL,'2020-02-21 14:49:00',NULL,1),(15200000005,'user5','01d5e30f5175a8e5d400dbccc246ec24','462323b864e4b771575988ee9a2ff53d',NULL,'2020-02-21 14:49:00',NULL,1),(15200000006,'user6','58590abe8858c2d2a454d47031508767','3c865c50db46e2c2ab23d7a46514306c',NULL,'2020-02-21 14:49:00',NULL,1),(15200000007,'user7','16b087c10529dfc9c2748c8b618a036c','de71b2cee7f43ec6f8280a7b648918b9',NULL,'2020-02-21 14:49:00',NULL,1),(15200000008,'user8','ce8e253af93a6dd5b1e1428fc440384d','0347add28ebeb35d488a5b3cd6ecbaa4',NULL,'2020-02-21 14:49:00',NULL,1),(15200000009,'user9','d45d0017364821139dd8f1c3fe23bdc7','1064199853f99477575f32626c172220',NULL,'2020-02-21 14:49:00',NULL,1);

/*Table structure for table `order_info` */

DROP TABLE IF EXISTS `order_info`;

CREATE TABLE `order_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `goods_id` bigint(20) DEFAULT NULL,
  `delivery_addr_id` bigint(20) DEFAULT NULL,
  `goods_name` varchar(16) DEFAULT NULL,
  `goods_count` int(11) DEFAULT '0',
  `goods_price` decimal(10,2) DEFAULT '0.00',
  `order_channel` tinyint(4) DEFAULT '0',
  `status` tinyint(4) DEFAULT '0',
  `create_date` datetime DEFAULT NULL,
  `pay_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=618 DEFAULT CHARSET=utf8;

/*Data for the table `order_info` */

insert  into `order_info`(`id`,`user_id`,`goods_id`,`delivery_addr_id`,`goods_name`,`goods_count`,`goods_price`,`order_channel`,`status`,`create_date`,`pay_date`) values (617,15200000000,1,NULL,'iphoneX',1,'0.01',1,0,'2020-02-26 12:39:48',NULL);

/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `name` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

/*Data for the table `user` */

insert  into `user`(`id`,`name`) values (1,'javaxl');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
