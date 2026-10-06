-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: fd_db_test
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
-- Table structure for table `fd_account_roles`
--

DROP TABLE IF EXISTS `fd_account_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fd_account_roles` (
  `crud_value` varchar(1) DEFAULT NULL,
  `fdrl_efctv_dt` date DEFAULT NULL,
  `fdrl_is_primary` bit(1) DEFAULT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `fdrl_id` bigint NOT NULL AUTO_INCREMENT,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `fdrl_prd_role_id` varchar(20) DEFAULT NULL,
  `fdrl_role_typ` varchar(30) NOT NULL,
  `cust_id` varchar(32) NOT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `fda_id` char(36) NOT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`fdrl_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_account_roles`
--

LOCK TABLES `fd_account_roles` WRITE;
/*!40000 ALTER TABLE `fd_account_roles` DISABLE KEYS */;
/*!40000 ALTER TABLE `fd_account_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fd_account_sequence`
--

DROP TABLE IF EXISTS `fd_account_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fd_account_sequence` (
  `crud_value` varchar(1) DEFAULT NULL,
  `fdsq_efctv_dt` date DEFAULT NULL,
  `fdsq_seq_width` int NOT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `fdsq_prefix` varchar(6) DEFAULT NULL,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `fdsq_last_seq` bigint NOT NULL,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `fdsq_branch_cd` varchar(10) NOT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  `fdsq_branch_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`fdsq_branch_cd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_account_sequence`
--

LOCK TABLES `fd_account_sequence` WRITE;
/*!40000 ALTER TABLE `fd_account_sequence` DISABLE KEYS */;
/*!40000 ALTER TABLE `fd_account_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fd_accounts`
--

DROP TABLE IF EXISTS `fd_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fd_accounts` (
  `crud_value` varchar(1) DEFAULT NULL,
  `fda_accrued_int_amt` decimal(18,4) DEFAULT NULL,
  `fda_ccy_cd` varchar(3) NOT NULL,
  `fda_ccy_decimals` int DEFAULT NULL,
  `fda_closure_dt` date DEFAULT NULL,
  `fda_efctv_dt` date DEFAULT NULL,
  `fda_int_rt` decimal(6,4) DEFAULT NULL,
  `fda_last_accrual_dt` date DEFAULT NULL,
  `fda_last_captlz_dt` date DEFAULT NULL,
  `fda_mat_amt` decimal(18,2) DEFAULT NULL,
  `fda_mat_dt` date DEFAULT NULL,
  `fda_open_dt` date DEFAULT NULL,
  `fda_principal_amt` decimal(18,2) NOT NULL,
  `fda_principal_bal` decimal(18,2) DEFAULT NULL,
  `fda_tds_rt` decimal(6,4) DEFAULT NULL,
  `fda_tenure_months` int DEFAULT NULL,
  `fda_value_dt` date DEFAULT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `fda_day_count_conv` varchar(10) DEFAULT NULL,
  `fda_acct_num` varchar(20) DEFAULT NULL,
  `fda_category_cd` varchar(20) DEFAULT NULL,
  `fda_compound_freq` varchar(20) DEFAULT NULL,
  `fda_int_typ` varchar(20) DEFAULT NULL,
  `fda_payout_freq` varchar(20) DEFAULT NULL,
  `fda_prd_code` varchar(20) NOT NULL,
  `fda_rate_id` varchar(20) DEFAULT NULL,
  `fda_sts` varchar(20) DEFAULT NULL,
  `fda_mat_instruction` varchar(30) DEFAULT NULL,
  `cust_id` varchar(32) NOT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `fda_id` char(36) NOT NULL,
  `fda_renewed_from_id` char(36) DEFAULT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  `fda_prd_name_snap` varchar(100) DEFAULT NULL,
  `fda_cust_name_snap` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`fda_id`),
  UNIQUE KEY `UK3p7bt4sravd4idakr4gg5lna4` (`fda_acct_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_accounts`
--

LOCK TABLES `fd_accounts` WRITE;
/*!40000 ALTER TABLE `fd_accounts` DISABLE KEYS */;
/*!40000 ALTER TABLE `fd_accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fd_batch_run_log`
--

DROP TABLE IF EXISTS `fd_batch_run_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fd_batch_run_log` (
  `crud_value` varchar(1) DEFAULT NULL,
  `fdjb_business_dt` date NOT NULL,
  `fdjb_efctv_dt` date DEFAULT NULL,
  `fdjb_read_cnt` int DEFAULT NULL,
  `fdjb_skip_cnt` int DEFAULT NULL,
  `fdjb_write_cnt` int DEFAULT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `fdjb_end_ts` datetime(6) DEFAULT NULL,
  `fdjb_id` bigint NOT NULL AUTO_INCREMENT,
  `fdjb_start_ts` datetime(6) DEFAULT NULL,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `fdjb_sts` varchar(20) NOT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  `fdjb_job_name` varchar(50) NOT NULL,
  `fdjb_error_msg` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`fdjb_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_batch_run_log`
--

LOCK TABLES `fd_batch_run_log` WRITE;
/*!40000 ALTER TABLE `fd_batch_run_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `fd_batch_run_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fd_business_clock`
--

DROP TABLE IF EXISTS `fd_business_clock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fd_business_clock` (
  `crud_value` varchar(1) DEFAULT NULL,
  `fdbc_business_dt` date NOT NULL,
  `fdbc_efctv_dt` date DEFAULT NULL,
  `fdbc_prev_business_dt` date DEFAULT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `fdbc_last_eod_ts` datetime(6) DEFAULT NULL,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `fdbc_entity_cd` varchar(10) NOT NULL,
  `fdbc_eod_sts` varchar(20) DEFAULT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`fdbc_entity_cd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_business_clock`
--

LOCK TABLES `fd_business_clock` WRITE;
/*!40000 ALTER TABLE `fd_business_clock` DISABLE KEYS */;
/*!40000 ALTER TABLE `fd_business_clock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fd_gl_accounts`
--

DROP TABLE IF EXISTS `fd_gl_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fd_gl_accounts` (
  `crud_value` varchar(1) DEFAULT NULL,
  `fdgl_ccy_cd` varchar(3) DEFAULT NULL,
  `fdgl_current_bal` decimal(18,2) DEFAULT NULL,
  `fdgl_efctv_dt` date DEFAULT NULL,
  `fdgl_is_active` bit(1) DEFAULT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `fdgl_cd` varchar(20) NOT NULL,
  `fdgl_typ` varchar(20) DEFAULT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  `fdgl_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`fdgl_cd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_gl_accounts`
--

LOCK TABLES `fd_gl_accounts` WRITE;
/*!40000 ALTER TABLE `fd_gl_accounts` DISABLE KEYS */;
INSERT INTO `fd_gl_accounts` VALUES ('U','INR',0.00,'2026-09-13',_binary '','+05:30','2026-09-13 16:41:17.236118','2026-09-13 16:41:17.236118','2026-09-13 16:41:17.236118','FD_DEP_LIAB','LIABILITY','fdservice','FD_SYSTEM','fdservice','d00c0703-20eb-443d-8bfb-10f8be9a25f5','Fixed Deposits - Customer Liability'),('U','INR',0.00,'2026-09-13',_binary '','+05:30','2026-09-13 16:41:17.236118','2026-09-13 16:41:17.236118','2026-09-13 16:41:17.236118','FD_INT_EXP','EXPENSE','fdservice','FD_SYSTEM','fdservice','410cca66-3918-4e2f-9cf4-d0064db721ad','Interest Expense on Fixed Deposits'),('U','INR',0.00,'2026-09-13',_binary '','+05:30','2026-09-13 16:41:17.236118','2026-09-13 16:41:17.236118','2026-09-13 16:41:17.236118','FD_PENALTY_INC','INCOME','fdservice','FD_SYSTEM','fdservice','b4bdf193-9a3f-4304-af2f-30f1ef947c6c','Premature Withdrawal Penalty Income'),('U','INR',0.00,'2026-09-13',_binary '','+05:30','2026-09-13 16:41:17.236118','2026-09-13 16:41:17.236118','2026-09-13 16:41:17.236118','FD_SETTLEMENT','ASSET','fdservice','FD_SYSTEM','fdservice','523f45ca-7349-450b-8492-ca3f6caea950','Customer Settlement Clearing');
/*!40000 ALTER TABLE `fd_gl_accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fd_gl_entries`
--

DROP TABLE IF EXISTS `fd_gl_entries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fd_gl_entries` (
  `crud_value` varchar(1) DEFAULT NULL,
  `fdge_amt` decimal(18,2) NOT NULL,
  `fdge_dr_cr` varchar(1) NOT NULL,
  `fdge_efctv_dt` date DEFAULT NULL,
  `fdge_post_dt` date NOT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `fdge_fdt_id` bigint DEFAULT NULL,
  `fdge_id` bigint NOT NULL AUTO_INCREMENT,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `fdge_gl_cd` varchar(20) NOT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `fdge_txn_grp_id` char(36) NOT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  `fdge_narrative` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`fdge_id`),
  KEY `FK_FDGE_FDGL` (`fdge_gl_cd`),
  KEY `FK_FDGE_FDT` (`fdge_fdt_id`),
  CONSTRAINT `FK_FDGE_FDGL` FOREIGN KEY (`fdge_gl_cd`) REFERENCES `fd_gl_accounts` (`fdgl_cd`),
  CONSTRAINT `FK_FDGE_FDT` FOREIGN KEY (`fdge_fdt_id`) REFERENCES `fd_transactions` (`fdt_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_gl_entries`
--

LOCK TABLES `fd_gl_entries` WRITE;
/*!40000 ALTER TABLE `fd_gl_entries` DISABLE KEYS */;
/*!40000 ALTER TABLE `fd_gl_entries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fd_transactions`
--

DROP TABLE IF EXISTS `fd_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fd_transactions` (
  `crud_value` varchar(1) DEFAULT NULL,
  `fdt_amt` decimal(18,2) NOT NULL,
  `fdt_bal_after` decimal(18,2) DEFAULT NULL,
  `fdt_bal_before` decimal(18,2) DEFAULT NULL,
  `fdt_ccy_cd` varchar(3) DEFAULT NULL,
  `fdt_dr_cr` varchar(1) NOT NULL,
  `fdt_efctv_dt` date DEFAULT NULL,
  `fdt_tds_rt` decimal(6,4) DEFAULT NULL,
  `fdt_txn_dt` date NOT NULL,
  `fdt_value_dt` date DEFAULT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `fdt_id` bigint NOT NULL AUTO_INCREMENT,
  `fdt_reversal_of_id` bigint DEFAULT NULL,
  `fdt_txn_ts` datetime(6) DEFAULT NULL,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `fdt_pan_snap` varchar(10) DEFAULT NULL,
  `fdt_txn_typ` varchar(20) NOT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `fda_id` char(36) NOT NULL,
  `fdt_txn_grp_id` char(36) NOT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  `fdt_remarks` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`fdt_id`),
  KEY `FK_FDT_FDA` (`fda_id`),
  KEY `FK_FDT_REVERSAL_OF` (`fdt_reversal_of_id`),
  CONSTRAINT `FK_FDT_FDA` FOREIGN KEY (`fda_id`) REFERENCES `fd_accounts` (`fda_id`),
  CONSTRAINT `FK_FDT_REVERSAL_OF` FOREIGN KEY (`fdt_reversal_of_id`) REFERENCES `fd_transactions` (`fdt_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_transactions`
--

LOCK TABLES `fd_transactions` WRITE;
/*!40000 ALTER TABLE `fd_transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `fd_transactions` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-13 17:01:29
