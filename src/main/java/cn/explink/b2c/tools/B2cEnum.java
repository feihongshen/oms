package cn.explink.b2c.tools;

/**
 * ------------------------注意！
 * 【29】开头的枚举约定为唯品会备用！-----------------------------------
 */
public enum B2cEnum {
	LieBo(20001, "裂帛", "liebo"), // 裂帛
	DangDang(20002, "当当", "dangdang"), // 当当
	MinSiDa(20004, "敏思达", "minsida"), // 敏思达
	Tmall(20005, "天猫-淘宝商城", "tmall"), JuMeiYouPin(20006, "聚美优品", "jumeiyoupin"), // 聚美优品
	KuaiDi100(20007, "快递100", "kuaidi100"), // 快递100
	VipShop_shanghai(20008, "唯品会_上海仓", "vipshop_shanghai"), // 唯品会shanghai
	VipShop_beijing(20010, "唯品会_北京仓", "vipshop_beijing"), // 唯品会 beijing
	VipShop_nanhai(20011, "唯品会_南海仓", "vipshop_nanhai"), // 唯品会 nanhai
	Yihaodian(20009, "一号店", "yihaodian"), VipShop_huabei(20012, "唯品会_华北仓", "vipshop_huabei"), // 唯品会
																								// VipShop_huabei
	VipShop_huanan(20013, "唯品会_华南仓", "vipshop_huabei"), // 唯品会 VipShop_huanan
	Smile(20015, "广州思迈速递", "Smile"), LefengWang(20016, "乐蜂网", "lefengwang"), YiXun(20017, "易讯网", "yixun"), Rufengda(20018, "如风达-凡客", "rufengda"), VipShop_tuangou(20019, "唯品会_团购仓",
			"vipshop_tuangou"), VipShop_chengdu(20020, "唯品会_成都仓", "vipshop_chengdu"), JingDong(20021, "京东", "jingdong"), // 京东
	Gome(20022, "国美", "gome"), // 国美
	YouGou(20023, "优购", "yougou"), // 优购，跟快递100接口一致
	Yihaodian_beijing(20024, "一号店_北京", "yihaodian_beijing"), // 一号店 北京

	Tmall_ESuBao(20025, "天猫-E速宝", "tmall_esubao"), // 直接跟E速报合作的物流公司需配置这个
	Tmall_WanXiangESuBao(20026, "上海万象(天猫-E速宝)", "tmall_wanxiangesubao"), // 通过万象跟易速宝合作。
	Tmall_HuiTongTianXia(20027, "汇通天下(使用天猫API)", "tmall_huitongtianxia"), // 使用tmall接口的配送方，如汇通天下（汇通则作为tmall，小红帽则作为配送商）

	DangDang_chuku(20028, "当当网-订单导入", "dangdang_dataimport"), // 当当订单数据出库接口
	Tmall_ESuBaoShengXian(20029, "E速宝-生鲜", "tmall_esubaoshengxian"), // 直接跟E速报合作的物流公司需配置这个

	VipShop_shanghaishandong(20030, "唯品会_上海仓(山东)", "vipshop_shanghaishandong"), // 唯品会shanghaishandong
	VipShop_nanhaishandong(20031, "唯品会_南海仓(山东)", "vipshop_nanhaishandong"), // 唯品会
																			// nanhaishandong
	YangGuang(20032, "央广", "yangguang"), // 央广购物
	Tmall_ESuBaobangbaoshi(20033, "E速宝(帮宝适)", "tmall_esubaobangbaoshi"), // 通过万象跟易速宝合作。
	YeMaiJiu(20034, "也买酒", "yemaijiu"), GuangZhouABC(20035, "广州爱彼西", "GuangZhouABC"), HangZhouABC(20036, "杭州爱彼西", "HangZhouABC"), BaShaLiPing(20037, "芭莎礼品", "bashaliping"), YeMaiJiuSearch(20038,
			"也买酒_查询", "yemaijiusearch"), // 也买酒
	DongFangCJ(20039, "东方CJ", "dongfangcj"), // 东方CJ
	HaoXiangGou(20041, "好享购", "haoxianggou"), // 好享购J
	Amazon(20040, "亚马逊", "yamaxun"), // 亚马逊
	Rufengda_shandong(20042, "如风达-凡客(山东)", "rufengda_shandong"), Rufengda_gansu(20043, "如风达-凡客(甘肃)", "rufengda_gansu"), Rufengda_ningxia(20044, "如风达-凡客(宁夏)",
			"rufengda_ningxia"), Rufengda_shanxi(20045, "如风达-凡客(陕西)", "rufengda_shanxi"), Rufengda_qinghai(20046, "如风达-凡客(青海)", "rufengda_qinghai"),

