package cn.explink.b2c.tools;

import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Header;
import org.apache.camel.Headers;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import cn.explink.dao.MqExceptionDAO;
import cn.explink.domain.MqExceptionBuilder;
import cn.explink.domain.MqExceptionBuilder.MessageSourceEnum;

@Component
public class FromBaseService implements ApplicationListener<ContextRefreshedEvent> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	List<CacheBaseListener> cacheBaseListeners;

	@Autowired
	private CamelContext camelContext;
	
	@Autowired
	private MqExceptionDAO mqExceptionDAO;
	
	private static final String MQ_FROM_URI_USER = "jms:queue:VirtualTopicConsumers.cachebase_user.courierUpdate";
	
	private static final String MQ_FROM_URI_BRANCH = "jms:queue:VirtualTopicConsumers.cachebase_branch.savezhandian";
	
	private static final String MQ_FROM_URI_CUSTOMER = "jms:queue:VirtualTopicConsumers.cachebase_customer.courierUpdate";

	public void init() {
		logger.info("init base info camel routes");
		try {
			logger.info("enable base info install change listen routes");
			camelContext.addRoutes(new RouteBuilder() {
				@Override
				public void configure() throws Exception {
					from(MQ_FROM_URI_USER + "?concurrentConsumers=1").to("bean:fromBaseService?method=notifyChange(user)").routeId("cachebase_user");
					from(MQ_FROM_URI_BRANCH + "?concurrentConsumers=1").to("bean:fromBaseService?method=notifyChange(branch)").routeId("cachebase_branch");
					from(MQ_FROM_URI_CUSTOMER + "?concurrentConsumers=1").to("bean:fromBaseService?method=notifyChange(customer)").routeId("cachebase_customer");
				}
			});
		} catch (Exception e) {
			logger.error("init base change listen routs start fail", e);
		}

	}

	public void notifyChange(String type, @Headers() Map<String, String> parameters, @Header("MessageHeaderUUID") String messageHeaderUUID) {
		try{
			for (CacheBaseListener cacheBaseListener : cacheBaseListeners) {
				cacheBaseListener.onChange(parameters);
			}
		}catch(Exception e){
			// 把未完成MQ插入到数据库中, start
			String topic = null;
			if(type.equals("user")) {
				topic = MQ_FROM_URI_USER;
			} else if(type.equals("branch")) {
				topic = MQ_FROM_URI_BRANCH;
			} else if(type.equals("customer")) {
				topic = MQ_FROM_URI_CUSTOMER;
			}
			Map<String, String> headers = parameters;
			
			//消费MQ异常表
			this.mqExceptionDAO.save(MqExceptionBuilder.getInstance().buildExceptionCode(this.getClass().getSimpleName() + ".notifyChange")
					.buildExceptionInfo(e.toString()).buildTopic(topic)
					.buildMessageHeader(headers)
					.buildMessageHeaderUUID(messageHeaderUUID).buildMessageSource(MessageSourceEnum.receiver.getIndex()).getMqException());
			// 把未完成MQ插入到数据库中, end
		}
	}

	// public void notifyChange1(@Headers()Map<String, String> parameters){
	// for(CacheBaseListener cacheBaseListener:cacheBaseListeners){
	// cacheBaseListener.onChange(parameters);
	// }
	// }
	// public void notifyChange2(@Headers()Map<String, String> parameters){
	// for(CacheBaseListener cacheBaseListener:cacheBaseListeners){
	// cacheBaseListener.onChange(parameters);
	// }
	// }

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		init();
	}
}
