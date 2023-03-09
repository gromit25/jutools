package com.jutools.mathexp.instructions;

import java.util.HashMap;
import java.util.Stack;

/**
 * 
 * 
 * @author jmsohn
 */
public class LOAD_VAR extends Instruction {

	@Override
	public void execute(Stack<Object> stack, HashMap<String, Object> values) throws Exception {
		
		Object value = values.get(this.getParam(0));
		Class<?> valueClass = value.getClass();
		
		if(valueClass == int.class || valueClass == Integer.class
			|| valueClass == long.class || valueClass == Long.class
			|| valueClass == float.class || valueClass == Float.class) {
			
			stack.push(((Number)value).doubleValue());
			
		} else {
			stack.push(value);
		}
		
	}

}