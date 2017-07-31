/*
SQLyog Community Edition- MySQL GUI v7.01 
MySQL - 5.0.27-community-nt : Database - vidattendence
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE DATABASE /*!32312 IF NOT EXISTS*/`vidattendence` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `vidattendence`;

/*Table structure for table `attendence` */

DROP TABLE IF EXISTS `attendence`;

CREATE TABLE `attendence` (
  `sid` varchar(10) default NULL,
  `present` varchar(10) default NULL,
  `attDate` date default NULL,
  `entryTime` varchar(100) default NULL,
  `subject` varchar(100) default NULL,
  `fromtime` varchar(100) default NULL,
  `totime` varchar(100) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `attendence` */

insert  into `attendence`(`sid`,`present`,`attDate`,`entryTime`,`subject`,`fromtime`,`totime`) values ('11111','p','2017-03-21','12.45','MPR','12.00','13.00'),('44444','p','2017-03-21','12.45','MPR','12.00','13.00'),('22222','p','2017-03-21','12.45','MPR','12.00','13.00'),('33333','p','2017-03-21','12.45','MPR','12.00','13.00'),('11111','p','2017-03-21','18.52','GAP','18.00','19.00');

/*Table structure for table `student` */

DROP TABLE IF EXISTS `student`;

CREATE TABLE `student` (
  `sid` varchar(100) default NULL,
  `Student_Name` varchar(100) default NULL,
  `Student_Std` varchar(100) default NULL,
  `phoneno` varchar(100) default NULL,
  `email` varchar(100) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `student` */

insert  into `student`(`sid`,`Student_Name`,`Student_Std`,`phoneno`,`email`) values ('1414','bgfbbfgb','I.T.(B.E)','8478598598','gnghn@gmail.com');

/*Table structure for table `teacher` */

DROP TABLE IF EXISTS `teacher`;

CREATE TABLE `teacher` (
  `username` varchar(100) default NULL,
  `password` varchar(100) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `teacher` */

insert  into `teacher`(`username`,`password`) values ('a','a');

/*Table structure for table `timetable` */

DROP TABLE IF EXISTS `timetable`;

CREATE TABLE `timetable` (
  `Day` varchar(100) default NULL,
  `Subject` varchar(100) default NULL,
  `Fromtime` varchar(100) default NULL,
  `Totime` varchar(100) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `timetable` */

insert  into `timetable`(`Day`,`Subject`,`Fromtime`,`Totime`) values ('Wednesday','GAP','17.00','19.00'),('Tuesday','GAP','10.00','11.00'),('Tuesday','ECOM','11.00','12.00'),('Tuesday','MPR','12.00','13.00'),('Tuesday','GAP','18.00','19.00');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
