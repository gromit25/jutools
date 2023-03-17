package com.jutools.olexp.parser;

import com.jutools.instructions.Instruction;
import com.jutools.parserfw.AbstractParser;
import com.jutools.parserfw.EndStatusType;
import com.jutools.parserfw.TransferBuilder;
import com.jutools.parserfw.TransferEventHandler;

/**
 * 
 * 
 * @author jmsohn
 */
public class FactorParser extends AbstractParser<Instruction> {

	/**
	 * 
	 */
	public FactorParser() throws Exception {
		super();
	}

	@Override
	protected String getStartStatus() {
		return "START";
	}

	/**
	 * 파싱전 초기화 수행
	 */
	@Override
	protected void init() throws Exception {
		
		// 상태 변환 맵 추가
		this.putTransferMap("START", new TransferBuilder()
				.add(" \t", "START")
				.add("0-9\\-", "NUMBER", -1)
				.add("a-zA-Z\\_", "VAR", -1)
				.add("(", "EXPRESSION")
				.add("^ \t0-9\\-a-zA-Z\\_(", "ERROR")
				.build());
		
		this.putTransferMap("EXPRESSION", new TransferBuilder()
				.add("^)", "COMPARISON", -1)  //TODO 계속 변경해야 함
				.add(")", "ERROR")
				.build());
		
		this.putTransferMap("COMPARISON", new TransferBuilder()  //TODO
				.add(" \t", "COMPARISON")
				.add(")", "END")
				.build());
		
		// 종료 상태 추가
		this.putEndStatus("NUMBER", EndStatusType.IMMEDIATELY_END);
		this.putEndStatus("VAR", EndStatusType.IMMEDIATELY_END);
		this.putEndStatus("END", EndStatusType.IMMEDIATELY_END); // END 상태로 들어오면 Parsing을 중지
		this.putEndStatus("ERROR", EndStatusType.ERROR);
	}

	@TransferEventHandler(
			source={"START"},
			target={"NUMBER"}
	)
	public void handleNumber(Event event) throws Exception {
		NumberParser parser = new NumberParser();
		this.setNode(parser.parse(event.getReader()));
	}
	
	@TransferEventHandler(
			source={"START"},
			target={"VAR"}
	)
	public void handleVar(Event event) throws Exception {
		VarParser parser = new VarParser();
		this.setNode(parser.parse(event.getReader()));
	}
	
	@TransferEventHandler(
			source={"EXPRESSION"},
			target={"COMPARISON"}
	)
	public void handleExp(Event event) throws Exception {
		ComparisonParser parser = new ComparisonParser();
		this.setNode(parser.parse(event.getReader()));
	}

}
