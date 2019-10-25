package fr.lelouet.collectionholders.interfaces.collections;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * holder on an observable list. All calls should be synchronized.
 * <p>
 * an {@link ObsListHolder} is a layer over a {@link ObservableList}. It
 * provides methods to
 * <ul>
 * <li>{@link #followItems(ListChangeListener)} and
 * {@link #unfollowItems(ListChangeListener)} the events in the underlying list.
 * Already processed events are processed again, so existing items will trigger
 * an event</li>
 * <li>{@link #get()} the internal data or {@link #apply(BiConsumer)} a
 * consumer.</li>
 * <li>{@link #waitData() wait} for data to be acquired at least once. Typically
 * a call to {@link #get()} or to {@link #apply(BiConsumer)} results in a call
 * to this method, but {@link #followItems(ListChangeListener)} does not, so a result
 * produced by using follow should be used after a call to this method.</li>
 * </ul>
 * <p>
 *
 */
public interface ObsListHolder<U> extends ObsCollectionHolder<U, List<U>, ListChangeListener<? super U>> {


	/**
	 * wait for at least one element to be added, then apply the consumer to the
	 * (index,elements) couples.
	 *
	 * @param cons
	 */
	public void apply(BiConsumer<Integer, U> cons);

	/**
	 *
	 * @return a cached list containing the reverse order of this one. If the list
	 *         does not already exist, creates a new one whose reverse is this
	 */
	ObsListHolder<U> reverse();

	@Override
	ObsListHolder<U> filter(Predicate<? super U> predicate);

	ObsListHolder<U> concat(@SuppressWarnings("unchecked") ObsListHolder<? extends U>... lists);

	@Override
	<K> ObsListHolder<K> mapItems(Function<U, K> mapper);

}
