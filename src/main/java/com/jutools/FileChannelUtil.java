package com.jutools;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * file channel 처리 관련 utility 클래스
 * 
 * @author jmsohn
 */
public class FileChannelUtil implements Closeable {
	
	/** 입출력을 위한 file channel */
	private FileChannel chnl;
	/** 입출력에 사용할 byte buffer */
	private ByteBuffer buffer;
	/** 입출력에 사용할 character set */
	private Charset charset;
	
	// 읽기 작업을 위한 속성들
	/** 현재 읽은 line을 임시저장하기 위한 큐 */
	private Queue<String> lines = new LinkedList<String>(); 
	/** 이전 읽을 데이터를 임시저장 */
	private byte[] preRead;
	/** 모두 읽었는지 여부 */
	private boolean isReadEnd;
	
	/**
	 * 생성자
	 * 
	 * @param chnl 입출력을 위한 file channel
	 * @param buffer 입출력에 사용할 byte buffer
	 * @param charset 입출력에 사용할 character set
	 */
	public FileChannelUtil(FileChannel chnl, ByteBuffer buffer, Charset charset) throws Exception {
		
		if(chnl == null) {
			throw new NullPointerException("");
		}
		
		if(buffer == null) {
			throw new NullPointerException("");
		}
		
		if(charset == null) {
			throw new NullPointerException("");
		}
		
		this.chnl = chnl;
		this.buffer = buffer;
		this.charset = charset;
		this.isReadEnd = false;
	}
	
	/**
	 * 생성자
	 * 
	 * @param chnl 입출력을 위한 file channel
	 * @param capacity 입출력에 사용할 byte buffer의 크기
	 * @param charset 입출력에 사용할 character set
	 */
	public FileChannelUtil(FileChannel chnl, int capacity, Charset charset) throws Exception {
		this(chnl, ByteBuffer.allocateDirect(capacity), charset);
	}
	
	/**
	 * 생성자
	 * 
	 * @param chnl 입출력을 위한 file channel
	 * @param buffer 입출력에 사용할 byte buffer
	 */
	public FileChannelUtil(FileChannel chnl, ByteBuffer buffer) throws Exception {
		this(chnl, buffer, Charset.defaultCharset());
	}
	
	/**
	 * 생성자
	 * 
	 * @param chnl 입출력을 위한 file channel
	 * @param capacity 입출력에 사용할 byte buffer의 크기
	 */
	public FileChannelUtil(FileChannel chnl, int capacity) throws Exception {
		this(chnl, capacity, Charset.defaultCharset());
	}
	
	/**
	 * 생성자
	 * 
	 * @param chnl 입출력을 위한 file channel
	 */
	public FileChannelUtil(FileChannel chnl) throws Exception {
		this(chnl, 1024 * 1024);
	}
	
	/**
	 * 생성자
	 * 
	 * @param file 입출력 file
	 * @param buffer 입출력에 사용할 byte buffer
	 * @param charset 입출력에 사용할 character set
	 * @param options channel open options
	 */
	public FileChannelUtil(File file, ByteBuffer buffer, Charset charset, OpenOption... options) throws Exception {
		this(FileChannel.open(file.toPath(), options), buffer, charset);
	}

	/**
	 * 생성자
	 * 
	 * @param file 입출력 file
	 * @param capacity 입출력에 사용할 byte buffer의 크기
	 * @param charset 입출력에 사용할 character set
	 * @param options channel open options
	 */
	public FileChannelUtil(File file, int capacity, Charset charset, OpenOption... options) throws Exception {
		this(FileChannel.open(file.toPath(), options), capacity, charset);
	}
	
	/**
	 * 생성자
	 * 
	 * @param file 입출력 file
	 * @param buffer 입출력에 사용할 byte buffer
	 * @param options channel open options
	 */
	public FileChannelUtil(File file, ByteBuffer buffer, OpenOption... options) throws Exception {
		this(FileChannel.open(file.toPath(), options), buffer);
	}
	
	/**
	 * 생성자
	 * 
	 * @param file 입출력 file
	 * @param capacity 입출력에 사용할 byte buffer의 크기
	 * @param options channel open options
	 */
	public FileChannelUtil(File file, int capacity, OpenOption... options) throws Exception {
		this(FileChannel.open(file.toPath(), options), capacity);
	}
	
	/**
	 * 생성자
	 * 
	 * @param file 입출력 file
	 * @param options channel open options
	 */
	public FileChannelUtil(File file, OpenOption... options) throws Exception {
		this(FileChannel.open(file.toPath(), options));
	}


