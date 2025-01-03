package com.jutools.instructions;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.jutools.parserfw.AbstractParser;

import lombok.Getter;

/**
 * Expression의 추상 클래스
 * 
 * @author jmsohn
 */
public abstract class AbstractEngine {
	
	/** Expression 문자열 */
	@Getter
	protected String exp;
	/** Expression 명령어 목록 */
	protected List<Instruction> insts;
	/** Expression 처리시 사용할 Stack */
	@Getter
	protected Stack<Object> stack = new Stack<Object>();
	/** Expression 내의 alias 메소드의 실제 메소드 - K: 메소드 alias 명, V: 실제 수행 메소드 */
	protected Map<String, MethodHandle> methods = new HashMap<>();

	/**
	 * root parser 반환
	 * -> root parser : 가장 처음 호출되는 parser
	 * 
	 * @return root parser
	 */
	protected abstract AbstractParser<Instruction> getRootParser() throws Exception;
	
	/**
	 * 생성자
	 * 
	 * @param exp expression
	 */
	protected AbstractEngine(String exp) throws Exception {
		
		if(exp == null) {
			throw new NullPointerException("math expression(exp) is null");
		}
		
		this.exp = exp;
		this.setMethod(BuiltInMethods.class);
		
		// Expression 파싱
		try {
			
			AbstractParser<Instruction> parser = this.getRootParser();
			this.insts = parser.parse(exp).travelPostOrder();
			
		} catch(Exception ex) {
			
			Throwable t = ex;
			while(t.getCause() != null) {
				t = t.getCause();
			}
			
			throw (Exception)t;
		}
		
		// 메소드 링킹
		this.linkMethod();
	}

	/**
	 * 실제 실행할 메소드 설정
	 * 
	 * @param methodClass 실제 실행할 메소드를 가지는 클래스(MethodAlias Annotation 사용)
	 * @return 현재 객체(fluent 코딩용)
	 */
	public AbstractEngine setMethod(Class<?> methodClass) throws Exception {

		// 입력값 검증
		if(methodClass == null) {
			throw new NullPointerException("method class is null");
		}
		
		// Method Handle 을 획득하기 위한 Lookup 객체 생성
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		
		// 각 메소드 별로 검사하여 메소드 추가
		for(Method method: methodClass.getMethods()) {
			
			// 메소드에 MethodAlias Annotation을 가져옴
			MethodAlias methodMap  = method.getAnnotation(MethodAlias.class);
			if(methodMap == null) {
				continue;
			}
			
			// 메소드는 public static 이어야 함
			int modifier = method.getModifiers();
			
			if(Modifier.isStatic(modifier) == false) {
				throw new Exception(methodClass.getCanonicalName() + "." + method.getName() + " method is not static");
			}
			
			if(Modifier.isPublic(modifier) == false) {
				throw new Exception(methodClass.getCanonicalName() + "." + method.getName() + " method is not public");
			}
			
			// Method Handle 획득
			MethodHandle methodHandle = lookup.unreflect(method);
			
			// 메소드 추가
			this.methods.put(methodMap.alias(), methodHandle);
		}
		
		return this;
	}
	
	/**
	 * 메소드 호출(INVOKE) 명령어의 alias에 실제 메소드를 연결(linking)
	 * 
	 * @return 현재 객체(fluent 코딩용)
	 */
	private AbstractEngine linkMethod() throws Exception {
		
		if(this.insts == null) {
			throw new NullPointerException("instruction list is null");
		}
		
		for(Instruction inst: this.insts) {
			
			if(inst instanceof INVOKE == false) {
				continue;
			}
			
			// method link 작업 수행
			INVOKE invokeInst = (INVOKE)inst;
			
			String alias = invokeInst.getParam(0); // method alias
			MethodHandle method = this.getMethod(alias); // get aliased method
			
			invokeInst.setMethod(method);
		}
		
		return this;
	}
	
	/**
	 * alias에 해당하는 메소드를 반환하는 메소드
	 * 
	 * @param alias 검색할 alias
	 * @return 메소드 핸들
	 */
	private MethodHandle getMethod(String alias) throws Exception {
		
		if(this.methods.containsKey(alias) == false) {
			throw new Exception("method is not found");
		}
		
		return this.methods.get(alias);
	}
	
	/**
	 * Expression 수행
	 * 
	 * @param values 
	 * @return 현재 객체(fluent 코딩용)
	 */
	public AbstractEngine execute(Map<String, ?> values) throws Exception {
		
		for(Instruction inst: insts) {
			inst.execute(this.stack, values);
		}
		
		return this;
	}
	
	/**
	 * stack의 최상단 값을 뽑아서 반환 
	 * 
	 * @param type stack의 값을 casting할 타입
	 * @return stack의 최상단 값
	 */
	public <T> T pop(Class<T> type) throws Exception {
		
		if(this.stack.isEmpty() == true) {
			
			return null;
			
		} else {
			
			Object obj = this.stack.pop();
			return type.cast(obj);
		}
	}
	
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder("");
		builder
			.append("INSTRUCTION LIST\n")
			.append("-------------------------\n");
		
		if(this.insts != null && this.insts.size() != 0) {
			for(Instruction inst: this.insts) {
				builder.append(inst).append("\n");
			}
		} else {
			builder.append("instruction list is empty.\n");
		}
		
		return builder.toString();
	}
}
