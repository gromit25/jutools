package com.jutools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * EnvUtil 클래스의 테스트 케이스
 * 
 * @author jmsohn
 */
public class EnvUtilTest {

	@Test
	//@SetEnvironmentVariable(key = "CONFIG_NAME", value = "john doe")
	public void testEnvPrimitiveString() {
		
		try {
			
			EnvUtil.set(Config.class);
			assertEquals("john doe", Config.NAME);
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			fail("exception is ocurred");
		}
	}
	
	@Test
	//@SetEnvironmentVariable(key = "CONFIG_BOOLEAN", value = "true")
	public void testEnvPrimitiveBoolean() {
		
		try {
			
			EnvUtil.set(Config.class);
			assertEquals(true, Config.BOOLEAN_VALUE);
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			fail("exception is ocurred");
		}
	}
	
	@Test
	//@SetEnvironmentVariable(key = "CONFIG_INT", value = "123")
	public void testEnvPrimitiveInt() {
		
		try {
			
			EnvUtil.set(Config.class);
			assertEquals(123, Config.INT_VALUE);
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			fail("exception is ocurred");
		}
	}
	
	@Test
	//@SetEnvironmentVariable(key = "CONFIG_FLOAT", value = "123")
	public void testEnvPrimitiveFloat() {
		
		try {
			
			EnvUtil.set(Config.class);
			assertEquals((float)123, Config.FLOAT_VALUE, 0);
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			fail("exception is ocurred");
		}
	}
	
	@Test
	//@SetEnvironmentVariable(key = "CONFIG_CLASS", value = "java.lang.Integer")
	public void testEnvMethod() {
		
		try {
			
			EnvUtil.set(Config.class);
			assertEquals(Integer.class, Config.CLASS_VALUE);
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			fail("exception is ocurred");
		}
	}
	
	@Test
	//@SetEnvironmentVariable(key = "CONFIG_STR_LIST", value = "john doe, hong gil-dong, jang gil-san")
	public void testEnvStrArray() {
		
		try {
			
			EnvUtil.set(Config.class);
			
			assertEquals(3, Config.STR_LIST.length);
			assertEquals("john doe", Config.STR_LIST[0]);
			assertEquals("hong gil-dong", Config.STR_LIST[1]);
			assertEquals("jang gil-san", Config.STR_LIST[2]);
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			fail("exception is ocurred");
		}
	}
	
	@Test
	//@SetEnvironmentVariable(key = "CONFIG_INT_LIST", value = "1, 2, 3")
	public void testEnvIntArray() {
		
		try {
			
			EnvUtil.set(Config.class);
			
			assertEquals(3, Config.INT_LIST.length);
			assertEquals(1, Config.INT_LIST[0]);
			assertEquals(2, Config.INT_LIST[1]);
			assertEquals(3, Config.INT_LIST[2]);
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			fail("exception is ocurred");
		}
	}
	
	@Test
	//@SetEnvironmentVariable(key = "CONFIG_CLASSES", value = "java.lang.Integer, java.lang.Float")
	public void testEnvMethodArray() {
		
		try {
			
			EnvUtil.set(Config.class);
			
			assertEquals(2, Config.CLASSES_VALUE.length);
			assertEquals(Integer.class, Config.CLASSES_VALUE[0]);
			assertEquals(Float.class, Config.CLASSES_VALUE[1]);
			
		} catch(Exception ex) {
			
			ex.printStackTrace();
			fail("exception is ocurred");
		}
	}

}
