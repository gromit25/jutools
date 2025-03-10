package com.jutools.publish.formatter.excel;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AreaPtgBase;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.jutools.publish.formatter.Formatter;
import com.jutools.publish.formatter.FormatterAttr;
import com.jutools.publish.formatter.FormatterException;
import com.jutools.publish.formatter.FormatterSpec;
import com.jutools.publish.formatter.text.PrintFormatter;
import com.jutools.publish.formatter.text.TextFlowFormatter;
import com.jutools.publish.formatter.text.TextFormatOutputStream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * cell formatter<br>
 * cell을 생성, cell 데이터 출력, cell style 적용 수행함<br>
 * <br>
 * 속성에 공통으로 cell의 row와 column 관련 속성들을 지정하기 위해<br>
 * rowColumn 표현식을 만듦<br>
 * 형식) "rowExp:columnExp"<br>
 * ex) "5:4" 6 row(0 부터 시작), 5 column("E" column, 0이 "A" column)<br>
 *     ":4"  1 row, 5 column, "5:" 6 row, 0 column("A" column)<br>
 *     "base.getRow() : base.getColumn() + 2" <- 스크립트 사용가능
 * 
 * @author jmsohn
 */
@FormatterSpec(group="excel", tag="cell")
public class CellFormatter extends AbstractExcelFormatter {

	/**
	 * cell의 위치<br>
	 * rowColumn 표현 형식 사용, defalut 값을 사용하지 않음<br>
	 * position이 설정되지 않으면, worksheet의 커서 위치를 사용함
	 */
	@Getter
	@Setter
	@FormatterAttr(name="position", mandatory=false)
	private RowColumnEval positionExpEval;
	
	/**
	 * cell의 병합 범위<br>
	 * default값은 "0:0", 0일 경우 병합하지 않음
	 */
	@Getter
	@Setter
	@FormatterAttr(name="span", mandatory=false)
	private RowColumnEval spanExpEval;
	
	/**
	 * cell의 크기 지정, pixel 단위임, rowColumn 표현 형식 사용<br>
     * row pixel은 정확히 지정되나, column pixel은 근사치로 지정됨<br>
     * default값은 “0:0”, 0일 경우 size 조정하지 않음
	 */
	@Getter
	@Setter
	@FormatterAttr(name="size", mandatory=false)
	private RowColumnEval sizeExpEval;
	
	/**
	 * 자동 크기 설정<br>
	 * true/false 값 설정, cell 자동 size 조정 기능 설정<br>
	 * default값은 false
	 */
	@Getter
	@Setter
	@FormatterAttr(name="autoSizing", mandatory=false)
	private boolean autoSizing = false;
	
	/**
	 * cellstyle 설정
	 */
	@Getter
	@Setter
	@FormatterAttr(name="style", mandatory=false)
	private String style;
	
	/**
	 * cell의 type 설정
	 */
	@Getter
	@Setter
	@FormatterAttr(name="type", mandatory=false)
	private CellType type;
	
	/** 
	 * 복사해 올 Cell의 위치
	 */
	@Getter
	@Setter
	@FormatterAttr(name="copyFrom", mandatory=false)
	private RowColumnEval copyFrom;

	/**
	 * cell에 표시될 텍스트를 생성하는 formatter
	 */
	@Getter(value=AccessLevel.PRIVATE)
	private TextFlowFormatter cellTextFormatter = new TextFlowFormatter();
	
	/**
	 * 생성자
	 */
	public CellFormatter() throws Exception {
		
		super();
		
		// Default 값 설정
		// RowColumnEval.compile은 예외를 발생시키기 때문에,
		// 생성자에서 Default 값을 설정함
		this.setSizeExpEval(RowColumnEval.compile("0:0", "0", "0"));
		this.setSpanExpEval(RowColumnEval.compile("0:0", "0", "0"));
	}
	
	@Override
	public void addText(String text) throws FormatterException {
		// Cell에 표시될 텍스트 추가
		this.getCellTextFormatter().addText(text);
	}

