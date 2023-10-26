package myserver;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @program: Learning
 * @description:
 * @author: 作者
 * @create: 2023-07-20 15:00
 */
public class TomcatServer {
    //创建日志对象
    static Logger logger = Logger.getLogger(myserver.TomcatServer.class);

    public static void main(String [] args){
        logger.trace("程序启动了");
        myserver.TomcatServer ts = new myserver.TomcatServer();
        int port = ts.parsePortFromXml();
        logger.debug("服务器配置端口为:"+port);
        ts.startServer(port);

    }

    private void startServer(int port) {
        boolean flag = true;
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);
            logger.debug("服务器启动成功，配置的端口号为:" + port);
            //TODO:可以读取server.xml文件中关于是否开启线程池的配置来决定是否开启线程池
            while (flag) {
                try {
                    Socket s = ss.accept();
                    logger.debug("客户端:" + s.getRemoteSocketAddress() + "连接上来了");
                    myserver.TaskService task = new myserver.TaskService(s);
                    Thread thread = new Thread(task);
                    thread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("客户端连接失败...");
                }
            }
        } catch (IOException e) {
            logger.error("服务器套接字创建失败...");
            e.printStackTrace();
        }
    }

    private int parsePortFromXml(){
        int port = 8080;
        //方案一:根据字节码的路径(Target/classes/)
        //TomcatServer.class.getClassLoader().getResourceAsStream();
        //方案二:
        String serverXmlPath = System.getProperty("user.dir")+ File.separator+"conf"+File.separator+"server.xml";
        try(
        InputStream iis = new FileInputStream(serverXmlPath);){
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document doc =documentBuilder.parse(iis);
            NodeList nl = doc.getElementsByTagName("Connector");
            for(int i =0;i<nl.getLength();i++){
                Element node = (Element) nl.item(0);
                port=Integer.parseInt(node.getAttribute("port"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
//        logger.info(serverXmlPath);
        return port;
    }
}
