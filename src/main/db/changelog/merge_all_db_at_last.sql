-- handle table

CREATE TABLE IF NOT EXISTS `orders_goods` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `cwb` varchar(100) DEFAULT NULL,
  `cretime` varchar(50) DEFAULT NULL,
  `goods_code` varchar(100) DEFAULT NULL,
  `goods_brand` varchar(100) DEFAULT NULL,
  `goods_name` varchar(100) DEFAULT NULL,
  `goods_spec` varchar(100) DEFAULT NULL,
  `goods_num` varchar(100) DEFAULT NULL,
  `return_reason` varchar(200) DEFAULT NULL,
  `goods_pic_url` varchar(300) DEFAULT NULL,
  `remark1` varchar(200) DEFAULT NULL,
  `remark2` varchar(200) DEFAULT NULL,
  `remark3` varchar(200) DEFAULT NULL,
  `remark4` varchar(200) DEFAULT NULL,
  `remark5` varchar(200) DEFAULT NULL,
  `remark6` varchar(200) DEFAULT NULL,
  `remark7` varchar(200) DEFAULT NULL,
  `remark8` varchar(200) DEFAULT NULL,
  `remark9` varchar(200) DEFAULT NULL,
  `remark10` varchar(200) DEFAULT NULL,
  KEY `id` (`id`),
  KEY `cwb_idx` (`cwb`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- handle column

call proc_add_column_if_not_exists('express_set_customer_info', 'autoProductcwbpre', 'ALTER TABLE `express_set_customer_info` ADD COLUMN `autoProductcwbpre` varchar(50) COLLATE utf8_bin DEFAULT '''';');
call proc_add_column_if_not_exists('express_set_customer_info', 'b2cEnum', 'ALTER TABLE `express_set_customer_info` ADD COLUMN `b2cEnum` varchar(50) COLLATE utf8_bin DEFAULT NULL;');
call proc_add_column_if_not_exists('express_set_customer_info', 'companyname', 'ALTER TABLE `express_set_customer_info` ADD COLUMN `companyname` varchar(100) COLLATE utf8_bin DEFAULT NULL;');
call proc_add_column_if_not_exists('express_set_customer_info', 'isAutoProductcwb', 'ALTER TABLE `express_set_customer_info` ADD COLUMN `isAutoProductcwb` int(4) DEFAULT ''0'';');
call proc_add_column_if_not_exists('express_set_customer_info', 'isFeedbackcwb', 'ALTER TABLE `express_set_customer_info` ADD COLUMN `isFeedbackcwb` int(4) DEFAULT ''0'';');
call proc_add_column_if_not_exists('express_set_customer_info', 'isUsetranscwb', 'ALTER TABLE `express_set_customer_info` ADD COLUMN `isUsetranscwb` int(4) DEFAULT ''0'';');
call proc_add_column_if_not_exists('express_set_customer_info', 'isqufendaxiaoxie', 'ALTER TABLE `express_set_customer_info` ADD COLUMN `isqufendaxiaoxie` int(4) DEFAULT ''1'';');
call proc_add_column_if_not_exists('express_set_customer_info', 'isypdjusetranscwb', 'ALTER TABLE `express_set_customer_info` ADD COLUMN `isypdjusetranscwb` int(4) DEFAULT ''0'';');
call proc_add_column_if_not_exists('express_set_customer_info', 'needchecked', 'ALTER TABLE `express_set_customer_info` ADD COLUMN `needchecked` int(11) DEFAULT ''0'';');
call proc_add_column_if_not_exists('express_set_customer_info', 'paytype', 'ALTER TABLE `express_set_customer_info` ADD COLUMN `paytype` int(4) DEFAULT NULL;');
call proc_add_column_if_not_exists('express_set_customer_info', 'smschannel', 'ALTER TABLE `express_set_customer_info` ADD COLUMN `smschannel` int(4) DEFAULT ''0'';');

-- 因为“The maximum row size for the used table type, not counting BLOBs, is 65535”错误，把最长的字段transcwb, multi_shipcwb，本来是varchar(1500)类型，改为text
ALTER TABLE `express_ops_cwb_detail` 
CHANGE COLUMN `transcwb` `transcwb` text COLLATE utf8_bin DEFAULT NULL,
CHANGE COLUMN `multi_shipcwb` `multi_shipcwb` text COLLATE utf8_bin DEFAULT NULL;

-- 因为“The maximum row size for the used table type, not counting BLOBs, is 65535”错误，remark5本来是varchar(1500)类型，改为text
ALTER TABLE `express_ops_cwb_detail` 
CHANGE COLUMN `customercommand` `customercommand` varchar(500) COLLATE utf8_bin DEFAULT NULL,
CHANGE COLUMN `firstleavedreasonid` `firstleavedreasonid` bigint(11) DEFAULT '0' COMMENT '滞留一级原因',
CHANGE COLUMN `infactfare` `infactfare` decimal(19,2) DEFAULT '0.00' COMMENT '实收运费',
CHANGE COLUMN `remark5` `remark5` text COLLATE utf8_bin DEFAULT NULL,
CHANGE COLUMN `reserve` `reserve` varchar(1000) COLLATE utf8_bin DEFAULT NULL,
CHANGE COLUMN `reserve1` `reserve1` varchar(1000) COLLATE utf8_bin DEFAULT NULL,
CHANGE COLUMN `shouldfare` `shouldfare` decimal(19,2) DEFAULT '0.00' COMMENT '应收运费',
CHANGE COLUMN `backreason` `backreason` varchar(500) COLLATE utf8_bin DEFAULT '',
CHANGE COLUMN `consigneename` `consigneename` varchar(500) COLLATE utf8_bin DEFAULT NULL,
CHANGE COLUMN `consigneepostcode` `consigneepostcode` varchar(500) COLLATE utf8_bin DEFAULT NULL,
CHANGE COLUMN `losereason` `losereason` varchar(225) COLLATE utf8_bin DEFAULT NULL,
CHANGE COLUMN `shiptime` `shiptime` varchar(100) COLLATE utf8_bin DEFAULT NULL,
CHANGE COLUMN `transcwb` `transcwb` mediumtext COLLATE utf8_bin,
CHANGE COLUMN `weishuakareason` `weishuakareason` varchar(225) COLLATE utf8_bin DEFAULT NULL;

ALTER TABLE `express_b2cdata_weisuda` 
CHANGE COLUMN `cwbordertypeid` `cwbordertypeid` varchar(11) COLLATE utf8_bin DEFAULT '1' COMMENT '订单类型',
CHANGE COLUMN `remark` `remark` varchar(500) COLLATE utf8_bin DEFAULT NULL;

-- handle index
call proc_create_index_if_not_exists('express_ops_cwb_detail', 'datail_emaildate_Idx', '', 'CREATE INDEX datail_emaildate_Idx on express_ops_cwb_detail(emaildate);');

