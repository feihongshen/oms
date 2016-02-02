package cn.explink.b2c.tpsdo;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

public class TimestampJsonValueProcessor implements JsonValueProcessor {
	private static final String DATE_FORMAT="yyyy-MM-dd HH:mm:ss";
	
	@Override
	public Object processArrayValue(Object arg0, JsonConfig arg1) {
		if(true){
			throw new UnsupportedOperationException();
		}
		return null;
	}

	@Override
	public Object processObjectValue(String arg0, Object value, JsonConfig arg2) {
		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT); 
		if(value instanceof Timestamp)
            return dateFormat.format((Timestamp) value);
		else
            return dateFormat.format((Date) value); 
		
	} 

}
