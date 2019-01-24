package edu.kndev.numbercite.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Element;

public class IndexFeature {
	private int textccc;
	private int tokenccc;
	private List<Element> lasttextsccc;
	private List<Element> lasttokensccc;
	
	/**
	 * 获取比较用的上一个标签,为后来比较y坐标做准备；同时获取上上一个字符所在的位置信息。
	 * 注意：如果遇到一个PAGE标签的第一个TEXT标签下的第一个TOKEN内容是数字，则认为它不是引文型上标 
	 */
	public Element getLastCompareElement_1(List<Element> textElements, int textindex, Element candidateToken,
			int tokenindex, List<Element> tokenElements) {
		Element lastcompareElement = null;
		textccc = textindex;
		tokenccc = tokenindex;
		lasttextsccc = textElements;
		lasttokensccc = tokenElements;
		if (tokenindex == 0) {// 如果TEXT的首个TOKEN内容是数字
			if (textindex == 0) {// 是第一个TEXT标签
				lastcompareElement = candidateToken;
			} else {
				List<Element> lasttokens = textElements.get(textindex - 1).elements();
				lastcompareElement = lasttokens.get(lasttokens.size() - 1);
				textccc = textindex - 1;
				tokenccc = lasttokens.size() - 1;
				lasttokensccc = lasttokens;
			}
		} else {// 如果数字标签不在首个TOKEN
			lastcompareElement = tokenElements.get(tokenindex - 1);
			tokenccc = tokenindex - 1;
		}
		return lastcompareElement;
	}

	/**
	 * （现在未使用）
	 * 获取比较用的上一个标签,为后来比较字体大小做准备
	 * 比较字体大小的时候，会出现Al2O3 23，上标前面是数字会影响结果，所以往前找，直到找到一个不是数字的字符
	 * @param textElements
	 * @param textindex
	 * @param candidateToken
	 * @param tokenindex
	 * @param tokenElements
	 * @return
	 */
	public Element getLastCompareElement_2(List<Element> textElements, int textindex, Element candidateToken,
			int tokenindex, List<Element> tokenElements) {
		Element lastcompareElement = null;
		if (tokenindex == 0) {// 如果TEXT的首个TOKEN内容是数字
			if (textindex == 0) {// 是第一个TEXT标签
				lastcompareElement = candidateToken;
			} else {
				// 防止上一个TOKEN是下标，往前多找一个。
				List<Element> lasttokens = textElements.get(textindex - 1).elements();
				int lastsize = lasttokens.size();
				lastcompareElement = lasttokens.get(lastsize - 1);
				if (lastcompareElement.attributeValue("font-size").equals(candidateToken.attributeValue("font-size"))) {
					if (lastsize == 1) {
						if (textindex > 1) {
							lasttokens = textElements.get(textindex - 2).elements();
							lastcompareElement = lasttokens.get(lastsize - 1);
						}
					} else {
						lastcompareElement = lasttokens.get(lastsize - 2);
					}
				}

			}
		} else {// 如果数字标签不在首个TOKEN
			lastcompareElement = tokenElements.get(tokenindex - 1);
			if (lastcompareElement.attributeValue("font-size").equals(candidateToken.attributeValue("font-size"))) {
				// 如果前面一个和候选字符的字体大小一样，往前再取一个
				if (tokenindex > 1) {
					lastcompareElement = tokenElements.get(tokenindex - 2);
				}
			}
		}
		return lastcompareElement;
	}
	
	/**
	 * 获取比较用的下一个标签,为后来比较y坐标做准备
	 * @param textElements
	 * @param tokenElements
	 * @param textindex
	 * @param tokenindex
	 * @param firsttoken
	 * @return
	 */
	public Element getNextCompareElement(List<Element> textElements, List<Element> tokenElements, int textindex,
			int tokenindex, Element firsttoken) {
		Element nextcompareElement = null;
		if (tokenindex < tokenElements.size() - 1) {// 如果数字所在的位置不是tokenElements的最后一个
			nextcompareElement = tokenElements.get(tokenindex + 1);
		} else {// 数字所在的位置是tokenElements的最后一个
			if (textindex == textElements.size() - 1) {// 已经是最后一个TEXT标签
				nextcompareElement = firsttoken;
			} else {
				List<Element> nexttokens = textElements.get(textindex + 1).elements();
				nextcompareElement = nexttokens.get(0);
			}

		}
		return nextcompareElement;
	}
	/**
	 * 确定候选上标的规则：
	 * 在确定候选上标的时候，判断标准要更严格：
	 * 1)字体大小必须比前后小；
	 * 2)前面不能是数字；不能是单独的一个字符（包括字母、符号）或者左括号+单独的字符；
	 * 3)后面不能是单独的数字、大写字母+小写字母、单独的大写字母。
	 */
	public boolean validCandidate(Element token, Element lasttoken, Element nexttoken) {
		String f1_str = token.attributeValue("font-size");
		double f1 = Double.parseDouble(f1_str);
		String f2_str = lasttoken.attributeValue("font-size");
		double f2 = Double.parseDouble(f2_str);
		String f3_str = nexttoken.attributeValue("font-size");
		double f3 = Double.parseDouble(f3_str);
		Pattern p1 = Pattern.compile("^(\\(|\\[|\\{)?(\\d+|.|cm|dm)$|^to$|^sin$|^cos$|^between$|^and$");
		Matcher m1 = p1.matcher(lasttoken.getText());
		Pattern p2 = Pattern.compile("^[A-Z0-9]$|^[A-Z][a-z]$");
		Matcher m2 = p2.matcher(nexttoken.getText());
		return f2-f1>0 && f3-f1>0 && !m1.find() && !m2.find();
	}
	
