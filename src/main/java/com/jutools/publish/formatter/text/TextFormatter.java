package com.jutools.publish.formatter.text;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import com.jutools.TextGen;
import com.jutools.publish.formatter.Formatter;
import com.jutools.publish.formatter.FormatterException;

/**
 * Text Formatter
 * 텍스트를 출력하는 Formatter
 * Tag 없이 다른 Formatter에서 생성하여 사용하기 때문에,
 * FormatterSpec을 따로 지정하지 않음
 * ex) StyleFormatter, TextFlowFormatter 등
 * 
 * @author jmsohn
 */
public class TextFormatter extends AbstractTextFormatter {
	
	/** 출력할 텍스트 메시지 */
	private TextGen generator;
	
	/**
	 * 생성자
	 * TextFormatterXmlHandler에 의해 생성되지 않고,
	 * TextFlowFormatter에셔 addText 메소드 수행시 생성됨
	 * 
	 * @param message 출력할 텍스트 메시지
	 */
	public TextFormatter(String message) throws FormatterException {
		try {
			this.generator = TextGen.compile(message);
		} catch(Exception ex) {
			throw new FormatterException(this, ex);
		}
	}

	@Override
	public void addText(String text) throws FormatterException {
		// do nothing
	}

	@Override
	public void addChildFormatter(Formatter formatter) throws FormatterException {
		// do nothing
	}

	@Override
	public void formatText(OutputStream out, Charset charset, Map<String, Object> values) throws FormatterException {
		
		try {
			// Output stream에 출력 수행
			out.write(this.generator.gen(values).getBytes(charset));
			out.flush();
		} catch(Exception ex) {
			throw new FormatterException(this, ex);
		}
	}

}