	@Override
	public void addChildFormatter(Formatter formatter) throws FormatterException {
		
		// Cell에 표시될 text/flow formatter 추가
		// text/flow formatter 여부는
		// TextFlowFormatter의 추상 클래스인 AbstractTextFormatter에서 체크함
		
		if(formatter instanceof TextFlowFormatter || formatter instanceof PrintFormatter) {
			this.getCellTextFormatter().addChildFormatter(formatter);
		} else {
			throw new FormatterException(this, "unexpected formatter type:" + formatter.getClass());
		}

	}

	@Override
	public void formatExcel(OutputStream out, Charset charset, Map<String, Object> values) throws FormatterException {
		
		try {
			
			////////////////////////////////////////////////////////////////
			// 1. 사용할 cell의 객체 가져옴 
			WorksheetFormatter parent = this.getParentInBranch(WorksheetFormatter.class);
			
			// Cell 위치의 Default 값은 worksheet의 커서 위치
			int rowPosition = parent.getCursorRowPosition();
			int columnPosition = parent.getCursorColumnPosition();
			
			// 만일 설정된 Cell 위치(position)가 설정되어 있으면,
			// 설정값으로 위치를 설정함
			if(null != this.getPositionExpEval()) {
				rowPosition = this.getPositionExpEval().evalRowValue(values);
				columnPosition = this.getPositionExpEval().evalColumnValue(values);				
			}
			
			// worksheet에 cell의 위치를 가져옴
			// baseCell이라고 이름지은 이유는
			// cell 병합이 있을 경우, 여러 cell을 다루기 때문에 기준점 cell이라는 의미임
			XSSFSheet sheet = parent.getWorksheet();
			XSSFCell baseCell = ExcelUtil.getCell(sheet, rowPosition, columnPosition);
			
			// worksheet의  cursor 위치를 현재 Cell의 위치로 설정함
			parent.setCursorPosition(rowPosition, columnPosition);
			
			////////////////////////////////////////////////////////////////
			// 2. 복사할 위치가 설정되어 있으면
			//    셀을 현재 셀로 복사함
			if(null != this.getCopyFrom()) {
				
				int copyRowPosition = this.getCopyFrom().evalRowValue(values);
				int copyColumnPosition = this.getCopyFrom().evalColumnValue(values);
				
				XSSFCell srcCell = ExcelUtil.getCell(sheet, copyRowPosition, copyColumnPosition);
				CellCopyPolicy policy = new CellCopyPolicy.Builder()
										.cellValue(true)
										.cellFormula(true)
										.cellStyle(true)
										.copyHyperlink(true)
										.build();
				
				baseCell.copyCellFrom(srcCell, policy);
				
				// formula 형태이면,
				// cell reference의 상대 위치를 계산하여 재설정함
				if(CellType.FORMULA == baseCell.getCellType()) {
					
					String formula = srcCell.getCellFormula();
					
					int rowDiff = rowPosition - copyRowPosition;
					int colDiff = columnPosition - copyColumnPosition;

					String copyFormula = copyFormula(sheet, formula, rowDiff, colDiff);
					baseCell.setCellFormula(copyFormula);
				}
			}
			
			////////////////////////////////////////////////////////////////
			// 3. cell type 설정과
			//    cell 표시될 메시지 생성 및 설정
			
			// cell에 표시될 메시지 변수
			String message = "";
			
			// 설정된 text formatter를 수행하여,
			// cell에 표시될 text를 설정함
			try(TextFormatOutputStream messageOut = new TextFormatOutputStream(new ByteArrayOutputStream())) {
				this.getCellTextFormatter().format(messageOut, charset, values);
				message = messageOut.toString();
			}
			
			// copyFrom 이 설정되어 있는데,
			// text(message)가 빈값일 때는 설정하지 않음
			// -> copyFrom 값을 유지하기 위함
			if(false == message.equals("") || null == this.getCopyFrom()) {
				CellFormatter.setValueToCell(baseCell, this.getType(), message);
			}
	
			////////////////////////////////////////////////////////////////
			// 4. cell 병합 작업 수행
			
			// 병합 cell 주소
			// default는 baseCell 만 설정
			CellRangeAddress spanCellAddr = new CellRangeAddress(rowPosition, rowPosition, columnPosition, columnPosition);
			
			// row와 column 병합 범위 표현식 수행 결과 설정
			int rowSpan = this.getSpanExpEval().evalRowValue(values);
			int columnSpan = this.getSpanExpEval().evalColumnValue(values);
			
			// 수행 결과가 0 이상일때, 병합 수행
			if(rowSpan > 0 || columnSpan > 0) {
				spanCellAddr = new CellRangeAddress(rowPosition, rowPosition + rowSpan, columnPosition, columnPosition + columnSpan);
				sheet.addMergedRegion(spanCellAddr);
			}
			
			////////////////////////////////////////////////////////////////
			// 5. cell 크기 설정
			//    병합된 cell 전체의 크기로 설정함
			//    ex) span이 4:5 에 size가 100:110 라면,
			//        row는 100/(4+1(baseCell)) = 20 픽셀씩 설정됨
			//        column은 110/(5+1) = 18 픽셀씩 설정되고,
			//        최종 column은 18 + (110%(4+1)) 즉, 18+2 = 20 픽셀이 설정됨
			
			// row 크기 계산하여 설정, 단위는 pixel
			int rowSize = this.getSizeExpEval().evalRowValue(values);
			if(rowSize > 0 && rowSpan >= 0) {
				
				int shareSize = rowSize / (rowSpan + 1);
				int restSize = rowSize % (rowSpan + 1);
				
				if(shareSize > 0) {
					for(int index = 0; index < rowSpan + 1; index++) {
						
						int rowPixel = shareSize;
						if(index == rowSpan) { 
							rowPixel += restSize;
						}
						
						XSSFRow row  = ExcelUtil.getRow(sheet, rowPosition + index);
						row.setHeightInPoints((float)Units.pixelToPoints(rowPixel));
					}
				}
			}
			
			// column 크기 계산하여 설정, 단위는 pixel
			// ※ 상기 row pixel은 정확히 지정되나, column pixel은 근사치로 지정됨
			int columnSize = this.getSizeExpEval().evalColumnValue(values);
			if(columnSize > 0 && columnSpan >= 0) {
				
				int shareSize = columnSize / (columnSpan + 1);
				int restSize = columnSize % (columnSpan + 1);
				
				if(shareSize > 0) {
					for(int index = 0; index < columnSpan + 1; index++) {
						
						int widthPixel = shareSize;
						if(index == columnSpan) { 
							widthPixel += restSize;
						}
						
						sheet.setColumnWidth(columnPosition + index, ExcelUtil.pixel2WidthUnits(widthPixel));
					}
				}
			}
			
			////////////////////////////////////////////////////////////////
			// 6. cell 자동 크기 설정
			//    병합된 모든 cell에 대해 각각 적용
			//
			//    ※ autoSize option은 처리시간이 많이 걸리는 
			//      항목으로 사용 자제
			if(this.isAutoSizing() == true) {
				
				Iterator<CellAddress> spanCellAddrIter = spanCellAddr.iterator();
				while(spanCellAddrIter.hasNext() == true) {
					
					CellAddress cellAddr = spanCellAddrIter.next();
					sheet.autoSizeColumn(cellAddr.getColumn());
				}
			}
	
			////////////////////////////////////////////////////////////////
			// 7. cell style 설정
			//    cell style 이름은 cell style 목록(CellStyleFormatter.CELLSTYLE_BUNDLE_NAME)에 있어야함
			//    병합된 모든 cell에 대해 각각 적용
			//
			@SuppressWarnings("unchecked")
			Map<String, XSSFCellStyle> styles = (Hashtable<String, XSSFCellStyle>)values.get(CellStyleFormatter.CELLSTYLE_BUNDLE_NAME);
			if(styles != null && this.getStyle() != null && styles.containsKey(this.getStyle())) {
				
				XSSFCellStyle style = styles.get(this.getStyle());
				
				Iterator<CellAddress> spanCellAddrIter = spanCellAddr.iterator();
				while(spanCellAddrIter.hasNext() == true) {
					
					CellAddress cellAddr = spanCellAddrIter.next();
					
					XSSFCell spanCell = ExcelUtil.getCell(sheet, cellAddr);
					spanCell.setCellStyle(style);
				}
			}
			
			/////////////////////////////////////////////////////////////////
			// 8. 후처리 
			//    Worksheet의 Cursor를 다음 칸으로 이동 시킴
			//
			parent.moveCursorToNextPosition();
			
		} catch(FormatterException fex) {
			throw fex;
		} catch(Exception ex) {
			throw new FormatterException(this, ex);
		}
		
		// cellTextFormatter에 자식 Formatter들이
		// 등록 되어 있기 때문에 CellFormatter의 자식 Formatter를 수행할 
		// 필요가 없다. 
		// cellTextFormatter는 formatExcel 메소드 진입부에서 이미 수행함
		// this.execChildFormatters(copy, charset, values);
	}
	
