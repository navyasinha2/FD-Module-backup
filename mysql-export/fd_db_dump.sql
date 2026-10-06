-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: fd_db
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
  `fdrl_id` bigint NOT NULL AUTO_INCREMENT,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `crud_value` varchar(1) DEFAULT NULL,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `cust_id` varchar(32) NOT NULL,
  `fdrl_efctv_dt` date DEFAULT NULL,
  `fda_id` char(36) NOT NULL,
  `fdrl_is_primary` bit(1) DEFAULT NULL,
  `fdrl_prd_role_id` varchar(20) DEFAULT NULL,
  `fdrl_role_typ` varchar(30) NOT NULL,
  PRIMARY KEY (`fdrl_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_account_roles`
--

LOCK TABLES `fd_account_roles` WRITE;
/*!40000 ALTER TABLE `fd_account_roles` DISABLE KEYS */;
INSERT INTO `fd_account_roles` VALUES (1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'CUST-002','2026-01-01','8f873049-2929-4ca8-8538-54790ff6e4b2',_binary '',NULL,'OWNER'),(2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'CUST-002','2026-01-01','3fff572a-7c91-464b-bff5-8e5694f8b0d2',_binary '',NULL,'OWNER'),(3,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'CUST-AGRIM-TEST','2026-01-01','35b6aecd-9262-4926-b6c4-6b8e2f6bb790',_binary '',NULL,'OWNER');
/*!40000 ALTER TABLE `fd_account_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fd_account_sequence`
--

DROP TABLE IF EXISTS `fd_account_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fd_account_sequence` (
  `fdsq_branch_cd` varchar(10) NOT NULL,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `crud_value` varchar(1) DEFAULT NULL,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `fdsq_branch_name` varchar(100) DEFAULT NULL,
  `fdsq_efctv_dt` date DEFAULT NULL,
  `fdsq_last_seq` bigint NOT NULL,
  `fdsq_prefix` varchar(6) DEFAULT NULL,
  `fdsq_seq_width` int NOT NULL,
  PRIMARY KEY (`fdsq_branch_cd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_account_sequence`
--

LOCK TABLES `fd_account_sequence` WRITE;
/*!40000 ALTER TABLE `fd_account_sequence` DISABLE KEYS */;
INSERT INTO `fd_account_sequence` VALUES ('001','2026-09-13 08:36:27.364348','+05:30','C','2026-09-13 08:36:27.364348','2026-09-13 08:36:27.364348','fdservice','FD_SYSTEM','c284c31c-c5af-4f81-b170-48ed0162e065','fdservice','Head Office','2026-09-13',3,'FD',6);
/*!40000 ALTER TABLE `fd_account_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fd_accounts`
--

DROP TABLE IF EXISTS `fd_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fd_accounts` (
  `fda_id` char(36) NOT NULL,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `crud_value` varchar(1) DEFAULT NULL,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `fda_accrued_int_amt` decimal(18,4) DEFAULT NULL,
  `fda_acct_num` varchar(20) DEFAULT NULL,
  `fda_category_cd` varchar(20) DEFAULT NULL,
  `fda_ccy_cd` varchar(3) NOT NULL,
  `fda_ccy_decimals` int DEFAULT NULL,
  `fda_closure_dt` date DEFAULT NULL,
  `fda_compound_freq` varchar(20) DEFAULT NULL,
  `cust_id` varchar(32) NOT NULL,
  `fda_cust_name_snap` varchar(255) DEFAULT NULL,
  `fda_day_count_conv` varchar(10) DEFAULT NULL,
  `fda_efctv_dt` date DEFAULT NULL,
  `fda_int_rt` decimal(6,4) DEFAULT NULL,
  `fda_int_typ` varchar(20) DEFAULT NULL,
  `fda_last_accrual_dt` date DEFAULT NULL,
  `fda_last_captlz_dt` date DEFAULT NULL,
  `fda_mat_amt` decimal(18,2) DEFAULT NULL,
  `fda_mat_dt` date DEFAULT NULL,
  `fda_mat_instruction` varchar(30) DEFAULT NULL,
  `fda_open_dt` date DEFAULT NULL,
  `fda_payout_freq` varchar(20) DEFAULT NULL,
  `fda_prd_code` varchar(20) NOT NULL,
  `fda_prd_name_snap` varchar(100) DEFAULT NULL,
  `fda_principal_amt` decimal(18,2) NOT NULL,
  `fda_principal_bal` decimal(18,2) DEFAULT NULL,
  `fda_rate_id` varchar(20) DEFAULT NULL,
  `fda_renewed_from_id` char(36) DEFAULT NULL,
  `fda_sts` varchar(20) DEFAULT NULL,
  `fda_tds_rt` decimal(6,4) DEFAULT NULL,
  `fda_tenure_months` int DEFAULT NULL,
  `fda_value_dt` date DEFAULT NULL,
  PRIMARY KEY (`fda_id`),
  UNIQUE KEY `UK3p7bt4sravd4idakr4gg5lna4` (`fda_acct_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_accounts`
--

LOCK TABLES `fd_accounts` WRITE;
/*!40000 ALTER TABLE `fd_accounts` DISABLE KEYS */;
INSERT INTO `fd_accounts` VALUES ('35b6aecd-9262-4926-b6c4-6b8e2f6bb790','2026-09-13 16:19:51.779988','+05:30','U','2026-09-13 16:19:51.779988','2026-09-13 16:19:51.779988','fdservice','FD_BATCH','99228660-ed74-4189-868f-37c518b4e906','fdservice',0.0000,'FD001000003',NULL,'INR',2,NULL,'QUARTERLY','CUST-AGRIM-TEST','Customer CUST-AGRIM-TEST','ACT/365','2026-01-01',6.5000,'COMPOUND','2026-04-01','2026-04-01',106660.15,'2027-01-01','PAYOUT','2026-01-01','QUARTERLY','FD-REG','Regular Fixed Deposit',100000.00,101602.74,'RATE-TEST',NULL,'ACTIVE',NULL,12,'2026-01-01'),('3fff572a-7c91-464b-bff5-8e5694f8b0d2','2026-09-13 08:37:49.807914','+05:30','U','2026-09-13 08:37:49.807914','2026-09-13 08:37:49.807914','fdservice','FD_API','ec54509a-87ce-4de1-a519-b507ee30f4ce','fdservice',0.0000,'FD001000002',NULL,'INR',2,'2026-07-16','QUARTERLY','CUST-002','Vikram Shah','ACT/365','2026-01-01',6.5000,'COMPOUND','2026-07-16','2026-07-16',106660.15,'2027-01-01','PAYOUT','2026-01-01','ON_MATURITY','FD-REG','Regular Fixed Deposit',100000.00,0.00,'RATE-12M',NULL,'CLOSED',NULL,12,'2026-01-01'),('8f873049-2929-4ca8-8538-54790ff6e4b2','2026-09-13 08:37:49.860641','+05:30','U','2026-09-13 08:37:49.860641','2026-09-13 08:37:49.860641','fdservice','FD_BATCH','f20a3a62-f610-4d93-a878-d704319d5ceb','fdservice',0.0000,'FD001000001',NULL,'INR',2,'2027-01-01','QUARTERLY','CUST-002','Vikram Shah','ACT/365','2026-01-01',6.5000,'COMPOUND','2027-01-01','2027-01-01',106660.15,'2027-01-01','PAYOUT','2026-01-01','ON_MATURITY','FD-REG','Regular Fixed Deposit',100000.00,0.00,'RATE-12M',NULL,'MATURED_CLOSED',NULL,12,'2026-01-01');
/*!40000 ALTER TABLE `fd_accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fd_batch_run_log`
--

DROP TABLE IF EXISTS `fd_batch_run_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fd_batch_run_log` (
  `fdjb_id` bigint NOT NULL AUTO_INCREMENT,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `crud_value` varchar(1) DEFAULT NULL,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `fdjb_business_dt` date NOT NULL,
  `fdjb_efctv_dt` date DEFAULT NULL,
  `fdjb_end_ts` datetime(6) DEFAULT NULL,
  `fdjb_error_msg` varchar(500) DEFAULT NULL,
  `fdjb_job_name` varchar(50) NOT NULL,
  `fdjb_read_cnt` int DEFAULT NULL,
  `fdjb_skip_cnt` int DEFAULT NULL,
  `fdjb_start_ts` datetime(6) DEFAULT NULL,
  `fdjb_sts` varchar(20) NOT NULL,
  `fdjb_write_cnt` int DEFAULT NULL,
  PRIMARY KEY (`fdjb_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_batch_run_log`
--

LOCK TABLES `fd_batch_run_log` WRITE;
/*!40000 ALTER TABLE `fd_batch_run_log` DISABLE KEYS */;
INSERT INTO `fd_batch_run_log` VALUES (1,'2026-09-13 08:37:49.757921','+05:30','U','2026-09-13 08:37:49.757921','2026-09-13 08:37:49.757921','fdservice','FD_BATCH','8ed42771-dc85-4f2a-b475-43a4c7024bdf','fdservice','2026-04-01','2026-04-01','2026-09-13 08:37:49.757921',NULL,'ACCRUAL',2,0,'2026-09-13 08:37:49.683252','SUCCESS',2),(2,'2026-09-13 08:37:49.774777','+05:30','U','2026-09-13 08:37:49.774777','2026-09-13 08:37:49.774777','fdservice','FD_BATCH','f2f019ae-c5df-4536-8cd1-1765675b73a7','fdservice','2026-04-01','2026-04-01','2026-09-13 08:37:49.774777',NULL,'ACCRUAL',0,0,'2026-09-13 08:37:49.766177','SUCCESS',0),(3,'2026-09-13 08:37:49.860641','+05:30','U','2026-09-13 08:37:49.860641','2026-09-13 08:37:49.860641','fdservice','FD_BATCH','c0cf5591-70a9-4acd-8f69-401262c8f7f2','fdservice','2027-01-01','2027-01-01','2026-09-13 08:37:49.860641',NULL,'ACCRUAL',1,0,'2026-09-13 08:37:49.840889','SUCCESS',1),(4,'2026-09-13 08:37:49.876662','+05:30','U','2026-09-13 08:37:49.876662','2026-09-13 08:37:49.876662','fdservice','FD_BATCH','3855b24b-0b94-4640-9313-c9f6ad715588','fdservice','2027-01-01','2027-01-01','2026-09-13 08:37:49.876662',NULL,'MATURITY',1,0,'2026-09-13 08:37:49.860641','SUCCESS',1),(5,'2026-09-13 16:17:24.350470','+05:30','U','2026-09-13 16:17:24.350470','2026-09-13 16:17:24.350470','fdservice','FD_BATCH','d0a6fabf-55a6-45dd-b778-59dc805d9090','fdservice','2026-02-01','2026-02-01','2026-09-13 16:17:24.350470',NULL,'ACCRUAL',1,0,'2026-09-13 16:17:24.139915','SUCCESS',1),(6,'2026-09-13 16:19:51.831057','+05:30','U','2026-09-13 16:19:51.831057','2026-09-13 16:19:51.831057','fdservice','FD_BATCH','5791115b-4486-443b-a75b-a4d2dd22f4e9','fdservice','2026-04-01','2026-04-01','2026-09-13 16:19:51.831057',NULL,'ACCRUAL',1,0,'2026-09-13 16:19:51.712145','SUCCESS',1),(7,'2026-09-13 16:20:56.560621','+05:30','U','2026-09-13 16:20:56.560621','2026-09-13 16:20:56.560621','fdservice','FD_BATCH','6f79973d-2d17-4fe2-af9d-48ee47999a50','fdservice','2026-04-01','2026-04-01','2026-09-13 16:20:56.560621',NULL,'ACCRUAL',0,0,'2026-09-13 16:20:56.513820','SUCCESS',0);
/*!40000 ALTER TABLE `fd_batch_run_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fd_business_clock`
--

DROP TABLE IF EXISTS `fd_business_clock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fd_business_clock` (
  `fdbc_entity_cd` varchar(10) NOT NULL,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `crud_value` varchar(1) DEFAULT NULL,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `fdbc_business_dt` date NOT NULL,
  `fdbc_efctv_dt` date DEFAULT NULL,
  `fdbc_eod_sts` varchar(20) DEFAULT NULL,
  `fdbc_last_eod_ts` datetime(6) DEFAULT NULL,
  `fdbc_prev_business_dt` date DEFAULT NULL,
  PRIMARY KEY (`fdbc_entity_cd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_business_clock`
--

LOCK TABLES `fd_business_clock` WRITE;
/*!40000 ALTER TABLE `fd_business_clock` DISABLE KEYS */;
INSERT INTO `fd_business_clock` VALUES ('FD','2026-09-13 16:19:51.706955','+05:30','U','2026-09-13 16:19:51.706955','2026-09-13 16:19:51.706955','fdservice','FD_API','d54c7aee-a55c-4dc4-a757-b5d4ebbacd81','fdservice','2026-04-01','2026-04-01','OPEN','2026-09-13 08:37:49.879961','2026-02-01');
/*!40000 ALTER TABLE `fd_business_clock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fd_gl_accounts`
--

DROP TABLE IF EXISTS `fd_gl_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fd_gl_accounts` (
  `fdgl_cd` varchar(20) NOT NULL,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `crud_value` varchar(1) DEFAULT NULL,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `fdgl_ccy_cd` varchar(3) DEFAULT NULL,
  `fdgl_current_bal` decimal(18,2) DEFAULT NULL,
  `fdgl_efctv_dt` date DEFAULT NULL,
  `fdgl_is_active` bit(1) DEFAULT NULL,
  `fdgl_name` varchar(100) DEFAULT NULL,
  `fdgl_typ` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`fdgl_cd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_gl_accounts`
--

LOCK TABLES `fd_gl_accounts` WRITE;
/*!40000 ALTER TABLE `fd_gl_accounts` DISABLE KEYS */;
INSERT INTO `fd_gl_accounts` VALUES ('FD_DEP_LIAB','2026-09-13 16:19:51.779988','+05:30','U','2026-09-13 16:19:51.779988','2026-09-13 16:19:51.779988','fdservice','FD_SYSTEM','e462e8a4-8482-451c-bbd0-b1ce32db8e2d','fdservice','INR',101602.74,'2026-09-13',_binary '','Fixed Deposits - Customer Liability','LIABILITY'),('FD_INT_EXP','2026-09-13 16:19:51.779988','+05:30','U','2026-09-13 16:19:51.779988','2026-09-13 16:19:51.779988','fdservice','FD_SYSTEM','dfde375d-933b-43d5-a899-75ef053bbfcd','fdservice','INR',11787.95,'2026-09-13',_binary '','Interest Expense on Fixed Deposits','EXPENSE'),('FD_PENALTY_INC','2026-09-13 08:37:49.806391','+05:30','U','2026-09-13 08:37:49.806391','2026-09-13 08:37:49.806391','fdservice','FD_SYSTEM','d0f0c0ce-6996-4e89-9b2e-539e9d33a966','fdservice','INR',35.25,'2026-09-13',_binary '','Premature Withdrawal Penalty Income','INCOME'),('FD_SETTLEMENT','2026-09-13 16:15:39.534189','+05:30','U','2026-09-13 16:15:39.534189','2026-09-13 16:15:39.534189','fdservice','FD_SYSTEM','ca7b0492-4372-41e0-b49e-770414ac7e2a','fdservice','INR',89850.04,'2026-09-13',_binary '','Customer Settlement Clearing','ASSET');
/*!40000 ALTER TABLE `fd_gl_accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fd_gl_entries`
--

DROP TABLE IF EXISTS `fd_gl_entries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fd_gl_entries` (
  `fdge_id` bigint NOT NULL AUTO_INCREMENT,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `crud_value` varchar(1) DEFAULT NULL,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `fdge_amt` decimal(18,2) NOT NULL,
  `fdge_dr_cr` varchar(1) NOT NULL,
  `fdge_efctv_dt` date DEFAULT NULL,
  `fdge_fdt_id` bigint DEFAULT NULL,
  `fdge_gl_cd` varchar(20) NOT NULL,
  `fdge_narrative` varchar(255) DEFAULT NULL,
  `fdge_post_dt` date NOT NULL,
  `fdge_txn_grp_id` char(36) NOT NULL,
  PRIMARY KEY (`fdge_id`),
  KEY `FK_FDGE_FDGL` (`fdge_gl_cd`),
  KEY `FK_FDGE_FDT` (`fdge_fdt_id`),
  CONSTRAINT `FK_FDGE_FDGL` FOREIGN KEY (`fdge_gl_cd`) REFERENCES `fd_gl_accounts` (`fdgl_cd`),
  CONSTRAINT `FK_FDGE_FDT` FOREIGN KEY (`fdge_fdt_id`) REFERENCES `fd_transactions` (`fdt_id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_gl_entries`
--

LOCK TABLES `fd_gl_entries` WRITE;
/*!40000 ALTER TABLE `fd_gl_entries` DISABLE KEYS */;
INSERT INTO `fd_gl_entries` VALUES (1,'2026-09-13 08:37:49.630010','+05:30','C','2026-09-13 08:37:49.630010','2026-09-13 08:37:49.630010','fdservice','FD_API','ff56b928-4fa4-46fa-b9ff-297d19ec31fb','fdservice',100000.00,'D','2026-01-01',1,'FD_SETTLEMENT','DEPOSIT FD001000001','2026-01-01','730c24c8-c9bb-48e8-9e05-de4c84ee1801'),(2,'2026-09-13 08:37:49.630010','+05:30','C','2026-09-13 08:37:49.630010','2026-09-13 08:37:49.630010','fdservice','FD_API','92f39d7b-4453-4f18-b738-6c1c27e55b1c','fdservice',100000.00,'C','2026-01-01',1,'FD_DEP_LIAB','DEPOSIT FD001000001','2026-01-01','730c24c8-c9bb-48e8-9e05-de4c84ee1801'),(3,'2026-09-13 08:37:49.658288','+05:30','C','2026-09-13 08:37:49.658288','2026-09-13 08:37:49.658288','fdservice','FD_API','4928083d-3e23-464a-b96d-83a6a8939d60','fdservice',100000.00,'D','2026-01-01',2,'FD_SETTLEMENT','DEPOSIT FD001000002','2026-01-01','82645575-cbb9-4009-96e1-8dfa88dabe96'),(4,'2026-09-13 08:37:49.658288','+05:30','C','2026-09-13 08:37:49.658288','2026-09-13 08:37:49.658288','fdservice','FD_API','4d68c4f6-9e53-4757-a20c-9ad854ecdab3','fdservice',100000.00,'C','2026-01-01',2,'FD_DEP_LIAB','DEPOSIT FD001000002','2026-01-01','82645575-cbb9-4009-96e1-8dfa88dabe96'),(5,'2026-09-13 08:37:49.741196','+05:30','C','2026-09-13 08:37:49.741196','2026-09-13 08:37:49.741196','fdservice','FD_BATCH','c4aaaea6-86af-45c5-8f30-873cff359d1e','fdservice',1602.74,'D','2026-04-01',3,'FD_INT_EXP','INTEREST FD001000001','2026-04-01','8756c69e-5698-4984-841e-cb67d382999f'),(6,'2026-09-13 08:37:49.741196','+05:30','C','2026-09-13 08:37:49.741196','2026-09-13 08:37:49.741196','fdservice','FD_BATCH','b7390c14-2e6d-407b-b2ee-6d137f8d426a','fdservice',1602.74,'C','2026-04-01',3,'FD_DEP_LIAB','INTEREST FD001000001','2026-04-01','8756c69e-5698-4984-841e-cb67d382999f'),(7,'2026-09-13 08:37:49.750504','+05:30','C','2026-09-13 08:37:49.750504','2026-09-13 08:37:49.750504','fdservice','FD_BATCH','18ab69e0-520b-460f-9a16-270ebcf0618a','fdservice',1602.74,'D','2026-04-01',4,'FD_INT_EXP','INTEREST FD001000002','2026-04-01','3b25ff6c-cd5c-4854-b1f2-bd30a57755b0'),(8,'2026-09-13 08:37:49.750504','+05:30','C','2026-09-13 08:37:49.750504','2026-09-13 08:37:49.750504','fdservice','FD_BATCH','dfbd1181-3445-4ced-bad6-5cbaeb9a8e13','fdservice',1602.74,'C','2026-04-01',4,'FD_DEP_LIAB','INTEREST FD001000002','2026-04-01','3b25ff6c-cd5c-4854-b1f2-bd30a57755b0'),(9,'2026-09-13 08:37:49.790378','+05:30','C','2026-09-13 08:37:49.790378','2026-09-13 08:37:49.790378','fdservice','FD_API','9f506f41-e669-42e6-b333-a4665b86bb9f','fdservice',1646.52,'D','2026-07-16',5,'FD_INT_EXP','INTEREST FD001000002','2026-07-16','121aabaa-2ff6-4612-b077-be9c306dbdcb'),(10,'2026-09-13 08:37:49.790378','+05:30','C','2026-09-13 08:37:49.790378','2026-09-13 08:37:49.790378','fdservice','FD_API','565431e7-02f4-478e-bddd-491a90043891','fdservice',1646.52,'C','2026-07-16',5,'FD_DEP_LIAB','INTEREST FD001000002','2026-07-16','121aabaa-2ff6-4612-b077-be9c306dbdcb'),(11,'2026-09-13 08:37:49.792166','+05:30','C','2026-09-13 08:37:49.792166','2026-09-13 08:37:49.792166','fdservice','FD_API','c837ffbf-02d0-433d-91de-57de535356af','fdservice',275.80,'D','2026-07-16',6,'FD_INT_EXP','INTEREST FD001000002','2026-07-16','66da10e4-6048-4478-8785-744597dc57ab'),(12,'2026-09-13 08:37:49.792166','+05:30','C','2026-09-13 08:37:49.792166','2026-09-13 08:37:49.792166','fdservice','FD_API','61525135-7295-4c89-8ee3-20f61cd3c201','fdservice',275.80,'C','2026-07-16',6,'FD_DEP_LIAB','INTEREST FD001000002','2026-07-16','66da10e4-6048-4478-8785-744597dc57ab'),(13,'2026-09-13 08:37:49.801097','+05:30','C','2026-09-13 08:37:49.801097','2026-09-13 08:37:49.801097','fdservice','FD_API','c413d4cb-be8d-4ad9-9e5f-5d799cd77b16','fdservice',35.25,'D','2026-07-16',7,'FD_DEP_LIAB','PENALTY FD001000002','2026-07-16','66da10e4-6048-4478-8785-744597dc57ab'),(14,'2026-09-13 08:37:49.801097','+05:30','C','2026-09-13 08:37:49.801097','2026-09-13 08:37:49.801097','fdservice','FD_API','e1ddb047-d4eb-4b62-be85-f611bcf4800b','fdservice',35.25,'C','2026-07-16',7,'FD_PENALTY_INC','PENALTY FD001000002','2026-07-16','66da10e4-6048-4478-8785-744597dc57ab'),(15,'2026-09-13 08:37:49.806391','+05:30','C','2026-09-13 08:37:49.806391','2026-09-13 08:37:49.806391','fdservice','FD_API','b3833558-f329-42c3-907f-f3ed75e7de23','fdservice',103489.81,'D','2026-07-16',8,'FD_DEP_LIAB','WITHDRAWAL FD001000002','2026-07-16','66da10e4-6048-4478-8785-744597dc57ab'),(16,'2026-09-13 08:37:49.806391','+05:30','C','2026-09-13 08:37:49.806391','2026-09-13 08:37:49.806391','fdservice','FD_API','10c7faeb-cfce-4ae8-b7ae-de43d563fa7d','fdservice',103489.81,'C','2026-07-16',8,'FD_SETTLEMENT','WITHDRAWAL FD001000002','2026-07-16','66da10e4-6048-4478-8785-744597dc57ab'),(17,'2026-09-13 08:37:49.847007','+05:30','C','2026-09-13 08:37:49.847007','2026-09-13 08:37:49.847007','fdservice','FD_BATCH','021776aa-0953-4b0c-8cfd-1c3c6e606a4f','fdservice',1646.52,'D','2027-01-01',9,'FD_INT_EXP','INTEREST FD001000001','2027-01-01','425569cb-bd4b-4d96-bba8-a35a11c0e5e8'),(18,'2026-09-13 08:37:49.847007','+05:30','C','2026-09-13 08:37:49.847007','2026-09-13 08:37:49.847007','fdservice','FD_BATCH','3745a64c-499a-463b-96be-a070d8435eb3','fdservice',1646.52,'C','2027-01-01',9,'FD_DEP_LIAB','INTEREST FD001000001','2027-01-01','425569cb-bd4b-4d96-bba8-a35a11c0e5e8'),(19,'2026-09-13 08:37:49.848522','+05:30','C','2026-09-13 08:37:49.848522','2026-09-13 08:37:49.848522','fdservice','FD_BATCH','ccf4cb12-15e9-4fa4-b330-bd593d1cb12e','fdservice',1691.59,'D','2027-01-01',10,'FD_INT_EXP','INTEREST FD001000001','2027-01-01','1c3e26df-59be-4135-867a-71707cfab9be'),(20,'2026-09-13 08:37:49.848522','+05:30','C','2026-09-13 08:37:49.848522','2026-09-13 08:37:49.848522','fdservice','FD_BATCH','947d04ed-bc93-4a18-8258-a0eb053fb277','fdservice',1691.59,'C','2027-01-01',10,'FD_DEP_LIAB','INTEREST FD001000001','2027-01-01','1c3e26df-59be-4135-867a-71707cfab9be'),(21,'2026-09-13 08:37:49.848522','+05:30','C','2026-09-13 08:37:49.848522','2026-09-13 08:37:49.848522','fdservice','FD_BATCH','a8c9a5c8-ec1a-47b5-a450-067a857bd62b','fdservice',1719.30,'D','2027-01-01',11,'FD_INT_EXP','INTEREST FD001000001','2027-01-01','cf9984f7-1aa3-4c06-8bd3-f7ee9e4bf7f3'),(22,'2026-09-13 08:37:49.848522','+05:30','C','2026-09-13 08:37:49.848522','2026-09-13 08:37:49.848522','fdservice','FD_BATCH','2695f405-bb12-4bb7-8709-f6c6d24bb16a','fdservice',1719.30,'C','2027-01-01',11,'FD_DEP_LIAB','INTEREST FD001000001','2027-01-01','cf9984f7-1aa3-4c06-8bd3-f7ee9e4bf7f3'),(23,'2026-09-13 08:37:49.860641','+05:30','C','2026-09-13 08:37:49.860641','2026-09-13 08:37:49.860641','fdservice','FD_BATCH','e35c3464-9657-4b2b-ab93-b9d6773f2302','fdservice',106660.15,'D','2027-01-01',12,'FD_DEP_LIAB','MATURITY_PAYOUT FD001000001','2027-01-01','e38ced0f-e47a-4e97-a6e9-6698ec3476ed'),(24,'2026-09-13 08:37:49.860641','+05:30','C','2026-09-13 08:37:49.860641','2026-09-13 08:37:49.860641','fdservice','FD_BATCH','d1ba66bf-d87d-497a-a076-4b19f94404a1','fdservice',106660.15,'C','2027-01-01',12,'FD_SETTLEMENT','MATURITY_PAYOUT FD001000001','2027-01-01','e38ced0f-e47a-4e97-a6e9-6698ec3476ed'),(25,'2026-09-13 16:15:39.491727','+05:30','C','2026-09-13 16:15:39.491727','2026-09-13 16:15:39.491727','fdservice','FD_API','e915b74e-a260-48df-94b5-d56491bc04bf','fdservice',100000.00,'D','2026-01-01',13,'FD_SETTLEMENT','DEPOSIT FD001000003','2026-01-01','332fb358-c6a4-430d-9d0b-2adc25539517'),(26,'2026-09-13 16:15:39.491727','+05:30','C','2026-09-13 16:15:39.491727','2026-09-13 16:15:39.491727','fdservice','FD_API','5ee10e8a-8fbe-4a51-9dfc-e791cc7749fb','fdservice',100000.00,'C','2026-01-01',13,'FD_DEP_LIAB','DEPOSIT FD001000003','2026-01-01','332fb358-c6a4-430d-9d0b-2adc25539517'),(27,'2026-09-13 16:19:51.750295','+05:30','C','2026-09-13 16:19:51.750295','2026-09-13 16:19:51.750295','fdservice','FD_BATCH','39fd2719-384b-4196-be07-ec6c8f24a32d','fdservice',1602.74,'D','2026-04-01',14,'FD_INT_EXP','INTEREST FD001000003','2026-04-01','f7b3893e-9629-459e-a81d-77529cecb263'),(28,'2026-09-13 16:19:51.750295','+05:30','C','2026-09-13 16:19:51.750295','2026-09-13 16:19:51.750295','fdservice','FD_BATCH','eeba4ce7-fe28-4a06-adf8-db2ddfaaaa67','fdservice',1602.74,'C','2026-04-01',14,'FD_DEP_LIAB','INTEREST FD001000003','2026-04-01','f7b3893e-9629-459e-a81d-77529cecb263');
/*!40000 ALTER TABLE `fd_gl_entries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fd_transactions`
--

DROP TABLE IF EXISTS `fd_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fd_transactions` (
  `fdt_id` bigint NOT NULL AUTO_INCREMENT,
  `acpt_ts` datetime(6) DEFAULT NULL,
  `acpt_ts_utc_ofst` varchar(6) DEFAULT NULL,
  `crud_value` varchar(1) DEFAULT NULL,
  `host_ts` datetime(6) DEFAULT NULL,
  `local_ts` datetime(6) DEFAULT NULL,
  `rule_system_id` varchar(32) DEFAULT NULL,
  `user_id` varchar(32) DEFAULT NULL,
  `uuid` varchar(36) DEFAULT NULL,
  `ws_id` varchar(32) DEFAULT NULL,
  `fdt_amt` decimal(18,2) NOT NULL,
  `fdt_bal_after` decimal(18,2) DEFAULT NULL,
  `fdt_bal_before` decimal(18,2) DEFAULT NULL,
  `fdt_ccy_cd` varchar(3) DEFAULT NULL,
  `fdt_dr_cr` varchar(1) NOT NULL,
  `fdt_efctv_dt` date DEFAULT NULL,
  `fda_id` char(36) NOT NULL,
  `fdt_pan_snap` varchar(10) DEFAULT NULL,
  `fdt_remarks` varchar(255) DEFAULT NULL,
  `fdt_reversal_of_id` bigint DEFAULT NULL,
  `fdt_tds_rt` decimal(6,4) DEFAULT NULL,
  `fdt_txn_dt` date NOT NULL,
  `fdt_txn_grp_id` char(36) NOT NULL,
  `fdt_txn_ts` datetime(6) DEFAULT NULL,
  `fdt_txn_typ` varchar(20) NOT NULL,
  `fdt_value_dt` date DEFAULT NULL,
  PRIMARY KEY (`fdt_id`),
  KEY `FK_FDT_FDA` (`fda_id`),
  KEY `FK_FDT_REVERSAL_OF` (`fdt_reversal_of_id`),
  CONSTRAINT `FK_FDT_FDA` FOREIGN KEY (`fda_id`) REFERENCES `fd_accounts` (`fda_id`),
  CONSTRAINT `FK_FDT_REVERSAL_OF` FOREIGN KEY (`fdt_reversal_of_id`) REFERENCES `fd_transactions` (`fdt_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fd_transactions`
--

LOCK TABLES `fd_transactions` WRITE;
/*!40000 ALTER TABLE `fd_transactions` DISABLE KEYS */;
INSERT INTO `fd_transactions` VALUES (1,'2026-09-13 08:37:49.630010','+05:30','C','2026-09-13 08:37:49.630010','2026-09-13 08:37:49.630010','fdservice','FD_API','c287a1a9-a688-491f-9029-2a03d216a22c','fdservice',100000.00,100000.00,0.00,'INR','C','2026-01-01','8f873049-2929-4ca8-8538-54790ff6e4b2',NULL,'Initial deposit',NULL,NULL,'2026-01-01','730c24c8-c9bb-48e8-9e05-de4c84ee1801','2026-09-13 08:37:49.630010','DEPOSIT','2026-01-01'),(2,'2026-09-13 08:37:49.658288','+05:30','C','2026-09-13 08:37:49.658288','2026-09-13 08:37:49.658288','fdservice','FD_API','6d4af909-6154-4f16-a57b-9b09b882cb06','fdservice',100000.00,100000.00,0.00,'INR','C','2026-01-01','3fff572a-7c91-464b-bff5-8e5694f8b0d2',NULL,'Initial deposit',NULL,NULL,'2026-01-01','82645575-cbb9-4009-96e1-8dfa88dabe96','2026-09-13 08:37:49.658288','DEPOSIT','2026-01-01'),(3,'2026-09-13 08:37:49.736818','+05:30','C','2026-09-13 08:37:49.736818','2026-09-13 08:37:49.736818','fdservice','FD_BATCH','cc3dae79-8225-4ca2-b3ac-aeab842a108b','fdservice',1602.74,101602.74,100000.00,'INR','C','2026-04-01','8f873049-2929-4ca8-8538-54790ff6e4b2',NULL,'Interest capitalized for period ending 2026-04-01',NULL,NULL,'2026-04-01','8756c69e-5698-4984-841e-cb67d382999f','2026-09-13 08:37:49.736818','INTEREST','2026-04-01'),(4,'2026-09-13 08:37:49.750504','+05:30','C','2026-09-13 08:37:49.750504','2026-09-13 08:37:49.750504','fdservice','FD_BATCH','7b34085f-8556-4420-9bfb-6ca28ee420d8','fdservice',1602.74,101602.74,100000.00,'INR','C','2026-04-01','3fff572a-7c91-464b-bff5-8e5694f8b0d2',NULL,'Interest capitalized for period ending 2026-04-01',NULL,NULL,'2026-04-01','3b25ff6c-cd5c-4854-b1f2-bd30a57755b0','2026-09-13 08:37:49.750504','INTEREST','2026-04-01'),(5,'2026-09-13 08:37:49.781836','+05:30','C','2026-09-13 08:37:49.781836','2026-09-13 08:37:49.781836','fdservice','FD_API','283e5789-61ac-49d9-87ce-22d4319c95d1','fdservice',1646.52,103249.26,101602.74,'INR','C','2026-07-16','3fff572a-7c91-464b-bff5-8e5694f8b0d2',NULL,'Interest capitalized for period ending 2026-07-01',NULL,NULL,'2026-07-16','121aabaa-2ff6-4612-b077-be9c306dbdcb','2026-09-13 08:37:49.781836','INTEREST','2026-07-01'),(6,'2026-09-13 08:37:49.792166','+05:30','C','2026-09-13 08:37:49.792166','2026-09-13 08:37:49.792166','fdservice','FD_API','80686438-d70d-4ec4-b0e2-e47917703b7c','fdservice',275.80,103525.06,103249.26,'INR','C','2026-07-16','3fff572a-7c91-464b-bff5-8e5694f8b0d2',NULL,'Interest capitalized for period ending 2026-07-16',NULL,NULL,'2026-07-16','66da10e4-6048-4478-8785-744597dc57ab','2026-09-13 08:37:49.792166','INTEREST','2026-07-16'),(7,'2026-09-13 08:37:49.801097','+05:30','C','2026-09-13 08:37:49.801097','2026-09-13 08:37:49.801097','fdservice','FD_API','fafca8ff-4860-411d-9c0b-1609bb0382ca','fdservice',35.25,103489.81,103525.06,'INR','D','2026-07-16','3fff572a-7c91-464b-bff5-8e5694f8b0d2',NULL,'Premature withdrawal penalty',NULL,NULL,'2026-07-16','66da10e4-6048-4478-8785-744597dc57ab','2026-09-13 08:37:49.801097','PENALTY','2026-07-16'),(8,'2026-09-13 08:37:49.806391','+05:30','C','2026-09-13 08:37:49.806391','2026-09-13 08:37:49.806391','fdservice','FD_API','345d24bf-2015-4b2d-88dc-842811d6a842','fdservice',103489.81,0.00,103489.81,'INR','D','2026-07-16','3fff572a-7c91-464b-bff5-8e5694f8b0d2',NULL,'Premature withdrawal - account closed',NULL,NULL,'2026-07-16','66da10e4-6048-4478-8785-744597dc57ab','2026-09-13 08:37:49.806391','WITHDRAWAL','2026-07-16'),(9,'2026-09-13 08:37:49.847007','+05:30','C','2026-09-13 08:37:49.847007','2026-09-13 08:37:49.847007','fdservice','FD_BATCH','4250101e-d0ad-4150-b87c-932852a206fc','fdservice',1646.52,103249.26,101602.74,'INR','C','2027-01-01','8f873049-2929-4ca8-8538-54790ff6e4b2',NULL,'Interest capitalized for period ending 2026-07-01',NULL,NULL,'2027-01-01','425569cb-bd4b-4d96-bba8-a35a11c0e5e8','2026-09-13 08:37:49.847007','INTEREST','2026-07-01'),(10,'2026-09-13 08:37:49.848522','+05:30','C','2026-09-13 08:37:49.848522','2026-09-13 08:37:49.848522','fdservice','FD_BATCH','d63da282-4756-4359-aed4-b6684d691d1e','fdservice',1691.59,104940.85,103249.26,'INR','C','2027-01-01','8f873049-2929-4ca8-8538-54790ff6e4b2',NULL,'Interest capitalized for period ending 2026-10-01',NULL,NULL,'2027-01-01','1c3e26df-59be-4135-867a-71707cfab9be','2026-09-13 08:37:49.848522','INTEREST','2026-10-01'),(11,'2026-09-13 08:37:49.848522','+05:30','C','2026-09-13 08:37:49.848522','2026-09-13 08:37:49.848522','fdservice','FD_BATCH','b72e4a20-72a5-4307-ad55-1b8a40dd3974','fdservice',1719.30,106660.15,104940.85,'INR','C','2027-01-01','8f873049-2929-4ca8-8538-54790ff6e4b2',NULL,'Interest capitalized for period ending 2027-01-01',NULL,NULL,'2027-01-01','cf9984f7-1aa3-4c06-8bd3-f7ee9e4bf7f3','2026-09-13 08:37:49.848522','INTEREST','2027-01-01'),(12,'2026-09-13 08:37:49.860641','+05:30','C','2026-09-13 08:37:49.860641','2026-09-13 08:37:49.860641','fdservice','FD_BATCH','26ab70d3-bd24-4b1f-be91-8f77789204db','fdservice',106660.15,0.00,106660.15,'INR','D','2027-01-01','8f873049-2929-4ca8-8538-54790ff6e4b2',NULL,'Maturity payout on 2027-01-01',NULL,NULL,'2027-01-01','e38ced0f-e47a-4e97-a6e9-6698ec3476ed','2026-09-13 08:37:49.860641','MATURITY_PAYOUT','2027-01-01'),(13,'2026-09-13 16:15:39.479635','+05:30','C','2026-09-13 16:15:39.479635','2026-09-13 16:15:39.479635','fdservice','FD_API','4e783b61-c07b-4795-bc4b-714f908aaae5','fdservice',100000.00,100000.00,0.00,'INR','C','2026-01-01','35b6aecd-9262-4926-b6c4-6b8e2f6bb790',NULL,'Initial deposit',NULL,NULL,'2026-01-01','332fb358-c6a4-430d-9d0b-2adc25539517','2026-09-13 16:15:39.479635','DEPOSIT','2026-01-01'),(14,'2026-09-13 16:19:51.744180','+05:30','C','2026-09-13 16:19:51.744180','2026-09-13 16:19:51.744180','fdservice','FD_BATCH','c96ea5a5-ff52-4af7-a595-6d2080ae0fd2','fdservice',1602.74,101602.74,100000.00,'INR','C','2026-04-01','35b6aecd-9262-4926-b6c4-6b8e2f6bb790',NULL,'Interest capitalized for period ending 2026-04-01',NULL,NULL,'2026-04-01','f7b3893e-9629-459e-a81d-77529cecb263','2026-09-13 16:19:51.744180','INTEREST','2026-04-01');
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

-- Dump completed on 2026-09-13 17:01:20
