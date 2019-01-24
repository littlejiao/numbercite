package edu.kndev.numbercite;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Element;

import edu.kndev.numbercite.feature.IndexFeature;
import edu.kndev.numbercite.util.CiteUtil;

public class CiteIndex {

	// 一篇文章中，所有的引用型上标在PAGE中的位置，以及在PAGE对应的TEXT中的位置
	private Map<Integer, List<Integer>> citeindexs;

	// 一篇文章中，所有的引用型上标在PAGE中的位置，以及在PAGE对应的TEXT所对应的TOKEN位置
	private Map<Integer, List<Integer>> tokenindexs;

	// 文章对应的page
	private List<Element> pageElements;

	// 一篇文章中，所有的引用型字符串，可以有重复的
	private List<String> indexstrs;

	private List<String> initialindex;

	public List<String> getIndexstrs() {
		return indexstrs;
	}

	public Map<Integer, List<Integer>> getTokenindexs() {
		return tokenindexs;
	}

	public void setTokenindexs(Map<Integer, List<Integer>> tokenindexs) {
		this.tokenindexs = tokenindexs;
	}

	public void setIndexstrs(List<String> indexstrs) {
		this.indexstrs = indexstrs;
	}

	public List<Element> getPageElements() {
		return pageElements;
	}

	public void setPageElements(List<Element> pageElements) {
		this.pageElements = pageElements;
	}

	public Map<Integer, List<Integer>> getCiteindexs() {
		return citeindexs;
	}

	public void setCiteindexs(Map<Integer, List<Integer>> citeindexs) {
		this.citeindexs = citeindexs;
	}

	public static void main(String[] args) {
//		File file = new File("D:\\pdf\\xmls");
//		File[] files = file.listFiles();
//		for (File f : files) {
//			if (f.getName().startsWith("t") && f.getName().endsWith(".xml")) {
//				CiteIndex cindex = new CiteIndex(f);
//				System.out.println(f.getName());
//				System.out.println(cindex.initialindex);
//				System.out.println("-----------------------------------------");
//			}
//		}
		 CiteIndex cindex = new CiteIndex(new File("D:\\pdf\\xmls\\t8.xml"));
		 System.out.println(cindex.getIndexstrs());
		 System.out.println(cindex.getCiteindexs());
		 System.out.println(cindex.getTokenindexs());
	}

	public CiteIndex(File file) {
		getIndex(file);
		// TODO Auto-generated constructor stub
	}

