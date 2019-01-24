package edu.kndev.numbercite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;

import edu.kndev.numbercite.feature.SentenceFeature;
import edu.kndev.numbercite.util.CiteUtil;
import kndev.model.mongo.paper.Sentence;

public class CiteSentence {
	
	/**
	 * 给定xml所在的路径，获取引用句及上标
	 * @param args
	 */
	public List<Sentence> getResult(File xmlFullPath){
		CiteSentence s = new CiteSentence();
		CiteUtil cutil = new CiteUtil();
		CiteIndex cindex = new CiteIndex(xmlFullPath);
		List<String> allSentences = s.getAllSentence(cindex);
		List<String> indexstrs = cindex.getIndexstrs();
		List<Sentence> sentences = cutil.conform(indexstrs, allSentences);
		return sentences;
	}
	
	public static void main(String[] args) {
		CiteSentence s = new CiteSentence();
		CiteIndex cindex = new CiteIndex(new File("xmls/b.xml"));
		CiteUtil cutil = new CiteUtil();
		List<String> ss = s.getAllSentence(cindex);
		List<String> indexstrs = cindex.getIndexstrs();
		System.out.println("原始句子数："+ss.size());
		for(int i=0;i<ss.size();i++) {
			System.out.println(ss.get(i));
			System.out.println(indexstrs.get(i));
		}
		List<Sentence> list = cutil.conform(indexstrs, ss);
		System.out.println("整理后句子数："+list.size());
		for(Sentence sen : list) {
			System.out.println(sen.getText());
			System.out.println(sen.getReferencesOrders());
			System.out.println("--------------------------");
		}
//		int size = ss.size();
//		for (int i = 0; i < size; i++) {
//			System.out.println(ss.get(i));
//			System.out.println(indexstrs.get(i));
//			System.out.println("---------");
//		}
//		File file = new File("xmls");
//		File[] files = file.listFiles();
//		for (File f : files) {
//			System.out.println(f.getName());
//			CiteSentence s = new CiteSentence();
//			CiteIndex cindex = new CiteIndex(f);
//			CiteUtil cutil = new CiteUtil();
//			List<String> ss = s.getAllSentence(cindex);
//			List<String> indexstrs = cindex.getIndexstrs();
//			int size = ss.size();
//			for (int i = 0; i < size; i++) {
//				System.out.println(ss.get(i));
//				System.out.println(indexstrs.get(i));
//				System.out.println("---------");
//			}
//			System.out.println("**************************************************************************************");
//		}

	}

	/**
	 * 获取所有句子
	 */
	public List<String> getAllSentence(CiteIndex cindex) {
		List<String> sentences = new ArrayList<String>();
		SentenceFeature sfeature = new SentenceFeature();
		List<Element> pageElements = cindex.getPageElements();
		Map<Integer, List<Integer>> citeindexs = cindex.getCiteindexs();
		Map<Integer, List<Integer>> tokenindexs = cindex.getTokenindexs();
		Set<Integer> pageindex = citeindexs.keySet();
		for (int page : pageindex) {
			Element pageElement = pageElements.get(page);
			List<Element> textElements = pageElement.elements("TEXT");
			
			// 在第page页的所有引用型上标所在的TEXT位置
			List<Integer> textLocations = citeindexs.get(page);
			List<Integer> tokenLocations = tokenindexs.get(page);	
			for (int i = 0; i < textLocations.size(); i++) {
				int tokenindex = tokenLocations.get(i);
				Map<String, Integer> startmap;
				Map<String, Integer> endmap;
				int starttext;
				int starttoken;
				int endtext;
				int endtoken;
				if (tokenindex == 0) {//上标在TEXT的首个TOKEN位置
					startmap = sfeature.getStart_1(textElements, textLocations.get(i));
					starttext= startmap.get("starttext");
					starttoken = startmap.get("starttoken");
					endmap = sfeature.getEnd_1(textElements, textLocations.get(i));
					endtext = endmap.get("endtext");
					endtoken = endmap.get("endtoken");
				}else {//上标在TEXT的中间
					startmap = sfeature.getStart_2(textElements,textLocations.get(i), tokenindex);
					starttext= startmap.get("starttext");
					starttoken = startmap.get("starttoken");
					endmap = sfeature.getEnd_2(textElements, textLocations.get(i) , tokenindex);
					endtext = endmap.get("endtext");
					endtoken = endmap.get("endtoken");
				}

				String sentence = getSentence(textElements, starttext, starttoken, endtext, endtoken);
				String changeSentence = sfeature.changeSentence(sentence);
				//去掉句子前面的Introduction
				if(changeSentence.toLowerCase().trim().startsWith("introduction")) {
					changeSentence = changeSentence.substring(12).trim();
				}
				sentences.add(changeSentence);
				//sentences.add(sentence);
			}
		}
		return sentences;
	}

	/**
	 * 已知开头和结尾的TEXT和TOKEN的位置，找之间包含的句子内容
	 * 
	 * @param textElements
	 * @param starttext
	 * @param starttoken
	 * @param endtext
	 * @param endtoken
	 * @return
	 */
	public String getSentence(List<Element> textElements, int starttext, int starttoken, int endtext, int endtoken) {

		StringBuilder sb = new StringBuilder();
		for (int i = starttext; i <= endtext; i++) {
			List<Element> tokenElements = textElements.get(i).elements("TOKEN");
			int tokensize = tokenElements.size();
			if (i == starttext) {// 起始的TEXT
				for (int j = starttoken; j < tokensize; j++) {
					Element e = tokenElements.get(j);
					String s = e.getText();
					String y = e.attributeValue("y");
					sb.append(s + " ");
				}
			} else if (i == endtext) {// 最后一个TEXT
				for (int j = 0; j <= endtoken; j++) {
					Element e = tokenElements.get(j);
					String s = e.getText();
					String y = e.attributeValue("y");
					sb.append(s + " ");
				}
			} else {// 中间的TEXT
				for (int j = 0; j < tokensize; j++) {
					Element e = tokenElements.get(j);
					String s = e.getText();
					String y = e.attributeValue("y");
					sb.append(s + " ");
				}
			}
		}
		// System.out.println(sb.toString());
		// SentenceFeature sf = new SentenceFeature();
		// System.out.println(sf.changeSentence(sb.toString()));
		// System.out.println("------------");
		return sb.toString();
	}

}
