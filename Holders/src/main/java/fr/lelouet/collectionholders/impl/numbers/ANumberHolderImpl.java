package fr.lelouet.collectionholders.impl.numbers;

import java.util.function.BiPredicate;

import fr.lelouet.collectionholders.impl.NotNullObjHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObjHolder;
import fr.lelouet.collectionholders.interfaces.numbers.BoolHolder;
import fr.lelouet.collectionholders.interfaces.numbers.NumberHolder;

public abstract class ANumberHolderImpl<Contained extends Number, Self extends NumberHolder<Contained, Self>>
extends NotNullObjHolderImpl<Contained> implements NumberHolder<Contained, Self> {

	public ANumberHolderImpl() {
	}

	public ANumberHolderImpl(Contained value) {
		super(value);
	}

	@Override
	public BoolHolder test(BiPredicate<Contained, Contained> test, Self b) {
		return ObjHolder.combine(this, b, BoolHolderImpl::new, (u, v) -> test.test(u, v));
	}
}