	LeJieDi(200048, "乐捷递", "lejiedi"), // 乐捷递
	HomeGou(20049, "家有购物", "homegou"), // 家有购物

	Yihaodian_shandong(20050, "一号店_山东", "yihaodian_shandong"), // 一号店 山东
	Tmall_zhouqigou(20051, "天猫-周期购", "tmall_zhouqigou"), // 新的宅配服务
	Moonbasa(20052, "梦芭莎", "moonbasa"),

	DPFoss1(20047, "德邦物流1", "DPFoss1"), DPFoss2(20053, "德邦物流2", "DPFoss2"), DPFoss3(20054, "德邦物流3", "DPFoss3"), DPFoss4(20055, "德邦物流4", "DPFoss4"), Maikaolin(20060, "麦考林", "maikaolin"), // 麦考林
	Huitongtx(20056, "汇通天下", "huitongtx"),

	VipShop_cangku1(20057, "唯品会_仓库1", "vipshop_cangku1"), VipShop_cangku2(20058, "唯品会_仓库2", "vipshop_cangku2"), VipShop_cangku3(20059, "唯品会_仓库3", "vipshop_cangku3"), Saohuobang(20065, "扫货帮",
			"saohuobang"), Saohuobang_2(20070, "扫货帮美甲", "saohuobang_2"), Liantong(20061, "联通", "liantong"), Wanxiang(20062, "万象-查询", "wangxiang"), Telecomshop(20063, "电信商城", "telecomshop"),

	Tmall_bak1(20064, "天猫-备用1", "tmall_bak1"), // 新的宅配服务
	Tmall_bak2(20066, "天猫-备用2", "tmall_bak2"), // 新的宅配服务
	Benlaishenghuo(20067, "本来生活", "benlaishenghuo"), Jiuxian(20068, "酒仙网", "jiuxian"), happyGo(20071, "快乐购", "happyGo"), Maisike(20073, "迈思可", "maisike"), wholeLine(20074, "全线快递", "QXKD"), MaiMaiBao(
			20076, "买卖宝", "mmb"), ChinaMobile(20077, "中国移动", "chinamobile"), Letv(20078, "乐视网",
					"letv"), YongHuics(20079, "永辉超市", "yonghuics"), Hxgdms(20080, "好享购DMS", "hxgdms"), SFexpress(20081, "顺丰快递", "sfexpress"), EfastERP_bak(20082, "中兴云购ERP", "efastERP_bak"),

	Wangjiu(20083, "网酒网", "wangjiu"),

	HomegoBJ(20084, "家有购物（北京）", "homegoubj"), LeChong(20085, "乐宠（科捷）", "lechong"), Smiled(20086, "思迈下游", "smiled"), SFexpressXHM(20087, "顺丰快递(小红帽)", "sfexpressxhm"), Zhongliang(20088, "顺丰快递(小红帽)",
			"zhongliang"), JiuYe(20089, "九曳", "jiuye"), Lefeng(20093, "乐蜂网-回传", "lefeng"), Guangzhoutonglu(20095, "广州通路", "guangzhoutonglu"), GuangzhoutongluWaifadan(20096, "广州通路对接_外发单",
					"guangzhoutongluwaifadanduijie"), VipShop_OXO(20090, "唯品会_oxo", "vipshop_OXO"), JiuYe1(20101, "九曵1", "jiuye_1"), JiuYe2(20102, "九曵2", "jiuye_2"), JiuYe3(20103, "九曵3",
							"jiuye_3"), JiuYe4(20104, "九曵4", "jiuye_4"), JiuYe5(20105, "九曵5", "jiuye_5"), Feiniuwang(20107, "飞牛网(http)", "feiniuwang"), YongHui(20108, "永辉",
									"yonghui"), ZheMeng(20117, "哲盟-安达信", "zhemeng"), HaoYiGou(20120, "好易购", "haoyigou"), GuangXinDianXin(20121, "广信电信", "guangxindianxin"),

