package cn.explink.b2c.feiniuwang;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Traceslist {
	@JsonProperty(value = "logisticproviderid")
	private String logisticproviderid;
	@JsonProperty(value = "mailno")
	private String mailno;
	@JsonProperty(value = "txlogisticid")
	private String txlogisticid;
	@JsonProperty(value = "returnno")
	private String returnno;
	@JsonProperty(value = "transitno")
	private String transitno;
	@JsonProperty(value = "traces")
	private List<Traces> traces;
	
	public String getLogisticproviderid() {
		return logisticproviderid;
	}
	public void setLogisticproviderid(String logisticproviderid) {
		this.logisticproviderid = logisticproviderid;
	}
	public String getTxlogisticid() {
		return txlogisticid;
	}
	public void setTxlogisticid(String txlogisticid) {
		this.txlogisticid = txlogisticid;
	}
	public String getMailno() {
		return mailno;
	}
	public void setMailno(String mailno) {
		this.mailno = mailno;
	}
	public String getReturnno() {
		return returnno;
	}
	public void setReturnno(String returnno) {
		this.returnno = returnno;
	}
	public String getTransitno() {
		return transitno;
	}
	public void setTransitno(String transitno) {
		this.transitno = transitno;
	}
	public List<Traces> getTraces() {
		return traces;
	}
	public void setTraces(List<Traces> traces) {
		this.traces = traces;
	}
	
}
