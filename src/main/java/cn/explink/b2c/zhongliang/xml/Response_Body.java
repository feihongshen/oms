package cn.explink.b2c.zhongliang.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;


public class Response_Body {
private List<Response_Order> Orders=new ArrayList<Response_Order>();

@XmlElement(name="Order")
public List<Response_Order> getOrders() {
	return Orders;
}

public void setOrders(List<Response_Order> orders) {
	Orders = orders;
}


}
