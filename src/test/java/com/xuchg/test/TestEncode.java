package com.xuchg.test;

import java.io.File;

import com.xuchg.common.EncodingDetect;

public class TestEncode {

	public static void main(String[] args) {
		
		String file = "C:\\Users\\13194\\Desktop\\test.md";
		String encode = EncodingDetect.getJavaEncode(file);
		System.out.println(encode);
	}
}
