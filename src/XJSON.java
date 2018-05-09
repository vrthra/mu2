package org.json;
import java.util.Locale;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import static java.lang.String.format;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;


import java.util.Enumeration;
import java.util.Properties;


import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;


import java.io.Reader;



/**
 * A XJSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>XJSONArray</code>, <code>XJSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>XJSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a XJSON text into a Java object. The
 * <code>toString</code> method converts to XJSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * XJSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there is <code>,</code>
 * &nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces, and
 * if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>, or
 * <code>null</code>.</li>
 * </ul>
 *
 * @author XJSON.org
 * @version 2016-08/15
 */
class XJSONArray implements Iterable<Object> {

    /**
     * The arrayList where the XJSONArray's properties are kept.
     */
    private final ArrayList<Object> myArrayList;

    /**
     * Construct an empty XJSONArray.
     */
    public XJSONArray() {
        this.myArrayList = new ArrayList<Object>();
    }

    /**
     * Construct a XJSONArray from a XJSONTokener.
     *
     * @param x
     *            A XJSONTokener
     * @throws XJSONException
     *             If there is a syntax error.
     */
    public XJSONArray(XJSONTokener x) throws XJSONException {
        this();
        if (x.nextClean() != '[') {
            throw x.syntaxError("A XJSONArray text must start with '['");
        }
        
        char nextChar = x.nextClean();
        if (nextChar == 0) {
            // array is unclosed. No ']' found, instead EOF
            throw x.syntaxError("Expected a ',' or ']'");
        }
        if (nextChar != ']') {
            x.back();
            for (;;) {
                if (x.nextClean() == ',') {
                    x.back();
                    this.myArrayList.add(XJSONObject.NULL);
                } else {
                    x.back();
                    this.myArrayList.add(x.nextValue());
                }
                switch (x.nextClean()) {
                case 0:
                    // array is unclosed. No ']' found, instead EOF
                    throw x.syntaxError("Expected a ',' or ']'");
                case ',':
                    nextChar = x.nextClean();
                    if (nextChar == 0) {
                        // array is unclosed. No ']' found, instead EOF
                        throw x.syntaxError("Expected a ',' or ']'");
                    }
                    if (nextChar == ']') {
                        return;
                    }
                    x.back();
                    break;
                case ']':
                    return;
                default:
                    throw x.syntaxError("Expected a ',' or ']'");
                }
            }
        }
    }

    /**
     * Construct a XJSONArray from a source XJSON text.
     *
     * @param source
     *            A string that begins with <code>[</code>&nbsp;<small>(left
     *            bracket)</small> and ends with <code>]</code>
     *            &nbsp;<small>(right bracket)</small>.
     * @throws XJSONException
     *             If there is a syntax error.
     */
    public XJSONArray(String source) throws XJSONException {
        this(new XJSONTokener(source));
    }

    /**
     * Construct a XJSONArray from a Collection.
     *
     * @param collection
     *            A Collection.
     */
    public XJSONArray(Collection<?> collection) {
        if (collection == null) {
            this.myArrayList = new ArrayList<Object>();
        } else {
            this.myArrayList = new ArrayList<Object>(collection.size());
            for (Object o: collection){
                this.myArrayList.add(XJSONObject.wrap(o));
            }
        }
    }

    /**
     * Construct a XJSONArray from an array
     *
     * @throws XJSONException
     *             If not an array or if an array value is non-finite number.
     */
    public XJSONArray(Object array) throws XJSONException {
        this();
        if (array.getClass().isArray()) {
            int length = Array.getLength(array);
            this.myArrayList.ensureCapacity(length);
            for (int i = 0; i < length; i += 1) {
                this.put(XJSONObject.wrap(Array.get(array, i)));
            }
        } else {
            throw new XJSONException(
                    "XJSONArray initial value should be a string or collection or array.");
        }
    }

    @Override
    public Iterator<Object> iterator() {
        return this.myArrayList.iterator();
    }

    /**
     * Get the object value associated with an index.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return An object value.
     * @throws XJSONException
     *             If there is no value for the index.
     */
    public Object get(int index) throws XJSONException {
        Object object = this.opt(index);
        if (object == null) {
            throw new XJSONException("XJSONArray[" + index + "] not found.");
        }
        return object;
    }

    /**
     * Get the boolean value associated with an index. The string values "true"
     * and "false" are converted to boolean.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return The truth.
     * @throws XJSONException
     *             If there is no value for the index or if the value is not
     *             convertible to boolean.
     */
    public boolean getBoolean(int index) throws XJSONException {
        Object object = this.get(index);
        if (object.equals(Boolean.FALSE)
                || (object instanceof String && ((String) object)
                        .equalsIgnoreCase("false"))) {
            return false;
        } else if (object.equals(Boolean.TRUE)
                || (object instanceof String && ((String) object)
                        .equalsIgnoreCase("true"))) {
            return true;
        }
        throw new XJSONException("XJSONArray[" + index + "] is not a boolean.");
    }

    /**
     * Get the double value associated with an index.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return The value.
     * @throws XJSONException
     *             If the key is not found or if the value cannot be converted
     *             to a number.
     */
    public double getDouble(int index) throws XJSONException {
        Object object = this.get(index);
        try {
            return object instanceof Number ? ((Number) object).doubleValue()
                    : Double.parseDouble((String) object);
        } catch (Exception e) {
            throw new XJSONException("XJSONArray[" + index + "] is not a number.", e);
        }
    }

    /**
     * Get the float value associated with a key.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return The numeric value.
     * @throws XJSONException
     *             if the key is not found or if the value is not a Number
     *             object and cannot be converted to a number.
     */
    public float getFloat(int index) throws XJSONException {
        Object object = this.get(index);
        try {
            return object instanceof Number ? ((Number) object).floatValue()
                    : Float.parseFloat(object.toString());
        } catch (Exception e) {
            throw new XJSONException("XJSONArray[" + index
                    + "] is not a number.", e);
        }
    }

    /**
     * Get the Number value associated with a key.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return The numeric value.
     * @throws XJSONException
     *             if the key is not found or if the value is not a Number
     *             object and cannot be converted to a number.
     */
    public Number getNumber(int index) throws XJSONException {
        Object object = this.get(index);
        try {
            if (object instanceof Number) {
                return (Number)object;
            }
            return XJSONObject.stringToNumber(object.toString());
        } catch (Exception e) {
            throw new XJSONException("XJSONArray[" + index + "] is not a number.", e);
        }
    }

    /**
    * Get the enum value associated with an index.
    * 
    * @param clazz
    *            The type of enum to retrieve.
    * @param index
    *            The index must be between 0 and length() - 1.
    * @return The enum value at the index location
    * @throws XJSONException
    *            if the key is not found or if the value cannot be converted
    *            to an enum.
    */
    public <E extends Enum<E>> E getEnum(Class<E> clazz, int index) throws XJSONException {
        E val = optEnum(clazz, index);
        if(val==null) {
            // XJSONException should really take a throwable argument.
            // If it did, I would re-implement this with the Enum.valueOf
            // method and place any thrown exception in the XJSONException
            throw new XJSONException("XJSONArray[" + index + "] is not an enum of type "
                    + XJSONObject.quote(clazz.getSimpleName()) + ".");
        }
        return val;
    }

    /**
     * Get the BigDecimal value associated with an index.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return The value.
     * @throws XJSONException
     *             If the key is not found or if the value cannot be converted
     *             to a BigDecimal.
     */
    public BigDecimal getBigDecimal (int index) throws XJSONException {
        Object object = this.get(index);
        try {
            return new BigDecimal(object.toString());
        } catch (Exception e) {
            throw new XJSONException("XJSONArray[" + index +
                    "] could not convert to BigDecimal.", e);
        }
    }

    /**
     * Get the BigInteger value associated with an index.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return The value.
     * @throws XJSONException
     *             If the key is not found or if the value cannot be converted
     *             to a BigInteger.
     */
    public BigInteger getBigInteger (int index) throws XJSONException {
        Object object = this.get(index);
        try {
            return new BigInteger(object.toString());
        } catch (Exception e) {
            throw new XJSONException("XJSONArray[" + index +
                    "] could not convert to BigInteger.", e);
        }
    }

    /**
     * Get the int value associated with an index.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return The value.
     * @throws XJSONException
     *             If the key is not found or if the value is not a number.
     */
    public int getInt(int index) throws XJSONException {
        Object object = this.get(index);
        try {
            return object instanceof Number ? ((Number) object).intValue()
                    : Integer.parseInt((String) object);
        } catch (Exception e) {
            throw new XJSONException("XJSONArray[" + index + "] is not a number.", e);
        }
    }

    /**
     * Get the XJSONArray associated with an index.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return A XJSONArray value.
     * @throws XJSONException
     *             If there is no value for the index. or if the value is not a
     *             XJSONArray
     */
    public XJSONArray getXJSONArray(int index) throws XJSONException {
        Object object = this.get(index);
        if (object instanceof XJSONArray) {
            return (XJSONArray) object;
        }
        throw new XJSONException("XJSONArray[" + index + "] is not a XJSONArray.");
    }

    /**
     * Get the XJSONObject associated with an index.
     *
     * @param index
     *            subscript
     * @return A XJSONObject value.
     * @throws XJSONException
     *             If there is no value for the index or if the value is not a
     *             XJSONObject
     */
    public XJSONObject getXJSONObject(int index) throws XJSONException {
        Object object = this.get(index);
        if (object instanceof XJSONObject) {
            return (XJSONObject) object;
        }
        throw new XJSONException("XJSONArray[" + index + "] is not a XJSONObject.");
    }

    /**
     * Get the long value associated with an index.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return The value.
     * @throws XJSONException
     *             If the key is not found or if the value cannot be converted
     *             to a number.
     */
    public long getLong(int index) throws XJSONException {
        Object object = this.get(index);
        try {
            return object instanceof Number ? ((Number) object).longValue()
                    : Long.parseLong((String) object);
        } catch (Exception e) {
            throw new XJSONException("XJSONArray[" + index + "] is not a number.", e);
        }
    }

    /**
     * Get the string associated with an index.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return A string value.
     * @throws XJSONException
     *             If there is no string value for the index.
     */
    public String getString(int index) throws XJSONException {
        Object object = this.get(index);
        if (object instanceof String) {
            return (String) object;
        }
        throw new XJSONException("XJSONArray[" + index + "] not a string.");
    }

    /**
     * Determine if the value is <code>null</code>.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return true if the value at the index is <code>null</code>, or if there is no value.
     */
    public boolean isNull(int index) {
        return XJSONObject.NULL.equals(this.opt(index));
    }

    /**
     * Get the number of elements in the XJSONArray, included nulls.
     *
     * @return The length (or size).
     */
    public int length() {
        return this.myArrayList.size();
    }

    /**
     * Get the optional object value associated with an index.
     *
     * @param index
     *            The index must be between 0 and length() - 1. If not, null is returned.
     * @return An object value, or null if there is no object at that index.
     */
    public Object opt(int index) {
        return (index < 0 || index >= this.length()) ? null : this.myArrayList
                .get(index);
    }

    /**
     * Get the optional boolean value associated with an index. It returns false
     * if there is no value at that index, or if the value is not Boolean.TRUE
     * or the String "true".
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return The truth.
     */
    public boolean optBoolean(int index) {
        return this.optBoolean(index, false);
    }

