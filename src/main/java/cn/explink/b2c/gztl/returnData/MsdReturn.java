package cn.explink.b2c.gztl.returnData;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MSD")
public class MsdReturn {
	@XmlElementWrapper(name = "Orders")
	@XmlElement(name = "Order")
	private List<GztlOrderReturnData> returnDatas;

	public List<GztlOrderReturnData> getReturnDatas() {
		return this.returnDatas;
	}

	public void setReturnDatas(List<GztlOrderReturnData> returnDatas) {
		this.returnDatas = returnDatas;
	}

}
