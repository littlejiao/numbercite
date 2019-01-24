package edu.kndev.numbercite.feature;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Element;

public class SentenceFeature {
	public static void main(String[] args) {
		SentenceFeature sf = new SentenceFeature();
		// System.out.println(sf.validDot("guoxiu.wang@uts.Adu.au")+"ccc");
		String s = sf.changeSentence(
				"substrate. 23 Both the dissociative and associative pathways are diﬀerent from that of IA-Pt catalysts, 6c,23 which follow a 2e − pathway and has H 2 O 2 as the product.");
		System.out.println(s);
		System.out.println("1-6".matches(".*\\d.*"));
		String ssss = "3.14";
		System.out.println(sf.validDot(ssss));
	}

	/**
	 * 引文数字在首个TOKEN的位置的情况下：找句子的结尾
	 * 
	 * @param textElements
	 * @param textindex
	 * @return
	 */
	public Map<String, Integer> getEnd_1(List<Element> textElements, int textindex) {
		Map<String, Integer> endmap = new HashMap<String, Integer>();
		int start = textindex - 1 < 0 ? 0 : textindex - 1;
		int end = textindex + 20 > textElements.size() - 1 ? textElements.size() - 1 : textindex + 20;
		for (int i = start; i <= end; i++) {
			Element textElement = textElements.get(i);
			List<Element> tokenElements = textElement.elements("TOKEN");
			String tokenText = null;
			if (i == start) {
				tokenText = tokenElements.get(tokenElements.size() - 1).getText().trim();
				if (validDot(tokenText)) {
					// System.out.println("找到结尾1");
					// 找到结尾的.所在的TEXT标签的index
					endmap.put("endtext", i);
					endmap.put("endtoken", tokenElements.size() - 1);
					// 如果找到的末尾是在数字上标之前，把末尾定到数字上标的位置
					if (endmap.get("endtext") < textindex) {
						endmap.put("endtext", textindex);
						endmap.put("endtoken", 0);
					}
					return endmap;
				}
			} else {
				for (int j = 0; j < tokenElements.size(); j++) {

					tokenText = tokenElements.get(j).getText();
					if (validDot(tokenText)) {
						// 找到结尾的.所在的TEXT标签的index
						// System.out.println("找到结尾2");
						endmap.put("endtext", i);
						endmap.put("endtoken", j);
						String endstr = "";
						if(j!=tokenElements.size()-1) {
							endstr = tokenElements.get(j+1).getText();
							if(endstr.matches(Regex.RegEx)) {
								endmap.put("endtoken", j+1);
							}
						}else{
							if(i!=end) {
								List<Element> templist = textElements.get(i+1).elements();
								endstr = templist.get(0).getText();
								if(endstr.matches(Regex.RegEx)) {
									endmap.put("endtext", i+1);
									endmap.put("endtoken", 0);
								}
							}
							
						}
						
						return endmap;
					}
				}
			}
		}
		// 如果往后找20个TEXT还没有找到这句话的结尾，证明后面跟的不是正文，直接放弃寻找
		if (!endmap.containsKey("endtext") || !endmap.containsKey("endtoken")) {
			endmap.put("endtext", textindex);
			endmap.put("endtoken", textElements.get(textindex).elements().size() - 1);
		}

		return endmap;
	}

	/**
	 * 引文数字不在首个TOKEN的位置情况下：找句子的结尾 既然不在首个token，就证明数字前面是有字符的，取该字符为开始的token
	 * 
	 * @param textElements
	 * @param textindex
	 * @param tokenindex
	 * @return
	 */
	public Map<String, Integer> getEnd_2(List<Element> textElements, int textindex, int tokenindex) {
		Map<String, Integer> endmap = new HashMap<String, Integer>();
		int tokenstart = tokenindex - 1;
		int end = textindex + 20 > textElements.size() - 1 ? textElements.size() - 1 : textindex + 20;
		for (int i = textindex; i <= end; i++) {
			Element textElement = textElements.get(i);
			List<Element> tokenElements = textElement.elements("TOKEN");
			String tokenText = null;
			if (i == textindex) {
				for (int j = tokenstart; j < tokenElements.size(); j++) {
					tokenText = tokenElements.get(j).getText();
					if (validDot(tokenText)) {
						// 找到结尾的.所在的TEXT标签的index
						// System.out.println("找到结尾2");
						endmap.put("endtext", i);
						endmap.put("endtoken", j);
						// 如果找到的末尾是在数字上标之前，把末尾定到数字上标的位置
						if (endmap.get("endtoken") < tokenindex) {
							endmap.put("endtext", textindex);
							endmap.put("endtoken", tokenindex);
						}
						return endmap;
					}
				}
			} else {
				for (int j = 0; j < tokenElements.size(); j++) {
					tokenText = tokenElements.get(j).getText();
					if (validDot(tokenText)) {
						// 找到结尾的.所在的TEXT标签的index
						// System.out.println("找到结尾2");
						endmap.put("endtext", i);
						endmap.put("endtoken", j);
						return endmap;
					}
				}
			}
		}
		// 如果往后找20个TEXT还没有找到这句话的结尾，证明后面跟的不是正文，直接放弃寻找
		if (!endmap.containsKey("endtext") || !endmap.containsKey("endtoken")) {
			endmap.put("endtext", textindex);
			endmap.put("endtoken", textElements.get(textindex).elements().size() - 1);
		}

		return endmap;
	}

