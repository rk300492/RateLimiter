import client.Request;
import org.junit.Assert;
import org.junit.Test;
import server.Response;
import util.Constants.*;
import util.Utility;

import java.nio.ByteBuffer;

public class UtilTest {

	@Test
	public void testEncodeAndDecodeRequest(){
		final Request request = new Request("client1" , 1, RequestType.GET , 3);
		ByteBuffer byteBuffer = Utility.encodeRequest(request);
		Assert.assertEquals(Utility.decodeRequest(byteBuffer).toString(),request.toString());
	}


	@Test
	public void testEncodeAndDecodeResponse(){
		final Response response = new Response("client1" , 1, ResponseStatus.SUCCESS,
				ResponseType.POST , null);
		ByteBuffer byteBuffer = Utility.encodeResponse(response);
		Assert.assertEquals(Utility.decodeResponse(byteBuffer).toString(),response.toString());
	}
}
