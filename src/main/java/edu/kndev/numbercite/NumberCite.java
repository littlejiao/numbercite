package edu.kndev.numbercite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.kndev.numbercite.util.PdftoxmlUtil;
import kndev.model.mongo.paper.Article;
import kndev.model.mongo.paper.Content;
import kndev.model.mongo.paper.Outline;
import kndev.model.mongo.paper.Sentence;

public class NumberCite {
	private Article article = new Article();
	private Outline outline = new Outline();
	private Content content = new Content();
	private List<Outline> firstOutline = new ArrayList<Outline>();
	private List<Content> firstContent = new LinkedList<Content>();
	private List<Sentence> sentences = new ArrayList<Sentence>();
	
	public List<Sentence> getSentences() {
		return sentences;
	}

	public Article getArticle() {
		return article;
	}

	//pdf存储的路径
	private String pdfurl;
	private String taskId;
	private String targetFileName;
	private String exePath;
	
	static String rootPath = "temp";
	
	/**
	 * 清除temp文件 
	 * @param taskId
	 */
	public static void clearTempFile(String taskId){
		File dir = new File(rootPath +"/" + taskId);
		if (dir.exists()) {
			try {
				FileUtils.deleteDirectory(dir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 构造函数：输入pdf所在路径，工程中暂时存储的xml路径，pdftoxml.exe所在路径，可获取该pdf对应的sentence对象
	 * @param pdfurl：pdf路径
	 * @param taskId：工程中暂存xml的路径
	 * @param exePath： 本地pdftoxml.exe所在路径
	 */
	public NumberCite(String pdfurl, String taskId, String exePath) {
		this.pdfurl = pdfurl;
		this.taskId = taskId;
		if(pdfurl.endsWith(".pdf")){
			File file = new File(pdfurl);
			if (file.exists()) {
				this.targetFileName = file.getName().replaceAll(".pdf", "") +".xml";
			}
			File dir = new File(rootPath +"/" + taskId);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String xmlurl = dir.getAbsolutePath()+"/"+targetFileName;
			System.out.println(xmlurl);
			PdftoxmlUtil pdf = new PdftoxmlUtil();
			System.out.println("---start convert pdf to xml---");
			//第一个为pdf所在路径，第二个为在工程目录下暂时存放的xml名称，第三个为pdftoxml.exe所在的路径
			boolean ok = pdf.pdf2xml(pdfurl, xmlurl, exePath);
			System.out.println("---over---");
			if(ok) {
				File xmlfile = new File(xmlurl);
				CiteSentence csentence = new CiteSentence();
				sentences = csentence.getResult(xmlfile);
				content.setSentences(sentences);
				firstContent.add(content);
				outline.setContents(firstContent);
				firstOutline.add(outline);
				article.setOutlines(firstOutline);
			}else {
				System.out.println("pdf转xml出现错误");
			}
		}else{
			System.out.println("pdf文件名格式错误");
		}
	}
	
	
}
