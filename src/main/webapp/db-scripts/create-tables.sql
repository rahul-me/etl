/*
SQLyog Community v11.24 (64 bit)
MySQL - 5.6.12-log : Database - gcnetl
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`gcnetl` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `gcnetl`;

/*Table structure for table `additional_properties` */

DROP TABLE IF EXISTS `additional_properties`;

CREATE TABLE `additional_properties` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `meter_id` varchar(255) DEFAULT NULL,
  `utility_id` varchar(255) DEFAULT NULL,
  `job_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKtb0uvsj7oka4xmjwa2nogu7ka` (`job_id`),
  CONSTRAINT `FKtb0uvsj7oka4xmjwa2nogu7ka` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `additional_properties` */

/*Table structure for table `datainputfiles` */

DROP TABLE IF EXISTS `datainputfiles`;

CREATE TABLE `datainputfiles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `data_type` varchar(255) DEFAULT NULL,
  `file_id` varchar(255) DEFAULT NULL,
  `file_link` varchar(255) DEFAULT NULL,
  `file_name` varchar(255) DEFAULT NULL,
  `input_file_type_id` int(11) DEFAULT NULL,
  `interval_length` int(11) DEFAULT NULL,
  `is_interval_start` bit(1) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `meter_name` varchar(255) DEFAULT NULL,
  `time_zone` varchar(255) DEFAULT NULL,
  `timestamp_format` varchar(255) DEFAULT NULL,
  `units` varchar(255) DEFAULT NULL,
  `space_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrjrloybl4nwg1fj9cja40u1pu` (`space_id`),
  CONSTRAINT `FKrjrloybl4nwg1fj9cja40u1pu` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `datainputfiles` */

/*Table structure for table `error_details` */

DROP TABLE IF EXISTS `error_details`;

CREATE TABLE `error_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `col_index` int(11) DEFAULT NULL,
  `date` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `interval_time` varchar(255) DEFAULT NULL,
  `meter_no` varchar(255) DEFAULT NULL,
  `row_index` int(11) DEFAULT NULL,
  `job_validation_status_id` bigint(20) DEFAULT NULL,
  `application_error_code` varchar(255) DEFAULT NULL,
  `application_error_message` varchar(255) DEFAULT NULL,
  `value` float DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4qcvckkolkjcscjic6qci7gpv` (`job_validation_status_id`),
  CONSTRAINT `FK4qcvckkolkjcscjic6qci7gpv` FOREIGN KEY (`job_validation_status_id`) REFERENCES `job_validation_status` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=58858 DEFAULT CHARSET=latin1;

/*Data for the table `error_details` */

/*Table structure for table `job_status` */

DROP TABLE IF EXISTS `job_status`;

CREATE TABLE `job_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_pmt_service_call` bit(1) DEFAULT NULL,
  `job_id` varchar(255) DEFAULT NULL,
  `pmt_response` varchar(255) DEFAULT NULL,
  `pmt_status` varchar(255) DEFAULT NULL,
  `transformation_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `job_status` */

/*Table structure for table `job_validation_status` */

DROP TABLE IF EXISTS `job_validation_status`;

CREATE TABLE `job_validation_status` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME DEFAULT NULL,
  `updated_at` DATETIME DEFAULT NULL,
  `input_file_type_id` INT(11) DEFAULT NULL,
  `is_data_valid` BIT(1) DEFAULT NULL,
  `is_missing_point_reoaired` BIT(1) DEFAULT NULL,
  `is_spike` BIT(1) DEFAULT NULL,
  `job_id` VARCHAR(255) DEFAULT NULL,
  `message` VARCHAR(255) DEFAULT NULL,
  `application_error_code` INT(11) DEFAULT NULL,
  `application_error_msg` VARCHAR(255) DEFAULT NULL,
  `job_status` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=INNODB AUTO_INCREMENT=397 DEFAULT CHARSET=latin1;

/*Data for the table `job_validation_status` */

/*Table structure for table `jobs` */

DROP TABLE IF EXISTS `jobs`;

