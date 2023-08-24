# JUTools(Java Utility Tools)
Java 관련 Utility 기능 모음     
JDK 1.8 기준으로 작성     
주의) lombok:1.18.26 을 사용하고 있음. IDE에 설정 필요    

### BytesUtil   
----------------------------------    
> TCP/UDP 통신, File read/write 등에서 byte 배열을 다루기 위한 Utility   
```java
ByteUtil.concat(bytes1, bytes2);    // bytes1, bytes2 배열을 합쳐서 바이트 배열로 반환
ByteUtil.cut(bytes1, 10, 20);       // bytes1의 10번째 부터 20번째까지 잘라서 바이트 배열로 반환
ByteUtil.split(bytes1, bytes2);     // bytes1을 bytes2로 나우어 바이트 배열의 Array로 반환

BytesUtil.contains(bytes1, bytes2);  // bytes1 내에 bytes2가 포함되어 있는지 여부를 반환 
BytesUtil.indexOf(bytes1, bytes2);   // bytes1 내에 bytes2가 처음으로 발견되는 위치를 반환
BytesUtil.endsWith(bytes1, bytes2);  // bytes1이 bytes2로 끝나는지 여부를 반환

BytesUtil.readAllBytes(new File("C:\\test.txt"));   // 파일의 모든 내용을 읽어 바이트 배열로 반환(스트림도 가능)
BytesUtil.readNBytes(new File("C:\\test.txt"), 10); // 파일의 내용 중 N 바이트까지만 읽어 반환(스트림도 가능)
```

### NIOBufferUtil   
----------------------------------    
> NIO Buffer의 메소드 중 일부가 1.8 버전에 없는 경우가 있어     
> 버전에 상관없이 지원하기 위한 용도로 만듦    
> JDK 1.8 이상에서 해당 메소드를 지원하면, 해당 메소드를 호출하도록 함
```java
NIOBufferUtil.flip(buffer);
NIOBufferUtil.clear(buffer);
```

### FileChannelUtil   
----------------------------------    
> 파일 채널을 통한 read/write Utility    
> read    
```java
try(
    FileChannelUtil channel = new FileChannelUtil(file, StandardOpenOption.READ);
) {
    String read = null;
    while((read = channel.readLine()) != null) {
        // 읽은 문자열 처리
        System.out.println(read);
    }
}
```
```java
try(
    FileChannelUtil channel = new FileChannelUtil(file, StandardOpenOption.READ);
) {
    channel.readLine(read -> {
        // 읽은 문자열 처리
        System.out.println(read);
    });
}
```
> write
```java
// write할 파일
File file = new File("resources/write_test.txt");
// 파일 채널 오픈 옵션
OpenOption[] options = {StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE};

try(
    FileChannelUtil channel = new FileChannelUtil(file, options);
) {
    channel.write("안녕하세요.");
}
```

### EnvUtil   
----------------------------------
> 환경변수를 클래스의 static 변수에 설정해 주는 Utility
```
SET NAME=HONG GIL-DONG
```

```java
public class Config {
    @Env(name = "NAME")          // "NAME" 환경변수 값을 Config.NAME에 설정하기 위한 어노테이션
    public static String NAME;
}

public class Test {
    public static void main(String[] args) throws Exception {
        EnvUtil.set(Config.class);       // 환경 변수
        System.out.println(Config.NAME); // "HONG GIL-DONG"을 출력
    }
}
```

### CipherUtil   
----------------------------------    
> 암복호화 Utility    
> * 양방향 암복호화 : AES 및 AES 키 생성
> * 단방향 암호화 : SHA   
>
> AES
```java
// AES 키 생성
String key = CipherUtil.genAES128Key();
// 암복호화할 문장
String msg = "테스트 입니다.";

// AES 암호화
String encryptedMsg = CipherUtil.encryptAES(key, msg);
System.out.println(encryptedMsg);

// AES 복호화
String decryptedMsg = CipherUtil.decryptAES(key, encryptedMsg);
System.out.println(decryptedMsg);
```
> SHA
```java

// 암호화할 문장
String msg = "Hello World";

// SHA 암호화 수행
String encryptedMsg = CipherUtil.encryptSHA512(msg);
System.out.println(encryptedMsg);
```