    /**
     * Get the optional boolean value associated with an index. It returns the
     * defaultValue if there is no value at that index or if it is not a Boolean
     * or the String "true" or "false" (case insensitive).
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @param defaultValue
     *            A boolean default.
     * @return The truth.
     */
    public boolean optBoolean(int index, boolean defaultValue) {
        try {
            return this.getBoolean(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional double value associated with an index. NaN is returned
     * if there is no value for the index, or if the value is not a number and
     * cannot be converted to a number.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return The value.
     */
    public double optDouble(int index) {
        return this.optDouble(index, Double.NaN);
    }

    /**
     * Get the optional double value associated with an index. The defaultValue
     * is returned if there is no value for the index, or if the value is not a
     * number and cannot be converted to a number.
     *
     * @param index
     *            subscript
     * @param defaultValue
     *            The default value.
     * @return The value.
     */
    public double optDouble(int index, double defaultValue) {
        Object val = this.opt(index);
        if (XJSONObject.NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof Number){
            return ((Number) val).doubleValue();
        }
        if (val instanceof String) {
            try {
                return Double.parseDouble((String) val);
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Get the optional float value associated with an index. NaN is returned
     * if there is no value for the index, or if the value is not a number and
     * cannot be converted to a number.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return The value.
     */
    public float optFloat(int index) {
        return this.optFloat(index, Float.NaN);
    }

    /**
     * Get the optional float value associated with an index. The defaultValue
     * is returned if there is no value for the index, or if the value is not a
     * number and cannot be converted to a number.
     *
     * @param index
     *            subscript
     * @param defaultValue
     *            The default value.
     * @return The value.
     */
    public float optFloat(int index, float defaultValue) {
        Object val = this.opt(index);
        if (XJSONObject.NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof Number){
            return ((Number) val).floatValue();
        }
        if (val instanceof String) {
            try {
                return Float.parseFloat((String) val);
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Get the optional int value associated with an index. Zero is returned if
     * there is no value for the index, or if the value is not a number and
     * cannot be converted to a number.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return The value.
     */
    public int optInt(int index) {
        return this.optInt(index, 0);
    }

    /**
     * Get the optional int value associated with an index. The defaultValue is
     * returned if there is no value for the index, or if the value is not a
     * number and cannot be converted to a number.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @param defaultValue
     *            The default value.
     * @return The value.
     */
    public int optInt(int index, int defaultValue) {
        Object val = this.opt(index);
        if (XJSONObject.NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof Number){
            return ((Number) val).intValue();
        }
        
        if (val instanceof String) {
            try {
                return new BigDecimal(val.toString()).intValue();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Get the enum value associated with a key.
     * 
     * @param clazz
     *            The type of enum to retrieve.
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return The enum value at the index location or null if not found
     */
    public <E extends Enum<E>> E optEnum(Class<E> clazz, int index) {
        return this.optEnum(clazz, index, null);
    }

    /**
     * Get the enum value associated with a key.
     * 
     * @param clazz
     *            The type of enum to retrieve.
     * @param index
     *            The index must be between 0 and length() - 1.
     * @param defaultValue
     *            The default in case the value is not found
     * @return The enum value at the index location or defaultValue if
     *            the value is not found or cannot be assigned to clazz
     */
    public <E extends Enum<E>> E optEnum(Class<E> clazz, int index, E defaultValue) {
        try {
            Object val = this.opt(index);
            if (XJSONObject.NULL.equals(val)) {
                return defaultValue;
            }
            if (clazz.isAssignableFrom(val.getClass())) {
                // we just checked it!
                @SuppressWarnings("unchecked")
                E myE = (E) val;
                return myE;
            }
            return Enum.valueOf(clazz, val.toString());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        } catch (NullPointerException e) {
            return defaultValue;
        }
    }


    /**
     * Get the optional BigInteger value associated with an index. The 
     * defaultValue is returned if there is no value for the index, or if the 
     * value is not a number and cannot be converted to a number.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @param defaultValue
     *            The default value.
     * @return The value.
     */
    public BigInteger optBigInteger(int index, BigInteger defaultValue) {
        Object val = this.opt(index);
        if (XJSONObject.NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof BigInteger){
            return (BigInteger) val;
        }
        if (val instanceof BigDecimal){
            return ((BigDecimal) val).toBigInteger();
        }
        if (val instanceof Double || val instanceof Float){
            return new BigDecimal(((Number) val).doubleValue()).toBigInteger();
        }
        if (val instanceof Long || val instanceof Integer
                || val instanceof Short || val instanceof Byte){
            return BigInteger.valueOf(((Number) val).longValue());
        }
        try {
            final String valStr = val.toString();
            if(XJSONObject.isDecimalNotation(valStr)) {
                return new BigDecimal(valStr).toBigInteger();
            }
            return new BigInteger(valStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional BigDecimal value associated with an index. The 
     * defaultValue is returned if there is no value for the index, or if the 
     * value is not a number and cannot be converted to a number.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @param defaultValue
     *            The default value.
     * @return The value.
     */
    public BigDecimal optBigDecimal(int index, BigDecimal defaultValue) {
        Object val = this.opt(index);
        if (XJSONObject.NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof BigDecimal){
            return (BigDecimal) val;
        }
        if (val instanceof BigInteger){
            return new BigDecimal((BigInteger) val);
        }
        if (val instanceof Double || val instanceof Float){
            return new BigDecimal(((Number) val).doubleValue());
        }
        if (val instanceof Long || val instanceof Integer
                || val instanceof Short || val instanceof Byte){
            return new BigDecimal(((Number) val).longValue());
        }
        try {
            return new BigDecimal(val.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional XJSONArray associated with an index.
     *
     * @param index
     *            subscript
     * @return A XJSONArray value, or null if the index has no value, or if the
     *         value is not a XJSONArray.
     */
    public XJSONArray optXJSONArray(int index) {
        Object o = this.opt(index);
        return o instanceof XJSONArray ? (XJSONArray) o : null;
    }

    /**
     * Get the optional XJSONObject associated with an index. Null is returned if
     * the key is not found, or null if the index has no value, or if the value
     * is not a XJSONObject.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return A XJSONObject value.
     */
    public XJSONObject optXJSONObject(int index) {
        Object o = this.opt(index);
        return o instanceof XJSONObject ? (XJSONObject) o : null;
    }

    /**
     * Get the optional long value associated with an index. Zero is returned if
     * there is no value for the index, or if the value is not a number and
     * cannot be converted to a number.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return The value.
     */
    public long optLong(int index) {
        return this.optLong(index, 0);
    }

    /**
     * Get the optional long value associated with an index. The defaultValue is
     * returned if there is no value for the index, or if the value is not a
     * number and cannot be converted to a number.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @param defaultValue
     *            The default value.
     * @return The value.
     */
    public long optLong(int index, long defaultValue) {
        Object val = this.opt(index);
        if (XJSONObject.NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof Number){
            return ((Number) val).longValue();
        }
        
        if (val instanceof String) {
            try {
                return new BigDecimal(val.toString()).longValue();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Get an optional {@link Number} value associated with a key, or <code>null</code>
     * if there is no such key or if the value is not a number. If the value is a string,
     * an attempt will be made to evaluate it as a number ({@link BigDecimal}). This method
     * would be used in cases where type coercion of the number value is unwanted.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return An object which is the value.
     */
    public Number optNumber(int index) {
        return this.optNumber(index, null);
    }

    /**
     * Get an optional {@link Number} value associated with a key, or the default if there
     * is no such key or if the value is not a number. If the value is a string,
     * an attempt will be made to evaluate it as a number ({@link BigDecimal}). This method
     * would be used in cases where type coercion of the number value is unwanted.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @param defaultValue
     *            The default.
     * @return An object which is the value.
     */
    public Number optNumber(int index, Number defaultValue) {
        Object val = this.opt(index);
        if (XJSONObject.NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof Number){
            return (Number) val;
        }
        
        if (val instanceof String) {
            try {
                return XJSONObject.stringToNumber((String) val);
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Get the optional string value associated with an index. It returns an
     * empty string if there is no value at that index. If the value is not a
     * string and is not null, then it is converted to a string.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @return A String value.
     */
    public String optString(int index) {
        return this.optString(index, "");
    }

    /**
     * Get the optional string associated with an index. The defaultValue is
     * returned if the key is not found.
     *
     * @param index
     *            The index must be between 0 and length() - 1.
     * @param defaultValue
     *            The default value.
     * @return A String value.
     */
    public String optString(int index, String defaultValue) {
        Object object = this.opt(index);
        return XJSONObject.NULL.equals(object) ? defaultValue : object
                .toString();
    }

    /**
     * Append a boolean value. This increases the array's length by one.
     *
     * @param value
     *            A boolean value.
     * @return this.
     */
    public XJSONArray put(boolean value) {
        return this.put(value ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Put a value in the XJSONArray, where the value will be a XJSONArray which
     * is produced from a Collection.
     *
     * @param value
     *            A Collection value.
     * @return this.
     * @throws XJSONException
     *            If the value is non-finite number.
     */
    public XJSONArray put(Collection<?> value) {
        return this.put(new XJSONArray(value));
    }

    /**
     * Append a double value. This increases the array's length by one.
     *
     * @param value
     *            A double value.
     * @return this.
     * @throws XJSONException
     *             if the value is not finite.
     */
    public XJSONArray put(double value) throws XJSONException {
        return this.put(Double.valueOf(value));
    }
    
    /**
     * Append a float value. This increases the array's length by one.
     *
     * @param value
     *            A float value.
     * @return this.
     * @throws XJSONException
     *             if the value is not finite.
     */
    public XJSONArray put(float value) throws XJSONException {
        return this.put(Float.valueOf(value));
    }

    /**
     * Append an int value. This increases the array's length by one.
     *
     * @param value
     *            An int value.
     * @return this.
     */
    public XJSONArray put(int value) {
        return this.put(Integer.valueOf(value));
    }

    /**
     * Append an long value. This increases the array's length by one.
     *
     * @param value
     *            A long value.
     * @return this.
     */
    public XJSONArray put(long value) {
        return this.put(Long.valueOf(value));
    }

    /**
     * Put a value in the XJSONArray, where the value will be a XJSONObject which
     * is produced from a Map.
     *
     * @param value
     *            A Map value.
     * @return this.
     * @throws XJSONException
     *            If a value in the map is non-finite number.
     * @throws NullPointerException
     *            If a key in the map is <code>null</code>
     */
    public XJSONArray put(Map<?, ?> value) {
        return this.put(new XJSONObject(value));
    }

    /**
     * Append an object value. This increases the array's length by one.
     *
     * @param value
     *            An object value. The value should be a Boolean, Double,
     *            Integer, XJSONArray, XJSONObject, Long, or String, or the
     *            XJSONObject.NULL object.
     * @return this.
     * @throws XJSONException
     *            If the value is non-finite number.
     */
    public XJSONArray put(Object value) {
        XJSONObject.testValidity(value);
        this.myArrayList.add(value);
        return this;
    }

    /**
     * Put or replace a boolean value in the XJSONArray. If the index is greater
     * than the length of the XJSONArray, then null elements will be added as
     * necessary to pad it out.
     *
     * @param index
     *            The subscript.
     * @param value
     *            A boolean value.
     * @return this.
     * @throws XJSONException
     *             If the index is negative.
     */
    public XJSONArray put(int index, boolean value) throws XJSONException {
        return this.put(index, value ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Put a value in the XJSONArray, where the value will be a XJSONArray which
     * is produced from a Collection.
     *
     * @param index
     *            The subscript.
     * @param value
     *            A Collection value.
     * @return this.
     * @throws XJSONException
     *             If the index is negative or if the value is non-finite.
     */
    public XJSONArray put(int index, Collection<?> value) throws XJSONException {
        return this.put(index, new XJSONArray(value));
    }

    /**
     * Put or replace a double value. If the index is greater than the length of
     * the XJSONArray, then null elements will be added as necessary to pad it
     * out.
     *
     * @param index
     *            The subscript.
     * @param value
     *            A double value.
     * @return this.
     * @throws XJSONException
     *             If the index is negative or if the value is non-finite.
     */
    public XJSONArray put(int index, double value) throws XJSONException {
        return this.put(index, Double.valueOf(value));
    }

    /**
     * Put or replace a float value. If the index is greater than the length of
     * the XJSONArray, then null elements will be added as necessary to pad it
     * out.
     *
     * @param index
     *            The subscript.
     * @param value
     *            A float value.
     * @return this.
     * @throws XJSONException
     *             If the index is negative or if the value is non-finite.
     */
    public XJSONArray put(int index, float value) throws XJSONException {
        return this.put(index, Float.valueOf(value));
    }

    /**
     * Put or replace an int value. If the index is greater than the length of
     * the XJSONArray, then null elements will be added as necessary to pad it
     * out.
     *
     * @param index
     *            The subscript.
     * @param value
     *            An int value.
     * @return this.
     * @throws XJSONException
     *             If the index is negative.
     */
    public XJSONArray put(int index, int value) throws XJSONException {
        return this.put(index, Integer.valueOf(value));
    }

    /**
     * Put or replace a long value. If the index is greater than the length of
     * the XJSONArray, then null elements will be added as necessary to pad it
     * out.
     *
     * @param index
     *            The subscript.
     * @param value
     *            A long value.
     * @return this.
     * @throws XJSONException
     *             If the index is negative.
     */
    public XJSONArray put(int index, long value) throws XJSONException {
        return this.put(index, Long.valueOf(value));
    }

    /**
     * Put a value in the XJSONArray, where the value will be a XJSONObject that
     * is produced from a Map.
     *
     * @param index
     *            The subscript.
     * @param value
     *            The Map value.
     * @return this.
     * @throws XJSONException
     *             If the index is negative or if the the value is an invalid
     *             number.
     * @throws NullPointerException
     *             If a key in the map is <code>null</code>
     */
    public XJSONArray put(int index, Map<?, ?> value) throws XJSONException {
        this.put(index, new XJSONObject(value));
        return this;
    }

    /**
     * Put or replace an object value in the XJSONArray. If the index is greater
     * than the length of the XJSONArray, then null elements will be added as
     * necessary to pad it out.
     *
     * @param index
     *            The subscript.
     * @param value
     *            The value to put into the array. The value should be a
     *            Boolean, Double, Integer, XJSONArray, XJSONObject, Long, or
     *            String, or the XJSONObject.NULL object.
     * @return this.
     * @throws XJSONException
     *             If the index is negative or if the the value is an invalid
     *             number.
     */
    public XJSONArray put(int index, Object value) throws XJSONException {
        if (index < 0) {
            throw new XJSONException("XJSONArray[" + index + "] not found.");
        }
        if (index < this.length()) {
            XJSONObject.testValidity(value);
            this.myArrayList.set(index, value);
            return this;
        }
        if(index == this.length()){
            // simple append
            return this.put(value);
        }
        // if we are inserting past the length, we want to grow the array all at once
        // instead of incrementally.
        this.myArrayList.ensureCapacity(index + 1);
        while (index != this.length()) {
            // we don't need to test validity of NULL objects
            this.myArrayList.add(XJSONObject.NULL);
        }
        return this.put(value);
    }

    /**
     * Remove an index and close the hole.
     *
     * @param index
     *            The index of the element to be removed.
     * @return The value that was associated with the index, or null if there
     *         was no value.
     */
    public Object remove(int index) {
        return index >= 0 && index < this.length()
            ? this.myArrayList.remove(index)
            : null;
    }

    /**
     * Determine if two XJSONArrays are similar.
     * They must contain similar sequences.
     *
     * @param other The other XJSONArray
     * @return true if they are equal
     */
    public boolean similar(Object other) {
        if (!(other instanceof XJSONArray)) {
            return false;
        }
        int len = this.length();
        if (len != ((XJSONArray)other).length()) {
            return false;
        }
        for (int i = 0; i < len; i += 1) {
            Object valueThis = this.myArrayList.get(i);
            Object valueOther = ((XJSONArray)other).myArrayList.get(i);
            if(valueThis == valueOther) {
                continue;
            }
            if(valueThis == null) {
                return false;
            }
            if (valueThis instanceof XJSONObject) {
                if (!((XJSONObject)valueThis).similar(valueOther)) {
                    return false;
                }
            } else if (valueThis instanceof XJSONArray) {
                if (!((XJSONArray)valueThis).similar(valueOther)) {
                    return false;
                }
            } else if (!valueThis.equals(valueOther)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Produce a XJSONObject by combining a XJSONArray of names with the values of
     * this XJSONArray.
     *
     * @param names
     *            A XJSONArray containing a list of key strings. These will be
     *            paired with the values.
     * @return A XJSONObject, or null if there are no names or if this XJSONArray
     *         has no values.
     * @throws XJSONException
     *             If any of the names are null.
     */
    public XJSONObject toXJSONObject(XJSONArray names) throws XJSONException {
        if (names == null || names.length() == 0 || this.length() == 0) {
            return null;
        }
        XJSONObject jo = new XJSONObject(names.length());
        for (int i = 0; i < names.length(); i += 1) {
            jo.put(names.getString(i), this.opt(i));
        }
        return jo;
    }

    /**
     * Make a XJSON text of this XJSONArray. For compactness, no unnecessary
     * whitespace is added. If it is not possible to produce a syntactically
     * correct XJSON text then null will be returned instead. This could occur if
     * the array contains an invalid number.
     * <p><b>
     * Warning: This method assumes that the data structure is acyclical.
     * </b>
     *
     * @return a printable, displayable, transmittable representation of the
     *         array.
     */
    @Override
    public String toString() {
        try {
            return this.toString(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Make a pretty-printed XJSON text of this XJSONArray.
     * 
     * <p>If <code>indentFactor > 0</code> and the {@link XJSONArray} has only
     * one element, then the array will be output on a single line:
     * <pre>{@code [1]}</pre>
     * 
     * <p>If an array has 2 or more elements, then it will be output across
     * multiple lines: <pre>{@code
     * [
     * 1,
     * "value 2",
     * 3
     * ]
     * }</pre>
     * <p><b>
     * Warning: This method assumes that the data structure is acyclical.
     * </b>
     * 
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @return a printable, displayable, transmittable representation of the
     *         object, beginning with <code>[</code>&nbsp;<small>(left
     *         bracket)</small> and ending with <code>]</code>
     *         &nbsp;<small>(right bracket)</small>.
     * @throws XJSONException
     */
    public String toString(int indentFactor) throws XJSONException {
        StringWriter sw = new StringWriter();
        synchronized (sw.getBuffer()) {
            return this.write(sw, indentFactor, 0).toString();
        }
    }

    /**
     * Write the contents of the XJSONArray as XJSON text to a writer. For
     * compactness, no whitespace is added.
     * <p><b>
     * Warning: This method assumes that the data structure is acyclical.
     *</b>
     *
     * @return The writer.
     * @throws XJSONException
     */
    public Writer write(Writer writer) throws XJSONException {
        return this.write(writer, 0, 0);
    }

    /**
     * Write the contents of the XJSONArray as XJSON text to a writer.
     * 
     * <p>If <code>indentFactor > 0</code> and the {@link XJSONArray} has only
     * one element, then the array will be output on a single line:
     * <pre>{@code [1]}</pre>
     * 
     * <p>If an array has 2 or more elements, then it will be output across
     * multiple lines: <pre>{@code
     * [
     * 1,
     * "value 2",
     * 3
     * ]
     * }</pre>
     * <p><b>
     * Warning: This method assumes that the data structure is acyclical.
     * </b>
     *
     * @param writer
     *            Writes the serialized XJSON
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @param indent
     *            The indentation of the top level.
     * @return The writer.
     * @throws XJSONException
     */
    public Writer write(Writer writer, int indentFactor, int indent)
            throws XJSONException {
        try {
            boolean commanate = false;
            int length = this.length();
            writer.write('[');

            if (length == 1) {
                try {
                    XJSONObject.writeValue(writer, this.myArrayList.get(0),
                            indentFactor, indent);
                } catch (Exception e) {
                    throw new XJSONException("Unable to write XJSONArray value at index: 0", e);
                }
            } else if (length != 0) {
                final int newindent = indent + indentFactor;

                for (int i = 0; i < length; i += 1) {
                    if (commanate) {
                        writer.write(',');
                    }
                    if (indentFactor > 0) {
                        writer.write('\n');
                    }
                    XJSONObject.indent(writer, newindent);
                    try {
                        XJSONObject.writeValue(writer, this.myArrayList.get(i),
                                indentFactor, newindent);
                    } catch (Exception e) {
                        throw new XJSONException("Unable to write XJSONArray value at index: " + i, e);
                    }
                    commanate = true;
                }
                if (indentFactor > 0) {
                    writer.write('\n');
                }
                XJSONObject.indent(writer, indent);
            }
            writer.write(']');
            return writer;
        } catch (IOException e) {
            throw new XJSONException(e);
        }
    }

    /**
     * Returns a java.util.List containing all of the elements in this array.
     * If an element in the array is a XJSONArray or XJSONObject it will also
     * be converted.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a java.util.List containing the elements of this array
     */
    public List<Object> toList() {
        List<Object> results = new ArrayList<Object>(this.myArrayList.size());
        for (Object element : this.myArrayList) {
            if (element == null || XJSONObject.NULL.equals(element)) {
                results.add(null);
            } else if (element instanceof XJSONArray) {
                results.add(((XJSONArray) element).toList());
            } else if (element instanceof XJSONObject) {
                results.add(((XJSONObject) element).toMap());
            } else {
                results.add(element);
            }
        }
        return results;
    }
}

/**
 * The XJSONException is thrown by the XJSON.org classes when things are amiss.
 *
 * @author XJSON.org
 * @version 2015-12-09
 */
class XJSONException extends RuntimeException {
    /** Serialization ID */
    private static final long serialVersionUID = 0;

    /**
     * Constructs a XJSONException with an explanatory message.
     *
     * @param message
     *            Detail about the reason for the exception.
     */
    public XJSONException(final String message) {
        super(message);
    }

    /**
     * Constructs a XJSONException with an explanatory message and cause.
     * 
     * @param message
     *            Detail about the reason for the exception.
     * @param cause
     *            The cause.
     */
    public XJSONException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new XJSONException with the specified cause.
     * 
     * @param cause
     *            The cause.
     */
    public XJSONException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }

}


/**
 * A XJSONObject is an unordered collection of name/value pairs. Its external
 * form is a string wrapped in curly braces with colons between the names and
 * values, and commas between the values and names. The internal form is an
 * object having <code>get</code> and <code>opt</code> methods for accessing
 * the values by name, and <code>put</code> methods for adding or replacing
 * values by name. The values can be any of these types: <code>Boolean</code>,
 * <code>XJSONArray</code>, <code>XJSONObject</code>, <code>Number</code>,
 * <code>String</code>, or the <code>XJSONObject.NULL</code> object. A
 * XJSONObject constructor can be used to convert an external form XJSON text
 * into an internal form whose values can be retrieved with the
 * <code>get</code> and <code>opt</code> methods, or to convert values into a
 * XJSON text using the <code>put</code> and <code>toString</code> methods. A
 * <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object, which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you. The opt methods differ from the get methods in that they
 * do not throw. Instead, they return a specified value, such as null.
 * <p>
 * The <code>put</code> methods add or replace values in an object. For
 * example,
 *
 * <pre>
 * myString = new XJSONObject()
 *         .put(&quot;XJSON&quot;, &quot;Hello, World!&quot;).toString();
 * </pre>
 *
 * produces the string <code>{"XJSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * the XJSON syntax rules. The constructors are more forgiving in the texts they
 * will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a
 * quote or single quote, and if they do not contain leading or trailing
 * spaces, and if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>,
 * or <code>null</code>.</li>
 * </ul>
 *
 * @author XJSON.org
 * @version 2016-08-15
 */
class XJSONObject {
    /**
     * XJSONObject.NULL is equivalent to the value that JavaScript calls null,
     * whilst Java's null is equivalent to the value that JavaScript calls
     * undefined.
     */
    private static final class Null {

        /**
         * There is only intended to be a single instance of the NULL object,
         * so the clone method returns itself.
         *
         * @return NULL.
         */
        @Override
        protected final Object clone() {
            return this;
        }

        /**
         * A Null object is equal to the null value and to itself.
         *
         * @param object
         *            An object to test for nullness.
         * @return true if the object parameter is the XJSONObject.NULL object or
         *         null.
         */
        @Override
        public boolean equals(Object object) {
            return object == null || object == this;
        }
        /**
         * A Null object is equal to the null value and to itself.
         *
         * @return always returns 0.
         */
        @Override
        public int hashCode() {
            return 0;
        }

        /**
         * Get the "null" string value.
         *
         * @return The string "null".
         */
        @Override
        public String toString() {
            return "null";
        }
    }

    /**
     * The map where the XJSONObject's properties are kept.
     */
    private final Map<String, Object> map;

    /**
     * It is sometimes more convenient and less ambiguous to have a
     * <code>NULL</code> object than to use Java's <code>null</code> value.
     * <code>XJSONObject.NULL.equals(null)</code> returns <code>true</code>.
     * <code>XJSONObject.NULL.toString()</code> returns <code>"null"</code>.
     */
    public static final Object NULL = new Null();

    /**
     * Construct an empty XJSONObject.
     */
    public XJSONObject() {
        // HashMap is used on purpose to ensure that elements are unordered by 
        // the specification.
        // XJSON tends to be a portable transfer format to allows the container 
        // implementations to rearrange their items for a faster element 
        // retrieval based on associative access.
        // Therefore, an implementation mustn't rely on the order of the item.
        this.map = new HashMap<String, Object>();
    }

    /**
     * Construct a XJSONObject from a XJSONTokener.
     *
     * @param x
     *            A XJSONTokener object containing the source string.
     * @throws XJSONException
     *             If there is a syntax error in the source string or a
     *             duplicated key.
     */
    public XJSONObject(XJSONTokener x) throws XJSONException {
        this();
        char c;
        String key;

        if (x.nextClean() != '{') {
            throw x.syntaxError("A XJSONObject text must begin with '{'");
        }
        for (;;) {
            c = x.nextClean();
            switch (c) {
            case 0:
                throw x.syntaxError("A XJSONObject text must end with '}'");
            case '}':
                return;
            default:
                x.back();
                key = x.nextValue().toString();
            }

            // The key is followed by ':'.

            c = x.nextClean();
            if (c != ':') {
                throw x.syntaxError("Expected a ':' after a key");
            }
            
            // Use syntaxError(..) to include error location
            
            if (key != null) {
                // Check if key exists
                if (this.opt(key) != null) {
                    // key already exists
                    throw x.syntaxError("Duplicate key \"" + key + "\"");
                }
                // Only add value if non-null
                Object value = x.nextValue();
                if (value!=null) {
                    this.put(key, value);
                }
            }

            // Pairs are separated by ','.

            switch (x.nextClean()) {
            case ';':
            case ',':
                if (x.nextClean() == '}') {
                    return;
                }
                x.back();
                break;
            case '}':
                return;
            default:
                throw x.syntaxError("Expected a ',' or '}'");
            }
        }
    }

    /**
     * Construct a XJSONObject from a Map.
     *
     * @param m
     *            A map object that can be used to initialize the contents of
     *            the XJSONObject.
     * @throws XJSONException
     *            If a value in the map is non-finite number.
     * @throws NullPointerException
     *            If a key in the map is <code>null</code>
     */
    public XJSONObject(Map<?, ?> m) {
        if (m == null) {
            this.map = new HashMap<String, Object>();
        } else {
            this.map = new HashMap<String, Object>(m.size());
            for (final Entry<?, ?> e : m.entrySet()) {
                if(e.getKey() == null) {
                    throw new NullPointerException("Null key.");
                }
                final Object value = e.getValue();
                if (value != null) {
                    this.map.put(String.valueOf(e.getKey()), wrap(value));
                }
            }
        }
    }

    /**
     * Construct a XJSONObject from an Object using bean getters. It reflects on
     * all of the public methods of the object. For each of the methods with no
     * parameters and a name starting with <code>"get"</code> or
     * <code>"is"</code> followed by an uppercase letter, the method is invoked,
     * and a key and the value returned from the getter method are put into the
     * new XJSONObject.
     * <p>
     * The key is formed by removing the <code>"get"</code> or <code>"is"</code>
     * prefix. If the second remaining character is not upper case, then the
     * first character is converted to lower case.
     * <p>
     * Methods that are <code>static</code>, return <code>void</code>,
     * have parameters, or are "bridge" methods, are ignored.
     * <p>
     * For example, if an object has a method named <code>"getName"</code>, and
     * if the result of calling <code>object.getName()</code> is
     * <code>"Larry Fine"</code>, then the XJSONObject will contain
     * <code>"name": "Larry Fine"</code>.
     * <p>
     * The {@link XJSONPropertyName} annotation can be used on a bean getter to
     * override key name used in the XJSONObject. For example, using the object
     * above with the <code>getName</code> method, if we annotated it with:
     * <pre>
     * &#64;XJSONPropertyName("FullName")
     * public String getName() { return this.name; }
     * </pre>
     * The resulting XJSON object would contain <code>"FullName": "Larry Fine"</code>
     * <p>
     * Similarly, the {@link XJSONPropertyName} annotation can be used on non-
     * <code>get</code> and <code>is</code> methods. We can also override key
     * name used in the XJSONObject as seen below even though the field would normally
     * be ignored:
     * <pre>
     * &#64;XJSONPropertyName("FullName")
     * public String fullName() { return this.name; }
     * </pre>
     * The resulting XJSON object would contain <code>"FullName": "Larry Fine"</code>
     * <p>
     * The {@link XJSONPropertyIgnore} annotation can be used to force the bean property
     * to not be serialized into XJSON. If both {@link XJSONPropertyIgnore} and
     * {@link XJSONPropertyName} are defined on the same method, a depth comparison is
     * performed and the one closest to the concrete class being serialized is used.
     * If both annotations are at the same level, then the {@link XJSONPropertyIgnore}
     * annotation takes precedent and the field is not serialized.
     * For example, the following declaration would prevent the <code>getName</code>
     * method from being serialized:
     * <pre>
     * &#64;XJSONPropertyName("FullName")
     * &#64;XJSONPropertyIgnore 
     * public String getName() { return this.name; }
     * </pre>
     * <p>
     * 
     * @param bean
     *            An object that has getter methods that should be used to make
     *            a XJSONObject.
     */
    public XJSONObject(Object bean) {
        this();
        this.populateMap(bean);
    }

    /**
     * Construct a XJSONObject from an Object, using reflection to find the
     * public members. The resulting XJSONObject's keys will be the strings from
     * the names array, and the values will be the field values associated with
     * those keys in the object. If a key is not found or not visible, then it
     * will not be copied into the new XJSONObject.
     *
     * @param object
     *            An object that has fields that should be used to make a
     *            XJSONObject.
     * @param names
     *            An array of strings, the names of the fields to be obtained
     *            from the object.
     */
    public XJSONObject(Object object, String names[]) {
        this(names.length);
        Class<?> c = object.getClass();
        for (int i = 0; i < names.length; i += 1) {
            String name = names[i];
            try {
                this.putOpt(name, c.getField(name).get(object));
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Construct a XJSONObject from a source XJSON text string. This is the most
     * commonly used XJSONObject constructor.
     *
     * @param source
     *            A string beginning with <code>{</code>&nbsp;<small>(left
     *            brace)</small> and ending with <code>}</code>
     *            &nbsp;<small>(right brace)</small>.
     * @exception XJSONException
     *                If there is a syntax error in the source string or a
     *                duplicated key.
     */
    public XJSONObject(String source) throws XJSONException {
        this(new XJSONTokener(source));
    }

    /**
     * Construct a XJSONObject from a ResourceBundle.
     *
     * @param baseName
     *            The ResourceBundle base name.
     * @param locale
     *            The Locale to load the ResourceBundle for.
     * @throws XJSONException
     *             If any XJSONExceptions are detected.
     */
    public XJSONObject(String baseName, Locale locale) throws XJSONException {
        this();
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale,
                Thread.currentThread().getContextClassLoader());

// Iterate through the keys in the bundle.

        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key != null) {

// Go through the path, ensuring that there is a nested XJSONObject for each
// segment except the last. Add the value using the last segment's name into
// the deepest nested XJSONObject.

                String[] path = ((String) key).split("\\.");
                int last = path.length - 1;
                XJSONObject target = this;
                for (int i = 0; i < last; i += 1) {
                    String segment = path[i];
                    XJSONObject nextTarget = target.optXJSONObject(segment);
                    if (nextTarget == null) {
                        nextTarget = new XJSONObject();
                        target.put(segment, nextTarget);
                    }
                    target = nextTarget;
                }
                target.put(path[last], bundle.getString((String) key));
            }
        }
    }
    
    /**
     * Constructor to specify an initial capacity of the internal map. Useful for library 
     * internal calls where we know, or at least can best guess, how big this XJSONObject
     * will be.
     * 
     * @param initialCapacity initial capacity of the internal map.
     */
    protected XJSONObject(int initialCapacity){
        this.map = new HashMap<String, Object>(initialCapacity);
    }

    /**
     * Accumulate values under a key. It is similar to the put method except
     * that if there is already an object stored under the key then a XJSONArray
     * is stored under the key to hold all of the accumulated values. If there
     * is already a XJSONArray, then the new value is appended to it. In
     * contrast, the put method replaces the previous value.
     *
     * If only one value is accumulated that is not a XJSONArray, then the result
     * will be the same as using put. But if multiple values are accumulated,
     * then the result will be like append.
     *
     * @param key
     *            A key string.
     * @param value
     *            An object to be accumulated under the key.
     * @return this.
     * @throws XJSONException
     *            If the value is non-finite number.
     * @throws NullPointerException
     *            If the key is <code>null</code>.
     */
    public XJSONObject accumulate(String key, Object value) throws XJSONException {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key,
                    value instanceof XJSONArray ? new XJSONArray().put(value)
                            : value);
        } else if (object instanceof XJSONArray) {
            ((XJSONArray) object).put(value);
        } else {
            this.put(key, new XJSONArray().put(object).put(value));
        }
        return this;
    }

    /**
     * Append values to the array under a key. If the key does not exist in the
     * XJSONObject, then the key is put in the XJSONObject with its value being a
     * XJSONArray containing the value parameter. If the key was already
     * associated with a XJSONArray, then the value parameter is appended to it.
     *
     * @param key
     *            A key string.
     * @param value
     *            An object to be accumulated under the key.
     * @return this.
     * @throws XJSONException
     *            If the value is non-finite number or if the current value associated with
     *             the key is not a XJSONArray.
     * @throws NullPointerException
     *            If the key is <code>null</code>.
     */
    public XJSONObject append(String key, Object value) throws XJSONException {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, new XJSONArray().put(value));
        } else if (object instanceof XJSONArray) {
            this.put(key, ((XJSONArray) object).put(value));
        } else {
            throw new XJSONException("XJSONObject[" + key
                    + "] is not a XJSONArray.");
        }
        return this;
    }

    /**
     * Produce a string from a double. The string "null" will be returned if the
     * number is not finite.
     *
     * @param d
     *            A double.
     * @return A String.
     */
    public static String doubleToString(double d) {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            return "null";
        }

// Shave off trailing zeros and decimal point, if possible.

        String string = Double.toString(d);
        if (string.indexOf('.') > 0 && string.indexOf('e') < 0
                && string.indexOf('E') < 0) {
            while (string.endsWith("0")) {
                string = string.substring(0, string.length() - 1);
            }
            if (string.endsWith(".")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
    }

    /**
     * Get the value object associated with a key.
     *
     * @param key
     *            A key string.
     * @return The object associated with the key.
     * @throws XJSONException
     *             if the key is not found.
     */
    public Object get(String key) throws XJSONException {
        if (key == null) {
            throw new XJSONException("Null key.");
        }
        Object object = this.opt(key);
        if (object == null) {
            throw new XJSONException("XJSONObject[" + quote(key) + "] not found.");
        }
        return object;
    }

    /**
    * Get the enum value associated with a key.
    * 
    * @param clazz
    *           The type of enum to retrieve.
    * @param key
    *           A key string.
    * @return The enum value associated with the key
    * @throws XJSONException
    *             if the key is not found or if the value cannot be converted
    *             to an enum.
    */
    public <E extends Enum<E>> E getEnum(Class<E> clazz, String key) throws XJSONException {
        E val = optEnum(clazz, key);
        if(val==null) {
            // XJSONException should really take a throwable argument.
            // If it did, I would re-implement this with the Enum.valueOf
            // method and place any thrown exception in the XJSONException
            throw new XJSONException("XJSONObject[" + quote(key)
                    + "] is not an enum of type " + quote(clazz.getSimpleName())
                    + ".");
        }
        return val;
    }

    /**
     * Get the boolean value associated with a key.
     *
     * @param key
     *            A key string.
     * @return The truth.
     * @throws XJSONException
     *             if the value is not a Boolean or the String "true" or
     *             "false".
     */
    public boolean getBoolean(String key) throws XJSONException {
        Object object = this.get(key);
        if (object.equals(Boolean.FALSE)
                || (object instanceof String && ((String) object)
                        .equalsIgnoreCase("false"))) {
            return false;
        } else if (object.equals(Boolean.TRUE)
                || (object instanceof String && ((String) object)
                        .equalsIgnoreCase("true"))) {
            return true;
        }
        throw new XJSONException("XJSONObject[" + quote(key)
                + "] is not a Boolean.");
    }

    /**
     * Get the BigInteger value associated with a key.
     *
     * @param key
     *            A key string.
     * @return The numeric value.
     * @throws XJSONException
     *             if the key is not found or if the value cannot 
     *             be converted to BigInteger.
     */
    public BigInteger getBigInteger(String key) throws XJSONException {
        Object object = this.get(key);
        try {
            return new BigInteger(object.toString());
        } catch (Exception e) {
            throw new XJSONException("XJSONObject[" + quote(key)
                    + "] could not be converted to BigInteger.", e);
        }
    }

    /**
     * Get the BigDecimal value associated with a key.
     *
     * @param key
     *            A key string.
     * @return The numeric value.
     * @throws XJSONException
     *             if the key is not found or if the value
     *             cannot be converted to BigDecimal.
     */
    public BigDecimal getBigDecimal(String key) throws XJSONException {
        Object object = this.get(key);
        if (object instanceof BigDecimal) {
            return (BigDecimal)object;
        }
        try {
            return new BigDecimal(object.toString());
        } catch (Exception e) {
            throw new XJSONException("XJSONObject[" + quote(key)
                    + "] could not be converted to BigDecimal.", e);
        }
    }

    /**
     * Get the double value associated with a key.
     *
     * @param key
     *            A key string.
     * @return The numeric value.
     * @throws XJSONException
     *             if the key is not found or if the value is not a Number
     *             object and cannot be converted to a number.
     */
    public double getDouble(String key) throws XJSONException {
        Object object = this.get(key);
        try {
            return object instanceof Number ? ((Number) object).doubleValue()
                    : Double.parseDouble(object.toString());
        } catch (Exception e) {
            throw new XJSONException("XJSONObject[" + quote(key)
                    + "] is not a number.", e);
        }
    }

    /**
     * Get the float value associated with a key.
     *
     * @param key
     *            A key string.
     * @return The numeric value.
     * @throws XJSONException
     *             if the key is not found or if the value is not a Number
     *             object and cannot be converted to a number.
     */
    public float getFloat(String key) throws XJSONException {
        Object object = this.get(key);
        try {
            return object instanceof Number ? ((Number) object).floatValue()
                    : Float.parseFloat(object.toString());
        } catch (Exception e) {
            throw new XJSONException("XJSONObject[" + quote(key)
                    + "] is not a number.", e);
        }
    }

    /**
     * Get the Number value associated with a key.
     *
     * @param key
     *            A key string.
     * @return The numeric value.
     * @throws XJSONException
     *             if the key is not found or if the value is not a Number
     *             object and cannot be converted to a number.
     */
    public Number getNumber(String key) throws XJSONException {
        Object object = this.get(key);
        try {
            if (object instanceof Number) {
                return (Number)object;
            }
            return stringToNumber(object.toString());
        } catch (Exception e) {
            throw new XJSONException("XJSONObject[" + quote(key)
                    + "] is not a number.", e);
        }
    }

    /**
     * Get the int value associated with a key.
     *
     * @param key
     *            A key string.
     * @return The integer value.
     * @throws XJSONException
     *             if the key is not found or if the value cannot be converted
     *             to an integer.
     */
    public int getInt(String key) throws XJSONException {
        Object object = this.get(key);
        try {
            return object instanceof Number ? ((Number) object).intValue()
                    : Integer.parseInt((String) object);
        } catch (Exception e) {
            throw new XJSONException("XJSONObject[" + quote(key)
                    + "] is not an int.", e);
        }
    }

    /**
     * Get the XJSONArray value associated with a key.
     *
     * @param key
     *            A key string.
     * @return A XJSONArray which is the value.
     * @throws XJSONException
     *             if the key is not found or if the value is not a XJSONArray.
     */
    public XJSONArray getXJSONArray(String key) throws XJSONException {
        Object object = this.get(key);
        if (object instanceof XJSONArray) {
            return (XJSONArray) object;
        }
        throw new XJSONException("XJSONObject[" + quote(key)
                + "] is not a XJSONArray.");
    }

    /**
     * Get the XJSONObject value associated with a key.
     *
     * @param key
     *            A key string.
     * @return A XJSONObject which is the value.
     * @throws XJSONException
     *             if the key is not found or if the value is not a XJSONObject.
     */
    public XJSONObject getXJSONObject(String key) throws XJSONException {
        Object object = this.get(key);
        if (object instanceof XJSONObject) {
            return (XJSONObject) object;
        }
        throw new XJSONException("XJSONObject[" + quote(key)
                + "] is not a XJSONObject.");
    }

    /**
     * Get the long value associated with a key.
     *
     * @param key
     *            A key string.
     * @return The long value.
     * @throws XJSONException
     *             if the key is not found or if the value cannot be converted
     *             to a long.
     */
    public long getLong(String key) throws XJSONException {
        Object object = this.get(key);
        try {
            return object instanceof Number ? ((Number) object).longValue()
                    : Long.parseLong((String) object);
        } catch (Exception e) {
            throw new XJSONException("XJSONObject[" + quote(key)
                    + "] is not a long.", e);
        }
    }

    /**
     * Get an array of field names from a XJSONObject.
     *
     * @return An array of field names, or null if there are no names.
     */
    public static String[] getNames(XJSONObject jo) {
        int length = jo.length();
        if (length == 0) {
            return null;
        }
        return jo.keySet().toArray(new String[length]);
    }

    /**
     * Get an array of field names from an Object.
     *
     * @return An array of field names, or null if there are no names.
     */
    public static String[] getNames(Object object) {
        if (object == null) {
            return null;
        }
        Class<?> klass = object.getClass();
        Field[] fields = klass.getFields();
        int length = fields.length;
        if (length == 0) {
            return null;
        }
        String[] names = new String[length];
        for (int i = 0; i < length; i += 1) {
            names[i] = fields[i].getName();
        }
        return names;
    }

    /**
     * Get the string associated with a key.
     *
     * @param key
     *            A key string.
     * @return A string which is the value.
     * @throws XJSONException
     *             if there is no string value for the key.
     */
    public String getString(String key) throws XJSONException {
        Object object = this.get(key);
        if (object instanceof String) {
            return (String) object;
        }
        throw new XJSONException("XJSONObject[" + quote(key) + "] not a string.");
    }

    /**
     * Determine if the XJSONObject contains a specific key.
     *
     * @param key
     *            A key string.
     * @return true if the key exists in the XJSONObject.
     */
    public boolean has(String key) {
        return this.map.containsKey(key);
    }

    /**
     * Increment a property of a XJSONObject. If there is no such property,
     * create one with a value of 1. If there is such a property, and if it is
     * an Integer, Long, Double, or Float, then add one to it.
     *
     * @param key
     *            A key string.
     * @return this.
     * @throws XJSONException
     *             If there is already a property with this name that is not an
     *             Integer, Long, Double, or Float.
     */
    public XJSONObject increment(String key) throws XJSONException {
        Object value = this.opt(key);
        if (value == null) {
            this.put(key, 1);
        } else if (value instanceof BigInteger) {
            this.put(key, ((BigInteger)value).add(BigInteger.ONE));
        } else if (value instanceof BigDecimal) {
            this.put(key, ((BigDecimal)value).add(BigDecimal.ONE));
        } else if (value instanceof Integer) {
            this.put(key, ((Integer) value).intValue() + 1);
        } else if (value instanceof Long) {
            this.put(key, ((Long) value).longValue() + 1L);
        } else if (value instanceof Double) {
            this.put(key, ((Double) value).doubleValue() + 1.0d);
        } else if (value instanceof Float) {
            this.put(key, ((Float) value).floatValue() + 1.0f);
        } else {
            throw new XJSONException("Unable to increment [" + quote(key) + "].");
        }
        return this;
    }

    /**
     * Determine if the value associated with the key is <code>null</code> or if there is no
     * value.
     *
     * @param key
     *            A key string.
     * @return true if there is no value associated with the key or if the value
     *        is the XJSONObject.NULL object.
     */
    public boolean isNull(String key) {
        return XJSONObject.NULL.equals(this.opt(key));
    }

    /**
     * Get an enumeration of the keys of the XJSONObject. Modifying this key Set will also
     * modify the XJSONObject. Use with caution.
     *
     * @see Set#iterator()
     * 
     * @return An iterator of the keys.
     */
    public Iterator<String> keys() {
        return this.keySet().iterator();
    }

    /**
     * Get a set of keys of the XJSONObject. Modifying this key Set will also modify the
     * XJSONObject. Use with caution.
     *
     * @see Map#keySet()
     *
     * @return A keySet.
     */
    public Set<String> keySet() {
        return this.map.keySet();
    }

    /**
     * Get a set of entries of the XJSONObject. These are raw values and may not
     * match what is returned by the XJSONObject get* and opt* functions. Modifying 
     * the returned EntrySet or the Entry objects contained therein will modify the
     * backing XJSONObject. This does not return a clone or a read-only view.
     * 
     * Use with caution.
     *
     * @see Map#entrySet()
     *
     * @return An Entry Set
     */
    protected Set<Entry<String, Object>> entrySet() {
        return this.map.entrySet();
    }

    /**
     * Get the number of keys stored in the XJSONObject.
     *
     * @return The number of keys in the XJSONObject.
     */
    public int length() {
        return this.map.size();
    }

    /**
     * Produce a XJSONArray containing the names of the elements of this
     * XJSONObject.
     *
     * @return A XJSONArray containing the key strings, or null if the XJSONObject
     *        is empty.
     */
    public XJSONArray names() {
        if(this.map.isEmpty()) {
            return null;
        }
        return new XJSONArray(this.map.keySet());
    }

    /**
     * Produce a string from a Number.
     *
     * @param number
     *            A Number
     * @return A String.
     * @throws XJSONException
     *             If n is a non-finite number.
     */
    public static String numberToString(Number number) throws XJSONException {
        if (number == null) {
            throw new XJSONException("Null pointer");
        }
        testValidity(number);

        // Shave off trailing zeros and decimal point, if possible.

        String string = number.toString();
        if (string.indexOf('.') > 0 && string.indexOf('e') < 0
                && string.indexOf('E') < 0) {
            while (string.endsWith("0")) {
                string = string.substring(0, string.length() - 1);
            }
            if (string.endsWith(".")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
    }

    /**
     * Get an optional value associated with a key.
     *
     * @param key
     *            A key string.
     * @return An object which is the value, or null if there is no value.
     */
    public Object opt(String key) {
        return key == null ? null : this.map.get(key);
    }

    /**
     * Get the enum value associated with a key.
     * 
     * @param clazz
     *            The type of enum to retrieve.
     * @param key
     *            A key string.
     * @return The enum value associated with the key or null if not found
     */
    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key) {
        return this.optEnum(clazz, key, null);
    }

    /**
     * Get the enum value associated with a key.
     * 
     * @param clazz
     *            The type of enum to retrieve.
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default in case the value is not found
     * @return The enum value associated with the key or defaultValue
     *            if the value is not found or cannot be assigned to <code>clazz</code>
     */
    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key, E defaultValue) {
        try {
            Object val = this.opt(key);
            if (NULL.equals(val)) {
                return defaultValue;
            }
            if (clazz.isAssignableFrom(val.getClass())) {
                // we just checked it!
                @SuppressWarnings("unchecked")
                E myE = (E) val;
                return myE;
            }
            return Enum.valueOf(clazz, val.toString());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        } catch (NullPointerException e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional boolean associated with a key. It returns false if there
     * is no such key, or if the value is not Boolean.TRUE or the String "true".
     *
     * @param key
     *            A key string.
     * @return The truth.
     */
    public boolean optBoolean(String key) {
        return this.optBoolean(key, false);
    }

    /**
     * Get an optional boolean associated with a key. It returns the
     * defaultValue if there is no such key, or if it is not a Boolean or the
     * String "true" or "false" (case insensitive).
     *
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return The truth.
     */
    public boolean optBoolean(String key, boolean defaultValue) {
        Object val = this.opt(key);
        if (NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof Boolean){
            return ((Boolean) val).booleanValue();
        }
        try {
            // we'll use the get anyway because it does string conversion.
            return this.getBoolean(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional BigDecimal associated with a key, or the defaultValue if
     * there is no such key or if its value is not a number. If the value is a
     * string, an attempt will be made to evaluate it as a number.
     *
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return An object which is the value.
     */
    public BigDecimal optBigDecimal(String key, BigDecimal defaultValue) {
        Object val = this.opt(key);
        if (NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof BigDecimal){
            return (BigDecimal) val;
        }
        if (val instanceof BigInteger){
            return new BigDecimal((BigInteger) val);
        }
        if (val instanceof Double || val instanceof Float){
            return new BigDecimal(((Number) val).doubleValue());
        }
        if (val instanceof Long || val instanceof Integer
                || val instanceof Short || val instanceof Byte){
            return new BigDecimal(((Number) val).longValue());
        }
        // don't check if it's a string in case of unchecked Number subclasses
        try {
            return new BigDecimal(val.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional BigInteger associated with a key, or the defaultValue if
     * there is no such key or if its value is not a number. If the value is a
     * string, an attempt will be made to evaluate it as a number.
     *
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return An object which is the value.
     */
    public BigInteger optBigInteger(String key, BigInteger defaultValue) {
        Object val = this.opt(key);
        if (NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof BigInteger){
            return (BigInteger) val;
        }
        if (val instanceof BigDecimal){
            return ((BigDecimal) val).toBigInteger();
        }
        if (val instanceof Double || val instanceof Float){
            return new BigDecimal(((Number) val).doubleValue()).toBigInteger();
        }
        if (val instanceof Long || val instanceof Integer
                || val instanceof Short || val instanceof Byte){
            return BigInteger.valueOf(((Number) val).longValue());
        }
        // don't check if it's a string in case of unchecked Number subclasses
        try {
            // the other opt functions handle implicit conversions, i.e. 
            // jo.put("double",1.1d);
            // jo.optInt("double"); -- will return 1, not an error
            // this conversion to BigDecimal then to BigInteger is to maintain
            // that type cast support that may truncate the decimal.
            final String valStr = val.toString();
            if(isDecimalNotation(valStr)) {
                return new BigDecimal(valStr).toBigInteger();
            }
            return new BigInteger(valStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional double associated with a key, or NaN if there is no such
     * key or if its value is not a number. If the value is a string, an attempt
     * will be made to evaluate it as a number.
     *
     * @param key
     *            A string which is the key.
     * @return An object which is the value.
     */
    public double optDouble(String key) {
        return this.optDouble(key, Double.NaN);
    }

    /**
     * Get an optional double associated with a key, or the defaultValue if
     * there is no such key or if its value is not a number. If the value is a
     * string, an attempt will be made to evaluate it as a number.
     *
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return An object which is the value.
     */
    public double optDouble(String key, double defaultValue) {
        Object val = this.opt(key);
        if (NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof Number){
            return ((Number) val).doubleValue();
        }
        if (val instanceof String) {
            try {
                return Double.parseDouble((String) val);
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Get the optional double value associated with an index. NaN is returned
     * if there is no value for the index, or if the value is not a number and
     * cannot be converted to a number.
     *
     * @param key
     *            A key string.
     * @return The value.
     */
    public float optFloat(String key) {
        return this.optFloat(key, Float.NaN);
    }

    /**
     * Get the optional double value associated with an index. The defaultValue
     * is returned if there is no value for the index, or if the value is not a
     * number and cannot be converted to a number.
     *
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default value.
     * @return The value.
     */
    public float optFloat(String key, float defaultValue) {
        Object val = this.opt(key);
        if (XJSONObject.NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof Number){
            return ((Number) val).floatValue();
        }
        if (val instanceof String) {
            try {
                return Float.parseFloat((String) val);
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Get an optional int value associated with a key, or zero if there is no
     * such key or if the value is not a number. If the value is a string, an
     * attempt will be made to evaluate it as a number.
     *
     * @param key
     *            A key string.
     * @return An object which is the value.
     */
    public int optInt(String key) {
        return this.optInt(key, 0);
    }

    /**
     * Get an optional int value associated with a key, or the default if there
     * is no such key or if the value is not a number. If the value is a string,
     * an attempt will be made to evaluate it as a number.
     *
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return An object which is the value.
     */
    public int optInt(String key, int defaultValue) {
        Object val = this.opt(key);
        if (NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof Number){
            return ((Number) val).intValue();
        }
        
        if (val instanceof String) {
            try {
                return new BigDecimal((String) val).intValue();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Get an optional XJSONArray associated with a key. It returns null if there
     * is no such key, or if its value is not a XJSONArray.
     *
     * @param key
     *            A key string.
     * @return A XJSONArray which is the value.
     */
    public XJSONArray optXJSONArray(String key) {
        Object o = this.opt(key);
        return o instanceof XJSONArray ? (XJSONArray) o : null;
    }

    /**
     * Get an optional XJSONObject associated with a key. It returns null if
     * there is no such key, or if its value is not a XJSONObject.
     *
     * @param key
     *            A key string.
     * @return A XJSONObject which is the value.
     */
    public XJSONObject optXJSONObject(String key) {
        Object object = this.opt(key);
        return object instanceof XJSONObject ? (XJSONObject) object : null;
    }

    /**
     * Get an optional long value associated with a key, or zero if there is no
     * such key or if the value is not a number. If the value is a string, an
     * attempt will be made to evaluate it as a number.
     *
     * @param key
     *            A key string.
     * @return An object which is the value.
     */
    public long optLong(String key) {
        return this.optLong(key, 0);
    }

    /**
     * Get an optional long value associated with a key, or the default if there
     * is no such key or if the value is not a number. If the value is a string,
     * an attempt will be made to evaluate it as a number.
     *
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return An object which is the value.
     */
    public long optLong(String key, long defaultValue) {
        Object val = this.opt(key);
        if (NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof Number){
            return ((Number) val).longValue();
        }
        
        if (val instanceof String) {
            try {
                return new BigDecimal((String) val).longValue();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Get an optional {@link Number} value associated with a key, or <code>null</code>
     * if there is no such key or if the value is not a number. If the value is a string,
     * an attempt will be made to evaluate it as a number ({@link BigDecimal}). This method
     * would be used in cases where type coercion of the number value is unwanted.
     *
     * @param key
     *            A key string.
     * @return An object which is the value.
     */
    public Number optNumber(String key) {
        return this.optNumber(key, null);
    }

    /**
     * Get an optional {@link Number} value associated with a key, or the default if there
     * is no such key or if the value is not a number. If the value is a string,
     * an attempt will be made to evaluate it as a number. This method
     * would be used in cases where type coercion of the number value is unwanted.
     *
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return An object which is the value.
     */
    public Number optNumber(String key, Number defaultValue) {
        Object val = this.opt(key);
        if (NULL.equals(val)) {
            return defaultValue;
        }
        if (val instanceof Number){
            return (Number) val;
        }
        
        if (val instanceof String) {
            try {
                return stringToNumber((String) val);
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Get an optional string associated with a key. It returns an empty string
     * if there is no such key. If the value is not a string and is not null,
     * then it is converted to a string.
     *
     * @param key
     *            A key string.
     * @return A string which is the value.
     */
    public String optString(String key) {
        return this.optString(key, "");
    }

    /**
     * Get an optional string associated with a key. It returns the defaultValue
     * if there is no such key.
     *
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return A string which is the value.
     */
    public String optString(String key, String defaultValue) {
        Object object = this.opt(key);
        return NULL.equals(object) ? defaultValue : object.toString();
    }

    /**
     * Populates the internal map of the XJSONObject with the bean properties. The
     * bean can not be recursive.
     *
     * @see XJSONObject#XJSONObject(Object)
     *
     * @param bean
     *            the bean
     */
    private void populateMap(Object bean) {
        Class<?> klass = bean.getClass();

        // If klass is a System class then set includeSuperClass to false.

        boolean includeSuperClass = klass.getClassLoader() != null;

        Method[] methods = includeSuperClass ? klass.getMethods() : klass.getDeclaredMethods();
        for (final Method method : methods) {
            final int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers)
                    && !Modifier.isStatic(modifiers)
                    && method.getParameterTypes().length == 0
                    && !method.isBridge()
                    && method.getReturnType() != Void.TYPE
                    && isValidMethodName(method.getName())) {
                final String key = getKeyNameFromMethod(method);
                if (key != null && !key.isEmpty()) {
                    try {
                        final Object result = method.invoke(bean);
                        if (result != null) {
                            this.map.put(key, wrap(result));
                            // we don't use the result anywhere outside of wrap
                            // if it's a resource we should be sure to close it
                            // after calling toString
                            if (result instanceof Closeable) {
                                try {
                                    ((Closeable) result).close();
                                } catch (IOException ignore) {
                                }
                            }
                        }
                    } catch (IllegalAccessException ignore) {
                    } catch (IllegalArgumentException ignore) {
                    } catch (InvocationTargetException ignore) {
                    }
                }
            }
        }
    }

    private boolean isValidMethodName(String name) {
        return !"getClass".equals(name) && !"getDeclaringClass".equals(name);
    }

    private String getKeyNameFromMethod(Method method) {
        final int ignoreDepth = getAnnotationDepth(method, XJSONPropertyIgnore.class);
        if (ignoreDepth > 0) {
            final int forcedNameDepth = getAnnotationDepth(method, XJSONPropertyName.class);
            if (forcedNameDepth < 0 || ignoreDepth <= forcedNameDepth) {
                // the hierarchy asked to ignore, and the nearest name override
                // was higher or non-existent
                return null;
            }
        }
        XJSONPropertyName annotation = getAnnotation(method, XJSONPropertyName.class);
        if (annotation != null && annotation.value() != null && !annotation.value().isEmpty()) {
            return annotation.value();
        }
        String key;
        final String name = method.getName();
        if (name.startsWith("get") && name.length() > 3) {
            key = name.substring(3);
        } else if (name.startsWith("is") && name.length() > 2) {
            key = name.substring(2);
        } else {
            return null;
        }
        // if the first letter in the key is not uppercase, then skip.
        // This is to maintain backwards compatibility before PR406
        // (https://github.com/stleary/XJSON-java/pull/406/)
        if (Character.isLowerCase(key.charAt(0))) {
            return null;
        }
        if (key.length() == 1) {
            key = key.toLowerCase(Locale.ROOT);
        } else if (!Character.isUpperCase(key.charAt(1))) {
            key = key.substring(0, 1).toLowerCase(Locale.ROOT) + key.substring(1);
        }
        return key;
    }

    /**
     * Searches the class hierarchy to see if the method or it's super
     * implementations and interfaces has the annotation.
     *
     * @param <A>
     *            type of the annotation
     *
     * @param m
     *            method to check
     * @param annotationClass
     *            annotation to look for
     * @return the {@link Annotation} if the annotation exists on the current method
     *         or one of it's super class definitions
     */
    private static <A extends Annotation> A getAnnotation(final Method m, final Class<A> annotationClass) {
        // if we have invalid data the result is null
        if (m == null || annotationClass == null) {
            return null;
        }

        if (m.isAnnotationPresent(annotationClass)) {
            return m.getAnnotation(annotationClass);
        }

        // if we've already reached the Object class, return null;
        Class<?> c = m.getDeclaringClass();
        if (c.getSuperclass() == null) {
            return null;
        }

        // check directly implemented interfaces for the method being checked
        for (Class<?> i : c.getInterfaces()) {
            try {
                Method im = i.getMethod(m.getName(), m.getParameterTypes());
                return getAnnotation(im, annotationClass);
            } catch (final SecurityException ex) {
                continue;
            } catch (final NoSuchMethodException ex) {
                continue;
            }
        }

        try {
            return getAnnotation(
                    c.getSuperclass().getMethod(m.getName(), m.getParameterTypes()),
                    annotationClass);
        } catch (final SecurityException ex) {
            return null;
        } catch (final NoSuchMethodException ex) {
            return null;
        }
    }

    /**
     * Searches the class hierarchy to see if the method or it's super
     * implementations and interfaces has the annotation. Returns the depth of the
     * annotation in the hierarchy.
     *
     * @param m
     *            method to check
     * @param annotationClass
     *            annotation to look for
     * @return Depth of the annotation or -1 if the annotation is not on the method.
     */
    private static int getAnnotationDepth(final Method m, final Class<? extends Annotation> annotationClass) {
        // if we have invalid data the result is -1
        if (m == null || annotationClass == null) {
            return -1;
        }

        if (m.isAnnotationPresent(annotationClass)) {
            return 1;
        }

        // if we've already reached the Object class, return -1;
        Class<?> c = m.getDeclaringClass();
        if (c.getSuperclass() == null) {
            return -1;
        }

        // check directly implemented interfaces for the method being checked
        for (Class<?> i : c.getInterfaces()) {
            try {
                Method im = i.getMethod(m.getName(), m.getParameterTypes());
                int d = getAnnotationDepth(im, annotationClass);
                if (d > 0) {
                    // since the annotation was on the interface, add 1
                    return d + 1;
                }
            } catch (final SecurityException ex) {
                continue;
            } catch (final NoSuchMethodException ex) {
                continue;
            }
        }

        try {
            int d = getAnnotationDepth(
                    c.getSuperclass().getMethod(m.getName(), m.getParameterTypes()),
                    annotationClass);
            if (d > 0) {
                // since the annotation was on the superclass, add 1
                return d + 1;
            }
            return -1;
        } catch (final SecurityException ex) {
            return -1;
        } catch (final NoSuchMethodException ex) {
            return -1;
        }
    }

    /**
     * Put a key/boolean pair in the XJSONObject.
     *
     * @param key
     *            A key string.
     * @param value
     *            A boolean which is the value.
     * @return this.
     * @throws XJSONException
     *            If the value is non-finite number.
     * @throws NullPointerException
     *            If the key is <code>null</code>.
     */
    public XJSONObject put(String key, boolean value) throws XJSONException {
        return this.put(key, value ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Put a key/value pair in the XJSONObject, where the value will be a
     * XJSONArray which is produced from a Collection.
     *
     * @param key
     *            A key string.
     * @param value
     *            A Collection value.
     * @return this.
     * @throws XJSONException
     *            If the value is non-finite number.
     * @throws NullPointerException
     *            If the key is <code>null</code>.
     */
    public XJSONObject put(String key, Collection<?> value) throws XJSONException {
        return this.put(key, new XJSONArray(value));
    }

    /**
     * Put a key/double pair in the XJSONObject.
     *
     * @param key
     *            A key string.
     * @param value
     *            A double which is the value.
     * @return this.
     * @throws XJSONException
     *            If the value is non-finite number.
     * @throws NullPointerException
     *            If the key is <code>null</code>.
     */
    public XJSONObject put(String key, double value) throws XJSONException {
        return this.put(key, Double.valueOf(value));
    }
    
    /**
     * Put a key/float pair in the XJSONObject.
     *
     * @param key
     *            A key string.
     * @param value
     *            A float which is the value.
     * @return this.
     * @throws XJSONException
     *            If the value is non-finite number.
     * @throws NullPointerException
     *            If the key is <code>null</code>.
     */
    public XJSONObject put(String key, float value) throws XJSONException {
        return this.put(key, Float.valueOf(value));
    }

    /**
     * Put a key/int pair in the XJSONObject.
     *
     * @param key
     *            A key string.
     * @param value
     *            An int which is the value.
     * @return this.
     * @throws XJSONException
     *            If the value is non-finite number.
     * @throws NullPointerException
     *            If the key is <code>null</code>.
     */
    public XJSONObject put(String key, int value) throws XJSONException {
        return this.put(key, Integer.valueOf(value));
    }

    /**
     * Put a key/long pair in the XJSONObject.
     *
     * @param key
     *            A key string.
     * @param value
     *            A long which is the value.
     * @return this.
     * @throws XJSONException
     *            If the value is non-finite number.
     * @throws NullPointerException
     *            If the key is <code>null</code>.
     */
    public XJSONObject put(String key, long value) throws XJSONException {
        return this.put(key, Long.valueOf(value));
    }

    /**
     * Put a key/value pair in the XJSONObject, where the value will be a
     * XJSONObject which is produced from a Map.
     *
     * @param key
     *            A key string.
     * @param value
     *            A Map value.
     * @return this.
     * @throws XJSONException
     *            If the value is non-finite number.
     * @throws NullPointerException
     *            If the key is <code>null</code>.
     */
    public XJSONObject put(String key, Map<?, ?> value) throws XJSONException {
        return this.put(key, new XJSONObject(value));
    }

    /**
     * Put a key/value pair in the XJSONObject. If the value is <code>null</code>, then the
     * key will be removed from the XJSONObject if it is present.
     *
     * @param key
     *            A key string.
     * @param value
     *            An object which is the value. It should be of one of these
     *            types: Boolean, Double, Integer, XJSONArray, XJSONObject, Long,
     *            String, or the XJSONObject.NULL object.
     * @return this.
     * @throws XJSONException
     *            If the value is non-finite number.
     * @throws NullPointerException
     *            If the key is <code>null</code>.
     */
    public XJSONObject put(String key, Object value) throws XJSONException {
        if (key == null) {
            throw new NullPointerException("Null key.");
        }
        if (value != null) {
            testValidity(value);
            this.map.put(key, value);
        } else {
            this.remove(key);
        }
        return this;
    }

    /**
     * Put a key/value pair in the XJSONObject, but only if the key and the value
     * are both non-null, and only if there is not already a member with that
     * name.
     *
     * @param key string
     * @param value object
     * @return this.
     * @throws XJSONException
     *             if the key is a duplicate
     */
    public XJSONObject putOnce(String key, Object value) throws XJSONException {
        if (key != null && value != null) {
            if (this.opt(key) != null) {
                throw new XJSONException("Duplicate key \"" + key + "\"");
            }
            return this.put(key, value);
        }
        return this;
    }

    /**
     * Put a key/value pair in the XJSONObject, but only if the key and the value
     * are both non-null.
     *
     * @param key
     *            A key string.
     * @param value
     *            An object which is the value. It should be of one of these
     *            types: Boolean, Double, Integer, XJSONArray, XJSONObject, Long,
     *            String, or the XJSONObject.NULL object.
     * @return this.
     * @throws XJSONException
     *             If the value is a non-finite number.
     */
    public XJSONObject putOpt(String key, Object value) throws XJSONException {
        if (key != null && value != null) {
            return this.put(key, value);
        }
        return this;
    }

    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within </, producing <\/,
     * allowing XJSON text to be delivered in HTML. In XJSON text, a string cannot
     * contain a control character or an unescaped quote or backslash.
     *
     * @param string
     *            A String
     * @return A String correctly formatted for insertion in a XJSON text.
     */
    public static String quote(String string) {
        StringWriter sw = new StringWriter();
        synchronized (sw.getBuffer()) {
            try {
                return quote(string, sw).toString();
            } catch (IOException ignored) {
                // will never happen - we are writing to a string writer
                return "";
            }
        }
    }

    public static Writer quote(String string, Writer w) throws IOException {
        if (string == null || string.length() == 0) {
            w.write("\"\"");
            return w;
        }

        char b;
        char c = 0;
        String hhhh;
        int i;
        int len = string.length();

        w.write('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
            case '\\':
            case '"':
                w.write('\\');
                w.write(c);
                break;
            case '/':
                if (b == '<') {
                    w.write('\\');
                }
                w.write(c);
                break;
            case '\b':
                w.write("\\b");
                break;
            case '\t':
                w.write("\\t");
                break;
            case '\n':
                w.write("\\n");
                break;
            case '\f':
                w.write("\\f");
                break;
            case '\r':
                w.write("\\r");
                break;
            default:
                if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
                        || (c >= '\u2000' && c < '\u2100')) {
                    w.write("\\u");
                    hhhh = Integer.toHexString(c);
                    w.write("0000", 0, 4 - hhhh.length());
                    w.write(hhhh);
                } else {
                    w.write(c);
                }
            }
        }
        w.write('"');
        return w;
    }

    /**
     * Remove a name and its value, if present.
     *
     * @param key
     *            The name to be removed.
     * @return The value that was associated with the name, or null if there was
     *         no value.
     */
    public Object remove(String key) {
        return this.map.remove(key);
    }

    /**
     * Determine if two XJSONObjects are similar.
     * They must contain the same set of names which must be associated with
     * similar values.
     *
     * @param other The other XJSONObject
     * @return true if they are equal
     */
    public boolean similar(Object other) {
        try {
            if (!(other instanceof XJSONObject)) {
                return false;
            }
            if (!this.keySet().equals(((XJSONObject)other).keySet())) {
                return false;
            }
            for (final Entry<String,?> entry : this.entrySet()) {
                String name = entry.getKey();
                Object valueThis = entry.getValue();
                Object valueOther = ((XJSONObject)other).get(name);
                if(valueThis == valueOther) {
                    continue;
                }
                if(valueThis == null) {
                    return false;
                }
                if (valueThis instanceof XJSONObject) {
                    if (!((XJSONObject)valueThis).similar(valueOther)) {
                        return false;
                    }
                } else if (valueThis instanceof XJSONArray) {
                    if (!((XJSONArray)valueThis).similar(valueOther)) {
                        return false;
                    }
                } else if (!valueThis.equals(valueOther)) {
                    return false;
                }
            }
            return true;
        } catch (Throwable exception) {
            return false;
        }
    }
    
    /**
     * Tests if the value should be tried as a decimal. It makes no test if there are actual digits.
     * 
     * @param val value to test
     * @return true if the string is "-0" or if it contains '.', 'e', or 'E', false otherwise.
     */
    protected static boolean isDecimalNotation(final String val) {
        return val.indexOf('.') > -1 || val.indexOf('e') > -1
                || val.indexOf('E') > -1 || "-0".equals(val);
    }
    
    /**
     * Converts a string to a number using the narrowest possible type. Possible 
     * returns for this function are BigDecimal, Double, BigInteger, Long, and Integer.
     * When a Double is returned, it should always be a valid Double and not NaN or +-infinity.
     * 
     * @param val value to convert
     * @return Number representation of the value.
     * @throws NumberFormatException thrown if the value is not a valid number. A public
     *      caller should catch this and wrap it in a {@link XJSONException} if applicable.
     */
    protected static Number stringToNumber(final String val) throws NumberFormatException {
        char initial = val.charAt(0);
        if ((initial >= '0' && initial <= '9') || initial == '-') {
            // decimal representation
            if (isDecimalNotation(val)) {
                // quick dirty way to see if we need a BigDecimal instead of a Double
                // this only handles some cases of overflow or underflow
                if (val.length()>14) {
                    return new BigDecimal(val);
                }
                final Double d = Double.valueOf(val);
                if (d.isInfinite() || d.isNaN()) {
                    // if we can't parse it as a double, go up to BigDecimal
                    // this is probably due to underflow like 4.32e-678
                    // or overflow like 4.65e5324. The size of the string is small
                    // but can't be held in a Double.
                    return new BigDecimal(val);
                }
                return d;
            }
            // integer representation.
            // This will narrow any values to the smallest reasonable Object representation
            // (Integer, Long, or BigInteger)
            
            // string version
            // The compare string length method reduces GC,
            // but leads to smaller integers being placed in larger wrappers even though not
            // needed. i.e. 1,000,000,000 -> Long even though it's an Integer
            // 1,000,000,000,000,000,000 -> BigInteger even though it's a Long
            //if(val.length()<=9){
            //    return Integer.valueOf(val);
            //}
            //if(val.length()<=18){
            //    return Long.valueOf(val);
            //}
            //return new BigInteger(val);
            
            // BigInteger version: We use a similar bitLenth compare as
            // BigInteger#intValueExact uses. Increases GC, but objects hold
            // only what they need. i.e. Less runtime overhead if the value is
            // long lived. Which is the better tradeoff? This is closer to what's
            // in stringToValue.
            BigInteger bi = new BigInteger(val);
            if(bi.bitLength()<=31){
                return Integer.valueOf(bi.intValue());
            }
            if(bi.bitLength()<=63){
                return Long.valueOf(bi.longValue());
            }
            return bi;
        }
        throw new NumberFormatException("val ["+val+"] is not a valid number.");
    }

    /**
     * Try to convert a string into a number, boolean, or null. If the string
     * can't be converted, return the string.
     *
     * @param string
     *            A String.
     * @return A simple XJSON value.
     */
    // Changes to this method must be copied to the corresponding method in
    // the XML class to keep full support for Android
    public static Object stringToValue(String string) {
        if (string.equals("")) {
            return string;
        }
        if (string.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (string.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (string.equalsIgnoreCase("null")) {
            return XJSONObject.NULL;
        }

        /*
         * If it might be a number, try converting it. If a number cannot be
         * produced, then the value will just be a string.
         */

        char initial = string.charAt(0);
        if ((initial >= '0' && initial <= '9') || initial == '-') {
            try {
                // if we want full Big Number support this block can be replaced with:
                // return stringToNumber(string);
                if (isDecimalNotation(string)) {
                    Double d = Double.valueOf(string);
                    if (!d.isInfinite() && !d.isNaN()) {
                        return d;
                    }
                } else {
                    Long myLong = Long.valueOf(string);
                    if (string.equals(myLong.toString())) {
                        if (myLong.longValue() == myLong.intValue()) {
                            return Integer.valueOf(myLong.intValue());
                        }
                        return myLong;
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return string;
    }

    /**
     * Throw an exception if the object is a NaN or infinite number.
     *
     * @param o
     *            The object to test.
     * @throws XJSONException
     *             If o is a non-finite number.
     */
    public static void testValidity(Object o) throws XJSONException {
        if (o != null) {
            if (o instanceof Double) {
                if (((Double) o).isInfinite() || ((Double) o).isNaN()) {
                    throw new XJSONException(
                            "XJSON does not allow non-finite numbers.");
                }
            } else if (o instanceof Float) {
                if (((Float) o).isInfinite() || ((Float) o).isNaN()) {
                    throw new XJSONException(
                            "XJSON does not allow non-finite numbers.");
                }
            }
        }
    }

    /**
     * Produce a XJSONArray containing the values of the members of this
     * XJSONObject.
     *
     * @param names
     *            A XJSONArray containing a list of key strings. This determines
     *            the sequence of the values in the result.
     * @return A XJSONArray of values.
     * @throws XJSONException
     *             If any of the values are non-finite numbers.
     */
    public XJSONArray toXJSONArray(XJSONArray names) throws XJSONException {
        if (names == null || names.length() == 0) {
            return null;
        }
        XJSONArray ja = new XJSONArray();
        for (int i = 0; i < names.length(); i += 1) {
            ja.put(this.opt(names.getString(i)));
        }
        return ja;
    }

    /**
     * Make a XJSON text of this XJSONObject. For compactness, no whitespace is
     * added. If this would not result in a syntactically correct XJSON text,
     * then null will be returned instead.
     * <p><b>
     * Warning: This method assumes that the data structure is acyclical.
     * </b>
     * 
     * @return a printable, displayable, portable, transmittable representation
     *         of the object, beginning with <code>{</code>&nbsp;<small>(left
     *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
     *         brace)</small>.
     */
    @Override
    public String toString() {
        try {
            return this.toString(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Make a pretty-printed XJSON text of this XJSONObject.
     * 
     * <p>If <code>indentFactor > 0</code> and the {@link XJSONObject}
     * has only one key, then the object will be output on a single line:
     * <pre>{@code {"key": 1}}</pre>
     * 
     * <p>If an object has 2 or more keys, then it will be output across
     * multiple lines: <code><pre>{
     *  "key1": 1,
     *  "key2": "value 2",
     *  "key3": 3
     * }</pre></code>
     * <p><b>
     * Warning: This method assumes that the data structure is acyclical.
     * </b>
     *
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @return a printable, displayable, portable, transmittable representation
     *         of the object, beginning with <code>{</code>&nbsp;<small>(left
     *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
     *         brace)</small>.
     * @throws XJSONException
     *             If the object contains an invalid number.
     */
    public String toString(int indentFactor) throws XJSONException {
        StringWriter w = new StringWriter();
        synchronized (w.getBuffer()) {
            return this.write(w, indentFactor, 0).toString();
        }
    }

    /**
     * Wrap an object, if necessary. If the object is <code>null</code>, return the NULL
     * object. If it is an array or collection, wrap it in a XJSONArray. If it is
     * a map, wrap it in a XJSONObject. If it is a standard property (Double,
     * String, et al) then it is already wrapped. Otherwise, if it comes from
     * one of the java packages, turn it into a string. And if it doesn't, try
     * to wrap it in a XJSONObject. If the wrapping fails, then null is returned.
     *
     * @param object
     *            The object to wrap
     * @return The wrapped value
     */
    public static Object wrap(Object object) {
        try {
            if (object == null) {
                return NULL;
            }
            if (object instanceof XJSONObject || object instanceof XJSONArray
                    || NULL.equals(object) || object instanceof XJSONString
                    || object instanceof Byte || object instanceof Character
                    || object instanceof Short || object instanceof Integer
                    || object instanceof Long || object instanceof Boolean
                    || object instanceof Float || object instanceof Double
                    || object instanceof String || object instanceof BigInteger
                    || object instanceof BigDecimal || object instanceof Enum) {
                return object;
            }

            if (object instanceof Collection) {
                Collection<?> coll = (Collection<?>) object;
                return new XJSONArray(coll);
            }
            if (object.getClass().isArray()) {
                return new XJSONArray(object);
            }
            if (object instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) object;
                return new XJSONObject(map);
            }
            Package objectPackage = object.getClass().getPackage();
            String objectPackageName = objectPackage != null ? objectPackage
                    .getName() : "";
            if (objectPackageName.startsWith("java.")
                    || objectPackageName.startsWith("javax.")
                    || object.getClass().getClassLoader() == null) {
                return object.toString();
            }
            return new XJSONObject(object);
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * Write the contents of the XJSONObject as XJSON text to a writer. For
     * compactness, no whitespace is added.
     * <p><b>
     * Warning: This method assumes that the data structure is acyclical.
     * </b>
     * 
     * @return The writer.
     * @throws XJSONException
     */
    public Writer write(Writer writer) throws XJSONException {
        return this.write(writer, 0, 0);
    }

    static final Writer writeValue(Writer writer, Object value,
            int indentFactor, int indent) throws XJSONException, IOException {
        if (value == null || value.equals(null)) {
            writer.write("null");
        } else if (value instanceof XJSONString) {
            Object o;
            try {
                o = ((XJSONString) value).toXJSONString();
            } catch (Exception e) {
                throw new XJSONException(e);
            }
            writer.write(o != null ? o.toString() : quote(value.toString()));
        } else if (value instanceof Number) {
            // not all Numbers may match actual XJSON Numbers. i.e. fractions or Imaginary
            final String numberAsString = numberToString((Number) value);
            try {
                // Use the BigDecimal constructor for its parser to validate the format.
                @SuppressWarnings("unused")
                BigDecimal testNum = new BigDecimal(numberAsString);
                // Close enough to a XJSON number that we will use it unquoted
                writer.write(numberAsString);
            } catch (NumberFormatException ex){
                // The Number value is not a valid XJSON number.
                // Instead we will quote it as a string
                quote(numberAsString, writer);
            }
        } else if (value instanceof Boolean) {
            writer.write(value.toString());
        } else if (value instanceof Enum<?>) {
            writer.write(quote(((Enum<?>)value).name()));
        } else if (value instanceof XJSONObject) {
            ((XJSONObject) value).write(writer, indentFactor, indent);
        } else if (value instanceof XJSONArray) {
            ((XJSONArray) value).write(writer, indentFactor, indent);
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            new XJSONObject(map).write(writer, indentFactor, indent);
        } else if (value instanceof Collection) {
            Collection<?> coll = (Collection<?>) value;
            new XJSONArray(coll).write(writer, indentFactor, indent);
        } else if (value.getClass().isArray()) {
            new XJSONArray(value).write(writer, indentFactor, indent);
        } else {
            quote(value.toString(), writer);
        }
        return writer;
    }

    static final void indent(Writer writer, int indent) throws IOException {
        for (int i = 0; i < indent; i += 1) {
            writer.write(' ');
        }
    }

    /**
     * Write the contents of the XJSONObject as XJSON text to a writer.
     * 
     * <p>If <code>indentFactor > 0</code> and the {@link XJSONObject}
     * has only one key, then the object will be output on a single line:
     * <pre>{@code {"key": 1}}</pre>
     * 
     * <p>If an object has 2 or more keys, then it will be output across
     * multiple lines: <code><pre>{
     *  "key1": 1,
     *  "key2": "value 2",
     *  "key3": 3
     * }</pre></code>
     * <p><b>
     * Warning: This method assumes that the data structure is acyclical.
     * </b>
     *
     * @param writer
     *            Writes the serialized XJSON
     * @param indentFactor
     *            The number of spaces to add to each level of indentation.
     * @param indent
     *            The indentation of the top level.
     * @return The writer.
     * @throws XJSONException
     */
    public Writer write(Writer writer, int indentFactor, int indent)
            throws XJSONException {
        try {
            boolean commanate = false;
            final int length = this.length();
            writer.write('{');

            if (length == 1) {
                final Entry<String,?> entry = this.entrySet().iterator().next();
                final String key = entry.getKey();
                writer.write(quote(key));
                writer.write(':');
                if (indentFactor > 0) {
                    writer.write(' ');
                }
                try{
                    writeValue(writer, entry.getValue(), indentFactor, indent);
                } catch (Exception e) {
                    throw new XJSONException("Unable to write XJSONObject value for key: " + key, e);
                }
            } else if (length != 0) {
                final int newindent = indent + indentFactor;
                for (final Entry<String,?> entry : this.entrySet()) {
                    if (commanate) {
                        writer.write(',');
                    }
                    if (indentFactor > 0) {
                        writer.write('\n');
                    }
                    indent(writer, newindent);
                    final String key = entry.getKey();
                    writer.write(quote(key));
                    writer.write(':');
                    if (indentFactor > 0) {
                        writer.write(' ');
                    }
                    try {
                        writeValue(writer, entry.getValue(), indentFactor, newindent);
                    } catch (Exception e) {
                        throw new XJSONException("Unable to write XJSONObject value for key: " + key, e);
                    }
                    commanate = true;
                }
                if (indentFactor > 0) {
                    writer.write('\n');
                }
                indent(writer, indent);
            }
            writer.write('}');
            return writer;
        } catch (IOException exception) {
            throw new XJSONException(exception);
        }
    }

    /**
     * Returns a java.util.Map containing all of the entries in this object.
     * If an entry in the object is a XJSONArray or XJSONObject it will also
     * be converted.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a java.util.Map containing the entries of this object
     */
    public Map<String, Object> toMap() {
        Map<String, Object> results = new HashMap<String, Object>();
        for (Entry<String, Object> entry : this.entrySet()) {
            Object value;
            if (entry.getValue() == null || NULL.equals(entry.getValue())) {
                value = null;
            } else if (entry.getValue() instanceof XJSONObject) {
                value = ((XJSONObject) entry.getValue()).toMap();
            } else if (entry.getValue() instanceof XJSONArray) {
                value = ((XJSONArray) entry.getValue()).toList();
            } else {
                value = entry.getValue();
            }
            results.put(entry.getKey(), value);
        }
        return results;
    }
}
@Documented
@Retention(RUNTIME)
@Target({METHOD})
/**
 * Use this annotation on a getter method to override the Bean name
 * parser for Bean -&gt; XJSONObject mapping. If this annotation is
 * present at any level in the class hierarchy, then the method will
 * not be serialized from the bean into the XJSONObject.
 */
@interface XJSONPropertyIgnore { }

@Documented
@Retention(RUNTIME)
@Target({METHOD})
/**
 * Use this annotation on a getter method to override the Bean name
 * parser for Bean -&gt; XJSONObject mapping. A value set to empty string <code>""</code>
 * will have the Bean parser fall back to the default field name processing.
 */
@interface XJSONPropertyName {
    /**
     * @return The name of the property as to be used in the XJSON Object.
     */
    String value();
}
/**
 * The <code>XJSONString</code> interface allows a <code>toXJSONString()</code>
 * method so that a class can change the behavior of
 * <code>XJSONObject.toString()</code>, <code>XJSONArray.toString()</code>,
 * and <code>XJSONWriter.value(</code>Object<code>)</code>. The
 * <code>toXJSONString</code> method will be used instead of the default behavior
 * of using the Object's <code>toString()</code> method and quoting the result.
 */
interface XJSONString {
    /**
     * The <code>toXJSONString</code> method allows a class to produce its own XJSON
     * serialization.
     *
     * @return A strictly syntactically correct XJSON text.
     */
    public String toXJSONString();
}


/**
 * A XJSONTokener takes a source string and extracts characters and tokens from
 * it. It is used by the XJSONObject and XJSONArray constructors to parse
 * XJSON source strings.
 * @author XJSON.org
 * @version 2014-05-03
 */
class XJSONTokener {
    /** current read character position on the current line. */
    private long character;
    /** flag to indicate if the end of the input has been found. */
    private boolean eof;
    /** current read index of the input. */
    private long index;
    /** current line of the input. */
    private long line;
    /** previous character read from the input. */
    private char previous;
    /** Reader for the input. */
    private final Reader reader;
    /** flag to indicate that a previous character was requested. */
    private boolean usePrevious;
    /** the number of characters read in the previous line. */
    private long characterPreviousLine;


    /**
     * Construct a XJSONTokener from a Reader. The caller must close the Reader.
     *
     * @param reader     A reader.
     */
    public XJSONTokener(Reader reader) {
        this.reader = reader.markSupported()
                ? reader
                        : new BufferedReader(reader);
        this.eof = false;
        this.usePrevious = false;
        this.previous = 0;
        this.index = 0;
        this.character = 1;
        this.characterPreviousLine = 0;
        this.line = 1;
    }


    /**
     * Construct a XJSONTokener from an InputStream. The caller must close the input stream.
     * @param inputStream The source.
     */
    public XJSONTokener(InputStream inputStream) {
        this(new InputStreamReader(inputStream));
    }


    /**
     * Construct a XJSONTokener from a string.
     *
     * @param s     A source string.
     */
    public XJSONTokener(String s) {
        this(new StringReader(s));
    }


    /**
     * Back up one character. This provides a sort of lookahead capability,
     * so that you can test for a digit or letter before attempting to parse
     * the next number or identifier.
     * @throws XJSONException Thrown if trying to step back more than 1 step
     *  or if already at the start of the string
     */
    public void back() throws XJSONException {
        if (this.usePrevious || this.index <= 0) {
            throw new XJSONException("Stepping back two steps is not supported");
        }
        this.decrementIndexes();
        this.usePrevious = true;
        this.eof = false;
    }

    /**
     * Decrements the indexes for the {@link #back()} method based on the previous character read.
     */
    private void decrementIndexes() {
        this.index--;
        if(this.previous=='\r' || this.previous == '\n') {
            this.line--;
            this.character=this.characterPreviousLine ;
        } else if(this.character > 0){
            this.character--;
        }
    }

    /**
     * Get the hex value of a character (base16).
     * @param c A character between '0' and '9' or between 'A' and 'F' or
     * between 'a' and 'f'.
     * @return  An int between 0 and 15, or -1 if c was not a hex digit.
     */
    public static int dehexchar(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c - ('A' - 10);
        }
        if (c >= 'a' && c <= 'f') {
            return c - ('a' - 10);
        }
        return -1;
    }

    /**
     * Checks if the end of the input has been reached.
     *  
     * @return true if at the end of the file and we didn't step back
     */
    public boolean end() {
        return this.eof && !this.usePrevious;
    }


    /**
     * Determine if the source string still contains characters that next()
     * can consume.
     * @return true if not yet at the end of the source.
     * @throws XJSONException thrown if there is an error stepping forward
     *  or backward while checking for more data.
     */
    public boolean more() throws XJSONException {
        if(this.usePrevious) {
            return true;
        }
        try {
            this.reader.mark(1);
        } catch (IOException e) {
            throw new XJSONException("Unable to preserve stream position", e);
        }
        try {
            // -1 is EOF, but next() can not consume the null character '\0'
            if(this.reader.read() <= 0) {
                this.eof = true;
                return false;
            }
            this.reader.reset();
        } catch (IOException e) {
            throw new XJSONException("Unable to read the next character from the stream", e);
        }
        return true;
    }


    /**
     * Get the next character in the source string.
     *
     * @return The next character, or 0 if past the end of the source string.
     * @throws XJSONException Thrown if there is an error reading the source string.
     */
    public char next() throws XJSONException {
        int c;
        if (this.usePrevious) {
            this.usePrevious = false;
            c = this.previous;
        } else {
            try {
                c = this.reader.read();
            } catch (IOException exception) {
                throw new XJSONException(exception);
            }
        }
        if (c <= 0) { // End of stream
            this.eof = true;
            return 0;
        }
        this.incrementIndexes(c);
        this.previous = (char) c;
        return this.previous;
    }

    /**
     * Increments the internal indexes according to the previous character
     * read and the character passed as the current character.
     * @param c the current character read.
     */
    private void incrementIndexes(int c) {
        if(c > 0) {
            this.index++;
            if(c=='\r') {
                this.line++;
                this.characterPreviousLine = this.character;
                this.character=0;
            }else if (c=='\n') {
                if(this.previous != '\r') {
                    this.line++;
                    this.characterPreviousLine = this.character;
                }
                this.character=0;
            } else {
                this.character++;
            }
        }
    }

    /**
     * Consume the next character, and check that it matches a specified
     * character.
     * @param c The character to match.
     * @return The character.
     * @throws XJSONException if the character does not match.
     */
    public char next(char c) throws XJSONException {
        char n = this.next();
        if (n != c) {
            if(n > 0) {
                throw this.syntaxError("Expected '" + c + "' and instead saw '" +
                        n + "'");
            }
            throw this.syntaxError("Expected '" + c + "' and instead saw ''");
        }
        return n;
    }


    /**
     * Get the next n characters.
     *
     * @param n     The number of characters to take.
     * @return      A string of n characters.
     * @throws XJSONException
     *   Substring bounds error if there are not
     *   n characters remaining in the source string.
     */
    public String next(int n) throws XJSONException {
        if (n == 0) {
            return "";
        }

        char[] chars = new char[n];
        int pos = 0;

        while (pos < n) {
            chars[pos] = this.next();
            if (this.end()) {
                throw this.syntaxError("Substring bounds error");
            }
            pos += 1;
        }
        return new String(chars);
    }


    /**
     * Get the next char in the string, skipping whitespace.
     * @throws XJSONException Thrown if there is an error reading the source string.
     * @return  A character, or 0 if there are no more characters.
     */
    public char nextClean() throws XJSONException {
        for (;;) {
            char c = this.next();
            if (c == 0 || c > ' ') {
                return c;
            }
        }
    }


    /**
     * Return the characters up to the next close quote character.
     * Backslash processing is done. The formal XJSON format does not
     * allow strings in single quotes, but an implementation is allowed to
     * accept them.
     * @param quote The quoting character, either
     *      <code>"</code>&nbsp;<small>(double quote)</small> or
     *      <code>'</code>&nbsp;<small>(single quote)</small>.
     * @return      A String.
     * @throws XJSONException Unterminated string.
     */
    public String nextString(char quote) throws XJSONException {
        char c;
        StringBuilder sb = new StringBuilder();
        for (;;) {
            c = this.next();
            switch (c) {
            case 0:
            case '\n':
            case '\r':
                throw this.syntaxError("Unterminated string");
            case '\\':
                c = this.next();
                switch (c) {
                case 'b':
                    sb.append('\b');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 'u':
                    try {
                        sb.append((char)Integer.parseInt(this.next(4), 16));
                    } catch (NumberFormatException e) {
                        throw this.syntaxError("Illegal escape.", e);
                    }
                    break;
                case '"':
                case '\'':
                case '\\':
                case '/':
                    sb.append(c);
                    break;
                default:
                    throw this.syntaxError("Illegal escape.");
                }
                break;
            default:
                if (c == quote) {
                    return sb.toString();
                }
                sb.append(c);
            }
        }
    }


    /**
     * Get the text up but not including the specified character or the
     * end of line, whichever comes first.
     * @param  delimiter A delimiter character.
     * @return   A string.
     * @throws XJSONException Thrown if there is an error while searching
     *  for the delimiter
     */
    public String nextTo(char delimiter) throws XJSONException {
        StringBuilder sb = new StringBuilder();
        for (;;) {
            char c = this.next();
            if (c == delimiter || c == 0 || c == '\n' || c == '\r') {
                if (c != 0) {
                    this.back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }


    /**
     * Get the text up but not including one of the specified delimiter
     * characters or the end of line, whichever comes first.
     * @param delimiters A set of delimiter characters.
     * @return A string, trimmed.
     * @throws XJSONException Thrown if there is an error while searching
     *  for the delimiter
     */
    public String nextTo(String delimiters) throws XJSONException {
        char c;
        StringBuilder sb = new StringBuilder();
        for (;;) {
            c = this.next();
            if (delimiters.indexOf(c) >= 0 || c == 0 ||
                    c == '\n' || c == '\r') {
                if (c != 0) {
                    this.back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }


    /**
     * Get the next value. The value can be a Boolean, Double, Integer,
     * XJSONArray, XJSONObject, Long, or String, or the XJSONObject.NULL object.
     * @throws XJSONException If syntax error.
     *
     * @return An object.
     */
    public Object nextValue() throws XJSONException {
        char c = this.nextClean();
        String string;

        switch (c) {
        case '"':
        case '\'':
            return this.nextString(c);
        case '{':
            this.back();
            return new XJSONObject(this);
        case '[':
            this.back();
            return new XJSONArray(this);
        }

        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it can be a number. An implementation (such as this one)
         * is allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */

        StringBuilder sb = new StringBuilder();
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            sb.append(c);
            c = this.next();
        }
        this.back();

        string = sb.toString().trim();
        if ("".equals(string)) {
            throw this.syntaxError("Missing value");
        }
        return XJSONObject.stringToValue(string);
    }


    /**
     * Skip characters until the next character is the requested character.
     * If the requested character is not found, no characters are skipped.
     * @param to A character to skip to.
     * @return The requested character, or zero if the requested character
     * is not found.
     * @throws XJSONException Thrown if there is an error while searching
     *  for the to character
     */
    public char skipTo(char to) throws XJSONException {
        char c;
        try {
            long startIndex = this.index;
            long startCharacter = this.character;
            long startLine = this.line;
            this.reader.mark(1000000);
            do {
                c = this.next();
                if (c == 0) {
                    // in some readers, reset() may throw an exception if
                    // the remaining portion of the input is greater than
                    // the mark size (1,000,000 above).
                    this.reader.reset();
                    this.index = startIndex;
                    this.character = startCharacter;
                    this.line = startLine;
                    return 0;
                }
            } while (c != to);
            this.reader.mark(1);
        } catch (IOException exception) {
            throw new XJSONException(exception);
        }
        this.back();
        return c;
    }

    /**
     * Make a XJSONException to signal a syntax error.
     *
     * @param message The error message.
     * @return  A XJSONException object, suitable for throwing
     */
    public XJSONException syntaxError(String message) {
        return new XJSONException(message + this.toString());
    }

    /**
     * Make a XJSONException to signal a syntax error.
     *
     * @param message The error message.
     * @param causedBy The throwable that caused the error.
     * @return  A XJSONException object, suitable for throwing
     */
    public XJSONException syntaxError(String message, Throwable causedBy) {
        return new XJSONException(message + this.toString(), causedBy);
    }

    /**
     * Make a printable string of this XJSONTokener.
     *
     * @return " at {index} [character {character} line {line}]"
     */
    @Override
    public String toString() {
        return " at " + this.index + " [character " + this.character + " line " +
                this.line + "]";
    }
}


public class XJSON {
    public static String testThis(String jsonStr) {
        switch (jsonStr.trim().charAt(0)) {
            case '[':
                return (new XJSONArray(jsonStr)).toString();
            case '{':
                return (new XJSONObject(jsonStr)).toString();
            default:
                throw new XJSONException(jsonStr);
        }
    }
    /*public static void main(String[] args) {
        testThis("[[]]");
        testThis("[1,{}]");
        testThis("[]");
        testThis("[1,2]");
        testThis("{}");
        testThis("{\"a\":1, \"b\":2}");
    }*/
}
