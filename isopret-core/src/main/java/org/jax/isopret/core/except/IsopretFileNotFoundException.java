package org.jax.isopret.core.except;

public class IsopretFileNotFoundException extends IsopretException{
    public IsopretFileNotFoundException() { super();}
    public IsopretFileNotFoundException(String msg) { super(msg);}
    public IsopretFileNotFoundException(String file, String path) {
        super(String.format("Could not find \"%s\" at\"%s\"", file, path));
    }
}