CREATE TABLE `jobs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `action` varchar(255) DEFAULT NULL,
  `callback_url` varchar(255) DEFAULT NULL,
  `input_file_id` varchar(255) DEFAULT NULL,
  `job_id` varchar(255) DEFAULT NULL,
  `output_file_link` varchar(255) DEFAULT NULL,
  `output_file_location` varchar(255) DEFAULT NULL,
  `output_file_name` varchar(255) DEFAULT NULL,
  `skip_transformation` bit(1) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `space_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfhlbo172jw7v5c63fpgls4fsv` (`space_id`),
  CONSTRAINT `FKfhlbo172jw7v5c63fpgls4fsv` FOREIGN KEY (`space_id`) REFERENCES `spaces` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `jobs` */

/*Table structure for table `logging_channels_log` */

DROP TABLE IF EXISTS `logging_channels_log`;

CREATE TABLE `logging_channels_log` (
  `ID_BATCH` int(11) DEFAULT NULL,
  `CHANNEL_ID` varchar(255) DEFAULT NULL,
  `LOG_DATE` datetime DEFAULT NULL,
  `LOGGING_OBJECT_TYPE` varchar(255) DEFAULT NULL,
  `OBJECT_NAME` varchar(255) DEFAULT NULL,
  `OBJECT_COPY` varchar(255) DEFAULT NULL,
  `REPOSITORY_DIRECTORY` varchar(255) DEFAULT NULL,
  `FILENAME` varchar(255) DEFAULT NULL,
  `OBJECT_ID` varchar(255) DEFAULT NULL,
  `OBJECT_REVISION` varchar(255) DEFAULT NULL,
  `PARENT_CHANNEL_ID` varchar(255) DEFAULT NULL,
  `ROOT_CHANNEL_ID` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `logging_channels_log` */

/*Table structure for table `metrics_log` */

DROP TABLE IF EXISTS `metrics_log`;

CREATE TABLE `metrics_log` (
  `ID_BATCH` int(11) DEFAULT NULL,
  `CHANNEL_ID` varchar(255) DEFAULT NULL,
  `LOG_DATE` datetime DEFAULT NULL,
  `METRICS_DATE` datetime DEFAULT NULL,
  `METRICS_CODE` varchar(255) DEFAULT NULL,
  `METRICS_DESCRIPTION` varchar(255) DEFAULT NULL,
  `METRICS_SUBJECT` varchar(255) DEFAULT NULL,
  `METRICS_TYPE` varchar(255) DEFAULT NULL,
  `METRICS_VALUE` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `metrics_log` */

/*Table structure for table `performance_log` */

DROP TABLE IF EXISTS `performance_log`;

CREATE TABLE `performance_log` (
  `ID_BATCH` int(11) DEFAULT NULL,
  `SEQ_NR` int(11) DEFAULT NULL,
  `LOGDATE` datetime DEFAULT NULL,
  `TRANSNAME` varchar(255) DEFAULT NULL,
  `STEPNAME` varchar(255) DEFAULT NULL,
  `STEP_COPY` int(11) DEFAULT NULL,
  `LINES_READ` bigint(20) DEFAULT NULL,
  `LINES_WRITTEN` bigint(20) DEFAULT NULL,
  `LINES_UPDATED` bigint(20) DEFAULT NULL,
  `LINES_INPUT` bigint(20) DEFAULT NULL,
  `LINES_OUTPUT` bigint(20) DEFAULT NULL,
  `LINES_REJECTED` bigint(20) DEFAULT NULL,
  `ERRORS` bigint(20) DEFAULT NULL,
  `INPUT_BUFFER_ROWS` bigint(20) DEFAULT NULL,
  `OUTPUT_BUFFER_ROWS` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `performance_log` */

/*Table structure for table `step_log` */

DROP TABLE IF EXISTS `step_log`;

CREATE TABLE `step_log` (
  `ID_BATCH` int(11) DEFAULT NULL,
  `CHANNEL_ID` varchar(255) DEFAULT NULL,
  `LOG_DATE` datetime DEFAULT NULL,
  `TRANSNAME` varchar(255) DEFAULT NULL,
  `STEPNAME` varchar(255) DEFAULT NULL,
  `STEP_COPY` int(11) DEFAULT NULL,
  `LINES_READ` bigint(20) DEFAULT NULL,
  `LINES_WRITTEN` bigint(20) DEFAULT NULL,
  `LINES_UPDATED` bigint(20) DEFAULT NULL,
  `LINES_INPUT` bigint(20) DEFAULT NULL,
  `LINES_OUTPUT` bigint(20) DEFAULT NULL,
  `LINES_REJECTED` bigint(20) DEFAULT NULL,
  `ERRORS` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `step_log` */

/*Table structure for table `transformation_log` */

DROP TABLE IF EXISTS `transformation_log`;

CREATE TABLE `transformation_log` (
  `ID_BATCH` int(11) DEFAULT NULL,
  `CHANNEL_ID` varchar(255) DEFAULT NULL,
  `TRANSNAME` varchar(255) DEFAULT NULL,
  `STATUS` varchar(15) DEFAULT NULL,
  `LINES_READ` bigint(20) DEFAULT NULL,
  `LINES_WRITTEN` bigint(20) DEFAULT NULL,
  `LINES_UPDATED` bigint(20) DEFAULT NULL,
  `LINES_INPUT` bigint(20) DEFAULT NULL,
  `LINES_OUTPUT` bigint(20) DEFAULT NULL,
  `LINES_REJECTED` bigint(20) DEFAULT NULL,
  `ERRORS` bigint(20) DEFAULT NULL,
  `STARTDATE` datetime DEFAULT NULL,
  `ENDDATE` datetime DEFAULT NULL,
  `LOGDATE` datetime DEFAULT NULL,
  `DEPDATE` datetime DEFAULT NULL,
  `REPLAYDATE` datetime DEFAULT NULL,
  `LOG_FIELD` mediumtext,
  KEY `IDX_transformation_log_1` (`ID_BATCH`),
  KEY `IDX_transformation_log_2` (`ERRORS`,`STATUS`,`TRANSNAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `transformation_log` */

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

DROP TABLE IF EXISTS `spaces`;

CREATE TABLE `spaces` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `generated_uuid` varchar(255) DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `timestamp` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1

