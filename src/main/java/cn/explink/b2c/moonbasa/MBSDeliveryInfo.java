 package cn.explink.b2c.moonbasa;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("DeliveryInfo")
public class MBSDeliveryInfo
{

  @XStreamAlias("DeliveryCode")
  private String DeliveryCode;

  @XStreamAlias("Status")
  private String Status;

  @XStreamAlias("Desc")
  private String Desc;

  @XStreamAlias("SignDate")
  private String SignDate;

  @XStreamAlias("Subscriber")
  private String Subscriber;

  @XStreamAlias("Carrier")
  private String Carrier;

  @XStreamAlias("Phone")
  private String Phone;

  public String getDeliveryCode()
  {
    return this.DeliveryCode;
  }
  public void setDeliveryCode(String deliveryCode) {
    this.DeliveryCode = deliveryCode;
  }
  public String getStatus() {
    return this.Status;
  }
  public void setStatus(String status) {
    this.Status = status;
  }
  public String getDesc() {
    return this.Desc;
  }
  public void setDesc(String desc) {
    this.Desc = desc;
  }
  public String getSignDate() {
    return this.SignDate;
  }
  public void setSignDate(String signDate) {
    this.SignDate = signDate;
  }
  public String getSubscriber() {
    return this.Subscriber;
  }
  public void setSubscriber(String subscriber) {
    this.Subscriber = subscriber;
  }
  public String getCarrier() {
    return this.Carrier;
  }
  public void setCarrier(String carrier) {
    this.Carrier = carrier;
  }
  public String getPhone() {
    return this.Phone;
  }
  public void setPhone(String phone) {
    this.Phone = phone;
  }
}