package honeyroasted.javatype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MethodType {
    private JavaType ret;
    private List<JavaType> params;
    private List<JavaType> generics;

    private MethodType(JavaType ret, List<JavaType> params, List<JavaType> generics) {
        this.ret = ret;
        this.params = params;
        this.generics = generics;
    }

    public static Builder builder(JavaType ret) {
        return new Builder(ret);
    }

    public Optional<JavaType> resolveVar(String var, MethodType parameterized) {
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

    public JavaType getRet() {
        return ret;
    }

    public List<JavaType> getParams() {
        return params;
    }

    public List<JavaType> getGenerics() {
        return generics;
    }

    public int genericCount() {
        return this.generics.size();
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

    public boolean isAssignableTo(MethodType other) {
        if (!this.ret.isAssignableTo(other.ret)) {
            return false;
        }

        if (this.params.size() != other.params.size()) {
            return false;
        }

        for (int i = 0; i < this.params.size(); i++) {
            if (!this.params.get(i).isAssignableTo(other.params.get(i))) {
                return false;
            }
        }

        return true;
    }

    public String getName() {
        return this.ret.getName() + " (" +
                this.params.stream().map(JavaType::getName).reduce((a, b) -> a + ", " + b).orElse("") + ")";
    }

    public static class Builder {
        private JavaType ret;
        private List<JavaType> params = new ArrayList<>();
        private List<JavaType> generics = new ArrayList<>();

        public Builder(JavaType ret) {
            this.ret = ret;
        }

        public Builder param(JavaType type) {
            this.params.add(type);
            return this;
        }

        public Builder params(JavaType... types) {
            Collections.addAll(this.params, types);
            return this;
        }

        public Builder params(Collection<JavaType> types) {
            this.params.addAll(types);
            return this;
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
            this.params.clear();
            this.generics.clear();;
            return this;
        }

        public MethodType build() {
            return new MethodType(this.ret, Collections.unmodifiableList(new ArrayList<>(this.params)),
                    Collections.unmodifiableList(new ArrayList<>(this.generics)));
        }

    }

}
