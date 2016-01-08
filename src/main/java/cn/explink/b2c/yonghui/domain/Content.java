package cn.explink.b2c.yonghui.domain;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Content implements Serializable {
	private String client_id;
	private String secret;
	private Order orderList;

	public String getClient_id() {
		return this.client_id;
	}

	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}

	public String getSecret() {
		return this.secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public Order getOrderList() {
		return this.orderList;
	}

	public void setOrderList(Order orderList) {
		this.orderList = orderList;
	}

}
