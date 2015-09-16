package panda.com.socketdemo.utils;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * 几种切割字符串方法的比较测试
 *
 * Created by Administrator on 2015/9/16:15:45.
 */
public class StringSpiltTest {
    String url = "HTTP/1.1 200 OK\r\n"
    + "Date: Wed, 16 Sep 2015 07:25:54 GMT\r\n"
    + "Content-Type: text/html\r\n"
    + "Content-Length: 14613\r\n"
    + "Last-Modified: Wed, 03 Sep 2014 02:48:32 GMT\r\n"
    + "Connection: Keep-Alive\r\n"
    + "Vary: Accept-Encoding\r\n"
    + "Set-Cookie: BAIDUID=F450ADD08CF4A773CD153E29DE8E0F01:FG=1; expires=Thu, 31-Dec-37 23:55:55 GMT; max-age=2147483647; path=/; domain=.baidu.com\r\n"
    + "Set-Cookie: BIDUPSID=F450ADD08CF4A773CD153E29DE8E0F01; expires=Thu, 31-Dec-37 23:55:55 GMT; max-age=2147483647; path=/; domain=.baidu.com\r\n"
    + "Set-Cookie: PSTM=1442388354; expires=Thu, 31-Dec-37 23:55:55 GMT; max-age=2147483647; path=/; domain=.baidu.com\r\n"
    + "Set-Cookie: BDSVRTM=0; path=/\r\n"
    + "P3P: CP=\" OTI DSP COR IVA OUR IND COM \"\r\n"
    + "Server: BWS/1.1\r\n"
    + "X-UA-Compatible: IE=Edge,chrome=1\r\n"
    + "Pragma: no-cache\r\n"
    + "Cache-control: no-cache\r\n"
    + "BDPAGETYPE: 1\r\n"
    + "BDQID: 0xd71ec087000031d5\r\n"
    + "BDUSERID: 0\r\n"
    + "Accept-Ranges: bytes\r\n\r\n"
    + "<html>\n"
    + "<head>\n"
    + "<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">\n"
    + "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\">\n"
    + "<link rel=\"dns-prefetch\" href=\"//s1.bdstatic.com\"/>\n"
    + "<link rel=\"dns-prefetch\" href=\"//t1.baidu.com\"/>\n"
    + "<link rel=\"dns-prefetch\" href=\"//t2.baidu.com\"/>\n"
    + "<link rel=\"dns-prefetch\" href=\"//t3.baidu.com\"/>\n"
    + "<link rel=\"dns-prefetch\" href=\"//t10.baidu.com\"/>\n"
    + "<link rel=\"dns-prefetch\" href=\"//t11.baidu.com\"/>";


    /**
     * 测试结果:第三种貌似更优
     *            第一种方法 第二种方法 第三种方法 第四种方法
     * 第1次测试： |-808483 -|-2234362-|-1102375-|-4955938-|
     * 第2次测试： |-395581 -|-418768 -|-379936 -|-2166476-|
     * 第3次测试： |-416254 -|-520457 -|-556216 -|-2394438-|
     * 第4次测试： |-495594 -|-724114 -|-464585 -|-2039366-|
     * 第5次测试： |-552305 -|-396419 -|-384965 -|-1743518-|
     * 第6次测试： |-2400026-|-480508 -|-515987 -|-1799950-|
     * 第7次测试： |-485816 -|-621028 -|-428825 -|-4080407-|
     * 第8次测试： |-425846 -|-456762 -|-383010 -|-4054426-|
     * 第9次测试： |-371555 -|-430781 -|-411784 -|-2001930-|
     * 平  试值:  |-705717.78-|-698113.22-|-513820.33-|-2804049.89-|
     */

    public void check() {
        System.out.println("第四种方法:使用jdk的indexOf切分字符串");
        long st4 = System.nanoTime();
        int k = 0;
        int count = 0;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < url.length(); i++) {
            if (url.substring(i, i + 1).equals("\n")) {
                if (count == 0) {
                    System.out.print(url.substring(0, i) + " ");
                } else if (count == 1) {
                    System.out.print(url.substring(k + 1, i) + " ");
                } else {
                    System.out.print(url.substring(k + 1, i) + " ");
                    System.out.print(url.substring(i + 1, url.length()) + " ");
                }
                k = i;
                count ++;
            }
        }
        System.out.println("\n第四种话费时间:" + (System.nanoTime() - st4));

        System.out.println("第二种方法:使用StringTokenizer切分字符串:");
        long st2 = System.nanoTime();
        StringTokenizer token = new StringTokenizer(url, "\n");
        while (token.hasMoreElements()) {
            System.out.print(token.nextElement() + " ");
        }
        System.out.println("\n第二种话费时间:" + (System.nanoTime() - st2));

        System.out.println("第一种方法:使用jdk的spilt切分字符串:");
        long st1 = System.nanoTime();
        String[] cache1 = url.split("\\n");
        for (int i = 0; i < cache1.length; i++) {
            System.out.print(cache1[i] + " ");
        }
        System.out.println("\n第一种话费时间:" + (System.nanoTime() - st1));


        System.out.println("第三种方法:使用jdk的pattern切分字符串:");
        long st3 = System.nanoTime();
        Pattern pattern = Pattern.compile("\\n");
        String[] cache2 = pattern.split(url);
        for (int i = 0; i < cache2.length; i++) {
            System.out.print(cache2[i] + " ");
        }
        System.out.println("\n第三种话费时间:" + (System.nanoTime() - st3));

    }

}
