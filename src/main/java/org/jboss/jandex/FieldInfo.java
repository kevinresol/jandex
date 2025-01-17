/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.jandex;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a field.
 *
 * <p>
 * <b>Thread-Safety</b>
 * </p>
 * This class is immutable and can be shared between threads without safe publication.
 *
 * @author Jason T. Greene
 *
 */
public final class FieldInfo implements AnnotationTarget {
    
    private ClassInfo clazz;
    private FieldInternal internal;

    FieldInfo() {
    }

    FieldInfo(ClassInfo clazz, FieldInternal internal) {
        this.clazz = clazz;
        this.internal = internal;
    }

    FieldInfo(ClassInfo clazz, byte[] name, Type type, short flags) {
        this(clazz, new FieldInternal(name, type, flags));
    }

    /**
     * Construct a new mock Field instance.
     *
     * @param clazz the class declaring the field
     * @param name the name of the field
     * @param type the Java field type
     * @param flags the field attributes
     * @return a mock field
     */
    public static FieldInfo create(ClassInfo clazz, String name, Type type, short flags) {
        if (clazz == null)
            throw new IllegalArgumentException("Clazz can't be null");

        if (name == null)
            throw new IllegalArgumentException("Name can't be null");

        return new FieldInfo(clazz, Utils.toUTF8(name), type, flags);
    }

    /**
     * Returns the local name of the field
     *
     * @return the local name of the field
     */
    public final String name() {
        return internal.name();
    }

    /**
     * Returns the class which declared the field
     *
     * @return the declaring class
     */
    public final ClassInfo declaringClass() {
        return clazz;
    }

    /**
     * Returns the <code>Type</code> declared on this field. This may be an array, a primitive, or a generic type definition.
     *
     * @return the type of this field
     */
    public final Type type() {
        return internal.type();
    }

    public final Kind kind() {
        return Kind.FIELD;
    }

    /**
     * Returns the list of annotation instances declared on this field. It may be empty, but never null.
     *
     * @return the list of annotations on this field
     */
    public List<AnnotationInstance> annotations() {
        return internal.annotations();
    }

    /**
     * Retrieves an annotation instance declared on this field. If an annotation by that name is not present, null will be returned.
     *
     * @param name the name of the annotation to locate on this field
     * @return the annotation if found, otherwise, null
     */
    public final AnnotationInstance annotation(DotName name) {
        return internal.annotation(name);
    }

    /**
     * Retrieves annotation instances declared on this field, by the name of the annotation.
     * 
     * If the specified annotation is repeatable (JLS 9.6), the result also contains all values from the container annotation instance.
     * 
     * @param name the name of the annotation
     * @param index the index used to obtain the annotation class
     * @return the annotation instances declared on this field, or an empty list if none
     * @throws IllegalArgumentException If the index does not contain the annotation definition or if it does not represent an annotation type
     */
    public final List<AnnotationInstance> annotationsWithRepeatable(DotName name, IndexView index) {
        AnnotationInstance ret = annotation(name);
        if (ret != null) {
            // Annotation present - no need to try to find repeatable annotations
            return Collections.singletonList(ret);
        }
        ClassInfo annotationClass = index.getClassByName(name);
        if (annotationClass == null) {
            throw new IllegalArgumentException("Index does not contain the annotation definition: " + name);
        }
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type: " + annotationClass);
        }
        AnnotationInstance repeatable = annotationClass.classAnnotation(Index.REPEATABLE);
        if (repeatable == null) {
            return Collections.emptyList();
        }
        Type containingType = repeatable.value().asClass();
        AnnotationInstance containing = annotation(containingType.name());
        if (containing == null) {
            return Collections.emptyList();
        }
        AnnotationInstance[] values = containing.value().asNestedArray();
        List<AnnotationInstance> instances = new ArrayList<AnnotationInstance>(values.length);
        for (AnnotationInstance nestedInstance : values) {
            instances.add(nestedInstance);
        }
        return instances;
    }

    /**
     * Returns whether or not the annotation instance with the given name occurs on this field
     *
     * @see #annotations()
     * @see #annotation(DotName)
     * @param name the name of the annotation to look for
     * @return true if the annotation is present, false otherwise
     */
    public final boolean hasAnnotation(DotName name) {
        return internal.hasAnnotation(name);
    }

    /**
     * Returns whether or not this field is declared as an element of an enum.
     *
     * @return true if the field is declared as an element of an enum, false
     *         otherwise.
     *
     * @see java.lang.reflect.Field#isEnumConstant()
     */
    public boolean isEnumConstant() {
        return (flags() & Modifiers.ENUM) != 0;
    }

    /**
     * Returns the access fields of this field. {@link Modifier} can be used on this value.
     *
     * @return the access flags of this field
     */
    public final short flags() {
        return internal.flags();
    }
    
    /**
     * 
     * @return {@code true} if this field is a synthetic field
     */
    public final boolean isSynthetic() {
        return Modifiers.isSynthetic(internal.flags());
    }

    /**
     * Returns a string representation describing this field. It is similar although not necessarily equivalent to a Java source code expression representing
     * this field.
     *
     * @return a string representation for this field
     */
    public String toString() {
        return internal.toString(clazz);
    }

    @Override
    public final ClassInfo asClass() {
        throw new IllegalArgumentException("Not a class");
    }

    @Override
    public final FieldInfo asField() {
        return this;
    }

    @Override
    public final MethodInfo asMethod() {
        throw new IllegalArgumentException("Not a method");
    }

    @Override
    public final MethodParameterInfo asMethodParameter() {
        throw new IllegalArgumentException("Not a method parameter");
    }

    @Override
    public final TypeTarget asType() {
        throw new IllegalArgumentException("Not a type");
    }

    @Override
    public RecordComponentInfo asRecordComponent() {
        throw new IllegalArgumentException("Not a record component");
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + clazz.hashCode();
        result = 31 * result + internal.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FieldInfo other = (FieldInfo) o;
        return clazz.equals(other.clazz) && internal.equals(other.internal);
    }

    void setType(Type type) {
        internal.setType(type);
    }

    void setAnnotations(List<AnnotationInstance> annotations) {
        internal.setAnnotations(annotations);
    }

    FieldInternal fieldInternal() {
        return internal;
    }

    void setFieldInternal(FieldInternal internal) {
        this.internal = internal;
    }

    void setClassInfo(ClassInfo clazz) {
        this.clazz = clazz;
    }
}
