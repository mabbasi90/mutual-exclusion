package ir.aos.singhal.resource;

import ir.aos.singhal.node.State;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SinghalToken implements Serializable {

    private List<State> tsv = new ArrayList<>();
    private List<Integer> tsn = new ArrayList<>();
    private Integer id;
    private AtomicInteger receiveCount = new AtomicInteger();

    public SinghalToken(int num, int id) {
        this.id = id;
        for (int i = 0; i < num; i++) {
            tsv.add(State.N);
            tsn.add(0);
        }
    }


    public synchronized void setState(int id, State state) {
        tsv.set(id, state);
    }

    public synchronized void setReqNum(int requesterId, int reqNum) {
        tsn.set(requesterId, reqNum);
    }

    public synchronized int getReqNum(int id) {
        return tsn.get(id);
    }

    public synchronized State getState(int id) {
        return tsv.get(id);
    }

    public Integer getId() {
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
        return id.toString();
    }
}
