package cn.explink.b2c.feiniuwang;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnData {
	@JsonProperty(value = "traceslist")
	private List<Traceslist> traceslist;

	public List<Traceslist> getTraceslist() {
		return traceslist;
	}
	public void setTraceslist(List<Traceslist> traceslist) {
		this.traceslist = traceslist;
	}
	
}
