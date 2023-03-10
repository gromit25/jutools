package com.jutools.publish.formatter.text;

import java.util.HashSet;
import java.util.Set;

import com.jutools.publish.formatter.FormatterXmlHandler;
import com.jutools.publish.formatter.flow.AbstractFlowComponentFormatter;

/**
 * 텍스트 출력을 위한 format 파일 파서
 * 
 * @author jmsohn
 */
public class TextFormatterXmlHandler extends FormatterXmlHandler {

	private Set<String> formatterGroupNames;

	/**
	 * 생성자
	 */
	public TextFormatterXmlHandler() throws Exception {
		super();
	}

	@Override
	protected Set<String> getFormatterGroupNames() {
		
		if(this.formatterGroupNames == null) {
			
			this.formatterGroupNames = new HashSet<String>();
			this.formatterGroupNames.add("flow");
			this.formatterGroupNames.add("text");
		}
		
		return this.formatterGroupNames;
	}

	@Override
	protected void setSubBasicFlow(AbstractFlowComponentFormatter formatter) throws Exception {
		formatter.setBasicFlowFormatter(new TextFlowFormatter());
	}

}
