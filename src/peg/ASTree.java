package peg;

public abstract class ASTree {

    public abstract OpList eval() throws RuntimeException;

    public abstract String toString();

}