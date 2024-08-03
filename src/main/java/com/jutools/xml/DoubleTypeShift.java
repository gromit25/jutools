package com.jutools.xml;

import java.util.Map;

/**
 * 
 * 
 * @author jmsohn
 */
class DoubleTypeShift implements TypeShift {

	@Override
	public void setValue(Map<String, Object> map, String name, String value) throws Exception {
		map.put(name, Double.parseDouble(value));
	}
}
