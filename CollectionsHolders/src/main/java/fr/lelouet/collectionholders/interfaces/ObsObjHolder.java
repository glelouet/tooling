package fr.lelouet.collectionholders.interfaces;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsSetHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsDoubleHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsFloatHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsLongHolder;

/**
 * holder on a single object.
 * <p>
 * call to listeners should be synchronized in the implementation, especially
 * over the hold data so that no follower can be added while a new data is being
 * set.
 * </p>
 *
 * <p>
 * This structure allow to manipulate future values, whatever be the current
 * state of the value generation.<br />
 * eg if you have a sensor, that retrieve data once every 30s when invoked, you
 * don't want to wait 30s for the sensor to retrieve the first data, especially
 * if you have several of them.<br />
 * Instead, such a sensor should return a ObsObjHolder&lt;value&gt; which you
 * can follow to trigger event.<br />
 *
 * </p>
 *
 * @param <U>
 *          the type that is hold. Specific implementation exist for basic
 *          numbers in the numbers package
 */
public interface ObsObjHolder<U> {

	/** return the internal object once it's retrieved. If this */
	public U get();

	/**
	 *
	 * @param defaultValue
	 *          value to hold until this' data is available
	 * @return a new object holder that contains the default value until it's
	 *         replaced by this' value.
	 */
	public ObsObjHolder<U> or(U defaultValue);

	/**
	 * add a consumer that follows the data inside. if there is already data, the
	 * consumer receives that data before exiting this method
	 *
	 * @param cons
	 *          the consumer that will receive new values.
	 * @param holder
	 *          the holder that ensure the consumer is useful. if omitted or set
	 *          to null, the class is used instead. Once the holder is no more
	 *          weak reachable, the listener will be removed.
	 */
	@SuppressWarnings("unchecked")
	public void follow(Consumer<U> cons, Consumer<Object>... holders);

	/**
	 * add a consumer that follows the data inside. if there is already data, the
	 * consumer receives that data before exiting this method
	 *
	 * @param cons
	 */
	public default void follow(Consumer<U> cons) {
		follow(cons, (Consumer<Object>[]) null);
	}

	default ObsObjHolder<U> peek(Consumer<U> observer) {
		follow(observer, (Consumer<Object>[]) null);
		return this;
	}

	/**
	 * remove a follower added with {@link #follow(Consumer)}
	 *
	 * @param cons
	 */
	public void unfollow(Consumer<U> cons);

	/**
	 * create a new obsObjHolder that mirros the value contained after
	 * transforming it.
	 *
	 * @param <V>
	 *          the type of the value contained
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder.
	 */
	<V> ObsObjHolder<V> map(Function<U, V> mapper);

	/**
	 * create a new object that mirrors the value hold in this, by transforming it
	 * into an int.
	 *
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder
	 */
	ObsIntHolder mapInt(ToIntFunction<U> mapper);

	/**
	 * create a new object that mirrors the value hold in this, by transforming it
	 * into a long.
	 *
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder
	 */
	ObsLongHolder mapLong(ToLongFunction<U> mapper);

	/**
	 * create a new object that mirrors the value hold in this, by transforming it
	 * into a double.
	 *
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder
	 */
	ObsDoubleHolder mapDouble(ToDoubleFunction<U> mapper);

	ObsFloatHolder mapFloat(ToDoubleFunction<U> mapper);

	/**
	 * create a new object that mirrors the value hold in this, by transforming it
	 * into a boolean.
	 *
	 * @param mapper
	 *          the method to transform the value
	 * @return a new holder
	 */
	ObsBoolHolder test(Predicate<U> test);

	/**
	 * create a new observable list that mirrors the value hold in this, after
	 * transforming it into a list
	 *
	 * @param <V>
	 *          the internal type of the list we generate
	 * @param generator
	 *          the function that transforms the value hold, in an iterable over
	 *          the list type.
	 * @return a new list holder.
	 */
	<V> ObsListHolder<V> toList(Function<U, Iterable<V>> generator);

	/**
	 * create a new observable set that mirrors the value hold in this, after
	 * transforming it into a set
	 *
	 * @param <V>
	 *          the internal type of the set we generate
	 * @param generator
	 *          the function that transforms the value hold, in an iterable over
	 *          the set type.
	 * @return a new set holder.
	 */
	<V> ObsSetHolder<V> toSet(Function<U, Iterable<V>> generator);

	/**
	 * unpack the internal value into an internal observable field.
	 * <p>
	 * eg if a holder contains an access, and that access can give an observable
	 * String lastConnect, we can have a obsHolder on the lastConnect value that
	 * is updated when the access or its lastConnect is updated
	 * </p>
	 *
	 * @param <V>
	 * @param unpacker
	 * @return
	 */
	<V> ObsObjHolder<V> unPack(Function<U, ObsObjHolder<V>> unpacker);
}
