package cn.explink.b2c.moonbasa;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ArrayOfDeliveryInfo")
public class MBSArrayOfDeliveryInfo
{

  @XStreamAlias("DeliveryInfo")
  private MBSDeliveryInfo DeliveryInfo;

  public MBSDeliveryInfo getDeliveryInfo()
  {
    return this.DeliveryInfo;
  }

  public void setDeliveryInfo(MBSDeliveryInfo deliveryInfo) {
    this.DeliveryInfo = deliveryInfo;
  }
}