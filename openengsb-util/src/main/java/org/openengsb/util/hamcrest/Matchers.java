/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.util.hamcrest;

import java.io.File;
import java.util.EventObject;

import org.hamcrest.Matcher;
import org.hamcrest.beans.HasProperty;
import org.hamcrest.beans.HasPropertyWithValue;
import org.hamcrest.collection.IsArrayContaining;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.collection.IsIn;
import org.hamcrest.collection.IsMapContaining;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.AnyOf;
import org.hamcrest.core.DescribedAs;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsAnything;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.IsSame;
import org.hamcrest.number.IsCloseTo;
import org.hamcrest.number.OrderingComparisons;
import org.hamcrest.object.HasToString;
import org.hamcrest.object.IsCompatibleType;
import org.hamcrest.object.IsEventFrom;
import org.hamcrest.text.IsEqualIgnoringCase;
import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.hamcrest.text.StringContains;
import org.hamcrest.text.StringEndsWith;
import org.hamcrest.text.StringStartsWith;
import org.hamcrest.xml.HasXPath;
import org.openengsb.util.hamcrest.file.Exists;


public class Matchers {
    public static Matcher<File> exists() {
        return Exists.exists();
    }

    // ========================================================================
    // original org.hamcrest.Matchers version 1.1
    // ========================================================================

    /**
     * Decorates another Matcher, retaining the behavior but allowing tests to
     * be slightly more expressive.
     * 
     * eg. assertThat(cheese, equalTo(smelly)) vs assertThat(cheese,
     * is(equalTo(smelly)))
     */
    public static <T> Matcher<T> is(Matcher<T> matcher) {
        return Is.is(matcher);
    }

    /**
     * This is a shortcut to the frequently used is(equalTo(x)).
     * 
     * eg. assertThat(cheese, is(equalTo(smelly))) vs assertThat(cheese,
     * is(smelly))
     */
    public static <T> Matcher<T> is(T value) {
        return Is.is(value);
    }

    /**
     * This is a shortcut to the frequently used
     * is(instanceOf(SomeClass.class)).
     * 
     * eg. assertThat(cheese, is(instanceOf(Cheddar.class))) vs
     * assertThat(cheese, is(Cheddar.class))
     */
    public static Matcher<java.lang.Object> is(java.lang.Class<?> type) {
        return Is.is(type);
    }

    /**
     * Inverts the rule.
     */
    public static <T> Matcher<T> not(Matcher<T> matcher) {
        return IsNot.not(matcher);
    }

    /**
     * This is a shortcut to the frequently used not(equalTo(x)).
     * 
     * eg. assertThat(cheese, is(not(equalTo(smelly)))) vs assertThat(cheese,
     * is(not(smelly)))
     */
    public static <T> Matcher<T> not(T value) {
        return IsNot.not(value);
    }

    /**
     * Is the value equal to another value, as tested by the
     * {@link java.lang.Object#equals} invokedMethod?
     */
    public static <T> Matcher<T> equalTo(T operand) {
        return IsEqual.equalTo(operand);
    }

    /**
     * Is the value an instance of a particular type?
     */
    public static Matcher<java.lang.Object> instanceOf(java.lang.Class<?> type) {
        return IsInstanceOf.instanceOf(type);
    }

    /**
     * Evaluates to true only if ALL of the passed in matchers evaluate to true.
     */
    public static <T> Matcher<T> allOf(Matcher<? extends T>... matchers) {
        return AllOf.allOf(matchers);
    }

    /**
     * Evaluates to true only if ALL of the passed in matchers evaluate to true.
     */
    public static <T> Matcher<T> allOf(java.lang.Iterable<Matcher<? extends T>> matchers) {
        return AllOf.allOf(matchers);
    }

    /**
     * Evaluates to true if ANY of the passed in matchers evaluate to true.
     */
    public static <T> Matcher<T> anyOf(Matcher<? extends T>... matchers) {
        return AnyOf.anyOf(matchers);
    }

    /**
     * Evaluates to true if ANY of the passed in matchers evaluate to true.
     */
    public static <T> Matcher<T> anyOf(java.lang.Iterable<Matcher<? extends T>> matchers) {
        return AnyOf.anyOf(matchers);
    }

    /**
     * Creates a new instance of IsSame
     * 
     * @param object The predicate evaluates to true only when the argument is
     *        this object.
     */
    public static <T> Matcher<T> sameInstance(T object) {
        return IsSame.sameInstance(object);
    }

    /**
     * This matcher always evaluates to true.
     */
    public static <T> Matcher<T> anything() {
        return IsAnything.anything();
    }

    /**
     * This matcher always evaluates to true.
     * 
     * @param description A meaningful string used when describing itself.
     */
    public static <T> Matcher<T> anything(java.lang.String description) {
        return IsAnything.anything(description);
    }

    /**
     * This matcher always evaluates to true. With type inference.
     */
    public static <T> Matcher<T> any(java.lang.Class<T> type) {
        return IsAnything.any(type);
    }

    /**
     * Matches if value is null.
     */
    public static <T> Matcher<T> nullValue() {
        return IsNull.nullValue();
    }

    /**
     * Matches if value is null. With type inference.
     */
    public static <T> Matcher<T> nullValue(java.lang.Class<T> type) {
        return IsNull.nullValue(type);
    }

    /**
     * Matches if value is not null.
     */
    public static <T> Matcher<T> notNullValue() {
        return IsNull.notNullValue();
    }

    /**
     * Matches if value is not null. With type inference.
     */
    public static <T> Matcher<T> notNullValue(java.lang.Class<T> type) {
        return IsNull.notNullValue(type);
    }

