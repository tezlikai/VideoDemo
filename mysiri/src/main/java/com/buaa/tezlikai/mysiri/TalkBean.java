package com.buaa.tezlikai.mysiri;

public class TalkBean {

	public String text;
	public boolean isAsk;// true表示提问,false是回答
	public int imageId = -1;// 回答的图片id

	public TalkBean(String text, boolean isAsk, int imageId) {
		this.text = text;
		this.isAsk = isAsk;
		this.imageId = imageId;
	}

}
