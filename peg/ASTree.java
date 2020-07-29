package peg;

import java.util.HashMap;

public abstract class ASTree {
    protected static HashMap<String, Integer> NTaddressMap;

    public abstract String eval();
}