    /**
     * Wraps an existing matcher and overrides the description when it fails.
     */
    public static <T> Matcher<T> describedAs(java.lang.String description, Matcher<T> matcher,
            java.lang.Object... values) {
        return DescribedAs.describedAs(description, matcher, values);
    }

    public static <T> Matcher<T[]> hasItemInArray(Matcher<T> elementMatcher) {
        return IsArrayContaining.hasItemInArray(elementMatcher);
    }

    public static <T> Matcher<T[]> hasItemInArray(T element) {
        return IsArrayContaining.hasItemInArray(element);
    }

    public static <T> Matcher<java.lang.Iterable<T>> hasItem(T element) {
        return IsCollectionContaining.hasItem(element);
    }

    public static <T> Matcher<java.lang.Iterable<T>> hasItem(Matcher<? extends T> elementMatcher) {
        return IsCollectionContaining.hasItem(elementMatcher);
    }

    public static <T> Matcher<java.lang.Iterable<T>> hasItems(Matcher<? extends T>... elementMatchers) {
        return IsCollectionContaining.hasItems(elementMatchers);
    }

    public static <T> Matcher<java.lang.Iterable<T>> hasItems(T... elements) {
        return IsCollectionContaining.hasItems(elements);
    }

    public static <K, V> Matcher<java.util.Map<K, V>> hasEntry(Matcher<K> keyMatcher, Matcher<V> valueMatcher) {
        return IsMapContaining.hasEntry(keyMatcher, valueMatcher);
    }

    public static <K, V> Matcher<java.util.Map<K, V>> hasEntry(K key, V value) {
        return IsMapContaining.hasEntry(key, value);
    }

    public static <K, V> Matcher<java.util.Map<K, V>> hasKey(Matcher<K> keyMatcher) {
        return IsMapContaining.hasKey(keyMatcher);
    }

    public static <K, V> Matcher<java.util.Map<K, V>> hasKey(K key) {
        return IsMapContaining.hasKey(key);
    }

    public static <K, V> Matcher<java.util.Map<K, V>> hasValue(Matcher<V> valueMatcher) {
        return IsMapContaining.hasValue(valueMatcher);
    }

    public static <K, V> Matcher<java.util.Map<K, V>> hasValue(V value) {
        return IsMapContaining.hasValue(value);
    }

    public static <T> Matcher<T> isIn(java.util.Collection<T> collection) {
        return IsIn.isIn(collection);
    }

    public static <T> Matcher<T> isIn(T[] param1) {
        return IsIn.isIn(param1);
    }

    public static <T> Matcher<T> isOneOf(T... elements) {
        return IsIn.isOneOf(elements);
    }

    public static Matcher<java.lang.Double> closeTo(double operand, double error) {
        return IsCloseTo.closeTo(operand, error);
    }

    public static <T extends java.lang.Comparable<T>> Matcher<T> greaterThan(T value) {
        return OrderingComparisons.greaterThan(value);
    }

    public static <T extends java.lang.Comparable<T>> Matcher<T> greaterThanOrEqualTo(T value) {
        return OrderingComparisons.greaterThanOrEqualTo(value);
    }

    public static <T extends java.lang.Comparable<T>> Matcher<T> lessThan(T value) {
        return OrderingComparisons.lessThan(value);
    }

    public static <T extends java.lang.Comparable<T>> Matcher<T> lessThanOrEqualTo(T value) {
        return OrderingComparisons.lessThanOrEqualTo(value);
    }

    public static Matcher<String> equalToIgnoringCase(String string) {
        return IsEqualIgnoringCase.equalToIgnoringCase(string);
    }

    public static Matcher<String> equalToIgnoringWhiteSpace(String string) {
        return IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(string);
    }

    public static Matcher<String> containsString(String substring) {
        return StringContains.containsString(substring);
    }

    public static Matcher<String> endsWith(String substring) {
        return StringEndsWith.endsWith(substring);
    }

    public static Matcher<String> startsWith(String substring) {
        return StringStartsWith.startsWith(substring);
    }

    public static <T> Matcher<T> hasToString(Matcher<String> toStringMatcher) {
        return HasToString.hasToString(toStringMatcher);
    }

    public static <T> Matcher<Class<?>> typeCompatibleWith(Class<T> baseType) {
        return IsCompatibleType.typeCompatibleWith(baseType);
    }

    /**
     * Constructs an IsEventFrom Matcher that returns true for any object
     * derived from <var>eventClass</var> announced by <var>source</var>.
     */
    public static Matcher<java.util.EventObject> eventFrom(Class<? extends java.util.EventObject> eventClass,
            Object source) {
        return IsEventFrom.eventFrom(eventClass, source);
    }

    /**
     * Constructs an IsEventFrom Matcher that returns true for any object
     * derived from {@link EventObject} announced by <var>source </var>.
     */
    public static Matcher<EventObject> eventFrom(Object source) {
        return IsEventFrom.eventFrom(source);
    }

    public static <T> Matcher<T> hasProperty(String propertyName) {
        return HasProperty.hasProperty(propertyName);
    }

    public static <T> Matcher<T> hasProperty(String propertyName, Matcher<?> value) {
        return HasPropertyWithValue.hasProperty(propertyName, value);
    }

    public static Matcher<org.w3c.dom.Node> hasXPath(String xPath, Matcher<String> valueMatcher) {
        return HasXPath.hasXPath(xPath, valueMatcher);
    }

    public static Matcher<org.w3c.dom.Node> hasXPath(String xPath) {
        return HasXPath.hasXPath(xPath);
    }

    private Matchers() {
        throw new AssertionError();
    }
}
