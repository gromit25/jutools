package com.jutools;

import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jutools.script.parser.ExpReader;
import com.jutools.script.parser.TreeNode;
import com.jutools.script.parser.exception.ParseException;

import lombok.Getter;

/**
 * 숫자 한글 표기 클래스
 * 
 * @author jmsohn
 */
public class KoreanNumExp {
	
	/** 숫자 표현 패턴 문자열 */
	private static String NUM_PATTERN = "(?<sign>\\-)?(?<intpart>[0-9]+)(\\.(?<decimalpart>[0-9]+))?";
	
	/** 숫자 표현 패턴 */
	private static Pattern numP = Pattern.compile(NUM_PATTERN);
	
	/** 음수 한글 표현 */
	private static String MINUS_KOREAN_EXP = "마이너스 ";
	
	/** 소수점 한글 표현 */ 
	private static String DECIMAL_POINT_KOREAN_EXP = "점";
	
	/**
	 * 한글 숫자 표현(0~9)
	 * 
	 * @author jmsohn
	 */
	private enum KoreanNum {
		
		영(0L), // 소수점 이하 표현시 사용
		일(1L),
		이(2L),
		삼(3L),
		사(4L),  
		오(5L),
		육(6L),
		칠(7L),
		팔(8L),
		구(9L);
		
		//--------------
		
		/** 한글 숫자에 대응되는 실제 숫자 */
		@Getter
		private long n;
		
		/**
		 * 생성자
		 * 
		 * @param n 숫자
		 */
		KoreanNum(long n) {
			this.n = n;
		}
		
		/**
		 * 한글 표현에 따른 한글 숫자 표현 반환
		 * 
		 * @param keyCh 한글 표현
		 * @return 한글 숫자 표현
		 */
		static KoreanNum get(char keyCh) throws Exception {
			
			String key = new String(new char[] {keyCh});
			for(KoreanNum value: KoreanNum.values()) {
				
				if(key.equals(value.name()) == true) {
					return value;
				}
			}
			
			return null;
		}
		
		/**
		 * 주어진 숫자에 따른 한글 숫자 표현 반환
		 *  
		 * @param num 주어진 숫자
		 * @return 한글 숫자 표현
		 */
		static KoreanNum get(long num) throws Exception {
			
			for(KoreanNum value: KoreanNum.values()) {
				
				if(value.n == num) {
					return value;
				}
			}
			
			return null;
		}
	}
	
	/**
	 * 천이하 단위 표현
	 * 
	 * @author jmsohn
	 */
	private enum KoreanThousandUnit {
		
		십(10L),
		백(100L),
		천(1000L);
		
		//--------------
		
		@Getter
		long factor;
		
		/**
		 * 생성자 
		 * 
		 * @param factor
		 */
		KoreanThousandUnit(long factor) {
			this.factor = factor;
		}
		
		/**
		 * 한글 표현에 따른 천이하 단위 표현을 반환
		 * 
		 * @param keyCh 한글 표현
		 * @return 천이하 단위 표현
		 */
		static KoreanThousandUnit get(char keyCh) throws Exception {
			
			//
			String key = new String(new char[] {keyCh});
			for(KoreanThousandUnit value: KoreanThousandUnit.values()) {
				
				if(key.equals(value.name()) == true) {
					return value;
				}
			}
			
			return null;
		}
	}
	
	/**
	 * 만단위 표현
	 * 
	 * @author jmsohn
	 */
	private enum KoreanTenThousandUnit {
		
		만(10000L),
		억(10000L*10000L),
		조(10000L*10000L*10000L),
		경(10000L*10000L*10000L*10000L); // long 값의 최대 범위까지
		
		//--------------
		
		@Getter
		long factor;
		
		/**
		 * 생성자
		 * 
		 * @param factor
		 */
		KoreanTenThousandUnit(long factor) {
			this.factor = factor;
		}
		
		/**
		 * 한글 표현에 따른 만단위 표현 반환
		 * 
		 * @param keyCh 한글 표현
		 * @return 만단위 표현
		 */
		static KoreanTenThousandUnit get(char keyCh) throws Exception {
			
			//
			String key = new String(new char[] {keyCh});
			for(KoreanTenThousandUnit value: KoreanTenThousandUnit.values()) {
				
				if(key.equals(value.name()) == true) {
					return value;
				}
			}
			
			return null;
		}
	}
	
