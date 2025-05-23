package com.jutools.script.mathexp.parser;

import com.jutools.script.engine.instructions.DIV;
import com.jutools.script.engine.instructions.Instruction;
import com.jutools.script.engine.instructions.MOD;
import com.jutools.script.engine.instructions.MUL;
import com.jutools.script.parser.AbstractParser;
import com.jutools.script.parser.EndStatusType;
import com.jutools.script.parser.TransferBuilder;
import com.jutools.script.parser.TransferEventHandler;
import com.jutools.script.parser.TreeNode;

/**
 * *,/ 연산 파싱 수행
 * 
 * @author jmsohn
 */
public class TermParser extends AbstractParser<Instruction> {
	
	/** *,/ 연산의 첫번째 파라미터의 tree node */
	private TreeNode<Instruction> p1;
	/** *,/ 연산의 두번째 파라미터의 tree node */
	private TreeNode<Instruction> p2;
	/** *,/ 연산 */
	private Instruction operation;

	/**
	 * 생성자
	 */
	public TermParser() throws Exception {
		super();
	}

	/**
	 * 시작상태 반환
	 */
	@Override
	protected String getStartStatus() {
		return "START";
	}
	
	/**
	 * 초기화 수행
	 */
	@Override
	protected void init() throws Exception {

		// 속성 변수 초기화
		this.p1 = null;
		this.p2 = null;
		this.operation = null;
		
		// 상태 전이 맵 설정
		this.putTransferMap("START", new TransferBuilder()
				.add(" \t", "START")
				.add("\\*\\/\\%", "OPERATION")
				.add("^ \t\\*\\/", "FACTOR_1", -1)
				.build());
		
		this.putTransferMap("FACTOR_1", new TransferBuilder()
				.add(" \t", "FACTOR_1")
				.add("\\*\\/\\%", "OPERATION")
				.add("^ \t\\*\\/", "END", -1)
				.build());
		
		this.putTransferMap("OPERATION", new TransferBuilder()
				.add(" \t", "OPERATION")
				.add("^ \t", "FACTOR_2", -1)
				.build());
		
		this.putTransferMap("FACTOR_2", new TransferBuilder()
				.add(" \t", "FACTOR_2")
				.add("\\*\\/\\%", "OPERATION")
				.add("^ \t\\*\\/\\%", "END", -1)
				.build());
		
		// 종료 상태 추가
		this.putEndStatus("FACTOR_1");
		this.putEndStatus("FACTOR_2");
		this.putEndStatus("END", EndStatusType.IMMEDIATELY_END); // END 상태로 들어오면 Parsing을 중지
	}
	
	/**
	 * *,/의 첫번째 파라미터 상태로 전이시 핸들러 메소드
	 * 
	 * @param event
	 */
	@TransferEventHandler(
			source={"START"},
			target={"FACTOR_1"}
	)
	public void handleP1(Event event) throws Exception {
		FactorParser parser = new FactorParser();
		this.p1 = parser.parse(event.getReader());
	}
	
	/**
	 * *,/,%의 연산자 상태로 전이시 핸들러 메소드
	 * 
	 * @param event
	 */
	@TransferEventHandler(
			source={"START", "FACTOR_1"},
			target={"OPERATION"}
	)
	public void handleOp(Event event) throws Exception {
		
		if(event.getCh() == '*') {
			this.operation = new MUL();
		} else if(event.getCh() == '/') {
			this.operation = new DIV();
		} else if(event.getCh() == '%') {
			this.operation = new MOD();
		} else {
			throw new Exception("Unexpected operation:" + event.getCh());
		}
		
	}
	
	/**
	 * *,/,%의 첫번째 파라미터 상태로 전이시 핸들러 메소드
	 * 
	 * @param event
	 */
	@TransferEventHandler(
			source={"OPERATION"},
			target={"FACTOR_2"}
	)
	public void handleP2(Event event) throws Exception {
		//
		FactorParser parser = new FactorParser();
		this.p2 = parser.parse(event.getReader());
	}
	
	/**
	 * 삼항 이상 연산 처리<br>
	 * <pre>
	 * ex) 2 *    3           *       4
	 *        (FACTOR_2 -> OPERATION)
	 * </pre>
	 *   
	 * @param event
	 */
	@TransferEventHandler(
			source={"FACTOR_2"},
			target={"OPERATION"}
	)
	public void handleNewOp(Event event) throws Exception {
		
		if(this.operation == null) {
			throw new Exception("operation is null");
		}
		
		if(this.p1 == null) {
			throw new Exception("p1 is null");
		}
		
		if(this.p2 == null) {
			throw new Exception("p2 is null");
		}
		
		// 삼항 이상 연산시 현재까지 설정된 설정된 연산은 p1 으로 할당
		// ex) 1 * 2 / 3 일 경우
		//     1 * 2는 새로운 p1 으로 설정되고
		//     3은 p2가 됨
		//     operation은 '/'가 됨
		TreeNode<Instruction> newP1 = new TreeNode<Instruction>(this.operation);
		newP1.addChild(this.p1);
		newP1.addChild(this.p2);

		this.handleOp(event); // this.operation 설정
		this.p1 = newP1;
		this.p2 = null;
	}

	/**
	 * 파싱 종료 처리
	 */
	public void exit() throws Exception {
		
		if(this.operation != null && this.p2 != null) {
			
			// *,/,% 연산이 존재하는 경우
			this.setNodeData(this.operation);
			this.addChild(this.p1);
			this.addChild(this.p2);
			
		} else {
			
			// *,/,% 연산이 존재하지 않는 경우
			this.setNode(this.p1);
		}
	}
}
