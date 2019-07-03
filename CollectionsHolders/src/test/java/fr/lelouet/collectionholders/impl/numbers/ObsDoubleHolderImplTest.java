package fr.lelouet.collectionholders.impl.numbers;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.collectionholders.interfaces.numbers.ObsBoolHolder;
import javafx.beans.property.SimpleObjectProperty;

public class ObsDoubleHolderImplTest {

	@Test
	public void testCreation() {
		ObsDoubleHolderImpl twenty = new ObsDoubleHolderImpl(new SimpleObjectProperty<>(20.0));
		Assert.assertEquals(twenty.add(4.0).get(), 24.0);
		Assert.assertEquals(twenty.sub(4.0).get(), 16.0);
		Assert.assertEquals(twenty.mult(4.0).get(), 80.0);
		Assert.assertEquals(twenty.div(4.0).get(), 5.0);

		Assert.assertEquals(twenty.get(), 20.0);

		ObsDoubleHolderImpl four = new ObsDoubleHolderImpl(new SimpleObjectProperty<>(4.0));
		Assert.assertEquals(twenty.add(four).get(), 24.0);
		Assert.assertEquals(twenty.sub(four).get(), 16.0);
		Assert.assertEquals(twenty.mult(four).get(), 80.0);
		Assert.assertEquals(twenty.div(four).get(), 5.0);

		// test predicate
		ObsBoolHolder even = twenty.test(i -> i % 2 == 0);
		ObsBoolHolder odd = even.not();

		Assert.assertTrue(even.get());
		Assert.assertFalse(odd.get());

	}

	@Test
	public void testCeilFloor() {
		ObsDoubleHolderImpl pi = new ObsDoubleHolderImpl(new SimpleObjectProperty<>(3.14));
		Assert.assertEquals((int) pi.ceil().get(), 4);
		Assert.assertEquals((int) pi.floor().get(), 3);
		ObsDoubleHolderImpl negpi = new ObsDoubleHolderImpl(new SimpleObjectProperty<>(-3.14));
		Assert.assertEquals((int) negpi.ceil().get(), -3);
		Assert.assertEquals((int) negpi.floor().get(), -4);
	}

}