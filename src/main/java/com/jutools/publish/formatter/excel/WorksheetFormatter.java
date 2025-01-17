package com.jutools.publish.formatter.excel;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.jutools.TextGen;
import com.jutools.publish.formatter.FormatterAttr;
import com.jutools.publish.formatter.FormatterException;
import com.jutools.publish.formatter.FormatterSpec;
import com.jutools.publish.formatter.flow.BasicFlowFormatter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * worksheet formatter
 * 신규 worksheet 생성
 * 
 * @author jmsohn
 */
@FormatterSpec(group="excel", tag="worksheet")
public class WorksheetFormatter extends BasicFlowFormatter {
	
	/**
	 * worksheet 명(필수)<br>
	 * -> TextGen 클래스를 이용하여 이름을 동적으로 생성, nameGen 속성 참조
	 */
	@Getter
	@FormatterAttr(name="name", mandatory=true)
	private String name;
	
	/** worksheet 명 생성기 */
	private TextGen nameGen;
	
	/** */
	@Getter
	@Setter
	@FormatterAttr(name="copyFrom", mandatory=false)
	private String copyFrom;
	
	@Getter
	@Setter
	@FormatterAttr(name="remove", mandatory=false)
	private boolean remove = false;
	
	@Getter
	@Setter(AccessLevel.PRIVATE)
	private XSSFSheet worksheet;

	/** 커서의 Row 위치(default 값 : 0) */
	@Getter
	private int cursorRowPosition = 0;
	
	/** 커서의 Column 위치(default 값 : 0) */
	@Getter
	private int cursorColumnPosition = 0;
	
	/** Cell 등에 대해 처리 완료된 경우 자동으로 이동할 커서 이동 방향(default값 :DOWN)*/
	@Getter
	@Setter
	private CursorDirection cursorMoveDirection = CursorDirection.DOWN;
	
	/** 커서 이동 방향 enum */
	public enum CursorDirection {
		
		// 아래 Cell로 이동
		DOWN {
			@Override
			public void move(WorksheetFormatter worksheet) {
				worksheet.setCursorRowPosition(worksheet.getCursorRowPosition() + 1);
			}
		},
		
		// 오른쪽 Cell로 이동
		RIGHT {
			@Override
			public void move(WorksheetFormatter worksheet) {
				worksheet.setCursorColumnPosition(worksheet.getCursorColumnPosition() + 1);
			}
		}; 
		
		/**
		 * worksheet의 cursor를 특정 방향으로 한칸 이동시킴
		 * 
		 * @param worksheet cursor를 이동시킬 worksheet
		 */
		public abstract void move(WorksheetFormatter worksheet);
	}

	@Override
	protected void execFormat(OutputStream out, Charset charset, Map<String, Object> values) throws FormatterException {
		
		try {
			
			// workbook formatter 설정
			WorkbookFormatter workbook = null;
			try {
				workbook = this.getParentInBranch(WorkbookFormatter.class);
			} catch(Exception ex) {
				throw new FormatterException(this, ex);
			}
			
			// worksheet 명 생성
			String worksheetName = this.nameGen.gen(values);
	
			// cloneFrom 이 설정 되지 않은 경우,
			// 설정된 worksheet명으로 worksheet 생성 및 활성화 sheet(active sheet)로 설정
			// worksheet가 없을 경우, 새로 만듦
			if(null == this.getCopyFrom()) {
				
				XSSFSheet sheet = workbook.getWorkbook().getSheet(worksheetName);
				if(sheet == null) {
					sheet = workbook.getWorkbook().createSheet(worksheetName);
				}
				
				this.setWorksheet(sheet);
			}
			// cloneFrom 설정되어 있는 경우,
			// cloneFrom Sheet에서 clone을 생성하여, 설정함
			else {
				
				int cloneFromIndex = workbook.getWorkbook().getSheetIndex(this.getCopyFrom());
				XSSFSheet sheet = workbook.getWorkbook().cloneSheet(cloneFromIndex, worksheetName);
				
				this.setWorksheet(sheet);
			}
			
			// 활성시트를 현재 시트로 설정함
			workbook.getWorkbook().setActiveSheet(
				workbook.getWorkbook().getSheetIndex(worksheetName)
			);
		
			// worksheet의 자식 formatter 수행
			this.execChildFormatters(out, charset, values);
			
			// 만일 remove 설정이 되어 있으면, 현 worksheet를 삭제함
			if(true == this.isRemove()) {
				int index = workbook.getWorkbook().getSheetIndex(worksheetName);
				workbook.getWorkbook().removeSheetAt(index);
			}
			
			// 첫 worksheet로 active worksheet를 변경
			workbook.getWorkbook().setActiveSheet(0);
			
		} catch(FormatterException fex) {
			throw fex;
		} catch(Exception ex) {
			throw new FormatterException(this, ex);
		}
	}
	
	/**
	 * worksheet 명 설정
	 * 
	 * @param name 설정할 worksheet 명
	 */
	public void setName(String name) throws Exception {
		
		if(name == null) {
			throw new NullPointerException("worksheet name is null.");
		}
		
		this.name = name;
		this.nameGen = TextGen.compile(name);
	}
	
	/**
	 * 특정 위치로 커서를 설정
	 * @param cursorRowPosition row 위치
	 * @param cursorColumnPosition column 위치
	 */
	public void moveCursorAt(int cursorRowPosition, int cursorColumnPosition) {
		this.setCursorRowPosition(cursorRowPosition);
		this.setCursorColumnPosition(cursorColumnPosition);
	}
	
	/**
	 * 설정된 방향에 따라 커서를 이동시킴
	 */
	public void moveCursorToNextPosition() {
		this.getCursorMoveDirection().move(this);
	}
	
	/**
	 * 커서의 위치를 설정함
	 * @param rowPosition 설정할 row 위치
	 * @param columnPosition 설정할 column 위치
	 */
	public void setCursorPosition(int rowPosition, int columnPosition) {
		this.setCursorRowPosition(rowPosition);
		this.setCursorColumnPosition(columnPosition);
	}
	
	/**
	 * 커서의 row 위치를 설정함
	 *   설정할 row 위치가 0이하일 경우, 0으로 설정함
	 * @param cursorRowPosition 설정할 row 위치
	 */
	public void setCursorRowPosition(int cursorRowPosition) {
		if(cursorRowPosition < 0) {
			this.cursorRowPosition = 0;
		} else {
			this.cursorRowPosition = cursorRowPosition;
		}
	}
	
	/**
	 * 커서의 column 위치를 설정함
	 *   설정할 column 위치가 0이하일 경우, 0으로 설정함
	 * @param cursorColumnPosition 설정할 column 위치
	 */
	public void setCursorColumnPosition(int cursorColumnPosition) {
		if(cursorColumnPosition < 0) {
			this.cursorColumnPosition = 0;
		} else {
			this.cursorColumnPosition = cursorColumnPosition;
		}
	}

}
