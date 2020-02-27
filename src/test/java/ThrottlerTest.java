import org.junit.*;
import throttling.Throttler;

import java.time.LocalDateTime;

public class ThrottlerTest {

	final Throttler throttler = new Throttler(10,5);

	final static String client1 = "C1";
	final static String client2 = "C2";
	final static String client3 = "C3";

	@Test
	public void testShouldThrottle(){

		for (int i=0; i<9 ;i++){

			throttler.shouldThrottle(client1, LocalDateTime.now() );
			throttler.shouldThrottle(client2, LocalDateTime.now() );
			throttler.shouldThrottle(client3, LocalDateTime.now() );

		}
		Assert.assertFalse(throttler.shouldThrottle(client1, LocalDateTime.now()));
		Assert.assertTrue(throttler.shouldThrottle(client1, LocalDateTime.now()));
		Assert.assertFalse(throttler.shouldThrottle(client2, LocalDateTime.now()));

		Assert.assertFalse(throttler.shouldThrottle(client1, LocalDateTime.now().plusMinutes(10)));



	}

}
