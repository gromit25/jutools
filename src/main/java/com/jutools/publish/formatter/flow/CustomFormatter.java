package com.jutools.publish.formatter.flow;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import com.jutools.publish.formatter.Formatter;
import com.jutools.publish.formatter.FormatterAttr;
import com.jutools.publish.formatter.FormatterException;
import com.jutools.publish.formatter.FormatterSpec;

import lombok.Getter;
import lombok.Setter;

@FormatterSpec(group="flow", tag="custom")
public class CustomFormatter extends BasicFlowFormatter {
	
	@Getter
	@Setter
	@FormatterAttr(name="class", mandatory=true)
	private Class<?> customClass;

	@Override
	public void addText(String text) throws FormatterException {
		// do nothing
	}

	@Override
	public void addChildFormatter(Formatter formatter) throws FormatterException {
		// CustomFormatter만 추가 가능
		if(formatter instanceof CustomFormatter) {
			super.addChildFormatter(formatter);
		} else {
			throw new FormatterException(this, "Unexpected Formatter type(CustomFormatter):" + formatter.getClass());
		}
	}

	@Override
	protected void execFormat(OutputStream out, Charset charset, Map<String, Object> values) throws FormatterException {
		try {
			
			if(Formatter.class.isAssignableFrom(this.getCustomClass()) == false) {
				throw new FormatterException(this, "Unexpected class(Expect:Formatter):" + this.getCustomClass());
			}
			
			Formatter customInstance = Formatter.class.cast(this.getCustomClass().getConstructor().newInstance());
			this.copy(customInstance);
			customInstance.format(out, charset, values);
			
		} catch(FormatterException fex) {
			throw fex;
		} catch(Exception ex) {
			throw new FormatterException(this, ex);
		}
	}

}
