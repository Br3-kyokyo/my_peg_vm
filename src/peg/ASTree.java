package peg;

public interface ASTree {

    public abstract OpList eval(ParsingEnv parsingEnv) throws RuntimeException;

    public abstract String toString();

}