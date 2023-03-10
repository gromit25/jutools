package com.jutools.publish.formatter.flow;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

import com.jutools.publish.formatter.Formatter;
import com.jutools.publish.formatter.FormatterException;

import lombok.AccessLevel;
import lombok.Getter;
  
/**
 * basic flow formatter
 * 흐름에 따라 순서대로 Formatter 수행함
 * format 테그를 기본적으로 처리하며,
 * 다른 Formatter에서 내장되어 사용되기도 함(ex. ForeachFormatter)
 * 따라서, FormatterSpec을 지정하지 않음
 * 
 * @author jmsohn
 */
public class BasicFlowFormatter extends AbstractFlowFormatter {
	
	/**
	 * 하위 Formatter 목록
	 */
	@Getter(value=AccessLevel.PROTECTED)
	private ArrayList<Formatter> childFormatterList = new ArrayList<Formatter>();
	
	@Override
	public void addText(String text) throws FormatterException {
		// do nothing
	}
	
	@Override
	public void addChildFormatter(Formatter formatter) throws FormatterException {
		// 입력값 체크
		if(formatter == null) {
			throw new FormatterException(this, "formatter is null");
		}
		
		// 자식 formatter 추가
		this.getChildFormatterList().add(formatter);
	}
	
	@Override
	protected void execFormat(OutputStream out, Charset charset, Map<String, Object> values) throws FormatterException {
		this.execChildFormatters(out, charset, values);
	}
	

	/**
	 * 등록된 자식 formatter들을 모두 수행함
	 * 
	 * @param out 출력 스트림
	 * @param parent 부모 Formatter
	 * @param charset 출력시 사용할 character set
	 * @param values value container
	 */
	protected void execChildFormatters(OutputStream out, Charset charset, Map<String, Object> values) throws FormatterException {
		
		// 하위 formatter들을 하나씩 수행함
		for(Formatter formatter: this.getChildFormatterList()) {
			try {
				formatter.format(out, charset, values);
			} catch(FormatterException fex) {
				throw fex;
			} catch(Exception ex) {
				throw new FormatterException(formatter, ex);
			}
		}
	}

}