	/**
	 * 숫자 위치(pos)의 숫자(n)의 천단위 한글 표현을 반환<br>
	 * 숫자 위치는 0부터 시작임<br>
	 * 예) 123의 1의 pos는 2, 3의 pos 는 0임
	 * 
	 * @param pos 숫자 자리 위치
	 * @param ch 특정 위치의 숫자의 문자
	 * @return 변환된 한글 표현
	 */
	private static String makeKoreanExpByThousand(int pos, char ch) throws Exception {
		
		// 입력값 검증
		if(pos < 0 || pos > 20) {
			throw new Exception("pos must be between 0 to 20:" + pos);
		}
		
		// 만일 숫자가 '0' 이면, 빈문자열을 반환
		if(ch == '0') {
			return ""; 
		}
		
		// 특정 위치의 숫자의 문자를 숫자로 변환
		int n = ch - '0';
		if(n < 0 || n > 9) {
			throw new Exception("ch must be between '0' to '9':" + ch);
		}
		
		// 한글 표현 변수
		StringBuilder koreanExp = new StringBuilder("");
		
		// 만 이하 단위의 위치
		int unitPos = pos % 4;
		
		// 숫자의 한글 표현을 추가
		// 단, 천,백,십에는 일(1)을 추가하지 않음
		// -> 일천(X), 천(O)
		if(n != 1 || unitPos == 0) {
			koreanExp.append((KoreanNum.values()[n]).name());
		}
		
		// 만 이하 단위 표현을 가져옴
		if(unitPos != 0) {
			koreanExp.append((KoreanThousandUnit.values()[unitPos - 1]).name());
		}
		
		return koreanExp.toString();
	}
	
	/**
	 * 숫자를 한글 표현으로 변환하여 출력<br>
	 * ex) n: 56789 -> "오만육천칠백팔십구"<br>
	 *     n: 1500010 -> "백오십만십"
	 * 
	 * @param n 한글 표현으로 변환할 숫자 
	 * @return 주어진 숫자의 한글 표현
	 */
	public static String toKorean(long n) throws Exception {
		
		// 값이 0일 경우 "영" 반환
		if(n == 0L) {
			return KoreanNum.영.name();
		}
		
		// 한글 표현식을 담을 변수
		StringBuilder koreanExp = new StringBuilder("");
		
		// 음수 부호 읽기 추가
		if(n < 0) {
			koreanExp.append(MINUS_KOREAN_EXP);
		}
		
		// 양수로 변환(abs), 음수라면 sign이 -1 이 되기 때문에 음수 * -1 이 되어 양수가 됨
		// 한글로 변환하기 위함
		n = MathUtil.sign(n) * n;
		
		// 문자열로 변환하여 오른쪽부터 숫자 위치에 따라 변환함
		// ex) "123" 일 경우, 첫번째 '1'과 위치 2을 "일백" 으로 변환 
		String nStr = Long.toString(n);
		int length = nStr.length();
		
		for(int pos = 0; pos < length; pos++) {
			
			// 숫자 자리 위치, ex) 123 의 1은 2번째 자리임
			int digitPos = length - 1 - pos;
			
			// 천단위로 변환 추가
			koreanExp.append(makeKoreanExpByThousand(digitPos , nStr.charAt(pos)));
			
			// 만단위 표현 추가
			if(digitPos % 4 == 0) {
				
				int tenThousandUnitPos = digitPos / 4;
				if(tenThousandUnitPos != 0) {
					koreanExp
						.append((KoreanTenThousandUnit.values()[tenThousandUnitPos - 1]).name());
				}
			}
		} // End of for pos
		
		return koreanExp.toString();
	}
	
	/**
	 * 숫자를 한글 표현으로 변환하여 출력<br>
	 * ex) n: 56789 -> "오만육천칠백팔십구"<br>
	 *     n: 1500010 -> "백오십만십"
	 * 
	 * @param n 한글 표현으로 변환할 숫자 
	 * @return 주어진 숫자의 한글 표현
	 */
	public static String toKorean(int n) throws Exception {
		return toKorean((long)n);
	}
	
