package com.dboper.search.test;

public class Base {

	public static void main(String[] args){
		String a="aas$as$basd";
		if(a.contains("$as$")){
			String[] parts=a.split("\\$as\\$");
			System.out.println(parts[0]);
		}else{
			System.err.println("没有");
		}
		
		System.out.println("as"+null);
	}
}
