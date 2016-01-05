#为用户表添加钱包的属性
#06-25  hys
alter table t_users add column wallet_address varchar(50); 
alter table t_users add column wallet_secret varchar(50); 

#添加 井通对标的审核 表
CREATE TABLE `t_bid_jingtum` (
`id`  int(11) NOT NULL AUTO_INCREMENT ,
`bid_id`  int(11) NULL DEFAULT NULL COMMENT '标的id' ,
`bid_address`  varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标在井通平台对应的id' ,
`start_time`  datetime NULL DEFAULT NULL COMMENT '创建时间' ,
`end_time`  datetime NULL DEFAULT NULL COMMENT '结束时间【状态修改时间】' ,
`result`  int(2) NULL DEFAULT 0 COMMENT '最终审批结果' ,
`status`  int(2) NULL DEFAULT 0 COMMENT '当前状态 【默认：未审核】' ,
PRIMARY KEY (`id`)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
AUTO_INCREMENT=1
ROW_FORMAT=COMPACT
;

# system_option  新增 系统配置项
INSERT INTO `t_system_options` VALUES (10101, 'recruitmentPeriod', '0', '募集期利息设置功能');
INSERT INTO `t_system_options` VALUES (10102, 'interest', '0.09', '每天利率');

#添加易宝支付 接口 ，其中账号为  测试账号，正式环境应该切换很账号
#07-02
INSERT INTO `t_dict_payment_gateways` VALUES ('3', '易宝支付', '10001126856', '10001126856', '69cl522AV6q613Ii4W6u8K6XuW8vM1N6bFgyv769220IuYe9u37N4y7rI4Pl', NULL, 1);
INSERT INTO `t_dict_payment_gateways` VALUES ('4', '井通支付', 'jp64GVM12GPjh9S1Df3yE3114i4FHDjso2', 'jp64GVM12GPjh9S1Df3yE3114i4FHDjso2', 'snfddURKmMmiUszH2au7dyM4qiR6M', null, 1);
#最后一个参数应该为mysql  类型：bit  值：1   手动填写

#向t_right_actions 添加  井通审核详情页 权限 
INSERT INTO `t_right_actions` VALUES (2038, 41, 'supervisor.bidManager.BidPlatformAction.reimbursementList', '平台借款标管理');
INSERT INTO `t_right_actions` VALUES (2039, 41, 'supervisor.bidManager.BidPlatformAction.jingtumingDetail', '平台借款标管理');
INSERT INTO `t_right_actions` VALUES (2040, 41, 'supervisor.bidManager.BidPlatformAction.auditTojingtum', '平台借款标管理');
INSERT INTO `t_right_actions` VALUES (2041, 41, 'supervisor.bidManager.BidPlatformAction.jingtumToFundraise', '平台借款标管理');






#添加满标转账记录表
CREATE TABLE `t_invest_jingtum` (
`id`  int(11) NOT NULL ,
`bid_id`  int(11) NOT NULL ,
`user_id`  int(11) NOT NULL ,
`bid_address`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
`invest_amount`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
`wallet_address`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
`wallet_secret`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
`create_time`  datetime NOT NULL ,
`status`  int(2) NOT NULL ,
PRIMARY KEY (`id`)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
AUTO_INCREMENT=1
ROW_FORMAT=COMPACT
;


#添加 投标记录 查询视图
CREATE VIEW `v_invest_jingtum`AS 
select `t_invests`.`id` AS `id`,`t_invests`.`bid_id` AS `bid_id`,`t_invests`.`user_id` AS `user_id`,`t_bid_jingtum`.`bid_address` AS `bid_address`,`t_invests`.`amount` AS `invest_amount`,`t_users`.`wallet_address` AS `wallet_address`,`t_users`.`wallet_secret` AS `wallet_secret` from (((((`t_invests` left join `t_bids` on((`t_bids`.`id` = `t_invests`.`bid_id`))) join `t_system_options`) left join `t_users` on((`t_users`.`id` = `t_invests`.`user_id`))) left join `t_users` `bid_user` on((`bid_user`.`id` = `t_bids`.`user_id`))) left join `t_bid_jingtum` on((`t_bid_jingtum`.`bid_id` = `t_bids`.`id`))) where (`t_system_options`.`_key` = 'loan_number') ;

#井通转账记录
CREATE TABLE `t_jingtum_transfer_details` (
`id`  int(11) NOT NULL AUTO_INCREMENT ,
`from_address`  varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '井通转账-账号' ,
`to_address`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '井通转账-账号密钥' ,
`trsfer_amount`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '井通转账-金额' ,
`from_currency`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '交易币种' ,
`client_resource_id`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '井通转账-资源号' ,
`hash`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '井通转账-交易HASH[可用于查询交易记录详情]' ,
`state`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '交易状态【validated：有效，pending:正在处理】' ,
`fee`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '交易费用，井通计价' ,
`date`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '交易时间 unixtime' ,
`result`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '支付交易的服务器结果，tesSUCCESS表示成功' ,
`success`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '支付服务器的结果 tesSUCCESS' ,
`create_time`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建时间【系统时间】' ,
PRIMARY KEY (`id`)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
COMMENT='p2x 平台  在井通上的转账记录'
AUTO_INCREMENT=1
ROW_FORMAT=COMPACT
;

#在正式环境中，请替换，当前系统中的【提现的商户编号[p1_custom]】，默认使用【易宝支付的测试账号】[controllers.supervisor.financeManager.PlatformAccountManager.199_line]
#在正式环境中，请替换，当前系统中的【提现的商户编号[p1_custom]】，默认使用【易宝支付的测试账号】[controllers.supervisor.financeManager.PlatformAccountManager.199_line]
#在正式环境中，请替换，当前系统中的【提现的商户编号[p1_custom]】，默认使用【易宝支付的测试账号】[controllers.supervisor.financeManager.PlatformAccountManager.199_line]
#还款记录，井通转账记录 ，和融资通收回记录
CREATE TABLE `t_payment_jingtum` (
`id`  int(11) NOT NULL AUTO_INCREMENT ,
`bid_address`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '标在井通的地址' ,
`periods`  int(11) NOT NULL COMMENT '本标当前的还款期数【第几期】' ,
`bid_user_id`  int(11) NOT NULL COMMENT '借款人id' ,
`bid_pay_id`  int(11) NOT NULL COMMENT '投资人id' ,
`invest_amount`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '还款金额 【本息和】' ,
`tong_amount`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '收回的融资通的金额  【本金】 收回【融资通】的金额【从投资者 向 p2x 收回】' ,
`create_time`  datetime NOT NULL COMMENT '本记录创建时间' ,
`update_time`  datetime NULL DEFAULT NULL COMMENT '更新时间' ,
`result`  int(2) NOT NULL COMMENT '本次还款 转账结果【CNY币种】' ,
`tong_result`  int(2) NOT NULL COMMENT '本次还款 【融资通】收回的结果' ,
PRIMARY KEY (`id`)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
COMMENT='p2x 还款记录  ，在井通的转账记录 ，收回融资通的记录'
AUTO_INCREMENT=1
ROW_FORMAT=COMPACT
;


CREATE TABLE `t_jingtum_withdraw_records` (
`id`  int(11) NOT NULL AUTO_INCREMENT ,
`bank_code`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '银行提现 状态码' ,
`orders`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '订单号' ,
`dcode`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '井通银行打款 状态码' ,
`bank_status`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '银行处理状态' ,
`error_msg`  varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '错误信息' ,
`result`  int(2) NULL DEFAULT 0 COMMENT '最终结果' ,
`remark1`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
`remark2`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
PRIMARY KEY (`id`)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
AUTO_INCREMENT=1
ROW_FORMAT=COMPACT
;



