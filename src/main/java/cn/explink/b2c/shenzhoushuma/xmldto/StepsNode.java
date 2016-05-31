package cn.explink.b2c.shenzhoushuma.xmldto;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Steps")
public class StepsNode {

	private List<StepNode> stepList;
	
	@XmlElement(name = "Step")
	public List<StepNode> getStepList() {
		return stepList;
	}

	public void setStepList(List<StepNode> stepList) {
		this.stepList = stepList;
	}
	
	
}