### CronJob   
----------------------------------    
> 일정시간 간격으로 Thread를 실행시키는 Utility    
> 리눅스 크론잡과 동일한 형식으로 설정 가능
```java
CronJob.builder()
    .cronExp("0 1,2 * * *")  // 매일 1시, 2시에 수행
    .job(new Runnable() {
        @Override
        public void run() {
            // 수행할 내용
            System.out.println("Hello world");
        }
    })
    .build().run();
```

### FileTracker   
----------------------------------    
> 파일의 변경사항에 대해 추적(Tracking)하는 Utility     
> tail -f 와 같은 기능임, 로그 추적 등에서 활용    
```java
FileTracker
    .create("C:\\test.log")
    .tracking(
        msg -> {
            // 파일에 추가된 메시지 처리
            System.out.println(msg);
        }
    );
```

### XMLUtil    
----------------------------------    
> XML 문서의 tag에 대해 쉽게 접근할 수 있는 Utility   
> DOM(Document Object Model) 파서를 기반으로 개발   
> 주요 특징   
> * select 메소드를 통해 XML 노드를 선택 및 조회   
> 포맷) 테그1 > 테그2(속성1='값') > ...   
> * 테그와 속성은 와일드 카드 매칭을 기본으로 함   
> * 값의 매칭은 아래의 세가지 형태를 제공함   
> 1 완전 매칭 : '일연'   
> 2 와일드 카드 매칭 : w'*지문?' -> "을지문덕" 매칭   
> 3 정규표현식 매칭 : p'[0-9]{3}' -> "123" 매칭   

```java
XMLArray books = XMLUtil
    .getRootNode("C:\\test.xml")
    .select("book > auth*(#text='일연')") // book 테그 이하에 author 테그 중 "일연"을 찾아 반환   
    .getParents(); 

// book 테그의 title에 설정된 text 출력
for(XMLNode book: books) {
    System.out.println(book.selectFirst("title").getText());
}
```

### MathUtil
----------------------------------    
> 수학식 관련 Utility    
> 주요 기능   
> * 설정 파일등에 수학식을 이용하여 값을 설정할 수 있도록 지원하기 위함    
> 예를 들어, 로그 파일 최대 사이즈 : 5 * 1024 KiB -> 5242880, "B" 로 분리   
```java
MathResult result = MathUtil.calculateWithUnit("3*4/ 2 + -2 * 2.5 MiB");

System.out.println(result.getValue());      // "1048576" 출력
System.out.println(result.getBaseUnit());   // "B" 출력
```

### StatUtil
----------------------------------    
> 통계 관련 Utility    
> 주요기능    
> * 실시간 모수(Parameter)/통계치(Statistic) 계산 기능    
>   평균, 분산, 표준편차, 왜도(skewness), 첨도(kurtosis) 계산    
>   계산을 위해 모든 데이터를 저장하게 되면 메모리 소요가 증가하기 떄문에    
>   데이터의 합, 제곱합, 세제곱합, 네제곱합과 개수 데이터만 가지고 실시간으로 계산함    
> * 실시간 6시그마 통제도(Control Chart)의 넬슨룰 위반 여부 검사 기능    
>   검사시에 카운팅을 bit 연산으로 하여 메모리 사용을 최소화    
>   [Nelson rules](https://en.wikipedia.org/wiki/Nelson_rules) 참고

```java
// 모수 계산 객체 생성
RTParameter rtparam = StatUtil.createRTParameter();
// 통계량 계산 객체 생성
RTStatistic rtstat = StatUtil.createRTStatistic();
// 넬슨룰 검사 객체 생성 - 여기에서는 rule1과 rule2 만 검사
NelsonRule nrule = StatUtil.createNelsonRule(10, 1, NelsonRule.RULE1|NelsonRule.RULE2);

for(double data: dataList) {

    // 데이터의 모수와 통계량 계산
    rtparam.add(data);
    rtstat.add(data);

    // 모수와 통계량의 개수, 합계, 평균, 분산, 표준편차, 왜도, 첨도를 JSON 형태로 출력
    System.out.println(rtparam);
    System.out.println(rtstat);

    // 넬슨룰 검사
    ArrayList<Integer> violatedRules = nrule.check(data);
    for(Integer ruleIndex: violatedRules) {
        // 넬슨룰 위반 처리
        System.out.println(ruleIndex);
    }
}
```