	/**
	 * 特征1：判断TEXT标签下，第一个TOKEN标签的内容是否是数字相关的字符串
	 * 
	 * @param numstr
	 * @return
	 */
	public boolean findNum(String numstr) {
		Pattern p1 = Pattern.compile(Regex.RegEx);
		Matcher m1 = p1.matcher(numstr);
		Pattern p2 = Pattern.compile(Regex.RegEx_Backup);
		Matcher m2 = p2.matcher(numstr);
		return m1.find() || m2.find();
	}

	/**
	 * 特征2：判断TEXT标签下，第一个TOKEN标签内容的y坐标是否小于前后，或者比前后大1以内
	 * 
	 * @param token1
	 * @param token2
	 * @return
	 */
	public boolean judgeY(Element token, Element lasttoken, Element nexttoken) {
		String y1_str = token.attributeValue("y");
		double y1 = Double.parseDouble(y1_str);
		String y2_str = lasttoken.attributeValue("y");
		double y2 = Double.parseDouble(y2_str);
		String y3_str = nexttoken.attributeValue("y");
		double y3 = Double.parseDouble(y3_str);
		double c1 = Math.abs(y2-y1);
		double c2 = Math.abs(y3-y1);
//		if(validgoto==true) {
//			return (c1<1 || y2-y1>1) && (c2<1 || y3-y1>1);
//		}else {
//			return y2-y1!=0 && y3-y1!=0 && (c1<1 || y2-y1>1) && (c2<1 || y3-y1>1);
//		}
		return y2-y1!=0 && y3-y1!=0 && (c1<1 || y2-y1>1) && (c2<1 || y3-y1>1);
//		return (y2 - y1 > 0 && y3 - y1 > 0) || (y1 - y2 > 0 && y1 - y2 < 1 && y1 - y3 > 0 && y1 - y3 < 1);
		
//		return (y2 - y1 > 0 && y3 - y1 > 0 && Math.abs(y2-y3)==0) || (y1 - y2 > 0 && y1 - y2 < 1 && y1 - y3 > 0 && y1 - y3 < 1 && Math.abs(y2-y3)==0);
	}

	
	/**
	 * （现在未使用）
	 * 特征3：判断TEXT标签下，第一个TOKEN标签内容的字体大小是否和其他的相同 只有在前面
	 */
	public boolean judgeFontSzie(Element token, Element lasttoken, Element nexttoken) {
		boolean result = false;
		String f1_str = token.attributeValue("font-size");
		double f1 = Double.parseDouble(f1_str);
		String f2_str = lasttoken.attributeValue("font-size");
		double f2 = Double.parseDouble(f2_str);
		String f3_str = nexttoken.attributeValue("font-size");
		double f3 = Double.parseDouble(f3_str);
		
		Element allastNode = getLastCompareElement_1(lasttextsccc, textccc, lasttoken, tokenccc, lasttokensccc);
		String allaststr = allastNode.attributeValue("font-size");
		Double allastdouble = Double.parseDouble(allaststr);
		
		if(Math.abs(f2 - f1) > 0 && Math.abs(f3 - f1) > 0 ) {
			result = true;
//			if(allastdouble!=f2) {
//				result = false;
//			}
		}
		return result;
	}

	/**
	 * 特征4：（未使用）
	 * 上标的后一个字符不能是单独的数字或者单独的大写字母
	 * 
	 * @param text1
	 * @param index
	 * @return
	 */
	public boolean validNext(Element nextToken) {
		boolean valid = true;
		Pattern p = Pattern.compile(Regex.RegEx_NumCap);
		Matcher m = p.matcher(nextToken.getText().trim());
		if (m.find()) {
			valid = false;
		}
		return valid;
	}

