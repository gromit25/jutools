package com.jutools.publish.formatter.excel.chart;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;

import com.jutools.publish.formatter.FormatterAttr;
import com.jutools.publish.formatter.FormatterException;
import com.jutools.publish.formatter.FormatterSpec;
import com.jutools.publish.formatter.excel.RangeEval;

import lombok.Getter;
import lombok.Setter;

@FormatterSpec(group="excel", tag="category-ds, value-ds")
public class DataSourceFormatter extends AbstractChartComponent {
	
	@Getter
	@Setter
	@FormatterAttr(name="range", mandatory=true)
	private RangeEval range;
	
	@Getter
	@Setter
	@FormatterAttr(name="type", mandatory=true)
	private DataSourceTypes type;

	@Override
	protected void execFormat(OutputStream out, Charset charset, Map<String, Object> values) throws FormatterException {
		
		//
		AbstractSeriesFormatter copy = this.getParent(AbstractSeriesFormatter.class);
		
		//
		int startRow = 0;
		int startColumn = 0;
		int endRow = 0;
		int endColumn = 0;
		
		try {
			startRow = this.getRange().evalStartRow(values);
			startColumn = this.getRange().evalStartColumn(values);
			endRow = this.getRange().evalEndRow(values);
			endColumn = this.getRange().evalEndColumn(values);
		} catch(Exception ex) {
			throw new FormatterException(this, ex);
		}
		
		CellRangeAddress rangeAddr = new CellRangeAddress(startRow, endRow, startColumn, endColumn);
		XDDFDataSource<?> ds = this.getType().createDataSource(copy.getWorksheet(), rangeAddr);

		//
		if(true == this.getTagName().equals("category-ds")) {
			copy.setCategoryDS(ds);
		} else if(true == this.getTagName().equals("value-ds")) {
			if(ds instanceof XDDFNumericalDataSource<?>) {
				copy.setValueDS((XDDFNumericalDataSource<?>)ds);
			} else {
				throw new FormatterException(this, "value-ds is not NUMBER type.");
			}
		} else {
			throw new FormatterException(this, "unexpected tag name:" + this.getTagName());
		}

	}
}
