package com.buaa.tezlikai.mysiri;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ListView lvList;
    StringBuffer mBuffer = new StringBuffer();

    private String[] mAnswers = new String[] { "这张怎么样?", "约吗?", "老地方见!",
            "不要再要美女了", "最后一张了" };

    private int[] mAnswerPics = new int[] { R.drawable.p1, R.drawable.p2,
            R.drawable.p3, R.drawable.p4 };

    private ArrayList<TalkBean> mTalkList = new ArrayList<TalkBean>();// 会话集合
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化语音识别
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5716eac8");

        setContentView(R.layout.activity_main);
        lvList = (ListView) findViewById(R.id.lv_list);
        mAdapter = new MyAdapter();
        lvList.setAdapter(mAdapter);
    }

    // 开始识别
    public void startRecognize(View view) {
        // 1.创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(this, null);
        // 2.设置accent、language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");

        // 3.设置回调接口
        mDialog.setListener(new RecognizerDialogListener() {

            @Override
            public void onResult(RecognizerResult results, boolean isLast) {
                System.out.println("isLast:" + isLast);
                String parseResult = parseData(results.getResultString());
                mBuffer.append(parseResult);

                if (isLast) {
                    // 会话已经结束
                    String finalResult = mBuffer.toString();
                    System.out.println("最终结果:" + finalResult);

                    mBuffer = new StringBuffer();// 此方法相当于清空buffer

                    // 提问对象
                    TalkBean askBean = new TalkBean(finalResult, true, -1);
                    mTalkList.add(askBean);

                    Random random = new Random();

                    // 回答对象
                    String answer = "没听清";
                    int imageId = -1;
                    if (finalResult.contains("你好")) {
                        answer = "你好呀!";
                    } else if (finalResult.contains("你是谁")) {
                        answer = "我是你的小助手!";
                    } else if (finalResult.contains("天王盖地虎")) {
                        answer = "小鸡炖蘑菇";
                        imageId = R.drawable.m;
                    } else if (finalResult.contains("美女")) {
                        // 取随机内容和随机图片
                        int strId = random.nextInt(mAnswers.length);
                        answer = mAnswers[strId];
                        imageId = mAnswerPics[random
                                .nextInt(mAnswerPics.length)];
                    }

                    // 回答对象
                    TalkBean answerBean = new TalkBean(answer, false, imageId);
                    mTalkList.add(answerBean);
                    mAdapter.notifyDataSetChanged();

                    lvList.setSelection(mTalkList.size() - 1);

                    startSpeak(answer);
                }
            }

            @Override
            public void onError(SpeechError arg0) {

            }
        });

        // 4.显示dialog，接收语音输入
        mDialog.show();
    }

    /**
     * 解析语音json
     *
     * @param resultString
     * @return
     */
    protected String parseData(String resultString) {
        Gson gson = new Gson();
        VoiceBean voiceBean = gson.fromJson(resultString, VoiceBean.class);

        StringBuffer sb = new StringBuffer();
        ArrayList<VoiceBean.WSBean> ws = voiceBean.ws;
        for (VoiceBean.WSBean wsBean : ws) {
            String word = wsBean.cw.get(0).w;
            sb.append(word);
        }

        return sb.toString();
    }

    // 语音合成
    public void startSpeak(String text) {
        // 1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        SpeechSynthesizer mTts = SpeechSynthesizer
                .createSynthesizer(this, null);

        // 2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        // 设置发音人（更多在线发音人，用户可参见 附录12.2
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan"); // 设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");// 设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");// 设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); // 设置云端
        // mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH,
        // "./sdcard/iflytek.pcm");
        // 3.开始合成
        mTts.startSpeaking(text, null);
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTalkList.size();
        }

        @Override
        public TalkBean getItem(int position) {
            return mTalkList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(),
                        R.layout.list_item, null);
                holder = new ViewHolder();
                holder.tvAsk = (TextView) convertView.findViewById(R.id.tv_ask);
                holder.tvAnswer = (TextView) convertView
                        .findViewById(R.id.tv_answer);
                holder.ivPic = (ImageView) convertView
                        .findViewById(R.id.iv_pic);
                holder.llAnswer = (LinearLayout) convertView
                        .findViewById(R.id.ll_answer);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            TalkBean item = getItem(position);

            if (item.isAsk) {
                holder.tvAsk.setVisibility(View.VISIBLE);
                holder.llAnswer.setVisibility(View.GONE);

                holder.tvAsk.setText(item.text);
            } else {
                holder.tvAsk.setVisibility(View.GONE);
                holder.llAnswer.setVisibility(View.VISIBLE);

                holder.tvAnswer.setText(item.text);

                if (item.imageId > 0) {// 回答内容有图片
                    holder.ivPic.setVisibility(View.VISIBLE);
                    holder.ivPic.setImageResource(item.imageId);
                } else {
                    holder.ivPic.setVisibility(View.GONE);
                }
            }

            return convertView;
        }

    }

    static class ViewHolder {
        public TextView tvAsk;
        public TextView tvAnswer;
        public ImageView ivPic;
        public LinearLayout llAnswer;
    }

    // 友盟统计
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    // 友盟统计
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}

