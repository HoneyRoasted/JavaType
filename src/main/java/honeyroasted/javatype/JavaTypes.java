package honeyroasted.javatype;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JavaTypes {
    public static final JavaType OBJECT = of(Object.class);
    public static final JavaType VOID = of(void.class);

    private static Map<Class, Class> boxByPrimitives = new HashMap<>();
    private static Map<Class, Class> primitivesByBox = new HashMap<>();
    private static Map<String, Class> primitivesByName = new HashMap<>();

    static {
        boxByPrimitives.put(byte.class, Byte.class);
        boxByPrimitives.put(short.class, Short.class);
        boxByPrimitives.put(char.class, Character.class);
        boxByPrimitives.put(int.class, Integer.class);
        boxByPrimitives.put(long.class, Long.class);
        boxByPrimitives.put(float.class, Float.class);
        boxByPrimitives.put(double.class, Double.class);
        boxByPrimitives.put(boolean.class, Boolean.class);
        boxByPrimitives.put(void.class, Void.class);

        primitivesByBox.put(Byte.class, byte.class);
        primitivesByBox.put(Short.class, short.class);
        primitivesByBox.put(Character.class, char.class);
        primitivesByBox.put(Integer.class, int.class);
        primitivesByBox.put(Long.class, long.class);
        primitivesByBox.put(Float.class, float.class);
        primitivesByBox.put(Double.class, double.class);
        primitivesByBox.put(Boolean.class, boolean.class);
        primitivesByBox.put(Void.class, void.class);

        primitivesByName.put("byte", byte.class);
        primitivesByName.put("short", short.class);
        primitivesByName.put("char", char.class);
        primitivesByName.put("int", int.class);
        primitivesByName.put("long", long.class);
        primitivesByName.put("float", float.class);
        primitivesByName.put("double", double.class);
        primitivesByName.put("boolean", boolean.class);
        primitivesByName.put("void", void.class);
    }

    public static Class box(Class primitive) {
        return primitive.isPrimitive() ? boxByPrimitives.get(primitive) : primitive;
    }

    public static Class unbox(Class box) {
        return primitivesByBox.containsKey(box) ? primitivesByBox.get(box) : box;
    }

    public static Class<?> getArrayType(Class<?> component, int dimensions) {
        return Array.newInstance(component, new int[dimensions]).getClass();
    }

    public static Class getCommonParent(List<Class> cls) {
        if (cls.isEmpty()) {
            return Object.class;
        } else if (cls.size() == 1) {
            return cls.get(0);
        }

        List<Class> current = new ArrayList<>();
        current.addAll(cls);

        while (current.stream().noneMatch(s -> cls.stream().allMatch(c -> s.isAssignableFrom(c)))) {
            List<Class> newCurrent = new ArrayList<>();
            for (Class c : current) {
                if (c.getSuperclass() != null) {
                    newCurrent.add(c.getSuperclass());
                }
                Collections.addAll(newCurrent, c.getInterfaces());
            }
            current = newCurrent;
        }

        return current.stream().filter(s -> cls.stream().allMatch(c -> s.isAssignableFrom(c))).findFirst().get();
    }

    public static MethodType of(Method method) {
        MethodType.Builder builder = MethodType.builder(of(method.getGenericReturnType()));

        for (Type type : method.getGenericParameterTypes()) {
            builder.param(of(type));
        }

        return builder.build();
    }

    public static GenericType ofParameterized(Class<?> cls) {
        GenericType.Builder builder = GenericType.builder(cls);
        for (Type param : cls.getTypeParameters()) {
            builder.generic(of(param));
        }
        return builder.build();
    }

    public static Optional<GenericType> resolveGenericsToSubtype(Class<?> sub, GenericType parent) {
        GenericType common = ofParameterized(sub);
        return resolveGenericsToSupertype(common, parent.getType()).map(type -> {
           List<JavaType> res = new ArrayList<>(common.getGenerics());

            for (int i = 0; i < common.genericCount(); i++) {
                JavaType gen = common.getGeneric(i);
                if (gen.isVariable()) {
                    int index = -1;

                    for (int j = 0; j < type.genericCount(); j++) {
                        JavaType test = type.getGeneric(j);
                        if (test.isVariable() && gen.getName().equalsIgnoreCase(test.getName())) {
                            index = j;
                            break;
                        }
                    }

                    if (index != -1) {
                        res.set(i, parent.getGeneric(index));
                    }
                }
            }

            return GenericType.builder(sub).generics(res).build();
        });
    }

    public static Optional<GenericType> resolveGenericsToSupertype(GenericType sub, Class<?> parent) {
        return getHierarchy(sub.getType(), parent).map(hierarchy -> {
            Map<String, JavaType> paramMap = new LinkedHashMap<>();
            GenericType subParams = ofParameterized(sub.getType());
            for (int i = 0; i < subParams.getGenerics().size(); i++) {
                paramMap.put(subParams.getGeneric(i).getName(), sub.getGeneric(i));
            }

            for (int i = 0; i < hierarchy.size() - 1; i++) {
                Map<String, JavaType> newParamMap = new LinkedHashMap<>();

                Class cls = hierarchy.get(i);
                Class sup = hierarchy.get(i + 1);

                GenericType superclassParams = ofParameterized(sup);
                GenericType superclassFilled = (GenericType) of(getInherited(cls, sup).get());

                for (int j = 0; j < superclassParams.genericCount(); j++) {
                    JavaType typeParam = superclassParams.getGeneric(j);
                    JavaType filledParam = superclassFilled.getGeneric(j);

                    if (filledParam.isVariable()) {
                        newParamMap.put(typeParam.getName(), paramMap.get(filledParam.getName()));
                    } else {
                        newParamMap.put(typeParam.getName(), filledParam);
                    }
                }

                paramMap = newParamMap;
            }

            GenericType parentGenerics = ofParameterized(parent);
            List<JavaType> generics = new ArrayList<>();

            for (JavaType param : parentGenerics.getGenerics()) {
                JavaType target = paramMap.get(param.getName());
                if (target.isVariable()) {
                    for (int i = 0; i < subParams.genericCount(); i++) {
                        JavaType subParam = subParams.getGeneric(i);
                        JavaType filled = sub.getGeneric(i);

                        if (subParam.getName().equals(target.getName())) {
                            generics.add(filled);
                            break;
                        }
                    }
                } else {
                    generics.add(target);
                }
            }

            return GenericType.builder(parent).generics(generics).build();
        });
    }

    public static JavaType of(Type type) {
        return of(type, 0);
    }

    public static JavaType of(Type type, int arr) {
        if (type instanceof Class) {
            return ofCls((Class<?>) type, arr);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) type;
            Type raw = ptype.getRawType();
            if (raw instanceof Class) {
                GenericType.Builder builder = GenericType.builder((Class<?>) raw);
                for (Type param : ptype.getActualTypeArguments()) {
                    builder.generic(of(param));
                }
                return builder.build().array(arr);
            } else {
                throw new IllegalArgumentException("Unknown raw type: " + type.getClass().getName());
            }
        } else if (type instanceof WildcardType) {
            WildcardType wtype = (WildcardType) type;
            VariableType.Builder builder = VariableType.builder("?");
            for (Type up : wtype.getUpperBounds()) {
                builder.upper(of(up));
            }

            for (Type low : wtype.getLowerBounds()) {
                builder.lower(of(low));
            }
            return builder.build().array(arr);
        } else if (type instanceof TypeVariable) {
            TypeVariable vtype = (TypeVariable) type;
            VariableType.Builder builder = VariableType.builder(vtype.getName());
            for (Type up : vtype.getBounds()) {
                builder.upper(of(up));
            }
            return builder.build();
        } else if (type instanceof GenericArrayType) {
            GenericArrayType atype = (GenericArrayType) type;
            return of(atype.getGenericComponentType(), arr + 1);
        } else {
            throw new IllegalArgumentException("Unknown type: " + type.getClass().getName());
        }
    }

    public static JavaType ofCls(Class<?> cls, int arr) {
        Class c = cls;
        while (c.isArray()) {
            c = c.getComponentType();
            arr++;
        }
        return GenericType.builder(c).build().array(arr);
    }

    public static JavaType of(Token<?> token) {
        return of(((ParameterizedType) token.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    private static List<Class> append(List<Class> tests, Class test) {
        List<Class> cls = new ArrayList<>();
        cls.addAll(tests);
        cls.add(test);
        return cls;
    }

    public static Optional<List<Class>> getHierarchy(Class sub, Class parent) {
        List<List<Class>> tests = new ArrayList<>();
        List<Class> firstTest = new ArrayList<>();
        firstTest.add(sub);
        tests.add(firstTest);

        while (!tests.isEmpty()) {
            List<List<Class>> newTests = new ArrayList<>();
            for (List<Class> test : tests) {
                Class target = test.get(test.size() - 1);
                if (target.equals(parent)) {
                    return Optional.of(test);
                } else {
                    Class superClass = target.getSuperclass();
                    if (superClass != null) {
                        newTests.add(append(test, superClass));
                    }

                    Class[] interfaces = target.getInterfaces();
                    for (Class face : interfaces) {
                        newTests.add(append(test, face));
                    }
                }
            }
            tests = newTests;
        }

        return Optional.empty();
    }

    public static Optional<Type> getInherited(Class cls, Class target) {
        if (target.equals(cls.getSuperclass())) {
            return Optional.ofNullable(cls.getGenericSuperclass());
        }

        Type[] interfaces = cls.getGenericInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (cls.getInterfaces()[i].equals(target)) {
                return Optional.ofNullable(interfaces[i]);
            }
        }

        return Optional.empty();
    }

}
