package myserver;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: Learning
 * @description:  从输入流中取出http请求，解析出相应的信息，存好
 * @author: 作者
 * @create: 2023-07-20 19:01
 */
public class MyHttpServletRequest {
    private Socket s;
    private Logger logger = Logger.getLogger(MyHttpServletRequest.class);
    private InputStream iis;
    //GET,POST，PUT,DELETE,HEAD,TRACE
    private String method;
    //定位符  http://localhost:01/168adxax/doUpload.action?uname=z&pwd=a
    private String requestURL;
    //标识符
    private String requestURI;
    //上下文  168adxax
    private String contextPath;
    //请求字符串,请求的地址栏参数,   age=20&sex=male
    private String queryString;
    //参数:  地址栏参数  uname=a&pwd=b  表单中的参数-请求实体 ins=打球，游泳，跳舞
    private Map<String,String[]> paramterMap = new ConcurrentHashMap<>();
    //协议类型:    http://
    private String scheme;
    //协议版本
    private String protocol;
    //项目的真实路径
    private String realPath;

    public MyHttpServletRequest(InputStream iis,Socket s){
        this.iis = iis;
        this.s = s;
        this.parseRequest();
    }

    /**
     * 解析方法
     */
    private void parseRequest(){
        String requestInfoString = readFromInputStream();  //从输入流中读取http请求信息(文本)
        if(requestInfoString == null || "".equals(requestInfoString.trim())){
            throw new RuntimeException("读取输入流异常....");
        }
        //2.解析http请求头(存各种信息)
        parseRequestInfoString(requestInfoString);

    }

    /**
     * 解析 http请求头
     * @param requestInfoString
     *             Http请求协议
     *             method 资源地址 协议版本
     *             请求头域:  值*
     *             空行
     *             请求实体
     *             资源地址:  /myweb/index.html?uname=a&pwd=b
     */
    private void parseRequestInfoString(String requestInfoString) {
        StringTokenizer st = new StringTokenizer(requestInfoString); //按空格切割
        this.method = st.nextToken();
        this.requestURI = st.nextToken();
        //requestURI 烤炉地址栏有参数
        int questionIndex = this.requestURI.lastIndexOf("?");
        if(questionIndex>=0){
            //有?，则有地址栏参数  -->参数存 queryString属性
            this.queryString = this.requestURI.substring(questionIndex+1);
            this.requestURI = this.requestURI.substring(0,questionIndex);
        }
        //第三部分:协议版本  HTTP/1.1
        this.protocol = st.nextToken();
        //HTTP
        this.scheme = this.protocol.substring(0,this.protocol.indexOf("/"));
        //requestURI两种情况对应两种不同的contentPath
        //        www.baidu.com  ->  GET /
        //            自己的项目   ->   /168adxax
        int slash2Index = this.requestURI.indexOf("/",1);
        if(slash2Index>=0){
            this.contextPath = this.requestURI.substring(0,slash2Index);
        }else{
            this.contextPath = this.requestURI;
        }
        //requestURL  统一资源定位符  http://ip:端口/requestURI
        this.requestURL = this.scheme + "://" +this.s.getLocalSocketAddress()+this.requestURI;

        //参数的处理  /168adxax/index.html?uname=a&pwd=b
        //从queryString中取出参数
        if(this.queryString!=null && this.queryString.length()>0){
            String []ps = this.queryString.split("&");
            for(String s : ps){
                String [] params = s.split("=");
                this.paramterMap.put(params[0],params[1].split(","));
            }
            //TODO:还有post的实体中也有参数
        }

        //realPath
        this.realPath = System.getProperty("user.dir")+ File.separator+"webapp";
    }


    /**
     *从输入流中读取http请求信息(文字)
     */
    private String readFromInputStream() {
        int length = -1;
        StringBuffer sb = null;
        byte[] bs = new byte[300*1024];   //TODO:300k足够存储  文件上传之外的请求
        try {
            length = this.iis.read(bs, 0, bs.length);
            //byte[] -->String
             sb = new StringBuffer();
            for (int i = 0; i < length; i++) {
                sb.append((char)bs[i]);
            }
        }catch (Exception e){
            logger.error("读取请求信息失败...");
            e.printStackTrace();
        }
        return sb.toString();
    }


    public String getMethod() {
        return method;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getQueryString() {
        return queryString;
    }

    public String[] getParamterValues(String name){
        if(paramterMap==null||paramterMap.size()<0){
            return null;
        }
        String[] values = this.paramterMap.get(name);
        if(values==null||values.length<0){
            return null;
        }
        return values;
    }

    public String getParamter(String name) {
        String []values = getParamterValues(name);
        if(values==null||values.length<0){
            return null;
        }
        return values[0];
    }

    public String getScheme() {
        return scheme;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getRealPath() {
        return realPath;
    }
}
