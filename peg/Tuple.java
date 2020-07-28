package peg;

public class Tuple<First, Second> {
    First first;
    Second second;

    public Tuple(First first, Second second) {
        this.first = first;
        this.second = second;
    }
}