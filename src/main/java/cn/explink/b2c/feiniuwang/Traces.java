package cn.explink.b2c.feiniuwang;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true )
public class Traces {
	@JsonProperty(value = "time")
	private String time;
	@JsonProperty(value = "desc")
	private String desc;
	@JsonProperty(value = "city")
	private String city;
	@JsonProperty(value = "facilityname")
	private String facilityname;
	@JsonProperty(value = "facilityno")
	private String facilityno;
	@JsonProperty(value = "facilitytype")
	private String facilitytype;
	@JsonProperty(value = "contacter")
	private String contacter;
	@JsonProperty(value = "contactphone")
	private String contactphone;
	@JsonProperty(value = "questiontype")
	private String questiontype;
	@JsonProperty(value = "questionreason")
	private String questionreason;
	@JsonProperty(value = "receivejober")
	private String receivejober;
	@JsonProperty(value = "allotman")
	private String allotman;
	@JsonProperty(value = "remark")
	private String remark;
	@JsonProperty(value = "weight")
	private String weight;
	@JsonProperty(value = "signman")
	private String signman;
	@JsonProperty(value = "action")
	private String action;
	@JsonProperty(value = "actiondesc")
	private String actiondesc;
	@JsonProperty(value = "nextSite")
	private String nextSite;
	
	public String getNextSite() {
		return nextSite;
	}
	public void setNextSite(String nextSite) {
		this.nextSite = nextSite;
	}
	public String getQuestionreason() {
		return questionreason;
	}
	public void setQuestionreason(String questionreason) {
		this.questionreason = questionreason;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getFacilityname() {
		return facilityname;
	}
	public void setFacilityname(String facilityname) {
		this.facilityname = facilityname;
	}
	public String getFacilityno() {
		return facilityno;
	}
	public void setFacilityno(String facilityno) {
		this.facilityno = facilityno;
	}
	public String getFacilitytype() {
		return facilitytype;
	}
	public void setFacilitytype(String facilitytype) {
		this.facilitytype = facilitytype;
	}
	public String getContacter() {
		return contacter;
	}
	public void setContacter(String contacter) {
		this.contacter = contacter;
	}
	public String getContactphone() {
		return contactphone;
	}
	public void setContactphone(String contactphone) {
		this.contactphone = contactphone;
	}
	public String getQuestiontype() {
		return questiontype;
	}
	public void setQuestiontype(String questiontype) {
		this.questiontype = questiontype;
	}
	public String getReceivejober() {
		return receivejober;
	}
	public void setReceivejober(String receivejober) {
		this.receivejober = receivejober;
	}
	public String getAllotman() {
		return allotman;
	}
	public void setAllotman(String allotman) {
		this.allotman = allotman;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public String getSignman() {
		return signman;
	}
	public void setSignman(String signman) {
		this.signman = signman;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getActiondesc() {
		return actiondesc;
	}
	public void setActiondesc(String actiondesc) {
		this.actiondesc = actiondesc;
	}
	
}
