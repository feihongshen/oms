package utiltest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


@ContextConfiguration(locations = {
		"/META-INF/spring/property-placeholder.xml",
		"/META-INF/spring/applicationContext.xml",
		"/META-INF/spring/applicationContext-job.xml",
		"/META-INF/spring/applicationContext-security.xml",
		"/META-INF/spring/applicationContext-jms.xml",
		"/META-INF/spring/applicationContext-cache.xml",
		"../applicationContext_testAdd.xml"})
public class BaseTest extends AbstractJUnit4SpringContextTests{
	
}
