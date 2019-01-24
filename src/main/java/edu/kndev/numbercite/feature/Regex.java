package edu.kndev.numbercite.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {
	//判断一个TOKEN里面的内容是不是“单纯数字型”的引用上标
	public static final String RegEx = "^\\(?([1-9][0-9]?|[1-3]\\d{2})\\)?((,|[^(a-zA-Z0-9\\u4e00-\\u9fa5.,)])+\\(?([1-9][0-9]?|[1-3]\\d{2})\\)?)*$";
	//判断一个TOKEN里面的内容是不是“数字字母型”的引用上标
	public static final String RegEx_Backup = "^([1-9][0-9]?[a-z]?(,[a-z])?|[1-3]\\d{2}[a-z]?(,[a-z])?)(,([1-9][0-9]?[a-z]?(,[a-z])?|[1-3]\\d{2}[a-z]?(,[a-z])?))*$";
	//判断一个TOKEN里面的内容是不是“斜杠型”的引用上标，例如“1-3”
	public static final String RegEx_Slash = "[^(a-zA-Z0-9\\u4e00-\\u9fa5,./\\\\*&())]";
	
	//判断一个TOKEN里面的内容是不是“逗号型”的引用上标，例如“1,3”
	public static final String RegEx_Comma = ",";
	
	//判断一个TOKEN里面的内容是不是“单独数字型”的引用上标，例如“1”
	public static final String RegEx_Number = "^\\d+$";
	
	//单独一个TEXT+单独一个TOKEN标签的情况下，判断它的后一个TOKEN标签是否为数字或大写字母开头，如果是，证明找到的这个数字不是引文上标
	public static final String RegEx_NumCap = "^[A-Z0-9]$|^[A-Z][a-z]$";
	
	//判断一个TOKEN的内容是不是数字、一个大小写字母、以及常用的一些平方立方单位 
	//目前认为大写字母后面跟上标数字是正确的; 防止上标前面是年份，去掉四位数的情况
	//上标前面不能是人名
	public static final String RegEx_Sb = "\\(?[A-Z]\\/[A-Za-z]$|[^\\w\\s,.:;)]$|^to$|^sin$|^cos$|^between$|^and$|^[^0-9a-zA-Z]?(cm|nm|dm)$|^\\(?[a-z]\\)?$"
			+"|\\(?\\d+\\.\\d+\\)?$|^\\(?\\d{1,3}\\)?$|^\\(?3-9\\d{3}\\)?$|^\\(?\\d{5,}\\)?$";
	
	//判断一个字符串中是否有句尾型的“.”   Z.(有待商议)  Zhang.xxxxx   al.xxxx    Fig. xxxxxx    GLCs.（句末结尾）
	public static final String RegEx_Dot_Front = "(^[A-Z][a-z]+|^al|^no|^num|^Num|etc|Ref|ref|Fig|fig|Tab|tab|e.g|[A-Z]\\.[A-Z])\\.$";
	public static final String RegEx_Dot_Behind = "^\\.[0-9]";
	
	
	public static void main(String[] args) {
		
		String s = "19,(29)";
		Pattern p = Pattern.compile(RegEx);
		//Pattern p = Pattern.compile("");
		Matcher m = p.matcher(s);
		if(m.find()) {
			System.out.println(m.group());
		}
//		Map<String, String> map = new HashMap<String, String>();
//		map.put("a", "b");
//		map.put("a", "c");
//		System.out.println(map);
//		
//		System.out.println("3".matches("\\d"));
	}
}
