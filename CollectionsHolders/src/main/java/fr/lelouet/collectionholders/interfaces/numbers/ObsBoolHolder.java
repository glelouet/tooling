package fr.lelouet.collectionholders.interfaces.numbers;

import fr.lelouet.collectionholders.impl.AObsObjHolder;
import fr.lelouet.collectionholders.interfaces.ObsObjHolder;
import fr.lelouet.collectionholders.interfaces.RWObsObjHolder;
import javafx.beans.value.ObservableBooleanValue;

public interface ObsBoolHolder extends ObsObjHolder<Boolean> {

	public <RWClass extends ObsBoolHolder & RWObsObjHolder<Boolean>> RWClass create();

	public default ObsBoolHolder or(ObsBoolHolder other) {
		return AObsObjHolder.join(this, other, this::create, (a, b) -> a || b);
	}

	public default ObsBoolHolder and(ObsBoolHolder other) {
		return AObsObjHolder.join(this, other, this::create, (a, b) -> a && b);
	}

	public default ObsBoolHolder xor(ObsBoolHolder other) {
		return AObsObjHolder.join(this, other, this::create, (a, b) -> a != b);
	}

	public default ObsBoolHolder or(boolean b) {
		return AObsObjHolder.map(this, this::create, (a) -> a || b);
	}

	public default ObsBoolHolder and(boolean b) {
		return AObsObjHolder.map(this, this::create, (a) -> a && b);
	}

	public default ObsBoolHolder xor(boolean b) {
		return AObsObjHolder.map(this, this::create, (a) -> a != b);
	}

	public ObsBoolHolder not();

	public ObservableBooleanValue asObservableBool();

}
