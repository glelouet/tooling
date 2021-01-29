package fr.lelouet.collectionholders.impl;

/**
 * Object holders that throw an exception when set to null. Used for numbers.
 *
 * @param <U>
 */
public class NotNullObsObjHolderImpl<U> extends ObsObjHolderSimple<U> {

	public NotNullObsObjHolderImpl() {
	}

	public NotNullObsObjHolderImpl(U u) {
		super(u);
	}

	@Override
	public synchronized void set(U newitem) {
		if(newitem==null) {
			throw new UnsupportedOperationException("null item forbidden");
		}
		super.set(newitem);
	}

}
