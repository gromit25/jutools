package com.jutools.publish.formatter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Formatter에서 발생한 예외<br>
 * - 예외가 발생한 Formatter 객체 보유<br>
 * - 예외가 발생한 attribute의 이름 보유<br>
 * <br>
 * 용도)<br>
 * 예외 발생시, Publisher Xml의 어느 부분인지 확인이 곤란하여 추가함<br>
 * 
 * @author jmsohn
 */
public class FormatterException extends Exception {

	private static final long serialVersionUID = -1772099764515741751L;
	
	/** 예외가 발생한 Formatter 객체 */
	@Getter
	@Setter(value=AccessLevel.PRIVATE)
	private Formatter formatter;
	
	/** 예외가 발생한 attribute 명 */
	@Getter
	@Setter(value=AccessLevel.PRIVATE)
	private String attributeName;
	
	/**
	 * 생성자
	 * 
	 * @param formatter 예외가 발생한 formatter 객체
	 * @param attributeName 예외가 발생한 attribute 명
	 * @param ex 발생 예외
	 */
	public FormatterException(Formatter formatter, String attributeName, Exception ex) {
		super(ex);
		this.setFormatter(formatter);
		this.setAttributeName(attributeName);
	}

	/**
	 * 생성자
	 * 
	 * @param formatter 예외가 발생한 formatter 객체
	 * @param attributeName 예외가 발생한 attribute 명
	 * @param msg 발생 예외 메시지
	 */
	public FormatterException(Formatter formatter, String attributeName, String msg) {
		this(formatter, attributeName, new Exception(msg));
	}
	
	/**
	 * 생성자
	 * 
	 * @param formatter 예외가 발생한 formatter 객체
	 * @param ex 발생 예외
	 */
	public FormatterException(Formatter formatter, Exception ex) {
		this(formatter, null, ex);
	}
	
	/**
	 * 생성자
	 * 
	 * @param formatter 예외가 발생한 formatter 객체
	 * @param msg 발생 예외 메시지
	 */
	public FormatterException(Formatter formatter, String msg) {
		this(formatter, new Exception(msg));
	}
	

	@Override
	public String getMessage() {
		String message = this.getLocMessage() + "\n"
				+ super.getMessage();
		return message;
	}
	
	/**
	 * Exception이 발생한 Formatter의 Line 번호를 반환
	 * 
	 * @return Exception이 발생한 Formatter의 Line 번호
	 */
	public int getLineNumber() {
		return this.getFormatter().getLineNumber();
	}
	
	/**
	 * Exception이 발생한 Formatter의 Column 번호를 반환
	 * 
	 * @return Exception이 발생한 Formatter의 Column 번호
	 */
	public int getColumnNumber() {
		return this.getFormatter().getColumnNumber();
	}
	
	/**
	 * 예외 발생 위치를 문자열로 변환하여 반환
	 * 
	 * @return 예외 발생 위치 문자열
	 */
	public String getLocMessage() {
		
		StringBuffer locMessage = new StringBuffer(""); 
		
		locMessage.append("Formatter XML tag at:")
			.append(" Line:")
			.append(this.getLineNumber())
			.append(", Column:")
			.append(this.getColumnNumber())
			.append(", tag name:")
			.append(this.getFormatter().getTagName());
		
		// attribute 이름이 있는 경우에만 출력함
		if(this.getAttributeName() != null) {
			locMessage.append(", attribute name:")
				.append(this.getAttributeName());
		}
		
		return locMessage.toString();
	}

}