	/**
	 * 特征5：判断两个元素字体颜色和字体类型是否一样
	 */
	public boolean validFont(Element token1, String standardcolor, String standardname) {
		String fontcolor = token1.attributeValue("font-color").toLowerCase();
		String fontname = token1.attributeValue("font-name").toLowerCase();
		return fontcolor.equals(standardcolor) && fontname.equals(standardname);
	}
	/**
	 * 特征6：判断上标与它的前一个TOKEN的x坐标，正常上标的x坐标要比它前面一个TOKEN的x坐标大
	 */
	public boolean judgeX(Element token , Element lasttoken) {
		String nowstr = token.attributeValue("x");
		Double nowx = Double.parseDouble(nowstr);

		String laststr = lasttoken.attributeValue("x");
		Double lastx = Double.parseDouble(laststr);
		return nowx-lastx>0;
	}
	
	/**
	 * 特征7：只针对不是数字的字符来判断
	 * 上标前面的TOKEN内容不能以单独数字，单独的小写字母以及常用的一些平方、立方单位结尾;同时判断是否符合字体大小的要求。
	 */
	public boolean validNoNum(Element token, Element lasttoken,Element nexttoken) {
		if(!lasttoken.getText().matches("\\d+")) {
			String f1_str = token.attributeValue("font-size");
			double f1 = Double.parseDouble(f1_str);
			String f2_str = lasttoken.attributeValue("font-size");
			double f2 = Double.parseDouble(f2_str);
			String f3_str = nexttoken.attributeValue("font-size");
			double f3 = Double.parseDouble(f3_str);
			Pattern p = Pattern.compile(Regex.RegEx_Sb);
			Matcher m = p.matcher(lasttoken.getText());
			return !m.find() && f2-f1>0 && f3-f1>0;
		}else {
			return true;
		}	
	}

	/**
	 * 特征8：只针对上标前面一个字符是数字的情况：上标前面的TOKEN内容不能以单独数字，单独的小写字母以及常用的一些平方、立方单位结尾;同时判断是否符合字体大小的要求。
	 * 如果上标前面是数字：
	 * 1）数字字体大小和上标一样，并且后面字符的大小和上标不一样，证明前面的数字是下标型或者上标型数字，例如分子式Al2O3；如果后面和上标一样，过滤掉。
	 * 2）数字字体大小和上标不一样，判断该数字字体和它的前一个是否也不一样。如果不一样，证明该数字还是下标型或者上标型数字；如果一样，证明是正常的数字，过滤掉。
	 */
	public boolean validNum(Element token, Element lasttoken, Element nexttoken) {
		if(lasttoken.getText().matches("\\d+")) {
			String nowstr = token.attributeValue("font-size");
			double nowdouble = Double.parseDouble(nowstr);

			String laststr = lasttoken.attributeValue("font-size");
			double lastdouble = Double.parseDouble(laststr);
			
			String nextstr = nexttoken.attributeValue("font-size");
			double nextdouble = Double.parseDouble(nextstr);
			
			Element allastNode = getLastCompareElement_1(lasttextsccc, textccc, lasttoken, tokenccc, lasttokensccc);
			String allaststr = allastNode.attributeValue("font-size");
			Double allastdouble = Double.parseDouble(allaststr);
			//之后要不要考虑不判断“前一个和前前一个的字体大小”
			if ((nowdouble - lastdouble == 0 && nextdouble - nowdouble>0)|| (lastdouble - nowdouble != 0 && allastdouble - lastdouble > 0)) {
				return true;
			} else {
				Pattern p = Pattern.compile(Regex.RegEx_Sb);
				Matcher m = p.matcher(lasttoken.getText());
				return !m.find() && lastdouble - nowdouble > 0 && nextdouble - nowdouble > 0;
			}
		}else {
			return true;
		}
	}
	

	/**
	 * 特征9：所有的上标一定有对应的字体信息，只有图表里面的一些数字无法获取字体信息
	 */
	public boolean haveFont(Element candidateToken) {
		return candidateToken.attributeValue("font-color") != null && candidateToken.attributeValue("font-name") != null
				&& candidateToken.attributeValue("font-size") != null;
	}
	/**
	 * 特征10：如果上标前面的字符是一些特殊字符，则过滤掉
	 */

