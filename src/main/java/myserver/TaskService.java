package myserver;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;

/**
 * @program: Learning
 * @description:
 * @author: 作者
 * @create: 2023-07-20 16:20
 */
public class TaskService implements Runnable{

    private Logger logger = Logger.getLogger(myserver.TaskService.class);

    private Socket s;
    private InputStream iis;
    private OutputStream oos;
    private boolean flag = true;

    public TaskService(Socket s){
        this.s = s;
        try {
            this.iis = s.getInputStream();
            this.oos = s.getOutputStream();
        } catch (Exception e) {
            logger.error("socket获取流异常");
            e.printStackTrace();
            flag = false;
        }


    }

    @Override
    public void run() {

        //TODO:Connection:keep-alive
        if(this.flag){
            //HttpServletRequest中解析出所有的信息(method,资源地址url，http版本，头域,参数paramter)
            //存在 HttpRequest 对象中
            MyHttpServletRequest request = new MyHttpServletRequest(this.iis,this.s);
            //响应  本地地址+资源地址url读取文件  拼接http响应 以流的形式网传给客户端
            MyHttpServletResponse response = new MyHttpServletResponse(request,this.oos);
            response.send();
        }
        try {
            iis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
