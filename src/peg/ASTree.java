package peg;

public abstract class ASTree {

    public abstract OpList eval(ParsingOption parsingOption) throws RuntimeException;

    public abstract String toString();

}