	VipShop_cangku4(20122, "唯品会_仓库4", "vipshop_cangku4"), VipShop_cangku5(20123, "唯品会_仓库5", "vipshop_cangku5"), VipShop_cangku6(20124, "唯品会_仓库6", "vipshop_cangku6"), VipShop_cangku7(20125, "唯品会_仓库7",
			"vipshop_cangku7"), VipShop_cangku8(20126, "唯品会_仓库8", "vipshop_cangku8"), VipShop_cangku9(29001, "唯品会_仓库9",
					"vipshop_cangku9"), VipShop_cangku10(29002, "唯品会_仓库10", "vipshop_cangku10"), VipShop_cangku11(29003, "唯品会_仓库11", "vipshop_cangku11"),

	VipShop_cangku12(29004, "唯品会_仓库12", "vipshop_cangku12"), // 添加20个接口----刘武强20160811
	VipShop_cangku13(29005, "唯品会_仓库13", "vipshop_cangku13"), VipShop_cangku14(29006, "唯品会_仓库14", "vipshop_cangku14"), VipShop_cangku15(29007, "唯品会_仓库15", "vipshop_cangku15"), VipShop_cangku16(29008,
			"唯品会_仓库16", "vipshop_cangku16"), VipShop_cangku17(29009, "唯品会_仓库17", "vipshop_cangku17"), VipShop_cangku18(29010, "唯品会_仓库18", "vipshop_cangku18"), VipShop_cangku19(29011, "唯品会_仓库19",
					"vipshop_cangku19"), VipShop_cangku20(29012, "唯品会_仓库20", "vipshop_cangku20"), VipShop_cangku21(29013, "唯品会_仓库21", "vipshop_cangku21"), VipShop_cangku22(29014, "唯品会_仓库22",
							"vipshop_cangku22"), VipShop_cangku23(29015, "唯品会_仓库23", "vipshop_cangku23"), VipShop_cangku24(29016, "唯品会_仓库24", "vipshop_cangku24"), VipShop_cangku25(29017, "唯品会_仓库25",
									"vipshop_cangku25"), VipShop_cangku26(29018, "唯品会_仓库26", "vipshop_cangku26"), VipShop_cangku27(29019, "唯品会_仓库27", "vipshop_cangku27"), VipShop_cangku28(29020,
											"唯品会_仓库28", "vipshop_cangku28"), VipShop_cangku29(29021, "唯品会_仓库29",
													"vipshop_cangku29"), VipShop_cangku30(29022, "唯品会_仓库30", "vipshop_cangku30"), VipShop_cangku31(29023, "唯品会_仓库31", "vipshop_cangku31"),
	/*------------------------注意！ 【29】开头的枚举约定为唯品会备用！-----------------------------------*/

	SuNing(20127, "苏宁易购", "suning"),

	Yihaodian_bakup1(20130, "一号店_备用1", "yihaodian_bakup1"), Yihaodian_bakup2(20131, "一号店_备用2", "yihaodian_bakup2"), Yihaodian_bakup3(20132, "一号店_备用3", "yihaodian_bakup3"), meilinkai(20135, "玫琳凯",
			"meilinkai"), HuanQiuGou(20136, "环球购物", "huanqiugouwu"), Zhongliang_bak1(21001, "中粮(我买网)_备用1", "zhongliang_bak1"), Zhongliang_bak2(21002, "中粮(我买网)_备用2",
					"zhongliang_bak2"), ThirdPartyOrder_2_DO(20129, "外单推DO", "thirdPartyOrder2DO"), VipShop_TPSAutomate(20227, "TPS自动化", "vipshop_tps_automate"), Shenzhoushuma(20137, "神州数码",
							"shenzhoushuma"), ZheMeng_track(23001, "哲盟_轨迹", "zhemeng_track"), TPS_MQ(22501, "tps订单下发接口", "tpsvipshop_mq"), TPS_TraceFeedback(22601, "订单轨迹回传给tps接口",
									"tps_traceFeedback"), TPS_MQExpress(22502, "tps快递单下发接口",
											"tpsvipshop_mqExpress"), TPS_kuajinggou(22503, "跨境购订单下发接口", "kuajinggouvipshop_mq"), MSS(21005, "美食送", "MSS");
	private int key;
	private String text;
	private String method;

	private B2cEnum(int key, String text, String method) {
		this.key = key;
		this.text = text;
		this.method = method;
	}

	public String getMethod() {
		return this.method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public int getKey() {
		return this.key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
