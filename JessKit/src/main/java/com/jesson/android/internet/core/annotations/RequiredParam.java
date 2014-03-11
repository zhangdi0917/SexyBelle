/**
 * Copyright 2011-2012 Renren Inc. All rights reserved.
 * － Powered by Team Pegasus. －
 */

package com.jesson.android.internet.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jesson.android.internet.core.RequestBase;

/**
 * 
 * Annotation used for REST request. All Requests inherit from
 * {@link RequestBase} MUST add Annotation (either this or {@link OptionalParam}
 * ) to their declared fields that should be send to the REST server.
 * 
 * Fields that annotated as RequiredParam will be send to the REST server with
 * NULL check. {@link RRException} will be thrown if required field is NULL at
 * runtime.
 * 
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface RequiredParam {
	String value() default "";
}