	/**
	 * cell type별로 value를 변환하여 cell value에 설정함
	 * @param cell 설정할 cell object
	 * @param type cell의 type
	 * @param value 설정할 value
	 */
	private static void setValueToCell(XSSFCell cell, CellType type, String value) throws Exception {
		
		// 입력값 검증 및 디폴트 설정
		if(null == cell) {
			throw new Exception("cell object is null.");
		}
		
		if(null == type) {
			type = cell.getCellType();
			if(null == type) {
				type = CellType.STRING;
			}
		}
		
		if(null == value) {
			value = "";
		}
		
		// 각 타입별로 셀에 설정
		switch(type) {
		case NUMERIC:
			cell.setCellValue(Double.parseDouble(value));
			break;
		case BOOLEAN:
			cell.setCellValue(Boolean.parseBoolean(value));
			break;
		case FORMULA:
			cell.setCellFormula(value);
			break;
		default:
			cell.setCellValue(value);
			break;
		}
	}
	
	/**
	 * formula의 cell reference의 상대 위치를 계산하여 반환함 
	 * https://stackoverflow.com/questions/47594254/apache-poi-update-formula-references-when-copying
	 * 
	 * @param sheet
	 * @param formula
	 * @param rowdiff
	 * @param coldiff
	 * @return
	 */
	private static String copyFormula(XSSFSheet sheet, String formula, int rowDiff, int colDiff) throws Exception {

		// 1. 원본 formula를 파싱함
		XSSFEvaluationWorkbook workbookWrapper = 
				XSSFEvaluationWorkbook.create((XSSFWorkbook)sheet.getWorkbook());
		Ptg[] ptgs = FormulaParser.parse(formula, workbookWrapper, FormulaType.CELL
				, sheet.getWorkbook().getSheetIndex(sheet));

		// 2. 파싱된 formula의 cell reference의 상대 위치를 계산함
		for(int i = 0; i < ptgs.length; i++) {
			
			if(ptgs[i] instanceof RefPtgBase) { // base class for cell references
				
				RefPtgBase ref = (RefPtgBase) ptgs[i];
				
				if (true == ref.isColRelative()) {
					ref.setColumn(ref.getColumn() + colDiff);
				}
				if (true == ref.isRowRelative()) {
					ref.setRow(ref.getRow() + rowDiff);
				}
			}
			else if (ptgs[i] instanceof AreaPtgBase) { // base class for range references
				
				AreaPtgBase ref = (AreaPtgBase) ptgs[i];
				
				if(true == ref.isFirstColRelative()) {
					ref.setFirstColumn(ref.getFirstColumn() + colDiff);
				}
				
				if(true == ref.isLastColRelative()) {
					ref.setLastColumn(ref.getLastColumn() + colDiff);
				}
				
				if(true == ref.isFirstRowRelative()) {
					ref.setFirstRow(ref.getFirstRow() + rowDiff);
				}
				
				if(true == ref.isLastRowRelative()) {
					ref.setLastRow(ref.getLastRow() + rowDiff);
				}
			}
		}

		// 3. 계산된 상대 위치를 넣고 formula 스트링을 재 생성함
		formula = FormulaRenderer.toFormulaString(workbookWrapper, ptgs);
		return formula;
	}
}
