package edu.kndev.numbercite.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;  
  
/** 
 * ���ڴ���Runtime.getRuntime().exec�����Ĵ������������ 
 * @author
 * 
 */  
public class StreamGobbler extends Thread {  
	String consoleout;
    InputStream is;  
    String type;  
    OutputStream os;  
          
    StreamGobbler(InputStream is, String type) {  
        this(is, type, null);  
    }  
  
    StreamGobbler(InputStream is, String type, OutputStream redirect) {  
        this.is = is;  
        this.type = type;  
        this.os = redirect;  
    }  
      
    public void run() {  
    	StringBuilder sb = new StringBuilder();
        InputStreamReader isr = null;  
        BufferedReader br = null;  
        PrintWriter pw = null;  
        try {  
            if (os != null)  
                pw = new PrintWriter(os);  
            isr = new InputStreamReader(is);  
            br = new BufferedReader(isr);  
            String line=null;  
            while ( (line = br.readLine()) != null) {  
                if (pw != null)  
                    pw.println(line);  
                //System.out.println(line);
                sb.append(line).append("\r\n");
            }
            consoleout = sb.toString();
            if (pw != null)  
                pw.flush();  
        } catch (IOException ioe) {  
            ioe.printStackTrace();    
        } finally{  
            try {  
                if(pw!=null)  
                    pw.close();  
                if(br!=null)  
                    br.close();  
                if(isr!=null)  
                    isr.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    }

	public String getConsoleout() {
		return consoleout;
	}  
}   