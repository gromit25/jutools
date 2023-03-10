package com.jutools.publish.formatter.flow;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import com.jutools.publish.formatter.Formatter;
import com.jutools.publish.formatter.FormatterException;

import lombok.Getter;
import lombok.Setter;

/**
 * 하위 flow를 가지는 formatter
 * ex) foreach, alt, for, while, if, case, default 등 하위 플로우를 가지는 경우
 * 
 * @author jmsohn
 */
public abstract class AbstractFlowComponentFormatter extends AbstractFlowFormatter {
	
	/**
	 * formatter의 하위 기본 flow
	 */
	@Getter
	@Setter
	private BasicFlowFormatter basicFlowFormatter = new BasicFlowFormatter();

	@Override
	public void addText(String text) throws FormatterException {
		this.getBasicFlowFormatter().addText(text);
	}

	@Override
	public void addChildFormatter(Formatter formatter) throws FormatterException {
		this.getBasicFlowFormatter().addChildFormatter(formatter);
	}

	@Override
	protected void execFormat(OutputStream out, Charset charset, Map<String, Object> values) throws FormatterException {
		this.getBasicFlowFormatter().execFormat(out, charset, values);
	}

}
