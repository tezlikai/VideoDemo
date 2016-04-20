package com.buaa.tezlikai.mysiri;

import java.util.ArrayList;

/**
 * 语音对象封装
 */
public class VoiceBean {

	public ArrayList<WSBean> ws;

	public class WSBean {
		public ArrayList<CWBean> cw;
	}

	public class CWBean {
		public String w;
	}
}
