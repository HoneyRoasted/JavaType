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

    @Override
    public String getName() {
        return this.type.getName();
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

    @Override
    public boolean isStrictlyAssignableTo(JavaType type) {
        if (type instanceof GenericType) {
            if (!type.getType().isAssignableFrom(this.type)) {
                return false;
            }

            List<JavaType> other = ((GenericType) type).generics;

            if (other.size() != this.generics.size()) {
                return false;
            }

            for (int i = 0; i < other.size(); i++) {
                if (!this.getGeneric(i).isStrictlyAssignableFrom(other.get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return type.isStrictlyAssignableFrom(this);
        }
    }

    @Override
    public boolean isStrictlyAssignableFrom(JavaType type) {
        if (type instanceof GenericType) {
            if (!this.type.isAssignableFrom(type.getType())) {
                return false;
            }

            List<JavaType> other = ((GenericType) type).generics;

            if (other.size() != this.generics.size()) {
                return false;
            }

            for (int i = 0; i < other.size(); i++) {
                if (!this.getGeneric(i).isStrictlyAssignableTo(other.get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return type.isStrictlyAssignableTo(this);
        }
    }

    @Override
    public boolean isAssignableTo(JavaType type) {
        if (type instanceof GenericType) {
            if (!type.getType().isAssignableFrom(this.type)) {
                return false;
            }

            List<JavaType> other = ((GenericType) type).generics;
            int limit = Math.min(other.size(), this.generics.size());
            for (int i = 0; i < limit; i++) {
                if (!this.getGeneric(i).isAssignableFrom(other.get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return type.isAssignableFrom(this);
        }
    }

    @Override
    public boolean isAssignableFrom(JavaType type) {
        if (type instanceof GenericType) {
            if (!this.type.isAssignableFrom(type.getType())) {
                return false;
            }

            List<JavaType> other = ((GenericType) type).generics;

            if (other.size() != this.generics.size()) {
                return false;
            }

            for (int i = 0; i < other.size(); i++) {
                if (!this.getGeneric(i).isAssignableTo(other.get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return type.isAssignableTo(this);
        }
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
