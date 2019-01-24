package edu.kndev.numbercite.util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

public class PdftoxmlUtil {  
	public static void main(String[] args) {
		PdftoxmlUtil pdftoxml = new PdftoxmlUtil();
		System.out.println(pdftoxml.pdf2xml("D:\\Tools\\grobid\\grobid-home\\pdf2xml\\win-64\\d.pdf", "/temp/123456768/d.xml", "D:\\Tools\\grobid\\grobid-home\\pdf2xml\\win-64"));
	}

	/**
	 * 将pdf解析成xml
	 * @param command 
	 * @param pdfName
	 * @param htmlName
	 * @param pdftoxmlpath
	 * @return
	 */
	public boolean pdf2xml(String pdfFullPath,String htmlFullPath,String pdftoxmlpath){  
		
		String comm = "pdftoxml.exe "+pdfFullPath+" "+htmlFullPath;
		Runtime rt = Runtime.getRuntime(); 
		Process p = null;  
		try {  
			p = rt.exec("cmd /c " + comm, null, new File(pdftoxmlpath));  
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "");                
			// kick off stderr    
			errorGobbler.start();
			errorGobbler.join();
			if(errorGobbler.getConsoleout().contains("error")) {
				return false;
			}
			StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(), "STDOUT");    
			// kick off stdout    
			outGobbler.start();
			p.waitFor(1,TimeUnit.MINUTES);
//			int w = p.waitFor();  
//			System.out.println(w);  
			int v = p.exitValue();  
			System.out.println(v);
			return true;  
		} catch (Exception e) {  
			e.printStackTrace();  
			try{  
				p.getErrorStream().close();  
				p.getInputStream().close();  
				p.getOutputStream().close();  
			}  
			catch(Exception ee){}  
		}finally{
			try{  
				p.getErrorStream().close();  
				p.getInputStream().close();  
				p.getOutputStream().close();  
			}  
			catch(Exception ee){}  
		}
		return false;  
	}  

}  
