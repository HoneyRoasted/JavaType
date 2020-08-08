package honeyroasted.javatype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class VariableType extends JavaType {
    private String name;
    private List<JavaType> upper;
    private List<JavaType> lower;

    private Class<?> effectiveType;

    private VariableType(String name, List<JavaType> upper, List<JavaType> lower) {
        this.name = name;
        this.upper = upper;
        this.lower = lower;
        this.effectiveType = upper.isEmpty() ? Object.class : JavaTypes.getCommonParent(upper.stream().map(JavaType::getType).collect(Collectors.toList()));
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Class<?> getType() {
        return this.effectiveType;
    }

    @Override
    public JavaType array(int dimensions) {
        return dimensions == 0 ? this : ArrayType.of(this, dimensions);
    }

    @Override
    public boolean isStrictlyAssignableTo(JavaType type) {
        return !this.upper.isEmpty() && this.upper.stream().anyMatch(t -> t.isStrictlyAssignableTo(type));
    }

    @Override
    public boolean isStrictlyAssignableFrom(JavaType type) {
        return !this.lower.isEmpty() && this.lower.stream().allMatch(t -> t.isStrictlyAssignableFrom(type));
    }

    @Override
    public boolean isAssignableTo(JavaType type) {
        return !this.upper.isEmpty() && this.upper.stream().anyMatch(t -> t.isAssignableTo(type));
    }

    @Override
    public boolean isAssignableFrom(JavaType type) {
        return !this.lower.isEmpty() && this.lower.stream().allMatch(t -> t.isAssignableFrom(type));
    }

    @Override
    public boolean isVariable() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariableType)) return false;
        VariableType that = (VariableType) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(upper, that.upper) &&
                Objects.equals(lower, that.lower);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, upper, lower);
    }

    public static class Builder {
        private String name;
        private List<JavaType> upper = new ArrayList<>();
        private List<JavaType> lower = new ArrayList<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder upper(JavaType type) {
            upper.add(type);
            return this;
        }

        public Builder lower(JavaType type) {
            lower.add(type);
            return this;
        }

        public Builder uppers(JavaType... types) {
            Collections.addAll(upper, types);
            return this;
        }

        public Builder lowers(JavaType... types) {
            Collections.addAll(lower, types);
            return this;
        }

        public Builder uppers(Collection<JavaType> types) {
            this.upper.addAll(types);
            return this;
        }

        public Builder lowers(Collection<JavaType> types) {
            this.lower.addAll(types);
            return this;
        }

        public Builder clear() {
            this.lower.clear();
            this.upper.clear();
            return this;
        }

        public VariableType build() {
            return new VariableType(this.name, Collections.unmodifiableList(new ArrayList<>(this.upper)), Collections.unmodifiableList(new ArrayList<>(this.lower)));
        }
    }
}
