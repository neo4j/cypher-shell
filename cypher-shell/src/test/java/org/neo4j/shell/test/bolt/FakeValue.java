package org.neo4j.shell.test.bolt;

import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.exceptions.value.Uncoercible;
import org.neo4j.driver.v1.types.*;
import org.neo4j.driver.v1.util.Function;

import java.util.List;
import java.util.Map;

/**
 * A fake value
 */
class FakeValue implements Value {

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Iterable<Value> values() {
        return null;
    }

    @Override
    public <T> Iterable<T> values(Function<Value, T> mapFunction) {
        return null;
    }

    @Override
    public Map<String, Object> asMap() {
        return null;
    }

    @Override
    public <T> Map<String, T> asMap(Function<Value, T> mapFunction) {
        throw new Uncoercible(getClass().getSimpleName(), "Map");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Iterable<String> keys() {
        return null;
    }

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public Value get(String key) {
        return null;
    }

    @Override
    public Value get(int index) {
        return null;
    }

    @Override
    public Type type() {
        return null;
    }

    @Override
    public boolean hasType(Type type) {
        return false;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public Object asObject() {
        throw new Uncoercible(getClass().getSimpleName(), "Object");
    }

    @Override
    public boolean asBoolean() {
        throw new Uncoercible(getClass().getSimpleName(), "Bool");
    }

    @Override
    public byte[] asByteArray() {
        throw new Uncoercible(getClass().getSimpleName(), "Byte[]");
    }

    @Override
    public String asString() {
        throw new Uncoercible(getClass().getSimpleName(), "String");
    }

    @Override
    public Number asNumber() {
        throw new Uncoercible(getClass().getSimpleName(), "Number");
    }

    @Override
    public long asLong() {
        throw new Uncoercible(getClass().getSimpleName(), "Long");
    }

    @Override
    public int asInt() {
        throw new Uncoercible(getClass().getSimpleName(), "Int");
    }

    @Override
    public double asDouble() {
        throw new Uncoercible(getClass().getSimpleName(), "Double");
    }

    @Override
    public float asFloat() {
        throw new Uncoercible(getClass().getSimpleName(), "Float");
    }

    @Override
    public List<Object> asList() {
        throw new Uncoercible(getClass().getSimpleName(), "List");
    }

    @Override
    public <T> List<T> asList(Function<Value, T> mapFunction) {
        throw new Uncoercible(getClass().getSimpleName(), "List");
    }

    @Override
    public Entity asEntity() {
        throw new Uncoercible(getClass().getSimpleName(), "Entity");
    }

    @Override
    public Node asNode() {
        throw new Uncoercible(getClass().getSimpleName(), "Node");
    }

    @Override
    public Relationship asRelationship() {
        throw new Uncoercible(getClass().getSimpleName(), "Relationship");
    }

    @Override
    public Path asPath() {
        throw new Uncoercible(getClass().getSimpleName(), "Path");
    }

    @Override
    public Value get(String key, Value defaultValue) {
        return null;
    }

    @Override
    public Object get(String key, Object defaultValue) {
        return null;
    }

    @Override
    public Number get(String key, Number defaultValue) {
        return null;
    }

    @Override
    public Entity get(String key, Entity defaultValue) {
        return null;
    }

    @Override
    public Node get(String key, Node defaultValue) {
        return null;
    }

    @Override
    public Path get(String key, Path defaultValue) {
        return null;
    }

    @Override
    public Relationship get(String key, Relationship defaultValue) {
        return null;
    }

    @Override
    public List<Object> get(String key, List<Object> defaultValue) {
        return null;
    }

    @Override
    public <T> List<T> get(String key, List<T> defaultValue, Function<Value, T> mapFunc) {
        return null;
    }

    @Override
    public Map<String, Object> get(String key, Map<String, Object> defaultValue) {
        return null;
    }

    @Override
    public <T> Map<String, T> get(String key, Map<String, T> defaultValue, Function<Value, T> mapFunc) {
        return null;
    }

    @Override
    public int get(String key, int defaultValue) {
        return 0;
    }

    @Override
    public long get(String key, long defaultValue) {
        return 0;
    }

    @Override
    public boolean get(String key, boolean defaultValue) {
        return false;
    }

    @Override
    public String get(String key, String defaultValue) {
        return null;
    }

    @Override
    public float get(String key, float defaultValue) {
        return 0;
    }

    @Override
    public double get(String key, double defaultValue) {
        return 0;
    }
}
