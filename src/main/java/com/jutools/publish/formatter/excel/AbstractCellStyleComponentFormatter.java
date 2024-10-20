package com.jutools.publish.formatter.excel;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import com.jutools.publish.formatter.Formatter;
import com.jutools.publish.formatter.FormatterException;
import com.jutools.publish.formatter.flow.BasicFlowFormatter;

/**
 * celltype 이하의 설정용 Formatter의 추상 클래스<br>
 * ex) BorderFormatter, BackgroundFormatter, AlignmentFormatter 등
 * 
 * @author jmsohn
 */
public abstract class AbstractCellStyleComponentFormatter  extends BasicFlowFormatter {
	
	/**
	 * cellstyle에 설정작업 수행
	 * 
	 * @param out 출력 스트림
	 * @param parent 부모 CellStyleFormatter
	 * @param charset 출력시 사용할 character set
	 * @param values value container
	 */
	protected abstract void formatCellStyle(OutputStream out, Charset charset, Map<String, Object> values) throws FormatterException;

	@Override
	public void addChildFormatter(Formatter formatter) throws FormatterException {
		
		if(formatter == null) {
			throw new FormatterException(this, "formatter is null");
		}
		
		if((formatter instanceof AbstractCellStyleComponentFormatter) == false) {
			throw new FormatterException(this, "unexpected formatter type(not AbstractCellStyleFormatter):" + formatter.getClass().getName());
		}
		
		super.addChildFormatter(formatter);
	}
	
	@Override
	protected void execFormat(OutputStream out, Charset charset, Map<String, Object> values) throws FormatterException {
		
		try {
			this.formatCellStyle(out, charset, values);
		} catch(FormatterException fex) {
			throw fex;
		} catch(Exception ex) {
			throw new FormatterException(this, ex);
		}
	}

	/**
	 * 부모 Formatter 중 CellStyleFormatter의 Excel Cell Style 객체를 반환함
	 * 
	 * @return 부모 Formatter 중 CellStyleFormatter의 Excel Cell Style 객체
	 */
	protected XSSFCellStyle getParentStyle() throws FormatterException {
		CellStyleFormatter parent = this.getParent(CellStyleFormatter.class);
		return parent.getStyle();
	}
}