	/**
	 * 숫자 문자열을 한글 표현 문자열로 변환<br>
	 * ex) "123.456" -> "백이십삼점사오륙"
	 *     "-0.12" -> "마이너스 영점일이"
	 * 
	 * @param numExpStr 숫자 문자열(ex 123.456)
	 * @return 한글 표현 문자열 
	 */
	public static String toKorean(String numExpStr) throws Exception {
		
		// 입력값 검증
		Matcher numM = numP.matcher(numExpStr);
		if(numM.matches() == false) {
			throw new Exception("num expression is not valid:" + numExpStr);
		}
		
		// 주어진 숫자의 한글 표현 변수
		StringBuilder koreanExpStr = new StringBuilder("");
		
		// 부호 획득 및 처리
		String signStr = numM.group("sign");
		if(signStr != null) {
			koreanExpStr.append(MINUS_KOREAN_EXP);
		}
		
		// 주어진 숫자의 정수부 획득 및 파싱
		long numIntPart = Long.parseLong(numM.group("intpart"));
		koreanExpStr.append(toKorean(numIntPart));
		
		// 주어진 숫자의 소수부 획득
		// 없을 경우 스킵
		String numDecimalPartStr = numM.group("decimalpart");
		if(numDecimalPartStr != null) {
			
			koreanExpStr.append(DECIMAL_POINT_KOREAN_EXP);
			
			for(int index = 0; index < numDecimalPartStr.length(); index++) {
				
				long num = numDecimalPartStr.charAt(index) - '0';
				koreanExpStr.append(KoreanNum.get(num).name());
			}
		}
		
		// 최종 결과 반환
		return koreanExpStr.toString();
	}
	
	//---------------------------
	
	/**
	 * 주어진 문자열을 숫자로 변환
	 * ex) n: "오만육천칠백팔십구", space: "" -> 56789<br>
	 *     n: "백오십만 십", space: " " -> 1500010 
	 * 
	 * @param koreanExp 변환할 숫자 한글 표현
	 * @return 변환된 숫자
	 */
	public static long toNumber(String koreanExp) throws Exception {
		
		// 입력값 검증
		if(StringUtil.isBlank(koreanExp) == true) {
			throw new Exception("koreanExp is blank.");
		}
		
		// 문자열 양쪽의 공백 제거
		koreanExp = koreanExp.trim();
		
		// 부호 변수
		int sign = 1;
		if(koreanExp.startsWith(MINUS_KOREAN_EXP) == true) {
			sign = -1;
			// 부호 문자열 삭제
			koreanExp = koreanExp.substring(MINUS_KOREAN_EXP.length(), koreanExp.length());
		}
		
		// 변환할 숫자 한글 표현에서 파싱 트리를 생성
		TreeNode<AbstractNode> root = parseKoreanExp(koreanExp);
		
		// 파싱 트리의 내용을 가져와 실행
		List<AbstractNode> nodes =  root.travelPostOrder();
		Stack<Long> stack = new Stack<>();
		
		for(AbstractNode node: nodes) {
			node.execute(stack);
		}
		
		// 파싱 트리 실행 결과 반환
		return stack.pop() * sign;
	}
	
	/**
	 * 
	 * 
	 * @param koreanExp
	 * @return
	 */
	public static String toNumberExp(String koreanExp) throws Exception {
		
		if(StringUtil.isBlank(koreanExp) == true) {
			throw new Exception("korean expression is null or blank.");
		}
		
		String[] splitedKoreanExp = koreanExp.split(DECIMAL_POINT_KOREAN_EXP);
		if(splitedKoreanExp.length != 1 && splitedKoreanExp.length != 2) {
			throw new Exception("korean expression is invalid:" + koreanExp);
		}
		
		//
		StringBuilder numberExp = new StringBuilder("");
		
		// 정수부 파싱 및 추가
		numberExp.append(toNumber(splitedKoreanExp[0]));
		
		// 소수부 추가
		// 있을 경우에만
		if(splitedKoreanExp.length == 2 && splitedKoreanExp[1].length() != 0) {
			
			numberExp.append(".");
			
			String decimalPart = splitedKoreanExp[1];
			for(int index = 0; index < decimalPart.length(); index++) {
				
				KoreanNum decimalNum = KoreanNum.get(decimalPart.charAt(index));
				numberExp.append(Long.toString(decimalNum.getN()));
			}
		}
		
		return numberExp.toString();
	}
	
