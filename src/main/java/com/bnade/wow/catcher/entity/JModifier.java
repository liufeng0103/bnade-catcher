package com.bnade.wow.catcher.entity;

public class JModifier {
	private int type;
	private int value;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "JModifier [type=" + type + ", value=" + value + "]";
	}
	
}