	/**
	 * 설정된 file channel에 문자열을 쓰는 메소드
	 * 
	 * @param msg file channel에 출력할 문자열
	 */
	public void write(String msg) throws Exception {

		byte[] msgBytes = msg.getBytes();

		int start = 0;
		int remains = msgBytes.length;

		while(remains > 0) {

			int length = msgBytes.length - start;

			if(length > buffer.limit()) {
				length = this.buffer.limit();
			}

			this.buffer.clear();
			this.buffer.put(msgBytes, start, length);
			this.buffer.flip();

			while(buffer.hasRemaining() == true) {
				this.chnl.write(buffer);
			}

			remains -= length;
			start += length;
			
		} // end of while

	}
	
	/**
	 * 설정된 file channel에서 한줄씩 읽어서 반환, 다 읽었을 경우 null 반환
	 * 한줄의 끝은 lineEnd에 설정
	 * 
	 * @param lineEnd 읽을 줄에 대한 구분자
	 * @return FileChannel에서 읽은 한줄
	 */
	public String readLine(String lineEnd) throws Exception {
		
		// 입력값 검증
		if(lineEnd == null) {
			throw new NullPointerException("line end is null"); 
		}
		
		if(lineEnd.isEmpty() == true) {
			throw new Exception("line end is not defined");
		}
		
		// 읽기가 이미 종료 되었으면 null 반환
		if(this.isReadEnd == true) {
			return null;
		}
		
		// 이전에 읽은 line이 있으면,
		// 이전 읽은 line을 반환
		if(this.lines.isEmpty() == false) {
			return this.lines.poll();
		}
		
		// 1. lineEnd가 포함될 때까지 읽거나 채널의 데이터를 끝날때까지 읽음
		byte[] read = this.preRead;
		this.preRead = null;
		
		byte[] lineEndBytes = lineEnd.getBytes();
		
		boolean isFirst = true;
		
		do {
			
			// -1이면 더이상 읽을 데이터가 없음
			if(this.chnl.read(this.buffer) == -1) {
				
				this.isReadEnd = true;
				
				// 처음 부터 읽을 것이 없으면 즉시 null 반환
				// preRead에 데이터가 있으면 preRead의 데이터를 반환
				if(isFirst == true) {
					
					if(read != null && read.length != 0) {
						return new String(read, this.charset);
					} else {
						return null;
					}
				}
				
				break;
			}
			
			isFirst = false;
			
			// buffer의 데이터를 byte 배열에 복사
			this.buffer.flip();  // Limit:Position, Position:0 로 변경  
			byte[] readNow = new byte[this.buffer.remaining()];  // remain: Limit - Position
			this.buffer.get(readNow); //buffer의 데이터를 byte 배열로 복사
			this.buffer.clear(); // buffer를 비움
			
			// 이전에 읽은 배열과 합침
			read = BytesUtil.concat(read, readNow);
			
		} while(BytesUtil.contains(read, lineEndBytes) == false); // 읽은 문자열 내에 lineEnd가 없는 경우 다시 읽음 
		
		// 2. 읽은 데이터를 구분자(lineEnd)로 분리함
		//    -> 0번째는 반환용으로 저장
		//    -> 중간은 큐(this.lines)에 저장
		//    -> 마지막은 이전 읽은 데이터(this.preRead)에 저장
		
		String line = null;
		ArrayList<byte[]> byteslines = BytesUtil.split(read, lineEndBytes, true);
		
		for(int index = 0; index < byteslines.size(); index++) {
			
			byte[] bytesline = byteslines.get(index);
			
			if(index == 0) {
				
				// 0번째는 반환용으로 저장
				line = new String(bytesline, this.charset);
				
			} else {
				
				if(index == byteslines.size() - 1) {
					// 마지막은 이전 읽은 데이터(this.preRead)에 저장
					this.preRead = bytesline;
				} else {
					// 중간은 큐(this.lines)에 저장
					this.lines.offer(new String(bytesline, this.charset));
				}
			}
			
		}
		
		return line;
	}
	
	/**
	 * 설정된 file channel에서 한줄씩 읽어서 반환, 다 읽었을 경우 null 반환
	 * 한줄의 끝은 "\r\n"
	 * 
	 * @return FileChannel에서 읽은 한줄
	 */
	public String readLine() throws Exception {
		return this.readLine("\r\n");
	}

	/**
	 * close file channel 
	 */
	@Override
	public void close() throws IOException {
		if(this.chnl.isOpen() == true) {
			this.chnl.close();
		}
	}

}