	public void getIndex(File file) {
		initialindex = new ArrayList<String>();
		indexstrs = new ArrayList<String>();
		citeindexs = new TreeMap<Integer, List<Integer>>(new Comparator<Integer>() {
			// 升序排列
			public int compare(Integer o1, Integer o2) {
				// TODO Auto-generated method stub
				return o1 - o2;
			}
		});
		tokenindexs = new TreeMap<Integer, List<Integer>>(new Comparator<Integer>() {
			// 升序排列
			public int compare(Integer o1, Integer o2) {
				// TODO Auto-generated method stub
				return o1 - o2;
			}
		});
		IndexFeature ifeature = new IndexFeature();
		CiteUtil cutil = new CiteUtil();
		Element root = cutil.getRoot(file);
		pageElements = root.elements("PAGE");

		// 获取一篇文章中标准的引用上标的字体信息
		Map<String, String> fontmap = ifeature.confirmNum(pageElements);
		String standardcolor = null;
		String standardname = null;
		String standardcolor_backup = null;
		String standardname_backup = null;
		boolean validgoto = false;
		if (fontmap.containsKey("fontcolor")) {
			// 用字体的类型和颜色来比较,由于发现引文上标字体大小可能不一样，所以先不考虑
			standardcolor = fontmap.get("fontcolor");
			standardname = fontmap.get("fontname");
			System.out.println(standardcolor);
			System.out.println(standardname);
		}
		if (fontmap.containsKey("fontcolor_backup")) {
			standardcolor_backup = fontmap.get("fontcolor_backup");
			standardname_backup = fontmap.get("fontname_backup");
			System.out.println(standardcolor_backup);
			System.out.println(standardname_backup);
		}
		if (fontmap.containsKey("goto")) {
			validgoto = true;
			System.out.println("这篇文章有goto属性");
		}
		if((standardcolor!=null &&!standardcolor.equals("#000000"))||(standardcolor_backup!=null && !standardcolor_backup.equals("#000000"))) {
			System.out.println("这篇文章是彩色");
		}
		for (int i = 0; i < pageElements.size(); i++) {
			List<Element> textElements = pageElements.get(i).elements("TEXT");
			for (int j = 0; j < textElements.size(); j++) {
				Element nowText = textElements.get(j);
				List<Element> tokenElements = nowText.elements("TOKEN");
				int tokensize = tokenElements.size();
				// 取所有的token来比较
				for (int m = 0; m < tokensize; m++) {
					Element firstNode = tokenElements.get(m);
					String firstNodeStr = firstNode.getText();

					// 如果有goto属性，就不用去用太多的规则
					if (validgoto) {
						if (ifeature.findNum(firstNodeStr) && firstNode.attributeValue("goto") != null) {
							Element lastcompareToken_1 = ifeature.getLastCompareElement_1(textElements, j, firstNode, m,
									tokenElements);
							Element nextcompareToken = ifeature.getNextCompareElement(textElements, tokenElements, j, m,
									firstNode);
							// 判断是否是数字型字符,接着判断数字型字符与上一个或者下一个字符的y坐标是否相同,字体大小是否相同，前一个是否是数字或者单位的指数
							if (ifeature.judgeY(firstNode, lastcompareToken_1, nextcompareToken)// y坐标必须比前后小或者大1以内
									&& ifeature.haveFont(firstNode)// 上标必须有字体信息
									// 符合上述条件之后，再判断该字符的字体颜色和类型是否和引文型上标的相同
									&& ((standardname != null
											&& ifeature.validFont(firstNode, standardcolor, standardname))
											|| (standardname_backup != null
													&& (ifeature.validFont(firstNode, standardcolor, standardname)
															|| ifeature.validFont(firstNode, standardcolor_backup,
																	standardname_backup))))){
//								 System.out.println(firstNodeStr);
//								 System.out.println(lastcompareToken_1.asXML());
//								 System.out.println(firstNode.asXML());
//								 System.out.println(nextcompareToken.asXML());
//								 System.out.println("---");
								addMessage(cutil, i, j, m, firstNodeStr);
							}
						}
					} else {// 如果没有goto属性，就把所有的规则都用上
						if (ifeature.findNum(firstNodeStr)) {
							Element lastcompareToken_1 = ifeature.getLastCompareElement_1(textElements, j, firstNode, m,
									tokenElements);
							Element nextcompareToken = ifeature.getNextCompareElement(textElements, tokenElements, j, m,
									firstNode);
							if (ifeature.judgeY(firstNode, lastcompareToken_1, nextcompareToken)
									&& ifeature.validNoNum(firstNode, lastcompareToken_1, nextcompareToken)
									&& ifeature.validNum(firstNode, lastcompareToken_1, nextcompareToken)
									&& ifeature.haveFont(firstNode)// 上标必须有字体信息
									&& ifeature.judgeX(firstNode, lastcompareToken_1)// 上标的x坐标必须比前面的大
									// 与标准上标字体的颜色和类型进行比较：可能有两种标准上标
									&& ((standardname != null
											&& ifeature.validFont(firstNode, standardcolor, standardname))
											|| (standardname_backup != null
													&& (ifeature.validFont(firstNode, standardcolor, standardname)
															|| ifeature.validFont(firstNode, standardcolor_backup,
																	standardname_backup))))) {
//								 System.out.println(firstNodeStr);
//								 System.out.println(lastcompareToken_1.asXML());
//								 System.out.println(firstNode.asXML());
//								 System.out.println(nextcompareToken.asXML());
//								 System.out.println("---");
								addMessage(cutil, i, j, m, firstNodeStr);
							}
						}
					}
				}
			}
		}
	}

	private void addMessage(CiteUtil cutil, int i, int j, int m, String firstNodeStr) {
		initialindex.add(firstNodeStr);
		indexstrs.add(cutil.getNum(firstNodeStr));
		if (!citeindexs.containsKey(i)) {
			List<Integer> temp = new ArrayList<Integer>();
			temp.add(j);
			citeindexs.put(i, temp);
		} else {
			citeindexs.get(i).add(j);
		}

		if (!tokenindexs.containsKey(i)) {
			List<Integer> temp = new ArrayList<Integer>();
			temp.add(m);
			tokenindexs.put(i, temp);
		} else {
			tokenindexs.get(i).add(m);
		}
	}
}
