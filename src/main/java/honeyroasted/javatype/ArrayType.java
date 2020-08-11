package honeyroasted.javatype;

import java.util.Objects;
import java.util.Optional;

public class ArrayType extends JavaType {
    private Class<?> effectiveType;

    private JavaType type;
    private int dimensions;

    private ArrayType(JavaType type, int dimensions) {
        if (dimensions <= 0) {
            throw new IllegalArgumentException("Array dimension <= 0");
        } else if (type.isArray()) {
            throw new IllegalArgumentException("Array type within array");
        }

        this.type = type;
        this.dimensions = dimensions;
        this.effectiveType = JavaTypes.getArrayType(type.getType(), dimensions);
    }

    public static ArrayType of(JavaType type, int dimensions) {
        if(type instanceof ArrayType) {
            throw new IllegalArgumentException("Array type " + type.getName());
        }

        return new ArrayType(type, dimensions);
    }

    public int getDimensions() {
        return dimensions;
    }

    public JavaType getAbsoluteComponent() {
        return this.type;
    }

    public JavaType getComponent() {
        return this.dimensions == 1 ? this.type : this.type.array(this.dimensions - 1);
    }

    @Override
    public Optional<? extends JavaType> resolveToSupertype(Class<?> parent) {
        if (this.type.isGeneric()) {
            return JavaTypes.resolveGenericsToSupertype((GenericType) this.type, parent).map(g -> g.array(this.dimensions));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<? extends JavaType> resolveToSubtype(Class<?> sub) {
        if (this.type.isGeneric()) {
            return JavaTypes.resolveGenericsToSubtype(sub, (GenericType) this.type).map(g -> g.array(this.dimensions));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public JavaType resolveVariables(GenericType filledType, GenericType paramedType, MethodType filledMethod, MethodType paramedMethod) {
        return this.type.resolveVariables(filledType, paramedType, filledMethod, paramedMethod).array(this.dimensions);
    }

    @Override
    public JavaType resolveVariables(GenericType filledType, GenericType paramedType) {
        return this.type.resolveVariables(filledType, paramedType).array(this.dimensions);
    }

    @Override
    public boolean isAssignableTo(JavaType other) {
        return other instanceof ArrayType && this.dimensions == ((ArrayType) other).dimensions && this.type.isAssignableTo(((ArrayType) other).type);
    }

    @Override
    public boolean isAssignableTo(JavaType other, int depth) {
        return other instanceof ArrayType && this.dimensions == ((ArrayType) other).dimensions && this.type.isAssignableTo(((ArrayType) other).type, depth);
    }

    @Override
    public String getName() {
        StringBuilder name = new StringBuilder();
        name.append(this.type.getName());
        for (int i = 0; i < this.dimensions; i++) {
            name.append("[]");
        }
        return name.toString();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.type);
        for (int i = 0; i < this.dimensions; i++) {
            str.append("[]");
        }
        return str.toString();
    }

    @Override
    public Class<?> getType() {
        return this.effectiveType;
    }

    @Override
    public JavaType array(int dimensions) {
        return this.dimensions + dimensions == 0 ? this.type : ArrayType.of(this.type, this.dimensions + dimensions);
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArrayType)) return false;
        ArrayType arrayType = (ArrayType) o;
        return dimensions == arrayType.dimensions &&
                Objects.equals(type, arrayType.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, dimensions);
    }
}
