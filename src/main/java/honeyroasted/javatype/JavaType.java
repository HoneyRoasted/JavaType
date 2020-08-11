package honeyroasted.javatype;

import java.util.Optional;

public abstract class JavaType {

    public abstract JavaType resolveVariables(GenericType filledType, GenericType paramedType, MethodType filledMethod, MethodType paramedMethod);

    public abstract JavaType resolveVariables(GenericType filledType, GenericType paramedType);

    public abstract boolean isAssignableTo(JavaType other);

    public abstract boolean isAssignableTo(JavaType other, int depth);

    public abstract String getName();

    public abstract Class<?> getType();

    public abstract JavaType array(int dimensions);

    public boolean isPrimitive() {
        return getType().isPrimitive();
    }

    public boolean isNumericPrimitive() {
        return getType().isPrimitive() && !getType().equals(boolean.class) && !getType().equals(void.class);
    }

    public JavaType box() {
        return this;
    }

    public JavaType unbox() {
        return this;
    }

    public Optional<? extends JavaType> resolveToSupertype(Class<?> parent) {
        return Optional.empty();
    }

    public Optional<? extends JavaType> resolveToSubtype(Class<?> sub) {
        return Optional.empty();
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
