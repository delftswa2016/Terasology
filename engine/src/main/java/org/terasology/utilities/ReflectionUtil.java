/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.utilities;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.terasology.rendering.nui.UIElement;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 *
 */
public final class ReflectionUtil {
    private ReflectionUtil() {
    }

    /**
     * Attempts to return the type of a parameter of a parameterised field. This uses compile-time information only - the
     * type should be obtained from a field with a the generic types bound.
     *
     * @param type
     * @param index
     * @return The type of the generic parameter at index for the given type, or null if it cannot be obtained.
     */
    // TODO - Improve parameter lookup to go up the inheritance tree more
    public static Type getTypeParameter(Type type, int index) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (parameterizedType.getActualTypeArguments().length < index + 1) {
            return null;
        }
        return parameterizedType.getActualTypeArguments()[index];
    }

    public static Class<?> getClassOfType(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) type).getRawType();
        }
        return null;
    }

    public static Method findGetter(Field field) {
        return findGetter(field.getName(), field.getDeclaringClass(), field.getType());
    }

    public static Method findGetter(String propertyName, Class beanClass, Class propertyType) {
        Method result = findMethod(beanClass, "get" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1));
        if (result != null && propertyType.equals(result.getReturnType())) {
            result.setAccessible(true);
            return result;
        }
        result = findMethod(beanClass, "is" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1));
        if (result != null && propertyType.equals(result.getReturnType())) {
            result.setAccessible(true);
            return result;
        }
        return null;
    }

    public static Method findSetter(Field field) {
        return findSetter(field.getName(), field.getDeclaringClass(), field.getType());
    }

    public static Method findSetter(String propertyName, Class beanClass, Class propertyType) {
        String setterName = "set" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1);
        Method result = findMethod(beanClass, setterName, propertyType);
        if (result != null) {
            result.setAccessible(true);
        }
        return result;
    }

    public static Method findMethod(Class<?> targetType, String methodName, Class<?>... parameters) {
        try {
            return targetType.getMethod(methodName, parameters);
        } catch (NoSuchMethodException me) {
            // We're expecting not to find methods
            return null;
        }
    }

    /**
     * Returns an ordered list of super classes and interfaces for the given class, that have a common base class.
     * The set is ordered with the deepest interface first, through all the interfaces, and then all the super classes.
     * @param forClass
     * @param baseClass
     * @return  an ordered list of super classes and interfaces for the given class, that have a common base class.
     */
    public static <T> List<Class<? extends T>> getInheritanceTree(Class<? extends T> forClass, Class<T> baseClass) {
        Set<Class<? extends T>> result = Sets.newLinkedHashSet();
        for (Class<?> interfaceType : forClass.getInterfaces()) {
            if (baseClass.isAssignableFrom(interfaceType)) {
                addInterfaceToInheritanceTree((Class<? extends T>) interfaceType, baseClass, result);
            }
        }
        addClassToInheritanceTree(forClass, baseClass, result);
        return Lists.newArrayList(result);
    }

    private static <T> void addClassToInheritanceTree(Class<? extends T> element, Class<T> baseClass, Set<Class<? extends T>> result) {
        if (baseClass.isAssignableFrom(element.getSuperclass())) {
            addClassToInheritanceTree((Class<? extends T>) element.getSuperclass(), baseClass, result);
        }
        result.add(element);
    }

    private static <T> void addInterfaceToInheritanceTree(Class<? extends T> interfaceType, Class<T> baseClass, Set<Class<? extends T>> result) {
        for (Class<?> parentInterface : interfaceType.getInterfaces()) {
            if (UIElement.class.isAssignableFrom(parentInterface)) {
                addInterfaceToInheritanceTree((Class<? extends T>) parentInterface, baseClass, result);
            }
        }
        result.add(interfaceType);
    }
}