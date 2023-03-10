package com.jutools.publish.formatter.excel;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.xml.sax.Attributes;

import com.jutools.publish.formatter.Formatter;
import com.jutools.publish.formatter.FormatterAttr;
import com.jutools.publish.formatter.FormatterException;
import com.jutools.publish.formatter.FormatterSpec;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * cell의 style을 정의하기 위한 클래스
 * 하위 Formatter로 AlignmentFormatter, BackgroundFormatter, BorderFormatter가 있음
 * 
 * @author jmsohn
 */
@FormatterSpec(group="excel", tag="cellstyle")
public class CellStyleFormatter extends AbstractWorkbookComponentFormatter {
	
	/** value container의 cellstyle 목록의 이름 */
	static String CELLSTYLE_BUNDLE_NAME = "_CELLSTYLE_BUNDLE_";

	/** value container의 cellstyle 목록에 설정할 이름 */
	@Getter
	@Setter
	@FormatterAttr(name="name")
	private String name;
	
	/** cell에 지정할 폰트 이름 */
	@Getter
	@Setter	
	@FormatterAttr(name="font", mandatory=false)
	private String font;
	
	/** 줄바꿈 설정, true/false 값 */
	@Getter
	@Setter
	@FormatterAttr(name="wrap", mandatory=false)
	private boolean wrap;
	
	/** */
	@Getter
	@Setter(AccessLevel.PRIVATE)	
	private XSSFCellStyle style;
	
	/**
	 * 생성자
	 */
	public CellStyleFormatter() {
		this.setWrap(false);
	}

	@Override
	public void setAttributes(Attributes attributes) throws FormatterException {
		
		// value container의 cellstyle 목록에 설정할 이름 설정
		String nameAttr = attributes.getValue("name");
		if(nameAttr == null || nameAttr.trim().equals("") == true) {
			throw new FormatterException(this, "cellstyle name is not set");
		}

		this.setName(nameAttr);
		
		// cell에 지정할 폰트 이름 설정
		String fontAttr = attributes.getValue("font");
		if(fontAttr != null && fontAttr.trim().equals("") == false) {
			this.setFont(fontAttr);
		}
		
		// 줄바꿈 설정, true/false 값
		String wrapAttr = attributes.getValue("wrap");
		if(wrapAttr != null && wrapAttr.trim().equals("") == false) {
			this.setWrap(Boolean.parseBoolean(wrapAttr));
		}
	}
	
	@Override
	public void addChildFormatter(Formatter formatter) throws FormatterException {
		
		// 입력값 검증
		if(formatter == null) {
			throw new FormatterException(this, "formatter is null");
		}
		
		// AbstractSubCellStyleFormatter 만 자식 Formatter로 등록될 수 있음
		// 다른 formatter가 들어올 경우 오류 처리함
		if((formatter instanceof AbstractCellStyleComponentFormatter) == false) {
			throw new FormatterException(this, "unexpected formatter type(not AbstractCellStyleFormatter):" + formatter.getClass().getName());
		}
		
		super.addChildFormatter(formatter);
	}
	
	@Override
	protected void formatExcel(OutputStream out, Charset charset, Map<String, Object> values) throws FormatterException {
		
		WorkbookFormatter parent = this.getParent(WorkbookFormatter.class);
		
		// excel workbook에서 style 생성
		this.setStyle(parent.getWorkbook().createCellStyle());
		
		// cell에 지정할 폰트 설정
		@SuppressWarnings("unchecked")
		Hashtable<String, XSSFFont> fonts = (Hashtable<String, XSSFFont>)values.get(FontFormatter.FONT_BUNDLE_NAME);
		if(fonts != null && this.getFont() != null && fonts.containsKey(this.getFont()) == true) {
			XSSFFont font = fonts.get(this.getFont());
			style.setFont(font);
		}
		
		// 줄바꿈 설정
		style.setWrapText(this.isWrap());
		
		// cellstyle 목록에 생성된 style 추가
		//
		@SuppressWarnings("unchecked")
		Hashtable<String, XSSFCellStyle> cellStyleMap = (Hashtable<String, XSSFCellStyle>)values.get(CELLSTYLE_BUNDLE_NAME);
		if(cellStyleMap == null) {
			cellStyleMap = new Hashtable<String, XSSFCellStyle>();
			try {
				values.put(CELLSTYLE_BUNDLE_NAME, cellStyleMap);
			} catch(Exception ex) {
				throw new FormatterException(this, ex);
			}
		}
		
		cellStyleMap.put(this.getName(), style);
		
		// 자식 formatter들 실행
		// 자식 formatter에서 style에 추가 속성을 설정함
		// ex) border, background, alignment 등
		super.execChildFormatters(out, charset, values);
	}
}
