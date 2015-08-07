package com.dboper.search.test;

import org.junit.Test;

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
	
	@Test
	public void testReplace(){
		String sql="select %tprefix%virtual_directory.* from %tprefix%virtual_directory where %tprefix%virtual_directory.entity_status!='OFFLINE'";
		System.out.println(sql.replaceAll("%tprefix%","cms_"));
	}
}