	//---------------------------
	/* 문자열을 숫자로 변환하기 위해 파싱 트리를 사용 */
	/* 파싱 트리 관련 클래스 선언               */
	
	/**
	 * 파싱 트리의 추상 연산자 노드 
	 * 
	 * @author jmsohn
	 */
	static abstract class AbstractNode {
		abstract void execute(Stack<Long> stack);
	}

	/**
	 * 설정된 값(value)를 스택으로 로딩하는 연산자 클래스
	 * 
	 * @author jmsohn
	 */
	static class LOAD extends AbstractNode {
		
		/** 스택에 로드할 값 */
		private long value;
		
		/**
		 * 생성자
		 * 
		 * @param value
		 */
		LOAD(long value) {
			this.value = value;
		}
		
		@Override
		void execute(Stack<Long> stack) {
			// 스택에 로드
			stack.push(this.value);
		}
		
		@Override
		public String toString() {
			return "LOAD " + this.value;
		}
	}
	
	/**
	 * 스택의 두 값을 더하여 결과를 스택에 넣는 연산자 클래스 
	 * 
	 * @author jmsohn
	 */
	static class ADD extends AbstractNode {

		@Override
		void execute(Stack<Long> stack) {
			
			// 스택에서 두값을 인출함
			long p1 = stack.pop();
			long p2 = stack.pop();
			
			// 인출한 두값을 더하여 다시 스택에 넣음
			stack.push(p1 + p2);
		}
		
		@Override
		public String toString() {
			return "ADD";
		}
	}
	
	/**
	 * 스택의 두 값을 곱하여 결과를 스택에 넣는 연산자 클래스 
	 * 
	 * @author jmsohn
	 */
	static class MUL extends AbstractNode {

		@Override
		void execute(Stack<Long> stack) {
			
			// 스택에서 두값을 인출함
			long p1 = stack.pop();
			long p2 = stack.pop();
			
			// 인출한 두값을 곱하여 다시 스택에 넣음
			stack.push(p1 * p2);
		}
		
		@Override
		public String toString() {
			return "MUL";
		}
	}
	
	/**
	 * 숫자 한글 표현식을 숫자로 변환
	 * 
	 * @param koreanExp 숫자로 변환할 숫자 한글 표현식
	 * @return 변환된 숫자
	 */
	private static TreeNode<AbstractNode> parseKoreanExp(String koreanExp) throws Exception {
		
		// 숫자 한글 표현식의 Reader 객체 생성
		ExpReader reader = new ExpReader(koreanExp);
		
		// 만단위 파싱 트리 생성
		// -> "십만천" 이면, "십만"을 먼저 파싱함
		TreeNode<AbstractNode> root = parseTenThousand(reader);

		// 다음 만단위 트리 파싱
		// -> "십만천" = "십만" + "천" 이기 때문에
		//    이전에 root 노드는 "십만"의 root 노드 임
		//    따라서, "천"에 대한 파싱 트리를 생성 후 
		//    기존 root 노드에 "천"의 파싱 트리를 더하면 됨
		int read = -1;
		while((read = reader.read()) != -1) {
			
			reader.unread(read);
			
			TreeNode<AbstractNode> add = new TreeNode<>(new ADD());
			
			add.addChild(root);
			add.addChild(parseTenThousand(reader));
			
			root = add;
		}
		
		// 최종 파싱 트리를 반환
		return root;
	}
	
