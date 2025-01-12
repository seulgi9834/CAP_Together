package com.example.together.Group;
//내가 가입한 그룹 보는 어댑터

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.EventDay;
import com.example.together.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

//사진 관련 부분은 일단 주석처리 했습니다. 굳이 그룹에서 데이터베이스 연동할 이유 없어보여서요.
public class Together_CustomAdapter extends RecyclerView.Adapter<Together_CustomAdapter.CustomViewHoler> {


    private ArrayList<gmake_list> arrayList;
    private Context context;
    private Intent intent;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();
    String uname;



    Dialog PlanDialog;//그룹 가입을 위한 Dialog
    TextView dia_content; //다이얼로그 내용


    public Together_CustomAdapter(ArrayList<gmake_list> arrayList,String uname, Context context) {
        this.arrayList = arrayList;
        this.uname = uname;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.together_list_item, parent, false);
        CustomViewHoler holer = new CustomViewHoler(view);
        return holer;

    }

    @Override
    public void onBindViewHolder(@NonNull final CustomViewHoler holder, int position) {

        //다이얼로그 관련 설정
        PlanDialog=new Dialog(context); //context로 하니까 잘 됩니다.
        PlanDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//제목 제거
        PlanDialog.setContentView(R.layout.group_dialog);

        dia_content = (TextView)PlanDialog.findViewById(R.id.dia_content);// setContentView에 대한 고찰..

        holder.Gname.setText(arrayList.get(position).getgname());
        holder.master.setText(arrayList.get(position).getmaster());
        String Gname = holder.Gname.getText().toString(); //그룹 이름을 저 변수에 담는다!
        String master = holder.master.getText().toString();
        //클릭 이벤트
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
                databaseReference = database.getReference();

                //닉네임 바뀐거 적용
                databaseReference.child("Together_group_list").child(Gname).child("user").child(uid).child("uname").setValue(uname);


                intent = new Intent(context, look_group.class); //그룹 상세 화면으로 연결
                intent.putExtra("uname",uname);
                intent.putExtra("Gname", Gname); //그룹 이름 넘겨서 열기
                intent.putExtra("master", master); //본인의 마스터 정보를 넘기기
                context.startActivity(intent); //액티비티 열기

            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if(master.equals("yes")) {
                    dia_content.setText(Gname + " 그룹을\n삭제하시겠습니까?");
                    showPlanDialog(Gname, master);
                }
                else{
                    dia_content.setText(Gname + " 그룹을\n탈퇴하시겠습니까?");
                    showPlanDialog(Gname, master);
                }

                return true;
            }
        });

    }


    @Override
    public int getItemCount() {
        //삼항 연산자, 배열이 비어있지 않으면 왼쪽이 실행!
        return (arrayList != null ? arrayList.size() : 0);
    }


    public class CustomViewHoler extends RecyclerView.ViewHolder {
        //ImageView iv_people;
        TextView Gname;
        TextView master;


        public CustomViewHoler(@NonNull View itemView) {
            super(itemView);
            //this.iv_people = itemView.findViewById(R.id.iv_people);
            this.Gname = itemView.findViewById(R.id.Gname);
            this.master = itemView.findViewById(R.id.master);

        }
    }


    //그룹 탈퇴 다이얼로그 호출(다이얼로그 관련 코드)
    public void showPlanDialog(String gname, String master){
        PlanDialog.show(); //다이얼로그 출력
        PlanDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//끝부분을 둥굴게 하기 위해 투명색 지정
        Button noBtn= PlanDialog.findViewById(R.id.noBtn);//취소 버튼
        Button yesBtn=PlanDialog.findViewById(R.id.yesBtn);//저장 버튼

        //취소 버튼
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(master.equals("yes")) {
                    Toast.makeText(v.getContext(),"그룹을 탈퇴하고싶다면 그룹장을 양도하세요.", Toast.LENGTH_SHORT).show(); //토스트로 실험
                }
                PlanDialog.dismiss();//다이얼로그 닫기
            }
        });

        //확인 버튼
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    database = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
                    databaseReference = database.getReference();


                    if(master.equals("yes")){// 그룹 삭제
                        databaseReference.child("Together_group_list").child(gname).removeValue();
                        databaseReference.child("User").child(uid).child("Group").child(gname).removeValue();
                    }
                    else{
                        databaseReference.child("Together_group_list").child(gname).child("gap").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                int value = (int)snapshot.getValue(Integer.class);
                                value -=1;
                                databaseReference.child("Together_group_list").child(gname).child("gap").setValue(value);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // 디비를 가져오던중 에러 발생 시
                                //Log.e("MainActivity", String.valueOf(databaseError.toException())); // 에러문 출력
                            }
                        });
                        databaseReference.child("Together_group_list").child(gname).child("user").child(uid).removeValue();
                        databaseReference.child("User").child(uid).child("Group").child(gname).removeValue();
                    }

                }catch(Exception e){//예외
                    e.printStackTrace();
                    //Toast.makeText(getApplicationContext().getContext(),"오류발생",Toast.LENGTH_SHORT).show();//토스메세지 출력
                }
                PlanDialog.dismiss();//다이얼로그 닫기
            }
        });

    }
}