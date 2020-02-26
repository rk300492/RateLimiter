package throttling;


import javafx.util.Pair;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// "THE" Rate limiting module
// Todo : Give a detailed explanation of what this class does.
public class Throttler {

    final Map<String, Pair<LocalDateTime, AtomicInteger>> clientToClientEndTimeAndCount;

    public Throttler() {
        clientToClientEndTimeAndCount = new ConcurrentHashMap<>();
    }


    public boolean shouldThrottle(String clientID, LocalDateTime current) {

        if (clientToClientEndTimeAndCount.containsKey(clientID)) {
            final Pair<LocalDateTime, AtomicInteger> clientEndTimeAndCount = clientToClientEndTimeAndCount.get(clientID);
            final LocalDateTime endTime = clientEndTimeAndCount.getKey();
            final AtomicInteger count = clientEndTimeAndCount.getValue();

            if (count.get() >= 10) {
                if (Duration.between(current, endTime).toMinutesPart() <= 0) {
                    return true;
                }else{
                    clientToClientEndTimeAndCount.put(clientID, new Pair<>(current.plusMinutes(2), new AtomicInteger(1)));
                }
            }else{
                count.incrementAndGet();
                clientToClientEndTimeAndCount.put(clientID, new Pair<>(endTime, count));
            }

        } else {
            clientToClientEndTimeAndCount.put(clientID, new Pair<>(current, new AtomicInteger(1)));
        }
        return false;
    }

    public LocalDateTime getClientEndTime (String clientID){
        return clientToClientEndTimeAndCount.containsKey(clientID) ?  clientToClientEndTimeAndCount.get(clientID).getKey() : null;
    }

    public int getClientCount (String clientID){
        return clientToClientEndTimeAndCount.containsKey(clientID) ?  clientToClientEndTimeAndCount.get(clientID).getValue().get() : 0;
    }
}
