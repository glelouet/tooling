package fr.lelouet.tools.holders.examples.lazycache;

import java.util.Date;

public interface Requested<T> {
	T get();

	Date getExpiry();
}
