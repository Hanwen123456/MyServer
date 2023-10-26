package myserver;

import java.io.*;

/**
 * @program: Learning
 * @description:
 * @author: 作者
 * @create: 2023-07-21 10:19
 */
public class MyHttpServletResponse {
    private MyHttpServletRequest request;
    private OutputStream oos;

    public MyHttpServletResponse(MyHttpServletRequest request, OutputStream oos) {
        this.request = request;
        this.oos = oos;
    }

    public void send(){
        String uri = this.request.getRequestURI();  //wowotuan/index.html
        String realpath = this.request.getRealPath();  //服务器路径
        File f = new File(realpath,uri);
        byte[] fileContent = null;
        String responseProtocol = null;
        if(!f.exists()){
            //文件不存在  回4xx
            fileContent=readFile(new File(realpath,"/404.html"));
            responseProtocol = gen404(fileContent);
        }else{
            //文件存在  则回2xx
            fileContent=readFile(f);
            responseProtocol = gen200(fileContent);
        }
        try{
        oos.write(responseProtocol.getBytes());
        oos.flush();
        oos.write(fileContent);
        oos.flush();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(oos!=null){
                try {
                    this.oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String gen200(byte[] fileContent) {
        String protocol200 = "";
        //先取出请求的资源的类型
        String uri = this.request.getRequestURI();   //   /wowotuan/index.html
        //从uri中取后缀名
        int index =  uri.lastIndexOf(".");
        if(index>=0){
            index=index+1;
        }
        //TODO:策略模式读取.xml文件中的文件类型配置
        String fileExtension = uri.substring(index);
        if("JPG".equalsIgnoreCase(fileExtension)){
            protocol200 = "HTTP/1.1 200 OK\r\nContent-Type: image/jpeg\r\nContent-length: "+fileContent.length+"\r\n\r\n";
        }
        else if("css".equalsIgnoreCase(fileExtension)){
            protocol200 = "HTTP/1.1 200 OK\r\nContent-Type: text/css\r\nContent-length: "+fileContent.length+"\r\n\r\n";
        } else if("js".equalsIgnoreCase(fileExtension)){
            protocol200 = "HTTP/1.1 200 OK\r\nContent-Type: application/javascript\r\nContent-length: "+fileContent.length+"\r\n\r\n";
        }else if("gif".equalsIgnoreCase(fileExtension)){
            protocol200 = "HTTP/1.1 200 OK\r\nContent-Type: image/gif\r\nContent-length: "+fileContent.length+"\r\n\r\n";
        }else if("PNG".equalsIgnoreCase(fileExtension)){
            protocol200 = "HTTP/1.1 200 OK\r\nContent-Type: image/png\r\nContent-length: "+fileContent.length+"\r\n\r\n";
        }else{
            protocol200 = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-length: "+fileContent.length+"\r\n\r\n";
        }
        return protocol200;
    }

    private String gen404(byte[] fileContent) {
        String protocol = "HTTP/1.1 404 Not Found\r\nContent-type:text/html;charset=utf-8\r\nContent-length: "+fileContent.length+"\r\n";
        protocol+="Server: kitty server\r\n\r\n";
        return protocol;
    }

    /**
     * 读取本地文件
     * @param file
     * @return
     */
    private byte[] readFile(File file){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] bs = new byte[100 * 1024];
            int length = -1;
            while ((length = fis.read(bs, 0, bs.length)) != -1) {
                baos.write(bs, 0, length);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return baos.toByteArray();
    }
}