	/**
	 * 만단위 파싱 트리 생성
	 * 
	 * @param reader 파싱할 문자열 Reader
	 * @return 파싱 트리
	 */
	private static TreeNode<AbstractNode> parseTenThousand(ExpReader reader) throws Exception {
		
		// 천단위 파싱트리 생성 이후, 만단위 파싱트리를 추가함
		// ex) "삼백이십만" -> "삼백이십" 파싱트리 생성 후, "만"에 대한 파싱트리를 추가함
		
		// 천단위 파싱트리 생성
		// ex) "삼백이십" -> "삼백"에 대한 파싱 트리 생성, "이십"에 대한 파싱 트리 생성
		//                 두 파싱 트리를 ADD의 자식 트리로 만듦
		
		// 상기 예에서 "삼백" 에 대한 파싱 트리 생성
		TreeNode<AbstractNode> root = parseThousand(reader);
		
		int read = -1;
		while((read = reader.read()) != -1) {
			
			reader.unread(read);
			
			// 상기 예에서 "이십"에 대한 파싱 트리 생성
			TreeNode<AbstractNode> node = parseThousand(reader);
			// 파싱 트리가 없는 경우, 만단위 문자일 수 있으니 종료
			if(node == null) {
				break;
			}
			
			// 상기 예에서 "삼백" 파싱 트리와 "이십" 파싱 트리를 ADD의 자식 노드로 만들고,
			// root 노드는 최상위 root인 ADD node를 보도록 함
			TreeNode<AbstractNode> add = new TreeNode<>(new ADD());
			
			add.addChild(root);
			add.addChild(node);
			
			root = add;
		}
		
		// 만단위는 숫자 없이 존재할 수 없음
		// 예를 들어) 만이천오백(X), 일만이천오백(O)
		//         억이천(X), 일억이천(O)
		//         조삼백억(X), 일조삼백억(O)
		if(root == null) {
			read = reader.read();
			throw new ParseException(reader.getPos(), (char)read, "NONE");
		}
		
		// 만단위 파싱 트리 추가
		// MUL 노드 이하에 천단위 파싱 트리에 만단위 파싱 트리를 추가함
		// ex) "삼백이십만" = "삼백이십" * "만" 임
		read = reader.read();
		if(read != -1) {
			
			char ch = (char)read;
			KoreanTenThousandUnit tenThousandUnit = KoreanTenThousandUnit.get(ch);
			
			if(tenThousandUnit != null) {
				
				TreeNode<AbstractNode> mul = new TreeNode<>(new MUL());
				
				mul.addChild(root);
				mul.addChild(new TreeNode<>(new LOAD(tenThousandUnit.getFactor())));
				
				root = mul;
			}
		}
		
		// 모든 검사가 이상없으며,
		// 파싱트리가 정상적으로 생성된 경우
		// 파싱 트리의 root node를 반환함
		return root;
	}
	
	/**
	 * 
	 * @param reader
	 * @return
	 */
	private static TreeNode<AbstractNode> parseThousand(ExpReader reader) throws Exception {
		
		//
		TreeNode<AbstractNode> root = null;
		
		//
		TreeNode<AbstractNode> nNode = null;
		{
			
			int read = reader.read();
			if(read == -1) {
				return null;
			}
			
			//
			char ch = (char)read;
			
			//
			KoreanNum n = KoreanNum.get(ch);
			
			if(n != null) {
				
				nNode = new TreeNode<>(new LOAD(n.getN()));
				root = nNode;
				
			} else {
				reader.unread(read);
			}
		}
		
		//
		TreeNode<AbstractNode> thousandUnitNode = null;
		{
			
			int read = reader.read();
			if(read == -1) {
				// 만일 이전 숫자 노드 읽은 것이 있으면 숫자 노드를 반환
				// 없을 경우 null 이 반환됨
				return root;
			}

			char ch = (char)read;
			
			//
			KoreanThousandUnit thousandUnit = KoreanThousandUnit.get(ch);
			
			if(thousandUnit != null) {
				
				thousandUnitNode = new TreeNode<>(new LOAD(thousandUnit.getFactor()));
				
				//
				if(nNode == null) {
					nNode = new TreeNode<>();
					nNode.setData(new LOAD(1));
				}
				
				//
				TreeNode<AbstractNode> mul = new TreeNode<>(new MUL());
				
				mul.addChild(nNode);
				mul.addChild(thousandUnitNode);
				
				root = mul;
				
			} else {
				reader.unread(read);
			}
		}
		
		return root;
	}
}
