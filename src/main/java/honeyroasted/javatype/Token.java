package honeyroasted.javatype;

public abstract class Token<T> {

    public JavaType resolve() {
        return JavaTypes.of(this);
    }

}
