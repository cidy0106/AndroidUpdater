package com.xidige.updater.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.xidige.updater.R;
/**
 * 进度提示
 * @author lenovo
 *
 */
public class WaitingProgressDialog extends Dialog {
	private TextView titleTextView=null;
	private ImageView imageView=null;
	private Animation animation=null;
	
	public WaitingProgressDialog(Context context) {
		super(context,R.style.WaitingProgressTheme);
		// TODO Auto-generated constructor stub
		setCancelable(false);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		View view = LayoutInflater.from(getContext()).inflate(R.layout.progress_waiting_layout, null);  
		titleTextView = (TextView) view.findViewById(R.id.progress_title_textView);
		imageView=(ImageView) view.findViewById(R.id.progress_imageView);
        setContentView(view);
        animation=AnimationUtils.loadAnimation(getContext(), R.anim.waiting_animation);        
	}
	@Override
	public void show() {
		// TODO Auto-generated method stub
//		if(!super.isShowing()){
			super.show();
			imageView.startAnimation(animation);
//		}		
	}
	@Override
	public void hide() {
		// TODO Auto-generated method stub
//		if(super.isShowing()){
			super.hide();
			imageView.clearAnimation();
//		}
	}
	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
		imageView.clearAnimation();
	}
	public void setMessage(CharSequence msg){
		titleTextView.setText(msg);
	}
}
