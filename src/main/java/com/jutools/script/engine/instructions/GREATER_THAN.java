package com.jutools.script.engine.instructions;

/**
 * 두 숫자 비교(큼) 명령어 클래스
 * 
 * @author jmsohn
 */
public class GREATER_THAN extends BiNumberInstruction {

	@Override
	public Object process(Double p1, Double p2) throws Exception {
		return p1 > p2;
	}

}
