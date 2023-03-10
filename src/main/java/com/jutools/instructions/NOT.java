package com.jutools.instructions;

import java.util.Map;
import java.util.Stack;

public class NOT extends Instruction {

	@Override
	public void execute(Stack<Object> stack, Map<String, Object> values) throws Exception {
		
		Object p1 = stack.pop();
		
		if(p1 instanceof Boolean == false) {
			throw new Exception("Unexpected type: " + p1.getClass());
		}
		
		stack.push(!((Boolean)p1));
	}

}
