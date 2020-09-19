// package vm;

// public class MemoTable {

// MemoEntry[][] memo;

// public MemoTable(int rulenum, int inputlength) {
// memo = new MemoEntry[rulenum][inputlength];
// }

// public MemoEntry get(int ruleid, int ip) {
// MemoEntry entry = memo[ruleid][ip];
// return entry;
// }

// public void set(int ruleid, int ip, int memoip, boolean success) {
// // System.out.println("set: (" + ruleid + ":" + ip + ") = (" + memoip + "," +
// // success + ")");
// memo[ruleid][ip] = new MemoEntry(memoip, success);
// }
// }

// class MemoEntry {
// boolean success;
// int ip;

// MemoEntry(int ip, boolean success) {
// this.success = success;
// this.ip = ip;
// }
// }

package vm;

import java.util.HashMap;

public class MemoTable {

    HashMap<Integer, HashMap<Integer, MemoEntry>> memo = new HashMap<Integer, HashMap<Integer, MemoEntry>>();

    public MemoTable(int rulenum) {
        for (int i = 0; i < rulenum; i++)
            memo.put(i, new HashMap<Integer, MemoEntry>());
    }

    public MemoEntry get(int ruleid, int ip) {
        return memo.get(ruleid).get(ip);
    }

    public void set(int ruleid, int ip, MemoEntry entry) {
        memo.get(ruleid).put(ip, entry);
    }
}

class MemoEntry {
    boolean success;
    int ip;

    MemoEntry(int ip, boolean success) {
        this.success = success;
        this.ip = ip;
    }
}
