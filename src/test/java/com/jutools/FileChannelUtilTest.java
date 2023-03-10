package com.jutools;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

class FileChannelUtilTest {

	@Test
	void testRead1() throws Exception {
		
		File file = new File("resources/read_test.txt");
		
		try(
			FileChannel chnl = FileChannel.open(file.toPath(), StandardOpenOption.READ);
			FileChannelUtil util = new FileChannelUtil(chnl);
		) {
			
			int readCnt = 0;
			while(util.readLine() != null) {
				readCnt++;
			}
			
			assertEquals(46, readCnt);
			
		} catch(Exception ex) {
			fail("exception is occured");
		}
	}
	
	@Test
	void testRead2() throws Exception {
		
		File file = new File("resources/read_test.txt");
		
		try(
			FileChannel chnl = FileChannel.open(file.toPath(), StandardOpenOption.READ);
			FileChannelUtil util = new FileChannelUtil(chnl, 10);
		) {
			
			int readCnt = 0;
			while(util.readLine() != null) {
				readCnt++;
			}
			
			assertEquals(46, readCnt);
			
		} catch(Exception ex) {
			fail("exception is occured");
		}
	}
	
	@Test
	void testRead3() throws Exception {
		
		File file = new File("resources/read_test.txt");
		
		try(
			FileChannelUtil util = new FileChannelUtil(file, StandardOpenOption.READ);
		) {
			
			int readCnt = 0;
			while(util.readLine() != null) {
				readCnt++;
			}
			
			assertEquals(46, readCnt);
			
		} catch(Exception ex) {
			fail("exception is occured");
		}
	}
	
	@Test
	void testRead4() throws Exception {
		
		File file = new File("resources/read_test.txt");
		
		try(
			FileChannelUtil util = new FileChannelUtil(file, 10, StandardOpenOption.READ);
		) {
			
			int readCnt = 0;
			while(util.readLine() != null) {
				readCnt++;
			}
			
			assertEquals(46, readCnt);
			
		} catch(Exception ex) {
			fail("exception is occured");
		}
	}

}
