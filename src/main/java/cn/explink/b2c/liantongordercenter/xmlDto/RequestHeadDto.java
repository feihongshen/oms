package cn.explink.b2c.liantongordercenter.xmlDto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "UNI_BSS_HEAD")
public class RequestHeadDto {
	private String RESPCODE;
	private String RESPDESC;

	@XmlElement(name = "RESP_CODE")
	public String getRESPCODE() {
		return this.RESPCODE;
	}

	public void setRESPCODE(String rESPCODE) {
		this.RESPCODE = rESPCODE;
	}

	@XmlElement(name = "RESP_DESC")
	public String getRESPDESC() {
		return this.RESPDESC;
	}

	public void setRESPDESC(String rESPDESC) {
		this.RESPDESC = rESPDESC;
	}

}
