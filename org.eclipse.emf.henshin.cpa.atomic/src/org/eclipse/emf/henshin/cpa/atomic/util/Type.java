package org.eclipse.emf.henshin.cpa.atomic.util;

public enum Type {
	conflicts("conflict"), dependencies("dependency");
	
	private String singular;

	public String getSingularName() {
		return singular;
	}

	Type(String singular) {
		this.singular = singular;
	}
}