	/**
	 * 引文数字在首个TOKEN的位置的情况下：找句子的开头
	 * 
	 * @param textElements
	 * @param textindex
	 * @return
	 */
	public Map<String, Integer> getStart_1(List<Element> textElements, int textindex) {
		Map<String, Integer> startmap = new HashMap<String, Integer>();
		int start = textindex - 20 < 0 ? 0 : textindex - 20;
		for (int i = textindex - 1; i >= start; i--) {
			Element textElement = textElements.get(i);
			List<Element> tokenElements = textElement.elements("TOKEN");
			String tokenText = null;
			if (i == textindex - 1) {
				for (int j = tokenElements.size() - 2; j >= 0; j--) {
					tokenText = tokenElements.get(j).getText().trim();
					if (validDot(tokenText)) {
						// System.out.println("找到开头last");
						// 找到结尾的.所在的TEXT标签的index
						startmap.put("starttext", i);
						startmap.put("starttoken", j);
						return startmap;
					}
				}
			} else {
				for (int j = tokenElements.size() - 1; j >= 0; j--) {
					tokenText = tokenElements.get(j).getText();
					if (validDot(tokenText)) {
						// 找到结尾的.所在的TEXT标签的index
						// System.out.println("找到开头 前面好几个");
						startmap.put("starttext", i);
						startmap.put("starttoken", j);
						return startmap;
					}
				}
			}
		}
		// 如果往前找20个还没有找到上一句的结尾
		// 1）是往前找到了0还没有找到，那就把0作为开头
		// 2）往前找20个还没有找到0，并且还没有结尾.，那就证明前面有很多糟糕的字符，取前3个TEXT
		if (!startmap.containsKey("starttext")) {
			if (start == 0) {
				startmap.put("starttext", 0);
				startmap.put("starttoken", 0);
			} else {
				startmap.put("starttext", textindex - 3);
				startmap.put("starttoken", 0);
			}

		}
		return startmap;
	}

	/**
	 * 引文数字不在首个TOKEN的位置情况下：找句子的开头 
	 * 从当前数字所在的TEXT标签里面，TOKEN所在的位置往前找
	 * 
	 * @param textElements
	 * @param textindex
	 * @param tokenindex
	 * @return
	 */
	public Map<String, Integer> getStart_2(List<Element> textElements, int textindex, int tokenindex) {
		Map<String, Integer> startmap = new HashMap<String, Integer>();
		int start = textindex - 20 < 0 ? 0 : textindex - 20;
		for (int i = textindex; i >= start; i--) {
			Element textElement = textElements.get(i);
			List<Element> tokenElements = textElement.elements("TOKEN");
			String tokenText = null;
			if (i == textindex) {
				for (int j = tokenindex - 2; j >= 0; j--) {
					tokenText = tokenElements.get(j).getText().trim();
					if (validDot(tokenText)) {
						// System.out.println("找到开头last");
						// 找到结尾的.所在的TEXT标签的index
						startmap.put("starttext", i);
						startmap.put("starttoken", j);
						return startmap;
					}
				}
			} else {
				for (int j = tokenElements.size() - 1; j >= 0; j--) {
					tokenText = tokenElements.get(j).getText();
					if (validDot(tokenText)) {
						// System.out.println("找到开头 前面好几个");
						startmap.put("starttext", i);
						startmap.put("starttoken", j);
						return startmap;
					}
				}
			}

		}
		// 如果往前找20个还没有找到上一句的结尾
		// 1）是往前找到了0还没有找到，那就把0作为开头
		// 2）往前找20个还没有找到0，并且还没有结尾.，那就证明前面有很多糟糕的字符，取前3个TEXT
		if (!startmap.containsKey("starttext")) {
			if (start == 0) {
				startmap.put("starttext", 0);
				startmap.put("starttoken", 0);
			} else {
				startmap.put("starttext", textindex - 3);
				startmap.put("starttoken", 0);
			}

		}
		return startmap;
	}

	/**
	 * 特征6：判断一个TOKEN标签里面的字符串是否含“.”，并且“.”的前面是不是数字或者大写字母
	 */
	public boolean validDot(String str) {
		boolean dot = false;
		String[] strarr = str.split("\\s");
		for (String strr : strarr) {
			if (strr.contains(".")) {
				int dotindex = strr.lastIndexOf(".");
				String front = strr.substring(0,dotindex+1);
				String behind = strr.substring(dotindex,strr.length());
				// 点前面不能是ref、e.g等字符
				Pattern p = Pattern.compile(Regex.RegEx_Dot_Front);
				Matcher m = p.matcher(front);
				// 点之后不能是数字
				Pattern p1 = Pattern.compile(Regex.RegEx_Dot_Behind);
				Matcher m1 = p1.matcher(behind);
				if (!m1.find() && !m.find()) {
					dot = true;
				}
			}
		}

		return dot;
	}

	/**
	 * 将句子改写为完整、规则的句子，因为之前得到的句子会包含“xxxx .”这样的信息 句子里面可能含有很多个"."，例如“shdh. 10.0.32
	 * INTRODUCTION I am a dog.”,需要判断一下哪个是有效的"."
	 * 
	 * @param sentence
	 * @return
	 */
	public String changeSentence(String sentence) {
		int target = 0;
		boolean find = false;
		String[] str = sentence.trim().split("\\s");
		for (int i = str.length - 3; i >= 0; i--) {
			if (validDot(str[i])) {
				target = i;
				find = true;
				break;
			}
		}
		StringBuffer sb = new StringBuffer();
		if (find) {
			for (int i = target + 1; i < str.length; i++) {
				sb.append(str[i] + " ");
			}
		} else {
			// 往前没有找到"."，先全部都要吧
			sb.append(sentence);
		}
		String result = sb.toString().trim();
		String[] strr = result.split("\\s");
		
		if (strr[0].matches(".*\\d.*")) {
			int length = strr[0].length();
			result = result.substring(length).trim();
		}
		return result;
	}

}
