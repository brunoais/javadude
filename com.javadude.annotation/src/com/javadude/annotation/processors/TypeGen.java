// CODE GENERATED BY JAVADUDE BEAN ANNOTATION PROCESSOR
// -- DO NOT EDIT  -  THIS CODE WILL BE REGENERATED! --
package com.javadude.annotation.processors;

import java.util.Map;

public abstract class TypeGen extends BasePushable  {
	public TypeGen() {
		;
	}

	private java.lang.String name_;
	public java.lang.String getName() {
		return name_;
	}
	public void setName(java.lang.String value)  {
		name_ = value;
	}

	private final java.util.List<com.javadude.annotation.processors.Method> methods_ = new java.util.ArrayList<com.javadude.annotation.processors.Method>();
	public com.javadude.annotation.processors.Method getMethod(int i) {
		return methods_.get(i);
	}
	public java.util.List<com.javadude.annotation.processors.Method> getMethods() {
		return methods_;
	}
	public boolean methodsContains(com.javadude.annotation.processors.Method value) {
		return methods_.contains(value);
	}
	public void addMethod(com.javadude.annotation.processors.Method value) {
		if (value == null) throw new IllegalArgumentException("Cannot add null to method");
		methods_.add(value);
	}
	public void addMethod(int i, com.javadude.annotation.processors.Method value) {
		if (value == null) throw new IllegalArgumentException("Cannot add null to method");
		methods_.add(i, value);
	}
	public void clearMethods() {
		methods_.clear();
	}

	@Override
	public java.lang.String toString() {
		return getClass().getName() + '[' + paramString() + ']';
	}
	protected java.lang.String paramString() {
		return
		"name=" + name_ +
		",methods=" + methods_;
	}
	@Override
	public java.util.Map<java.lang.String, java.lang.Object> createPropertyMap() {
		Map<String, Object> map = super.createPropertyMap();
		map.put("name", getName());
		map.put("methods", getMethods());
		return map;
	}
}
