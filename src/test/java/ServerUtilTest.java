import org.junit.Assert;
import org.junit.Test;
import server.ServerUtil;
import util.Constants;

public class ServerUtilTest {

	@Test
	public void testHttpPing(){
		Assert.assertEquals(200 , ServerUtil.getStatus(Constants.URL));
		Assert.assertEquals(500 , ServerUtil.getStatus("ww.airt.co"));
	}
}
