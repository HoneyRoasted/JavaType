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

    public List<JavaType> getLower() {
        return lower;
    }

    public List<JavaType> getUpper() {
        return upper;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Override
    public JavaType resolveVariables(GenericType filledType, GenericType paramedType, MethodType filledMethod, MethodType paramedMethod) {
        return filledType.resolveVar(this.name, paramedType).orElseGet(() -> {
            VariableType.Builder builder = VariableType.builder(this.name);

            for (JavaType upper : this.upper) {
                builder.upper(upper.resolveVariables(filledType, paramedType, filledMethod, paramedMethod));
            }

            for (JavaType lower : this.lower) {
                builder.lower(lower.resolveVariables(filledType, paramedType, filledMethod, paramedMethod));
            }

            return builder.build();
        });
    }

    @Override
    public JavaType resolveVariables(GenericType filledType, GenericType paramedType) {
        return filledType.resolveVar(this.name, paramedType).orElseGet(() -> {
            VariableType.Builder builder = VariableType.builder(this.name);

            for (JavaType upper : this.upper) {
                builder.upper(upper.resolveVariables(filledType, paramedType));
            }

            for (JavaType lower : this.lower) {
                builder.lower(lower.resolveVariables(filledType, paramedType));
            }

            return builder.build();
        });
    }

    @Override
    public boolean isAssignableTo(JavaType other) {
        return !this.upper.isEmpty() && this.upper.stream().anyMatch(t -> t.isAssignableTo(other));
    }

    @Override
    public boolean isAssignableTo(JavaType other, int depth) {
        if (other instanceof VariableType) {
            VariableType v = (VariableType) other;

            if (this.getUpper().isEmpty()) {
                return false;
            }

            return this.getUpper().stream().allMatch(t -> v.getUpper().stream().allMatch(t::isAssignableTo)) &&
                    this.getLower().stream().allMatch(t -> v.isAssignableTo(t, depth));
        }
        return false;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.name);

        if (!this.upper.isEmpty()) {
            str.append(" extends ");

            for (int i = 0; i < this.upper.size(); i++) {
                str.append(this.upper.get(i));
                if (i != this.upper.size() - 1) {
                    str.append(" & ");
                }
            }

            if (!this.lower.isEmpty()) {
                str.append(" |");
            }
        }

        if (!this.lower.isEmpty()) {
            str.append(" super ");

            for (int i = 0; i < this.lower.size(); i++) {
                str.append(this.lower.get(i));
                if (i != this.lower.size() - 1) {
                    str.append(" & ");
                }
            }
        }

        return str.toString();
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
            return new VariableType(this.name, Collections.unmodifiableList(this.upper.stream().filter(j -> !j.equals(JavaTypes.OBJECT)).collect(Collectors.toList())),
                    Collections.unmodifiableList(new ArrayList<>(this.lower)));
        }
    }
}
