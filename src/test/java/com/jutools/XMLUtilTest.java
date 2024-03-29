package com.jutools;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.junit.Test;

import com.jutools.xml.XMLArray;
import com.jutools.xml.XMLNode;

/**
 * XMLUtil 클래스의 테스트 케이스
 * 
 * @author jmsohn
 */
public class XMLUtilTest {
	
	private static final String XML_TEXT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
			+ "<bookstore>\r\n"
			+ "  <book category=\"역사\">\r\n"
			+ "    <isbn>0001</isbn>\r\n"
			+ "    <title lang=\"en\">Historiae</title>\r\n"
			+ "    <author>Author 1</author>\r\n"
			+ "    <year>2001</year>\r\n"
			+ "    <price>29.99</price>\r\n"
			+ "  </book>\r\n"
			+ "  <book category=\"역사\">\r\n"
			+ "    <isbn>0002</isbn>\r\n"
			+ "    <title lang=\"kr\">삼국사기</title>\r\n"
			+ "    <author>김부식</author>\r\n"
			+ "    <year>1145</year>\r\n"
			+ "    <price>39.95</price>\r\n"
			+ "  </book>\r\n"
			+ "  <book category=\"수학\">\r\n"
			+ "    <isbn>0003</isbn>\r\n"
			+ "    <title lang=\"kr\">공업수학-1</title>\r\n"
			+ "    <author>장길산</author>\r\n"
			+ "    <year>2006</year>\r\n"
			+ "    <price>39.95</price>\r\n"
			+ "  </book>\r\n"
			+ "  <book category=\"수학\">\r\n"
			+ "    <isbn>0004</isbn>\r\n"
			+ "    <title lang=\"kr\">공업수학-2</title>\r\n"
			+ "    <author>장길산</author>\r\n"
			+ "    <year>2018</year>\r\n"
			+ "    <price>39.95</price>\r\n"
			+ "  </book>\r\n"
			+ "  <book category=\"수학\">\r\n"
			+ "    <isbn>0005</isbn>\r\n"
			+ "    <title lang=\"kr\">위상 수학</title>\r\n"
			+ "    <author>홍길동</author>\r\n"
			+ "    <year>2010</year>\r\n"
			+ "    <price>39.95</price>\r\n"
			+ "  </book>\r\n"
			+ "  <book category=\"컴퓨터공학\">\r\n"
			+ "    <isbn>0006</isbn>\r\n"
			+ "    <title lang=\"kr\">오토마타</title>\r\n"
			+ "    <author>손시연</author>\r\n"
			+ "    <year>2023</year>\r\n"
			+ "    <price>39.95</price>\r\n"
			+ "  </book>\r\n"
			+ "  <book category=\"역사\">\r\n"
			+ "    <isbn>0007</isbn>\r\n"
			+ "    <title lang=\"kr\">삼국유사</title>\r\n"
			+ "    <author>일'연</author>\r\n"
			+ "    <year>1310</year>\r\n"
			+ "    <price>39.95</price>\r\n"
			+ "  </book>\r\n"
			+ "</bookstore>";
	
	private static final String XML_NAMESPACE_TEXT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
			+ "<bookstore>\r\n"
			+ "  <ns:book category=\"역사\">\r\n"
			+ "    <isbn>0001</isbn>\r\n"
			+ "    <title lang=\"en\">Historiae</title>\r\n"
			+ "    <author>Author 1</author>\r\n"
			+ "    <year>2001</year>\r\n"
			+ "    <price>29.99</price>\r\n"
			+ "  </ns:book>\r\n"
			+ "  <ns:book category=\"역사\">\r\n"
			+ "    <isbn>0002</isbn>\r\n"
			+ "    <title lang=\"kr\">삼국사기</title>\r\n"
			+ "    <author>김부식</author>\r\n"
			+ "    <year>1145</year>\r\n"
			+ "    <price>39.95</price>\r\n"
			+ "  </ns:book>\r\n"
			+ "</bookstore>";
	
	/**
	 *
	 * 
	 * @param books
	 */
	private static boolean checkISBNs(XMLArray books, String... isbns) throws Exception {
		
		for(String isbn: isbns) {
			
			int count = books.select("isbn(#text='" + isbn + "')").size();
			
			if(count == 0) {
				return false;
			}
		}

		return true;
	}
	
	@Test
	public void testSelect1() throws Exception {
		
		XMLArray books = XMLUtil
				.getRootNode(new ByteArrayInputStream(XML_TEXT.getBytes()))
				.select("book");
		
		assertTrue(checkISBNs(books, "0001", "0002", "0003", "0004", "0005", "0006", "0007"));
	}

	@Test
	public void testSelect2() throws Exception {
		
		XMLArray books = XMLUtil
				.getRootNode(new ByteArrayInputStream(XML_TEXT.getBytes()))
				.select("book(category='역사')");
		
		assertTrue(checkISBNs(books, "0001", "0002", "0007"));
	}
	
	@Test
	public void testSelect3() throws Exception {
		
		XMLArray books = XMLUtil
				.getRootNode(new ByteArrayInputStream(XML_TEXT.getBytes()))
				.select("book(category=w'역?')");
		
		assertTrue(checkISBNs(books, "0001", "0002", "0007"));
	}
	
	@Test
	public void testSelect4() throws Exception {
		
		XMLArray books = XMLUtil
				.getRootNode(new ByteArrayInputStream(XML_TEXT.getBytes()))
				.select("book(category=p'컴.{4}')");
		
		assertTrue(checkISBNs(books, "0006"));
	}


	@Test
	public void testSelect999() throws Exception {
		
		XMLArray books = XMLUtil
				.getRootNode(new ByteArrayInputStream(XML_TEXT.getBytes()))
				.select("book > author(#text='일\\'연')")
				.getParents();
		
		assertTrue(checkISBNs(books, "0007"));
	}
	
	@Test
	public void test() throws Exception {
		
		XMLArray printNodes = XMLUtil
					.getRootNode(new File("resources/publisher/testformat.xml"))
					.select("foreach>style>print");
		
		for(XMLNode node: printNodes) {
			System.out.println(node.getAttribute("exp"));
		}
	}
	
	@Test
	public void test1() throws Exception {
		
		XMLNode rootNode = XMLUtil
					.getRootNode(new File("resources/publisher/testformat.xml"));

		System.out.println(rootNode.getText());
	}
	
	@Test
	public void testNamespace1() throws Exception {
		
		XMLArray books = XMLUtil
				.getRootNode(new ByteArrayInputStream(XML_NAMESPACE_TEXT.getBytes()))
				.select("ns:book");

		System.out.println(books.getFirst());
	}
}
