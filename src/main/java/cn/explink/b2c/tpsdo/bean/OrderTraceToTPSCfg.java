package cn.explink.b2c.tpsdo.bean;

public class OrderTraceToTPSCfg extends ThirdPartyOrder2DOCfg{
	private int sendMaxCount;//每次推送轨迹数量

	public int getSendMaxCount() {
		return sendMaxCount;
	}

	public void setSendMaxCount(int sendMaxCount) {
		this.sendMaxCount = sendMaxCount;
	}
}
