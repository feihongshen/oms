package cn.explink.jms.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CwbOrderWithDeliveryState {
	private DmpCwbOrder cwbOrder;
	private DmpDeliveryState deliveryState;
	private TransCwbDetail transCwbDetail;

	public TransCwbDetail getTransCwbDetail() {
		return transCwbDetail;
	}

	public void setTransCwbDetail(TransCwbDetail transCwbDetail) {
		this.transCwbDetail = transCwbDetail;
	}

	public DmpCwbOrder getCwbOrder() {
		return cwbOrder;
	}

	public void setCwbOrder(DmpCwbOrder cwbOrder) {
		this.cwbOrder = cwbOrder;
	}

	public DmpDeliveryState getDeliveryState() {
		return deliveryState;
	}

	public void setDeliveryState(DmpDeliveryState deliveryState) {
		this.deliveryState = deliveryState;
	}
}
