/*******************************************************************************
 * Copyright (c) 2008 Scott Stanchfield
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.javadude.listenerlist.test;

public class ExceptionWrapper1 extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public ExceptionWrapper1(Throwable cause) {
		super(cause);
	}
}
