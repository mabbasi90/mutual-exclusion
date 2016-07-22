package ir.aos.raymond.resource;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Mohammad on 7/11/2016.
 */
public class RaymondToken implements Serializable {
    private Integer id;
    private AtomicInteger receiveCount = new AtomicInteger();

    public RaymondToken(Integer id) {
        this.id = id;
    }

    public synchronized Integer getId() {
        return id;
    }

    public void incrementReceive() {
        receiveCount.incrementAndGet();
    }

    public void resetReceiveCount() {
        receiveCount.set(0);
    }

    public int getReceiveCount() {
        return receiveCount.get();
    }

    @Override
    public String toString() {
        return "Token " + id.toString();
    }

}
