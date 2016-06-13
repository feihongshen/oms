package cn.explink.util;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimestampDeserializer extends JsonDeserializer<Timestamp> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public Timestamp deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String credate = jp.getText();
		Timestamp timestamp = null;
		try {
			Date date = df.parse(credate);
			timestamp = new Timestamp(date.getTime());
		} catch (ParseException e) {
			logger.info(credate,e);
		}
		
		return timestamp;
	}

}
