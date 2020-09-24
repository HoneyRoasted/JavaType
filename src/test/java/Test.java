import honeyroasted.javatype.GenericType;
import honeyroasted.javatype.JavaType;
import honeyroasted.javatype.JavaTypes;
import honeyroasted.javatype.VariableType;

import java.util.List;

public class Test {

    public static void main(String[] args) {
        JavaType a = GenericType.builder(List.class).generic(VariableType.builder("?").build()).build();
        JavaType b = GenericType.builder(List.class).generic(JavaTypes.of(String.class)).build();

        System.out.println(b.isAssignableTo(a));
    }

}
