package peg;

public abstract class ASTree {

    public abstract OpList eval(ParsingEnv parsingEnv) throws RuntimeException;

    public abstract String toString();

}