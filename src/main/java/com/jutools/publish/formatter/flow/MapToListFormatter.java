package com.jutools.publish.formatter.flow;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import org.xml.sax.Attributes;

import com.jutools.publish.formatter.Formatter;
import com.jutools.publish.formatter.FormatterAttr;
import com.jutools.publish.formatter.FormatterException;
import com.jutools.publish.formatter.FormatterSpec;
import com.jutools.script.olexp.OLExp;

import lombok.Getter;
import lombok.Setter;

/**
 * java Collection의 Map 객체를 List 객체로 변환
 * 
 * @author jmsohn
 */
@FormatterSpec(group="flow", tag="mapToList")
public class MapToListFormatter extends AbstractFlowFormatter {
	
	/** value container에 설정할 이름 */
	@Getter
	@Setter
	@FormatterAttr(name="name", mandatory=true)
	private String name;
	
	/** 변환할 map 스크립트 Evaluator */
	@Getter
	@Setter
	@FormatterAttr(name="mapExp", mandatory=true)
	private OLExp mapExp;

	/** List로 변환시 sorting 기준 클래스 */
	@Getter
	@Setter
	private Class<?> comparator;

	@Override
	public void setAttributes(Attributes attributes) throws FormatterException {
		
		try {
			
			// List로 변환시 sorting 기준 클래스 설정
			// sorting 기준 클래스는 java.util.Comparator 인터페이스의 구현체여야 함
			String comparatorAttr = attributes.getValue("comparator");
			if(comparatorAttr != null && comparatorAttr.trim().equals("") == false) {
				
				Class<?> comparatorClazz = Class.forName(comparatorAttr);
				if(Comparator.class.isAssignableFrom(comparatorClazz) == false) {
					throw new Exception("comparator is not java.lang.Comparator class");
				}
				
				this.setComparator(Class.forName(comparatorAttr));
			} else {
				this.setComparator(null);
			}
			
		} catch(Exception ex) {
			throw new FormatterException(this, ex);
		}
	}

	@Override
	public void addText(String text) throws FormatterException {
		throw new FormatterException(this, "Unexpected text value:" + text);
	}

	@Override
	public void addChildFormatter(Formatter formatter) throws FormatterException {
		throw new FormatterException(this, "Unexpected formatter");
	}

	@Override
	protected void execFormat(OutputStream out, Charset charset, Map<String, Object> values) throws FormatterException {
		
		// 1. map 객체를 가져옴
		Object mapObj = null;
		try {
			mapObj = this.getMapExp().execute(values).pop(Object.class);
		} catch(Exception ex) {
			throw new FormatterException(this, ex);
		}
		
		if(mapObj == null || (mapObj instanceof Map) == false) {
			throw new FormatterException(this, "N/A(" + this.getMapExp().getScript() + " is not Map Object)");
		}
		
		// 위에서 Map 타입 체크하였기 때문에 suppress 시킴
		// TODO
		@SuppressWarnings("unchecked")
		Map<Object, Object> map = (Map<Object, Object>)mapObj;
		
		// 2. list 객체에 추가함
		ArrayList<Object> list = new ArrayList<Object>();
		map.forEach((key, object) -> {
			list.add(object);
		});
		
		// 3. comparator가 있을 경우, sorting 수행
		if(this.getComparator() != null) {
			try {
				// attribute 설정시 체크함
				@SuppressWarnings("unchecked")
				Comparator<Object> comparatorObj = (Comparator<Object>)this.getComparator().getConstructor().newInstance();
				list.sort(comparatorObj);
			} catch(Exception ex) {
				throw new FormatterException(this, ex);
			}
		}
		
		// 4. 만들어진 List를  value container에 넣음
		try {
			values.put(this.getName(), list);
		} catch(Exception ex) {
			throw new FormatterException(this, ex);
		}
	}

}