	/**
	 * 特征10：将最大引用上标后的小于5的数去掉
	 */
//	public void removeEnd(List<String> indexstrs,Map<Integer, List<Integer>> citeindexs , Map<Integer, List<Integer>> tokenindexs) {
//		Map<Integer, List<Integer>> text = citeindexs;
//		Map<Integer, List<Integer>> token = tokenindexs;
//		//将indexstrs划分成和map一样的块儿
//		for(int i=0;i<text.size();i++) {
//			List<>
//		}
//	}
	/**
	 * 找到一个一定是引用的上标对应的字体颜色,例如“1-4”这种。
	 * 如果找不到的话，去找“1，6”这种，但是这种会有干扰，所以把除第一页外所有的“1,6”找出来存到一个list，判断字体颜色是否相似
	 * 
	 * @param pageElements
	 * @return
	 */
	public Map<String, String> confirmNum(List<Element> pageElements) {
		String citeNumColor = null;
		String citeNumSize = null;
		String citeNumName = null;
		Map<String, String> fontmap = new HashMap<String, String>();
		List<Element> gotos = new ArrayList<Element>();
		List<String> colors = new ArrayList<String>();
		List<String> names = new ArrayList<String>();
		List<String> sizes = new ArrayList<String>();
		List<String> colors1 = new ArrayList<String>();
		List<String> names1 = new ArrayList<String>();
		List<String> sizes1 = new ArrayList<String>();

		for (int i = 0; i < pageElements.size(); i++) {
			List<Element> textElements = pageElements.get(i).elements("TEXT");
			for (int j = 0; j < textElements.size(); j++) {
				List<Element> tokenElements = textElements.get(j).elements("TOKEN");
				// 取所有token来比较
				for (int m = 0; m < tokenElements.size(); m++) {
					Element firstToken = tokenElements.get(m);
					String str = firstToken.getText();
					if (findNum(str)) {
						Element lastcompareToken_1 = getLastCompareElement_1(textElements, j, firstToken, m,tokenElements);
						Element lastcompareToken_2 = getLastCompareElement_2(textElements, j, firstToken, m,tokenElements);
						Element nextcompareToken = getNextCompareElement(textElements, tokenElements, j, m, firstToken);
						if (judgeY(firstToken, lastcompareToken_1, nextcompareToken)
//								&& validNoNum(firstToken,lastcompareToken_1,nextcompareToken) 
//								&& validNum(firstToken,lastcompareToken_1,nextcompareToken) 
//								&& validNext(nextcompareToken)
								&& validCandidate(firstToken, lastcompareToken_1, nextcompareToken)
								// 上标必须有字体的信息
								&& haveFont(firstToken)
								//上标的x坐标必须比前面的大
								&& judgeX(firstToken, lastcompareToken_1)) {
							// 判断该篇文章是否有goto属性
							if (firstToken.attributeValue("goto") != null) {
								gotos.add(firstToken);
							}
							Pattern p1 = Pattern.compile(Regex.RegEx_Slash);
							Matcher m1 = p1.matcher(str);
							// Pattern p2 = Pattern.compile(Regex.RegEx_Comma);
							// Matcher m2 = p2.matcher(str);
							// Pattern p3 = Pattern.compile(Regex.RegEx_Number);
							// Matcher m3 = p3.matcher(str);
							if (m1.find()) {
								colors.add(firstToken.attributeValue("font-color").toLowerCase());
								names.add(firstToken.attributeValue("font-name").toLowerCase());
								sizes.add(firstToken.attributeValue("font-size").toLowerCase());
							} else if (i == 0 && j >= 20) {
								// 去除首页前20个和后20个TEXT的影响，获取首页候选上标信息
								colors1.add(firstToken.attributeValue("font-color").toLowerCase());
								names1.add(firstToken.attributeValue("font-name").toLowerCase());
								sizes1.add(firstToken.attributeValue("font-size").toLowerCase());
							} else if (i > 0 && validNext(nextcompareToken)) {
								// 从第二页开始，获取所有的候选上标信息
								colors1.add(firstToken.attributeValue("font-color").toLowerCase());
								names1.add(firstToken.attributeValue("font-name").toLowerCase());
								sizes1.add(firstToken.attributeValue("font-size").toLowerCase());
							}

						}
					}
				}

			}
		}
		// 从存储有goto属性的Element来判断该篇文章是否是指向型的上标，5是阈值（如果一篇文章的上标是带有goto属性的，那至少要有5个参考文献都带有吧）
		if (gotos.size() > 5) {
			fontmap.put("goto", "yes");
		}
		// 如果出现“1-3”类似的字符，就统计全部的“1-3”类似的字符，选择其中出现最多的当作标准引文上标
		if (names.size() > 0) {
			//判断存储“1-3”这种类型的种类数，因为遇到过一篇文章中有两种上标，可能字体不一样
			Map<String,Integer> namekinds = judgeKinds(names);
			Map<String,Integer> colorkinds = judgeKinds(colors);
			int fontnum = namekinds.size();
			int colornum = colorkinds.size();
			if(fontnum == 2) {
				List<String> tempfontname = new ArrayList<String>(namekinds.keySet()); 
				citeNumName = tempfontname.get(0);
				citeNumColor = colors.get(namekinds.get(citeNumName));
				citeNumSize = sizes.get(namekinds.get(citeNumName));
				fontmap.put("fontcolor", citeNumColor);
				fontmap.put("fontname", citeNumName);
				fontmap.put("fontsize", citeNumSize);
				String citeNumName_backup = tempfontname.get(1);
				String citeNumColor_backup = colors.get(namekinds.get(citeNumName_backup));
				String citeNumSize_backup = sizes.get(namekinds.get(citeNumName_backup));
				fontmap.put("fontcolor_backup", citeNumColor_backup);
				fontmap.put("fontname_backup", citeNumName_backup);
				fontmap.put("fontsize_backup", citeNumSize_backup);	
			}else if(colornum ==2){
				List<String> tempfontcolor = new ArrayList<String>(colorkinds.keySet()); 
				citeNumColor = tempfontcolor.get(0);
				citeNumName = names.get(colorkinds.get(citeNumColor));
				citeNumSize = sizes.get(colorkinds.get(citeNumColor));
				fontmap.put("fontcolor", citeNumColor);
				fontmap.put("fontname", citeNumName);
				fontmap.put("fontsize", citeNumSize);
				String citeNumColor_backup = tempfontcolor.get(1);
				String citeNumName_backup = names.get(colorkinds.get(citeNumColor_backup));
				String citeNumSize_backup = sizes.get(colorkinds.get(citeNumColor_backup));
				fontmap.put("fontcolor_backup", citeNumColor_backup);
				fontmap.put("fontname_backup", citeNumName_backup);
				fontmap.put("fontsize_backup", citeNumSize_backup);	
			}else {
				citeNumName = getMostCount(names);
				// 先按照引文型上标的颜色一定是一样的
				int index = names.indexOf(citeNumName);
				citeNumColor = colors.get(index);
				citeNumSize = sizes.get(index);
				fontmap.put("fontcolor", citeNumColor);
				fontmap.put("fontname", citeNumName);
				fontmap.put("fontsize", citeNumSize);
			}
		} else if(colors1.size() > 0){
			// 如果文中没有“1-3”类似的字符，选择所有候选上标中出现最多的当作标准引文上标
			citeNumColor = getMostCount(colors1);
			// 先按照引文型上标的颜色一定是一样的
			int index = colors1.indexOf(citeNumColor);
			citeNumName = names1.get(index);
			citeNumSize = sizes1.get(index);
			fontmap.put("fontcolor", citeNumColor);
			fontmap.put("fontname", citeNumName);
			fontmap.put("fontsize", citeNumSize);
		}
		System.out.println(names);
		System.out.println(names1);
		return fontmap;
	}
	/**
	 * 找出一个list中的所有不同元素，并记录其对应的位置
	 */
	public Map<String,Integer> judgeKinds(List<String> names) {
		Map<String,Integer> kinds = new HashMap<String, Integer>();
		for(int i=0;i<names.size();i++) {
			String fontname = names.get(i);
			if(!kinds.containsKey(fontname)) {
				kinds.put(fontname,i);
			}
		}
		return kinds;
	}
	/**
	 * 一个list中如果元素全部相同，返回该元素；如果元素不全相同，返回出现最多的元素
	 */
	public String getMostCount(List<String> list) {
		String result = "";
		if (list.size() == 1) {
			result = list.get(0);
		} else {
			Set set = new HashSet<String>(list);
			if (set.size() == 1) {
				result = list.get(0);
			} else {
				Map<String, Integer> map = new TreeMap<String, Integer>();
				for (String s : list) {
					if (!map.containsKey(s)) {
						map.put(s, 1);
					} else {
						map.put(s, map.get(s) + 1);
					}
				}
				List<Map.Entry<String, Integer>> maplist = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
				// 然后通过比较器来实现排序
				Collections.sort(maplist, new Comparator<Map.Entry<String, Integer>>() {
					// 降序排序
					public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
						// TODO Auto-generated method stub
						return o2.getValue() - o1.getValue();
					}
				});
				result = maplist.get(0).getKey();
			}
		}
		return result;
	}

}
