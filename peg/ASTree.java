package peg;

public abstract class ASTree {
    protected Modifire modifire;

    public ASTree(Modifire modifire) {
        this.modifire = modifire;
    }

    public abstract void printTree();

    public abstract void eval(VMCodeGeneratorResult result);
}