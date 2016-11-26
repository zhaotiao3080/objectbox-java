package io.objectbox.query;

import io.objectbox.Box;
import io.objectbox.Property;
import io.objectbox.annotation.apihint.Experimental;
import io.objectbox.annotation.apihint.Internal;
import io.objectbox.model.OrderFlags;

/**
 * Created by Markus on 13.10.2016.
 */
@Experimental
public class QueryBuilder<T> {
    /**
     * Reverts the order from ascending (default) to descending.
     */

    public final static int DESCENDING = OrderFlags.DESCENDING;

    /**
     * Makes upper case letters (e.g. "Z") be sorted before lower case letters (e.g. "a").
     * If not specified, the default is case insensitive for ASCII characters.
     */
    public final static int CASE_SENSITIVE = OrderFlags.CASE_SENSITIVE;

    /**
     * null values will be put last.
     * If not specified, by default null values will be put first.
     */
    public final static int NULLS_LAST = OrderFlags.NULLS_LAST;

    /**
     * null values should be treated equal to zero (scalars only).
     */
    public final static int NULLS_ZERO = OrderFlags.NULLS_ZERO;

    /**
     * For scalars only: changes the comparison to unsigned (default is signed).
     */
    public final static int UNSIGNED = OrderFlags.UNSIGNED;

    private final Box<T> box;

    private long handle;

    private static native long nativeCreate(long storeHandle, String entityName);

    private static native long nativeDestroy(long handle);

    private static native long nativeBuild(long handle);

    private static native void nativeOrder(long handle, int propertyId, int flags);

    // ------------------------------ (Not)Null------------------------------

    private static native void nativeNull(long handle, int propertyId);

    private static native void nativeNotNull(long handle, int propertyId);

    // ------------------------------ Integers ------------------------------

    private static native void nativeEqual(long handle, int propertyId, long value);

    private static native void nativeNotEqual(long handle, int propertyId, long value);

    private static native void nativeLess(long handle, int propertyId, long value);

    private static native void nativeGreater(long handle, int propertyId, long value);

    private static native void nativeBetween(long handle, int propertyId, long value1, long value2);

    private static native void nativeIn(long handle, int propertyId, int[] values);

    private static native void nativeIn(long handle, int propertyId, long[] values);

    // ------------------------------ Strings ------------------------------

    private static native void nativeEqual(long handle, int propertyId, String value);

    private static native void nativeNotEqual(long handle, int propertyId, String value);

    private static native void nativeContains(long handle, int propertyId, String value);

    private static native void nativeStartsWith(long handle, int propertyId, String value);

    private static native void nativeEndsWith(long handle, int propertyId, String value);

    // ------------------------------ FPs ------------------------------
    private static native void nativeLess(long handle, int propertyId, double value);

    private static native void nativeGreater(long handle, int propertyId, double value);


    @Internal
    public QueryBuilder(Box<T> box, long storeHandle, String entityName) {
        this.box = box;

        // This ensures that all properties have been set
        box.getProperties();

        handle = nativeCreate(storeHandle, entityName);
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void close() {
        if (handle != 0) {
            nativeDestroy(handle);
            handle = 0;
        }
    }

    /**
     * Builds the query and closes this QueryBuilder.
     */
    public Query<T> build() {
        if (handle == 0) {
            throw new IllegalStateException("This QueryBuilder has already been closed. Please use a new instance.");
        }
        long queryHandle = nativeBuild(handle);
        Query<T> query = new Query<T>(box, queryHandle);
        close();
        return query;
    }

    /**
     * Specifies given property to be used for sorting.
     * Shorthand for {@link #order(Property, int)} with flags equal to 0.
     *
     * @see #order(Property, int)
     */
    public QueryBuilder<T> order(Property property) {
        return order(property, 0);
    }

    /**
     * Specifies given property in descending order to be used for sorting.
     * Shorthand for {@link #order(Property, int)} with flags equal to {@link #DESCENDING}.
     *
     * @see #order(Property, int)
     */
    public QueryBuilder<T> orderDesc(Property property) {
        return order(property, DESCENDING);
    }

    /**
     * Defines the order with which the results are ordered (default: none).
     * You can chain multiple order conditions. The first applied order condition will be the most relevant.
     * Order conditions applied afterwards are only relevant if the preceding ones resulted in value equality.
     * <p>
     * Example:
     * <p>
     * queryBuilder.order(Name).orderDesc(YearOfBirth);
     * <p>
     * Here, "Name" defines the primary sort order. The secondary sort order "YearOfBirth" is only used to compare
     * entries with the same "Name" values.
     *
     * @param property the property defining the order
     * @param flags    Bit flags that can be combined using the binary OR operator (|). Available flags are
     *                 {@link #DESCENDING}, {@link #CASE_SENSITIVE}, {@link #NULLS_LAST}, {@link #NULLS_ZERO},
     *                 and {@link #UNSIGNED}.
     */
    public QueryBuilder<T> order(Property property, int flags) {
        nativeOrder(handle, property.getId(), flags);
        return this;
    }

    public QueryBuilder<T> isNull(Property property) {
        nativeNull(handle, property.getId());
        return this;
    }

    public QueryBuilder<T> notNull(Property property) {
        nativeNotNull(handle, property.getId());
        return this;
    }

    public QueryBuilder<T> equal(Property property, long value) {
        nativeEqual(handle, property.getId(), value);
        return this;
    }

    public QueryBuilder<T> notEqual(Property property, long value) {
        nativeNotEqual(handle, property.getId(), value);
        return this;
    }

    public QueryBuilder<T> less(Property property, long value) {
        nativeLess(handle, property.getId(), value);
        return this;
    }

    public QueryBuilder<T> greater(Property property, long value) {
        nativeGreater(handle, property.getId(), value);
        return this;
    }

    public QueryBuilder<T> between(Property property, long value1, long value2) {
        nativeBetween(handle, property.getId(), value1, value2);
        return this;
    }

    // FIXME DbException: invalid unordered_map<K, T> key
    public QueryBuilder<T> in(Property property, long[] values) {
        nativeIn(handle, property.getId(), values);
        return this;
    }

    public QueryBuilder<T> in(Property property, int[] values) {
        nativeIn(handle, property.getId(), values);
        return this;
    }

    public QueryBuilder<T> equal(Property property, String value) {
        nativeEqual(handle, property.getId(), value);
        return this;
    }

    public QueryBuilder<T> notEqual(Property property, String value) {
        nativeNotEqual(handle, property.getId(), value);
        return this;
    }

    public QueryBuilder<T> contains(Property property, String value) {
        nativeContains(handle, property.getId(), value);
        return this;
    }

    public QueryBuilder<T> startsWith(Property property, String value) {
        nativeStartsWith(handle, property.getId(), value);
        return this;
    }

    public QueryBuilder<T> endsWith(Property property, String value) {
        nativeEndsWith(handle, property.getId(), value);
        return this;
    }


    public QueryBuilder<T> less(Property property, double value) {
        nativeLess(handle, property.getId(), value);
        return this;
    }

    public QueryBuilder<T> greater(Property property, double value) {
        nativeGreater(handle, property.getId(), value);
        return this;
    }

}
