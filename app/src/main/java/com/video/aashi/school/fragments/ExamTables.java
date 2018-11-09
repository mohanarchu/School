package com.video.aashi.school.fragments;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.video.aashi.school.APIUrl;
import com.video.aashi.school.MainActivity;
import com.video.aashi.school.Navigation;
import com.video.aashi.school.R;
import com.video.aashi.school.adapters.Interfaces.MyInterface;
import com.video.aashi.school.adapters.arrar_adapterd.Exams;
import com.video.aashi.school.adapters.arrar_adapterd.Holiday_adapter;
import com.video.aashi.school.adapters.post_class.ExamTime;
import com.video.aashi.school.adapters.post_class.Hols;
import com.video.aashi.school.fragments.payments.Weekoffs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExamTables extends Fragment {




    Toolbar toolbar;
    List<Exams> examsList= new ArrayList<>();
  String acaid;
  String examid ;
  String locid ;
  String studentd;
  String classid;
  Retrofit retrofit;
  MyInterface myInterface;
  RecyclerView recyclerView;
  ExamAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exam_tables, container, false);
        toolbar =(android.support.v7.widget.Toolbar) getActivity().findViewById(R.id.toolbar);
        setHasOptionsMenu(true);
        toolbar.setTitle("Exam timetable");
        recyclerView =(RecyclerView)view.findViewById(R.id.exam_recycle);
        acaid = MainActivity.academicyear;
        examid ="1";
        locid = Navigation.location_id;
        studentd = Navigation.student_id;
        classid = Navigation.class_id;
        OkHttpClient defaulthttpClient = new OkHttpClient.Builder()
                .addInterceptor(
                        new Interceptor() {
                            @Override
                            public okhttp3.Response intercept(Chain chain) throws IOException {
                                Request request = chain.request().newBuilder()
                                        .addHeader("Content-Type", "application/json").build();
                                return chain.proceed(request);

                            }
                        }).build();
        retrofit = new Retrofit.Builder().baseUrl(APIUrl.BASE_URL).addConverterFactory
                (GsonConverterFactory.create())
                .client(defaulthttpClient)
                .build();
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        myInterface = retrofit.create(MyInterface.class);
        new getExams().execute();
        return  view;
    }


class getExams extends AsyncTask
{

    ProgressDialog progressDialog;
    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        examsList = new ArrayList<>();

        Call<ResponseBody> call = myInterface.getExams(new ExamTime(examid,locid,classid,studentd,acaid));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                String bodyString = null;
                try {
                    bodyString = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 Log.i("Tag", "MyExams" + bodyString);
                {
                    try {
                        JSONObject object = new JSONObject(bodyString);
                        JSONArray list = object.getJSONArray("Student Exam TimeTable Details");
                        for (int i = 0; i < list.length(); i++) {
                            JSONObject data = list.getJSONObject(i);

                            if(data.length() != 0) {

                                String subject, date, time, amorpm;
                                date = data.getString("examDtDisp");
                                subject = data.getString("subjectGeneralName");
                                time = data.getString("maxDuration");
                                amorpm = data.getString("examSession");
                                examsList.add(new Exams(time, amorpm, date, subject));
                                adapter = new ExamAdapter(examsList);

                                recyclerView.setAdapter(adapter);
                                progressDialog.dismiss();
                            }
                            if(progressDialog.isShowing())
                            {
                                progressDialog.dismiss();
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }



            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }

        });

        return null;
    }
}
    class ExamAdapter extends RecyclerView.Adapter<ExamTables.Viewholder> {


        List<Exams> exam_adapters;


        @NonNull
        @Override
        public Viewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.examtimetable, viewGroup, false);
            return new Viewholder(view);
        }

        public ExamAdapter(List<Exams> adapters) {
            this.exam_adapters = adapters;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Viewholder viewholder, int i) {

            viewholder.date.setText(examsList.get(i).getDate());
            viewholder.sub.setText(examsList.get(i).getSubject());
            int hours =  Integer.valueOf( examsList.get(i).getDuration())/ 60; //since both are ints, you get an int
            viewholder.duration.setText(hours+"hrs"+ "("+ exam_adapters.get(i).getAmorpm()+")");


        }

        @Override
        public int getItemCount() {
            return exam_adapters.size();
        }
    }

    public static class Viewholder extends RecyclerView.ViewHolder

    {
        TextView date;
        TextView sub;
        TextView duration;
        LinearLayout visibility;

        public Viewholder(@NonNull View itemView) {


            super(itemView);
            date =(TextView)itemView.findViewById(R.id.exam_date);
            duration=(TextView)itemView.findViewById(R.id.examduration);
            sub =(TextView)itemView.findViewById(R.id.examsub);





        }
    }
}

