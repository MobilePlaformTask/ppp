package com.example.wangshimeng.poetry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;

import java.util.ArrayList;
import java.util.List;

public class AnalysisActivity extends AppCompatActivity {

    private Button btnLastQuestion, btnNextQuestion;
    private TextView txtAnalysisQuestion, txtTrueAnswer, txtFalseAnswer, txtAnalysis, txtAnalysisQuestionName,txtAnalysisQuestionInfo;
    private List<AVObject> questionList = new ArrayList<AVObject>(); //题目列表
    private List<AVObject> mistakeList = new ArrayList<AVObject>(); //错题列表
    private int currentQuestionId = 0;//当前显示的题号
    String numbers = "";//错题序号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        txtAnalysisQuestion = (TextView) findViewById(R.id.txtAnalysisQuestion);
        txtTrueAnswer = (TextView) findViewById(R.id.txtTrueAnswer);
        txtFalseAnswer = (TextView) findViewById(R.id.txtFalseAnswer);
        txtAnalysis = (TextView) findViewById(R.id.txtAnalysis);
        btnLastQuestion = (Button) findViewById(R.id.btnLastQuestion);
        btnNextQuestion = (Button) findViewById(R.id.btnNextQuestion);
        btnLastQuestion.setEnabled(false);
        txtAnalysisQuestionName = (TextView) findViewById(R.id.txtAnalysisQuestionName);
        txtAnalysisQuestionInfo= (TextView) findViewById(R.id.txtAnalysisQuestionInfo);
        //接收参数   record的id


        //使用Intent对象得到传递来的参数
        Intent intent = getIntent();
        String value = intent.getStringExtra("record_id");
        // String value ="59045100a22b9d0065d841ec";
        AVQuery<AVObject> query = new AVQuery<>("Record");

        //查找对应的Record记录
        query.getInBackground(value, new GetCallback<AVObject>() {
            @Override
            public void done(final AVObject record, AVException e) {

                //查找这一套题的信息
                //查找这套题的全部题目
                AVQuery<AVObject> query1 = new AVQuery<>("Questions");
                query1.include("question_set_id");
                query1.orderByAscending("question_number");
                query1.whereEqualTo("question_set_id", AVObject.createWithoutData("Question_set", record.getAVObject("question_set_id").getObjectId()));

                query1.findInBackground(new FindCallback<AVObject>() {
                    @Override
                    public void done(List<AVObject> list, AVException e) {
                        if (e == null) {
                            questionList.addAll(list);
                            System.out.println("解析：");
                            for (AVObject question : list) {
                                String question_content = question.getString("question_content");//读取 question_content
                                String answer = question.getString("answer");// 读取 answer
                                Integer type = question.getInt("type_id");// 读取 type_id
                                String note = question.getString("note");// 读取 note
                                System.out.println(question_content + answer + type + note);
                            }

                            //查找错题集
                            AVQuery<AVObject> query2 = new AVQuery<>("Mistakes");
                            query2.include("question_id");
                            query2.orderByAscending("question_number");
                            query2.whereEqualTo("record_id", AVObject.createWithoutData("Record", record.getObjectId()));
                            query2.findInBackground(new FindCallback<AVObject>() {
                                @Override
                                public void done(List<AVObject> list, AVException e) {
                                    mistakeList.addAll(list);

                                    for (AVObject mistake : list) {

                                        AVObject question = mistake.getAVObject("question_id");
                                        numbers += question.getInt("question_number");
                                        numbers += ",";
                                    }

                                    //开始显示
                                    showAnalysis();

                                }
                            });
                        } else {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
    }


    public void showAnalysis() {

        if (currentQuestionId == 0) {
            btnLastQuestion.setEnabled(false);
            btnNextQuestion.setEnabled(true);
        } else if (currentQuestionId == questionList.size() - 1) {
            btnLastQuestion.setEnabled(true);
            btnNextQuestion.setEnabled(false);
        } else {
            btnLastQuestion.setEnabled(true);
            btnNextQuestion.setEnabled(true);
        }

        AVObject currentQuestion = questionList.get(currentQuestionId);
        String type = "";
        switch (currentQuestion.getInt("type_id")) {
            case 1:
                type = "选择题";
                break;
            case 2:
                type = "填下句";
                break;
            case 3:
                type = "点字成诗";
                break;
            case 4:
                type = "填空格";
                break;
            case 5:
                type = "填上句";
                break;
            case 6:
                type = "点字成诗";
                break;
        }

        if(currentQuestion.getInt("type_id")==1){
            String note[]=currentQuestion.getString("note").split(" ");
            txtAnalysisQuestion.setText(currentQuestion.getString("question_content")+"\n\n"+note[0]+"\n"+note[1]+"\n"+note[2]);
//            txtAnalysisQuestionInfo.setText(note[0]+"\n"+note[1]+"\n"+note[2]);
        }
        else{
//            txtAnalysisQuestionInfo.setText("");
            txtAnalysisQuestion.setText(currentQuestion.getString("question_content"));
        }

        txtAnalysisQuestionName.setText(currentQuestion.getInt("question_number") + "." + type);
//        txtAnalysisQuestion.setText(currentQuestion.getString("question_content"));
        txtTrueAnswer.setText(currentQuestion.getString("answer"));
        txtFalseAnswer.setText(currentQuestion.getString("answer"));
        txtAnalysis.setText(currentQuestion.getString("analysis"));

        //判断是否答错了
        if (numbers.contains(currentQuestion.getInt("question_number") + "")) {
            for (AVObject mistake : mistakeList) {
                AVObject question = mistake.getAVObject("question_id");
                if (question.getInt("question_number") == currentQuestionId + 1) {
                    txtFalseAnswer.setText(mistake.getString("mistake_answer"));
                }
            }
        }
    }


    public void next(View v) {
        if (currentQuestionId == questionList.size() - 1) {

        } else {
            currentQuestionId++;
            showAnalysis();
        }
    }

    public void last(View v) {
        if (currentQuestionId == 0) {

        } else {
            currentQuestionId--;
            showAnalysis();
        }
    }

    public void returnToHistory(View v) {
//         Intent intent = new Intent(AnalysisActivity.this, HistoryActivity.class);
//        startActivity(intent);
        onBackPressed();
    }

}
