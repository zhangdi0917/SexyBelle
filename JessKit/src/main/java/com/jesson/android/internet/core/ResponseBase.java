/**
 * Copyright 2011-2012 Renren Inc. All rights reserved.
 * － Powered by Team Pegasus. －
 */

package com.jesson.android.internet.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import com.jesson.android.internet.core.json.JsonProperty;

public abstract class ResponseBase {

	/**
	 * 
	 * print fields in response bean
	 * 
	 * @param level
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public String toString (int level) {

		StringBuffer sb = new StringBuffer();
		Class<?> c = this.getClass();
		Field[] fields = c.getDeclaredFields();
		StringBuffer prefix = new StringBuffer();
		if (level > 0) {
			for (int i = 0 ; i < level ; i ++ ) {
				prefix.append("     ");
			}
			prefix.append("|----");
		}
		sb.append(prefix + "--------------begin element--------------\r\n");
		for (Field field : fields) {
			
			field.setAccessible(true);
			String key = field.getName();
			if (field.isAnnotationPresent(JsonProperty.class)) {
				JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
				key = jsonProperty.value();
				
			}
			
			sb.append(prefix).append(key).append(" = ");
			try {
				Class<?> type = field.getType();
				Object obj = field.get(this);
				if (obj == null) {
					sb.append("\r\n");
					continue;
				}
				if (type.isArray()) {
					int length = Array.getLength(obj);
					sb.append("[");
					for (int i = 0 ; i < length ; i ++ ) {
						Object item = Array.get(obj, i);
						sb.append(item).append(",");
					}
					if (length > 0) {
						sb.deleteCharAt(sb.length() - 1);
					}
					sb.append("]");
					sb.append("\r\n");
				} else {
					if (List.class.isInstance(obj)) {
						sb.append("\r\n");
						List list = (List) obj;
						Iterator iterator = list.iterator();
						while (iterator.hasNext()) {
							Object o = iterator.next();
							String value = "";
							if (o instanceof ResponseBase) {
								Method toStringMethod = o.getClass().getSuperclass().getDeclaredMethod("toString", int.class);
								value = (String) toStringMethod.invoke(o, level + 1);
							} else {
								value = String.valueOf(o.toString());
							}	
							sb.append(value).append("\r\n");
						}
					} else {
						String value = "";
						if (obj instanceof ResponseBase) {
							sb.append("\r\n");
							Method toStringMethod = obj.getClass().getSuperclass().getDeclaredMethod("toString", int.class);
							value = (String) toStringMethod.invoke(obj, level + 1);
						} else {
							value = String.valueOf(obj);
						}	
						sb.append(value).append("\r\n");
					}
				}
			
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			
		}
		sb.append(prefix + "--------------end element--------------\r\n");
		
		return sb.toString();
	
	}

	public String toString () {
		return toString (0);
	}

}
