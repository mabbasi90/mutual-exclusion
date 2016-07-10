package ir.aos.singhal.resource;

import ir.aos.singhal.node.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SinghalResource {
    private List<State> sv = new ArrayList<>();
    private List<Integer> sn = new ArrayList<>();
    private int id;
    private SinghalToken singhalToken;

    public SinghalResource(int resourceId, int processorId, int processorsNum) {
        for (int i = 0; i < processorsNum; i++) {
            sn.add(0);
        }
        this.id = resourceId;
        if (processorId == 0) {
            sv.add(State.H);
            singhalToken = new SinghalToken(processorsNum, resourceId);
        } else {
            for (int i = 0; i < processorId; i++) {
                sv.add(State.R);
            }
        }
        for (int i = processorId; i < processorsNum; i++) {
            sv.add(State.N);
        }
    }

    public synchronized void setState(int id, State r) {
        sv.set(id, r);
    }

    public synchronized void incReqNum(int id) {
        sn.set(id, sn.get(id) + 1);
    }

    public synchronized List<Integer> getProcessorsWithState(State state) {
        return Arrays.stream(IntStream.range(0, sv.size()).filter(i -> state.equals(sv.get(i))).toArray()).boxed().collect(Collectors.toList());
    }

    public synchronized int getReqNum(int id) {
        return sn.get(id);
    }

    public synchronized void setReqNum(int id, int reqNum) {
        sn.set(id, reqNum);
    }

    public synchronized State getState(int id) {
        return sv.get(id);
    }

    public synchronized SinghalToken getSinghalToken() {
        return singhalToken;
    }

    public synchronized void setSinghalToken(SinghalToken singhalToken) {
        this.singhalToken = singhalToken;
    }

    public synchronized void removeToken() {
        this.singhalToken = null;
    }

    @Override
    public String toString() {
        return String.valueOf(this.id);
    }

}
