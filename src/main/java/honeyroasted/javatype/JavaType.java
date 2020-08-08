package honeyroasted.javatype;

public abstract class JavaType {

    public abstract String getName();

    public abstract Class<?> getType();

    public abstract JavaType array(int dimensions);

    public abstract boolean isStrictlyAssignableTo(JavaType type);

    public abstract boolean isStrictlyAssignableFrom(JavaType type);

    public abstract boolean isAssignableTo(JavaType type);

    public abstract boolean isAssignableFrom(JavaType type);

    public boolean isPrimitive() {
        return getType().isPrimitive();
    }

    public JavaType box() {
        return this;
    }

    public JavaType unbox() {
        return this;
    }

    public boolean isArray() {
        return false;
    }

    public boolean isVariable() {
        return false;
    }

    public boolean isGeneric() {
        return false;
    }

}
