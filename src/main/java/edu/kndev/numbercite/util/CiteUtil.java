package edu.kndev.numbercite.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import kndev.model.mongo.paper.Sentence;

public class CiteUtil {
	public static void main(String[] args) {
		CiteUtil cu = new CiteUtil();
		String s = cu.getNum("8a,b,13c");
		System.out.println(Arrays.toString("8,,13".split(",")));
	}
	/**
	 * 获取xml文件中的所有PAGE标签
	 */
	public Element getRoot(File file) {
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(file);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Element root = document.getRootElement();
		return root;
	}
	
	/**
	 * 获取一个xml文件中所有的TEXT标签
	 */
	public List<Element> getAllTextElements(List<Element> pageElements){
		// 开始遍历每一个PAGE标签,将PAGE下的TEXT标签全部存为一个list
		List<Element> allTEXT = new LinkedList<Element>();
		for (Element pe : pageElements) {
			List<Element> textElements = pe.elements("TEXT");
			allTEXT.addAll(textElements);
		}
		return allTEXT;
	}

	/**
	 * 从引用型上标中提取对应的数字，例如“1,3-5,6”对应的数字为1，3，4，5，6
	 */
	public String getNum(String firstNodeStr){
		firstNodeStr = firstNodeStr.replaceAll("[a-z]", "");
		List<Integer> list = new ArrayList<Integer>();
		String[] strarr = firstNodeStr.split(",");
		for(String str:strarr) {
			if(str.matches("\\d+")){
				list.add(Integer.parseInt(str));
			}else {
				List<Integer> barlist = getNumFromBar(str);
				list.addAll(barlist);
			}
		}
		Collections.sort(list);
		StringBuilder sb = new StringBuilder();
		for(Integer i:list) {
			sb.append(i+",");
		}
		return sb.toString();
	}
	/**
	 * 从“1-4”这样的字符串中提取全部数字1，2，3，4
	 * @param dotstr
	 * @return
	 */
	public List<Integer> getNumFromBar(String barstr){
		List<Integer> list = new ArrayList<Integer>();
		String[] strarr = barstr.split("[^(0-9)]");
		if(strarr.length==2) {
			int min = Integer.parseInt(strarr[0]);
			int max = Integer.parseInt(strarr[1]);
			for(int i=min;i<=max;i++) {
				list.add(i);
			}
		}
		return list;
	}
	
	/**
	 * 将重复的句子进行整合
	 */
	public List<Sentence> conform(List<String> indexstrs, List<String> allsentences){
		List<Sentence> newsentences = new ArrayList<Sentence>();
		List<Integer> numindexs = new ArrayList<Integer>();
		int size = indexstrs.size();
		for(int i=0;i<size;i++) {
			if(numindexs.contains(i)) {
				continue;
			}
			Sentence sen = new Sentence();
			sen.setQuotations(true);
			sen.setText(allsentences.get(i));
			sen.setReferencesOrders(new ArrayList<String>());
			StringBuilder sb = new StringBuilder(indexstrs.get(i));
			for(int j=i+1;j<size;j++) {	
				if(allsentences.get(i).trim().equals(allsentences.get(j).trim())) {
					sb.append(indexstrs.get(j));
					numindexs.add(j);
				}
			}
			for(String singlenum : sb.toString().split(",")) {
				sen.getReferencesOrders().add(singlenum);
			}
			newsentences.add(sen);
		}
		return newsentences;
	} 
	
}
