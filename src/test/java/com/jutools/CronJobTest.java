package com.jutools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.jutools.CronJob.CronExp;

/**
 * CronJob 클래스의 테스트 케이스
 * 
 * @author jmsohn
 */
public class CronJobTest {

	@Test
	public void testCronJob1() throws Exception {
		
		// 테스트에 1분 10초 걸림
		
		CronJob job = CronJob.builder()
			.cronExp("* * * * * *")
			.job(new Runnable() {
				@Override
				public void run() {
					try {
						long cur = System.currentTimeMillis();
						System.out.println("CRON:" + DateUtil.getTimeStr(cur));
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			})
			.build();
		
		job.run();
		
		Thread.sleep(1500);	// 1.5초 뒤 중단
		job.stop();
		
		assertTrue(true);
	}
	
	@Test
	public void testCronExp1_1() throws Exception {
		
		Calendar baseTime = new GregorianCalendar();
		baseTime.set(Calendar.YEAR, 2023);
		baseTime.set(Calendar.MONTH, 5 - 1);	// 5월
		baseTime.set(Calendar.DAY_OF_MONTH, 10);
		baseTime.set(Calendar.HOUR_OF_DAY, 14);
		baseTime.set(Calendar.MINUTE, 59);
		baseTime.set(Calendar.SECOND, 40);
		
		CronExp exp = CronExp.create("* * * * *");
		long nextTime = exp.getNextTimeInMillis(baseTime);
		
		assertEquals("2023-05-10", DateUtil.getDateStr(nextTime));
		assertEquals("15:00:00", DateUtil.getTimeStr(nextTime));
	}
	
	@Test
	public void testCronExp2_1() throws Exception {
		
		Calendar baseTime = new GregorianCalendar();
		baseTime.set(Calendar.YEAR, 2023);
		baseTime.set(Calendar.MONTH, 5 - 1);	// 5월
		baseTime.set(Calendar.DAY_OF_MONTH, 10);
		baseTime.set(Calendar.HOUR_OF_DAY, 14);
		baseTime.set(Calendar.MINUTE, 16);
		baseTime.set(Calendar.SECOND, 40);
		
		CronExp exp = CronExp.create("0,15,30,45 * * * *");
		long nextTime = exp.getNextTimeInMillis(baseTime);
		
		assertEquals("2023-05-10", DateUtil.getDateStr(nextTime));
		assertEquals("14:30:00", DateUtil.getTimeStr(nextTime));
	}
	
	@Test
	public void testCronExp2_2() throws Exception {
		
		Calendar baseTime = new GregorianCalendar();
		baseTime.set(Calendar.YEAR, 2023);
		baseTime.set(Calendar.MONTH, 5 - 1);	// 5월
		baseTime.set(Calendar.DAY_OF_MONTH, 10);
		baseTime.set(Calendar.HOUR_OF_DAY, 14);
		baseTime.set(Calendar.MINUTE, 16);
		baseTime.set(Calendar.SECOND, 40);
		
		CronExp exp = CronExp.create("15 * * * 7");
		long nextTime = exp.getNextTimeInMillis(baseTime);
		
		assertEquals("2023-05-14", DateUtil.getDateStr(nextTime));
		assertEquals("00:15:00", DateUtil.getTimeStr(nextTime));
	}
	
	@Test
	public void testCronExp2_3() throws Exception {
		
		Calendar baseTime = new GregorianCalendar();
		baseTime.set(Calendar.YEAR, 2023);
		baseTime.set(Calendar.MONTH, 2 - 1);	// 2월
		baseTime.set(Calendar.DAY_OF_MONTH, 28);
		baseTime.set(Calendar.HOUR_OF_DAY, 23);
		baseTime.set(Calendar.MINUTE, 59);
		baseTime.set(Calendar.SECOND, 30);
		
		CronExp exp = CronExp.create("15 * 1 * *");
		long nextTime = exp.getNextTimeInMillis(baseTime);
		
		assertEquals("2023-03-01", DateUtil.getDateStr(nextTime));
		assertEquals("00:15:00", DateUtil.getTimeStr(nextTime));
	}
	
	@Test
	public void testCronExp3_1() throws Exception {
		
		Calendar baseTime = new GregorianCalendar();
		baseTime.set(Calendar.YEAR, 2023);
		baseTime.set(Calendar.MONTH, 5 - 1);	// 5월
		baseTime.set(Calendar.DAY_OF_MONTH, 10);
		baseTime.set(Calendar.HOUR_OF_DAY, 14);
		baseTime.set(Calendar.MINUTE, 16);
		baseTime.set(Calendar.SECOND, 40);
		
		CronExp exp = CronExp.create("*/15 * * * *");
		long nextTime = exp.getNextTimeInMillis(baseTime);
		
		assertEquals("2023-05-10", DateUtil.getDateStr(nextTime));
		assertEquals("14:30:00", DateUtil.getTimeStr(nextTime));
	}
	
	@Test
	public void testCronExp3_2() throws Exception {
		
		Calendar baseTime = new GregorianCalendar();
		baseTime.set(Calendar.YEAR, 2023);
		baseTime.set(Calendar.MONTH, 5 - 1);	// 5월
		baseTime.set(Calendar.DAY_OF_MONTH, 10);
		baseTime.set(Calendar.HOUR_OF_DAY, 14);
		baseTime.set(Calendar.MINUTE, 16);
		baseTime.set(Calendar.SECOND, 40);
		
		CronExp exp = CronExp.create("*/15 18 * * *");
		long nextTime = exp.getNextTimeInMillis(baseTime);
		
		assertEquals("2023-05-10", DateUtil.getDateStr(nextTime));
		assertEquals("18:00:00", DateUtil.getTimeStr(nextTime));
	}
	
	@Test
	public void testCronExp3_3() throws Exception {
		
		Calendar baseTime = new GregorianCalendar();
		baseTime.set(Calendar.YEAR, 2023);
		baseTime.set(Calendar.MONTH, 5 - 1);	// 5월
		baseTime.set(Calendar.DAY_OF_MONTH, 10);
		baseTime.set(Calendar.HOUR_OF_DAY, 14);
		baseTime.set(Calendar.MINUTE, 16);
		baseTime.set(Calendar.SECOND, 40);
		
		CronExp exp = CronExp.create("*/15 * 11 * *");
		long nextTime = exp.getNextTimeInMillis(baseTime);
		
		assertEquals("2023-05-11", DateUtil.getDateStr(nextTime));
		assertEquals("00:00:00", DateUtil.getTimeStr(nextTime));
	}
	
	@Test
	public void testCronExp4_1() throws Exception {
		
		Calendar baseTime = new GregorianCalendar();
		baseTime.set(Calendar.YEAR, 2023);
		baseTime.set(Calendar.MONTH, 5 - 1);	// 5월
		baseTime.set(Calendar.DAY_OF_MONTH, 10);
		baseTime.set(Calendar.HOUR_OF_DAY, 14);
		baseTime.set(Calendar.MINUTE, 16);
		baseTime.set(Calendar.SECOND, 40);
		
		CronExp exp = CronExp.create("15-30 * * 6 *");
		long nextTime = exp.getNextTimeInMillis(baseTime);
		
		assertEquals("2023-06-01", DateUtil.getDateStr(nextTime));
		assertEquals("00:15:00", DateUtil.getTimeStr(nextTime));
	}
}
