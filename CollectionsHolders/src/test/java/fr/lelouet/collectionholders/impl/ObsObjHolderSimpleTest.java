package fr.lelouet.collectionholders.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.impl.numbers.ObsIntHolderImpl;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsListHolder;
import fr.lelouet.collectionholders.interfaces.collections.ObsMapHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsFloatHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsIntHolder;
import fr.lelouet.collectionholders.interfaces.numbers.ObsLongHolder;
import fr.lelouet.tools.lambdaref.GCManage;

public class ObsObjHolderSimpleTest {

	/**
	 * test when a follower is added, then no more reachable. The calls to
	 * {@link GCManage.#force()} allow to force the garbage collector to be
	 * triggered on a (internal) weak reference. The expected result is that the
	 * follower will be removed on next change of the data, while still keeping a
	 * follower that is still reachable(the length variable)
	 */
	@Test(timeOut = 500)
	public void testMemoryClean() {
		ObsObjHolderSimple<String> test = new ObsObjHolderSimple<>("a");
		addWeakReference(test);
		GCManage.force();
		test.set("c");
		Assert.assertEquals(test.followers(), 0);
		addWeakReference(test);
		ObsIntHolder length = test.mapInt(String::length);
		GCManage.force();
		test.set("aaa");
		Assert.assertEquals(test.followers(), 1);
		Assert.assertEquals(length.get(), (Integer) 3);
	}

	/**
	 * add a listener that is not referenced out of the call. This listener should
	 * not remain after enough data are allocated and the garbage collector is
	 * called.
	 *
	 * @param test
	 */
	protected void addWeakReference(ObsObjHolderSimple<String> test) {
		Assert.assertEquals(test.followers(), 0);
		for (int i = 1; i <= 1; i++) {
			ObsIntHolder length = test.mapInt(String::length);
			Assert.assertEquals(length.get(), (Integer) 1);
			Assert.assertEquals(test.followers(), i);
		}
	}

	/**
	 * test with chained weak references
	 */
	@Test(timeOut = 500, dependsOnMethods = "testMemoryClean")
	public void testChainRef() {
		ObsIntHolderImpl source = new ObsIntHolderImpl(2);
		// source times 2^4 = 16
		ObsIntHolder times16 = source.mult(2).mult(2).mult(2).mult(2);

		// first simple state check
		Assert.assertEquals(times16.get(), (Integer) 32);
		GCManage.force();
		source.set(3);
		Assert.assertEquals(times16.get(), (Integer) 48);

		// then add a weak reference and check if it is removed
		addWeakReference((ObsObjHolderSimple<String>) source.map(i -> "" + i));
		Assert.assertEquals(source.followers(), 2);
		GCManage.force();
		source.set(4);
		Assert.assertEquals(times16.get(), (Integer) 64);
		Assert.assertEquals(source.followers(), 1);

		// Same but with a block variable instead of a method variable.
		{
			source.mult(2);
		}
		Assert.assertEquals(source.followers(), 2);
		GCManage.force();
		source.set(5);
		Assert.assertEquals(times16.get(), (Integer) 80);
		Assert.assertEquals(source.followers(), 1);
	}

	@Test(timeOut = 500)
	public void testFollowNoref() {
		String[] hold = new String[1];
		ObsObjHolderSimple<String> test = new ObsObjHolderSimple<>();
		test.map(s -> s == null ? "" : s + s).follow(s -> hold[0] = s);
		Assert.assertEquals(hold[0], null);
		test.set("a");
		Assert.assertEquals(hold[0], "aa");
		GCManage.force();
		test.set("b");
		Assert.assertEquals(hold[0], "bb");
	}

	@Test(timeOut = 500)
	public void testMaps() {
		ObsObjHolderSimple<String> test = new ObsObjHolderSimple<>("hello");

		ObsFloatHolder averageFloat = test.mapFloat(s -> s.chars().mapToDouble(i -> (double) i).average().getAsDouble());
		ObsLongHolder totalLong = test.mapLong(s -> s.chars().mapToLong(i -> (long) i).sum());
		ObsListHolder<Character> listChars = test
				.mapList(s -> s.chars().mapToObj(i -> (char) i).collect(Collectors.toList()));
		ObsMapHolder<Character, Integer> map = test
				.mapMap(
						s -> s.chars().boxed().collect(Collectors.toMap(i -> (Character) (char) (int) i, i -> 1, Integer::sum)));
		Assert.assertEquals(averageFloat.get(), (Float) 106.4f);
		Assert.assertEquals(totalLong.get(), (Long) 532l);
		Assert.assertEquals(listChars.get(), Arrays.asList('h', 'e', 'l', 'l', 'o'));
		Map<Character, Integer> mapped = map.get();
		Assert.assertEquals(mapped.get('h'), (Integer) 1);
		Assert.assertEquals(mapped.get('l'), (Integer) 2);
		Assert.assertEquals(mapped.get('z'), null);
	}

	@Test(timeOut = 500)
	public void testWhenNull() {
		ObsObjHolderSimple<String> test = new ObsObjHolderSimple<>("hello");
		ObsObjHolder<String> notNull = test.when(s -> s == null, s -> "", s -> s);
		Assert.assertEquals(notNull.get(), "hello");
		test.set(null);
		Assert.assertEquals(notNull.get(), "");
	}

	@Test(timeOut = 500)
	public void testUnpack() {
		ObsIntHolderImpl h1 = new ObsIntHolderImpl(1);
		ObsIntHolderImpl h2 = new ObsIntHolderImpl(20);
		ObsObjHolderSimple<ObsIntHolderImpl> test = new ObsObjHolderSimple<>(h1);
		ObsObjHolder<Integer> res = test.unPack(h -> h);
		Assert.assertEquals(res.get(), (Integer) 1);
		test.set(h2);
		Assert.assertEquals(res.get(), (Integer) 20);
		h1.set(5);
		Assert.assertEquals(res.get(), (Integer) 20);
		h2.set(40);
		Assert.assertEquals(res.get(), (Integer) 40);
	}

}
