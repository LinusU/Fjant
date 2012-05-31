package com.linusu;

public class Indentation {
	
	protected int level;
	protected StringBuilder sb;
	
	public Indentation () {
		level = 0;
		sb = new StringBuilder();
	}
	
	public int value() {
		return level;
	}
	
	public int increase() {
		sb.append("    ");
		return ++level;
	}
	
	public int decrease() {
		sb.delete(level * 4 - 4, level * 4);
		return --level;
	}
	
	public String toString() {
		return sb.toString();
	}
	
}
