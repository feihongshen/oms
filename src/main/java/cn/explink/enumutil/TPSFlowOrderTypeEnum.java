package cn.explink.enumutil;

public enum TPSFlowOrderTypeEnum {
			registeScan(0,"揽件扫描"),
			inboundScan(1,"进站扫描"),
			sorttingScan(2,"分拣完成扫描"),
			outboundScan(3,"出站扫描"),
			packingScan(4,"打包扫描"),
			unpackScan(5,"拆包扫描"),
			weightScan(6,"称重扫描"),
			loadScan(7,"装车扫描"),
			unLoadScan(8,"卸车扫描"),
			deliveryScan(9,"派送扫描"),
			pickUpScan(10,"取件扫描"),
			signInScan(11,"签收扫描"),
			returnScan(12,"返站扫描"),
			retensionScan(13,"滞留扫描"),
			transferScan(14,"转站扫描"),
			exceptionScan(15,"异常扫描"),
			receiveScan(16,"收货扫描"),
			unPackCheckScan(17,"复核扫描"),
			unSorttingScan(18,"取消分拣"),
			sendScan(19,"发货扫描"),
			unSendScan(20,"取消发货"),
			startScan(21,"发车扫描"),
			unStartScan(22,"取消发车"),
			placeOrder(23,"已下单"),
			matchCarrier(24,"已分配省公司"),
			distributionOrg(25,"已分配站点"),
			distributionCourier(26,"已分配快递员"),
			receiveSuccess(27,"揽件成功"),
			provinceSurpass(28,"省公司超区"),
			siteSurpass(29,"站点超区"),
			receiveSurpass(30,"揽件超区"),
			receiveFail(31,"揽件失败"),
			ticklingDelay(32,"反馈滞留"),
			reserveClose(33,"关闭"),
			rejectScan(34,"拒收"),
			receiveFAILURE(35,"揽件失败"),
			onRoad(36,"在途"),
			cancel(37,"取消")

;
	private int value;
	private String text;


	private TPSFlowOrderTypeEnum(int value, String text) {
		this.value = value;
		this.text = text;
	}

	public int getValue() {
		return value;
	}

	public String getText() {
		return text;
	}
}
