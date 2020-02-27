package throttling;


import javafx.util.Pair;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/*
    "THE" Rate limiting module

    How does it work ?
    It contains a map indexed by the client ID and a pair containing the end time from the first request received in
    this hours along with the no. of requests seen so far.

    Before processing a request from the client , the server asks if it should throttle this request or not and the
    following logic kicks in

    Have I seen this clientID before in this throttler ?
        N -> Create a new entry in the throttling map and increment the count to one
          -> Let the request process

        Y -> Is the no. of requests greater than the throttling limit?

            N-> Increment the count and let the request process
            Y-> Is the current under the 60 mins window ? (endTime - current > 0 )
                Y-> Block this request and dont let it process
                N-> Update the new end time, set the counter to 1
                 -> Let the request process

 */

public class Throttler {

	final Map<String, Pair<LocalDateTime, AtomicInteger>> throttlingMap;
	final int throttleLimit, minutesToThrottle;

	public Throttler(int throttleLimit, int minutesToThrottle) {
		this.throttlingMap = new ConcurrentHashMap<>();
		this.throttleLimit = throttleLimit;
		this.minutesToThrottle = minutesToThrottle;
	}


	public boolean shouldThrottle(String clientID, LocalDateTime current) {

		if (throttlingMap.containsKey(clientID)) {
			final Pair<LocalDateTime, AtomicInteger> clientEndTimeAndCount = throttlingMap.get(clientID);
			final LocalDateTime endTime = clientEndTimeAndCount.getKey();
			final AtomicInteger count = clientEndTimeAndCount.getValue();

			if (count.get() >= throttleLimit) {
				if (Duration.between(current, endTime).toMinutesPart() >= 0) {
					return true;
				} else {
					throttlingMap.put(clientID, new Pair<>(current.plusMinutes(minutesToThrottle),
							new AtomicInteger(1)));
				}
			} else {
				count.incrementAndGet();
				throttlingMap.put(clientID, new Pair<>(endTime, count));
			}

		} else {
			throttlingMap.put(clientID, new Pair<>(current.plusMinutes(minutesToThrottle), new AtomicInteger(1)));
		}
		return false;
	}

	public LocalDateTime getClientEndTime(String clientID) {
		return throttlingMap.containsKey(clientID) ? throttlingMap.get(clientID).getKey() : null;
	}

	public int getClientCount(String clientID) {
		return throttlingMap.containsKey(clientID) ? throttlingMap.get(clientID).getValue().get() : 0;
	}

	public void clearClientThrottle(String clientID) {
		if (throttlingMap.containsKey(clientID)) {
			throttlingMap.remove(clientID);
		}
	}
}
