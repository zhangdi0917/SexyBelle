package com.jesson.android.internet.core.impl;

import android.content.Context;

import com.jesson.android.internet.core.BeanRequestInterface;

public class BeanRequestFactory {

	private static BeanRequestInterface gBeanRequestInterface;

	public synchronized static BeanRequestInterface createBeanRequestInterface(Context context) {
		if (gBeanRequestInterface == null) {
			gBeanRequestInterface = BeanRequestDefaultImplInternal.getInstance(context);
		}

		return gBeanRequestInterface;
	}

	public synchronized static void setgBeanRequestInterfaceImpl(BeanRequestInterface impl) {
		gBeanRequestInterface = impl;
	}
}
