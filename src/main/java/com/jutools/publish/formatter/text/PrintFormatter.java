package com.jutools.publish.formatter.text;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import com.jutools.mathexp.MathExp;
import com.jutools.publish.formatter.Formatter;
import com.jutools.publish.formatter.FormatterAttr;
import com.jutools.publish.formatter.FormatterException;
import com.jutools.publish.formatter.FormatterSpec;

import lombok.Getter;
import lombok.Setter;

/**
 * print formatter
 * 화면상에 표현식(exp) 수행결과를 출력
 *  
 * ex)
 * <print exp="info.getName()" length="20"/>
 * 
 * @author jmsohn
 */
@FormatterSpec(group="text", tag="print")
public class PrintFormatter extends AbstractTextFormatter {
	
	/** 표현식(exp)의 Evaluator */
	@Getter
	@Setter
	@FormatterAttr(name="exp", mandatory=true)
	private MathExp exp;
	
	/**
	 * 출력 길이
	 * 출력 길이(옵션) 설정되어 있지 않으면 수행 결과 스트링의 길이 만큼만 출력
	 */
	@Getter
	@Setter
	@FormatterAttr(name="length", mandatory=false)
	private int length = -1;
	
	@Override
	public void addText(String text) throws FormatterException {
		// do nothing
	}

	@Override
	public void addChildFormatter(Formatter fomatter) throws FormatterException {
		// do nothing
	}

	@Override
	public void formatText(OutputStream out, Charset charset, Map<String, Object> values) throws FormatterException {
		
		// 표현식(exp)을 수행한 후
		// 수행 결과 값을 반환함
		try {
			
			Object result = this.getExp().execute(values).getResult().getValue();
			
			// 수행 결과를 메세지에 설정함
			if(result != null) {
				out.write(
						PublisherUtil.makeString(result.toString(), this.getLength())
						// 개행만 있고 '|' 이 없을경우,
						// 개행 이후 부분을 최종적으로 잘라내기 때문에
						// '|'으로 변경해 줌
						.replaceAll("\n", "\n|")
						.getBytes(charset));
			} else {
				out.write(PublisherUtil.makeString("N/A", this.getLength()).getBytes(charset));
			}
			
			out.flush();
			
		} catch(Exception ex) {
			
			// 예외 발생시
			throw new FormatterException(this, ex);
		}
		
	}

}
