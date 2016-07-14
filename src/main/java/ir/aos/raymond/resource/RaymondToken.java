package ir.aos.raymond.resource;

import java.io.Serializable;

/**
 * Created by Mohammad on 7/11/2016.
 */
public class RaymondToken implements Serializable {
    private Integer id;

    public RaymondToken(Integer id) {
        this.id = id;
    }

    public synchronized Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Token " + id.toString();
    }
}
