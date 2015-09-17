# SocketDemo
简单的socket连接http网站的demo<br/>
1、使用socket对http网站进行长连接；<br/>
2、在长连接的过程中、发送http请求头；<br/>
3、接收http返回的响应头、并通过自己封装好的ResponseUtil类、对响应头进行分析处理<br/>
   1)实例化ResponseUtil类:ResponseUtil util = new ResponseUtil(response);<br/>
   2)实例化完即可直接使用工具类里面的方法，例如获取http的响应码 util.getResponseCode();<br/>

更新于2015.9.16：
目前该项目只有简单的对http网络请求、以及字符串的解析；后续会增加字节流的解析，使该demo能适用于更多的场合。
