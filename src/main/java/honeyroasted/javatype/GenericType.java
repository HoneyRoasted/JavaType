package honeyroasted.javatype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GenericType extends JavaType {
    private Class<?> type;
    private List<JavaType> generics;

    private GenericType(Class<?> type, List<JavaType> generics) {
        this.type = type;
        this.generics = generics;
    }

    public static Builder builder(Class<?> cls) {
        return new Builder(cls);
    }

    public static GenericType of(Class<?> cls) {
        return builder(cls).build();
    }

    public List<JavaType> getGenerics() {
        return generics;
    }

    public JavaType getGeneric(int i) {
        if (i < 0) {
            throw new IndexOutOfBoundsException(String.valueOf(i));
        } else if (i >= generics.size()) {
            return JavaTypes.OBJECT;
        } else {
            return this.generics.get(i);
        }
    }

    public JavaType resolveVariables(GenericType filledType, GenericType paramedType, MethodType filledMethod, MethodType paramedMethod) {
        GenericType.Builder builder = GenericType.builder(this.type);
        for (JavaType generic : this.getGenerics()) {
            if (generic instanceof VariableType) {
                String name = generic.getName();
                builder.generic(filledMethod.resolveVar(name, paramedMethod).orElse(
                        filledType.resolveVar(name, paramedType).orElse(generic.resolveVariables(filledType, paramedType, filledMethod, paramedMethod))));
            } else {
                builder.generic(generic.resolveVariables(filledType, paramedType, filledMethod, paramedMethod));
            }
        }

        return builder.build();
    }

    @Override
    public JavaType resolveVariables(GenericType filledType, GenericType paramedType) {
        GenericType.Builder builder = GenericType.builder(this.type);
        for (JavaType generic : this.getGenerics()) {
            if (generic instanceof VariableType) {
                String name = generic.getName();
                builder.generic(filledType.resolveVar(name, paramedType).orElse(generic.resolveVariables(filledType, paramedType)));
            } else {
                builder.generic(generic.resolveVariables(filledType, paramedType));
            }
        }

        return builder.build();
    }

    public Optional<JavaType> resolveVar(String var, GenericType parameterized) {
        int index = -1;
        for (int i = 0; i < parameterized.genericCount(); i++) {
            JavaType type = parameterized.getGeneric(i);
            if (type instanceof VariableType && type.getName().equals(var)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            return Optional.of(this.getGeneric(index));
        }

        return Optional.empty();
    }

    @Override
    public boolean isAssignableTo(JavaType other) {
        if (other instanceof GenericType) {
            GenericType g = (GenericType) other;
            if (!g.type.isAssignableFrom(this.type)) {
                return false;
            }

            int size = Math.max(g.genericCount(), this.genericCount());
            for (int i = 0; i < size; i++) {
                if (!this.getGeneric(i).isAssignableTo(g.getGeneric(i), 0)) {
                    return false;
                }
            }
            return true;
        } else if (other instanceof VariableType) {
            VariableType v = (VariableType) other;
            return !v.getLower().isEmpty() && v.getLower().stream().anyMatch(this::isAssignableTo);
        }
        return false;
    }

    @Override
    public boolean isAssignableTo(JavaType other, int depth) {
        if (other instanceof GenericType) {
            GenericType g = (GenericType) other;
            if (!this.type.equals(g.type)) {
                return false;
            }

            if (g.genericCount() != this.genericCount()) {
                return false;
            }

            for (int i = 0; i < g.genericCount(); i++) {
                if (!this.getGeneric(i).isAssignableTo(g.getGeneric(i), depth + 1)) {
                    return false;
                }
            }

            return true;
        } else if (other instanceof VariableType && depth == 0) {
            VariableType v = (VariableType) other;
            return v.getUpper().stream().allMatch(t -> this.isAssignableTo(t, depth)) &&
                    (v.getLower().stream().anyMatch(t -> t.isAssignableTo(this, depth)) || v.getLower().isEmpty());
        }
        return false;
    }

    @Override
    public String getName() {
        return this.type.getName();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.type.getName());

        if (!this.generics.isEmpty()) {
            str.append("<");
            for (int i = 0; i < this.generics.size(); i++) {
                str.append(this.getGeneric(i));
                if (i != this.generics.size() - 1) {
                    str.append(", ");
                }
            }
            str.append(">");
        }

        return str.toString();
    }

    @Override
    public Class<?> getType() {
        return this.type;
    }

    @Override
    public JavaType array(int dimensions) {
        return dimensions == 0 ? this : ArrayType.of(this, dimensions);
    }

    @Override
    public boolean isGeneric() {
        return true;
    }

    @Override
    public JavaType box() {
        return GenericType.builder(JavaTypes.box(this.type)).generics(this.generics).build();
    }

    @Override
    public JavaType unbox() {
        return GenericType.builder(JavaTypes.unbox(this.type)).generics(this.generics).build();
    }

    public Optional<GenericType> resolveToSupertype(Class<?> parent) {
        return JavaTypes.resolveGenericsToSupertype(this, parent);
    }

    public Optional<GenericType> resolveToSubtype(Class<?> sub) {
        return JavaTypes.resolveGenericsToSubtype(sub, this);
    }

    public int genericCount() {
        return this.generics.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenericType)) return false;
        GenericType that = (GenericType) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(generics, that.generics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, generics);
    }

    public static class Builder {
        private Class<?> type;
        private List<JavaType> generics;

        public Builder(Class<?> type) {
            if (type.isArray()) {
                throw new IllegalArgumentException("Array type not allowed");
            }

            this.type = type;
            this.generics = new ArrayList<>();
        }

        public Builder generic(JavaType type) {
            this.generics.add(type);
            return this;
        }

        public Builder generics(JavaType... types) {
            Collections.addAll(this.generics, types);
            return this;
        }

        public Builder generics(Collection<JavaType> types) {
            this.generics.addAll(types);
            return this;
        }

        public Builder clear() {
            this.generics.clear();
            return this;
        }

        public GenericType build() {
            return new GenericType(type, Collections.unmodifiableList(new ArrayList<>(this.generics)));
        }

    }

}
