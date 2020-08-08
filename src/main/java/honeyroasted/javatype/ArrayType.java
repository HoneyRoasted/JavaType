package honeyroasted.javatype;

import java.util.Objects;

public class ArrayType extends JavaType {
    private Class<?> effectiveType;

    private JavaType type;
    private int dimensions;

    private ArrayType(JavaType type, int dimensions) {
        if (dimensions <= 0) {
            throw new IllegalArgumentException("Array dimension <= 0");
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
    public boolean isStrictlyAssignableTo(JavaType type) {
        JavaType component;
        int dimensions;
        if (type instanceof ArrayType) {
            component = ((ArrayType) type).type;
            dimensions = ((ArrayType) type).dimensions;
        } else {
            component = type;
            dimensions = 0;
        }

        if (dimensions == this.dimensions && this.type.isStrictlyAssignableTo(component)) {
            return true;
        } else if (dimensions < this.dimensions && component.equals(JavaTypes.OBJECT)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isStrictlyAssignableFrom(JavaType type) {
        JavaType component;
        int dimensions;
        if (type instanceof ArrayType) {
            component = ((ArrayType) type).type;
            dimensions = ((ArrayType) type).dimensions;
        } else {
            component = type;
            dimensions = 0;
        }

        if (dimensions == this.dimensions && this.type.isStrictlyAssignableFrom(component)) {
            return true;
        } else if (dimensions > this.dimensions && this.type.equals(JavaTypes.OBJECT)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isAssignableTo(JavaType type) {
        JavaType component;
        int dimensions;
        if (type instanceof ArrayType) {
            component = ((ArrayType) type).type;
            dimensions = ((ArrayType) type).dimensions;
        } else {
            component = type;
            dimensions = 0;
        }

        if (dimensions == this.dimensions && this.type.isAssignableTo(component)) {
            return true;
        } else if (dimensions < this.dimensions && component.equals(JavaTypes.OBJECT)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isAssignableFrom(JavaType type) {
        JavaType component;
        int dimensions;
        if (type instanceof ArrayType) {
            component = ((ArrayType) type).type;
            dimensions = ((ArrayType) type).dimensions;
        } else {
            component = type;
            dimensions = 0;
        }

        if (dimensions == this.dimensions && this.type.isAssignableFrom(component)) {
            return true;
        } else if (dimensions > this.dimensions && this.type.equals(JavaTypes.OBJECT)) {
            return true;
        }

        return false;
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
