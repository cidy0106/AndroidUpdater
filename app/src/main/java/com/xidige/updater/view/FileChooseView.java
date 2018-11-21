package com.xidige.updater.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.xidige.updater.R;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileChooseView extends AlertDialog implements OnClickListener, OnItemClickListener {
	private View contentView=null;
	
	private TextView currentNameTextView=null;
	private TextView currentPathTextView=null;
	private Button okButton=null;
	private Button cancelButton=null;
	private Button parentButton=null;
	private ListView listView=null;
	private List<File>datas=null;
	private MyChooseAdapter adapter=new MyChooseAdapter();
	
	private ProgressDialog progressDialog=null;
	private CancelEventListener cancelEventListener=null;
	private OkEventListener okEventListener=null;
	private File chooseFile=null;//选中了什么
	
	public FileChooseView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initView();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(contentView);
		progressDialog=ProgressDialog.show(getContext(), null, null);
	}
	private void initView() {
		// TODO Auto-generated method stub
		contentView=LayoutInflater.from(getContext()).inflate(R.layout.choosefiledialog_layout, null);
        currentNameTextView = contentView.findViewById(R.id.textView_current_file_name);
        currentPathTextView = contentView.findViewById(R.id.textView_current_file_path);
        okButton = contentView.findViewById(R.id.button_choose_ok);
        cancelButton = contentView.findViewById(R.id.button_choose_cancel);
        listView = contentView.findViewById(R.id.listView_file_tochoose);
		listView.setOnItemClickListener(this);
		listView.setAdapter(adapter);
		okButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
        parentButton = contentView.findViewById(R.id.button_parent);
		parentButton.setOnClickListener(this);
		
//		progressDialog=ProgressDialog.show(getContext(), null, null);
	}
	@Override
	public void hide() {
		// TODO Auto-generated method stub
		if(progressDialog!=null){
			progressDialog.hide();
		}
		super.hide();
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId()==R.id.button_choose_ok){
			if(okEventListener!=null){
				okEventListener.onOk(chooseFile);
			}
			hide();
		}else if (v.getId()==R.id.button_choose_cancel) {
			if(cancelEventListener!=null){
				cancelEventListener.onCancel();
			}
			hide();
		}else if(v.getId()==R.id.button_parent){
			File p=this.chooseFile.getParentFile();
			if(p==null){
				p=new File("/");
			}
			setRootDir(p);
		}
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		if(datas!=null && position>-1 && position <datas.size() ){
			setRootDir(datas.get(position));
		}
	}
	public interface OkEventListener{
        void onOk(File file);
	}
	public interface CancelEventListener{
        void onCancel();
	}
	class MyChooseAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if(datas!=null ){
				return datas.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			if(datas!=null && position>-1 && position <datas.size() ){
				return datas.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(datas!=null && position>-1 && position <datas.size() ){
				ViewHolder viewHolder=null;
				if(convertView==null){
					convertView=LayoutInflater.from(parent.getContext()).inflate(R.layout.choosefile_item, null);
					viewHolder=new ViewHolder();

                    viewHolder.file = convertView.findViewById(R.id.textView_file);
					convertView.setTag(viewHolder);
				}else{
					viewHolder=(ViewHolder) convertView.getTag();
				}
				File file=datas.get(position);
				viewHolder.file.setText(file.getName());
				return convertView;
			}
			return null;
		}
		class ViewHolder{
			TextView file;
		}
	}
	public void setRootDir(File dir){
		this.chooseFile=dir;
		currentNameTextView.setText(dir.getName());
		currentPathTextView.setText(dir.getAbsolutePath());
		new Thread(refreshRunning).start();
	}
	public void chooseDir(boolean isDir){
		this.isDir=isDir;
	}
	private boolean isDir=false;//是否只是目录
	private FileFilter fileFilter= new FileFilter() {
		
		@Override
		public boolean accept(File pathname) {
			// TODO Auto-generated method stub
			if(isDir){
				return pathname.isDirectory();
			}else{
				return true;
			}
		}
	};
	private Comparator<File> sortFile=new Comparator<File>() {

		@Override
		public int compare(File lhs, File rhs) {
			// TODO Auto-generated method stub
			return lhs.getName().compareTo(rhs.getName());
		}
	};
	private Runnable refreshRunning = new Runnable() {
		public void run() {
			handler.sendEmptyMessage(HANDLER_MSG_LOADINGOPEN);
			File []children=chooseFile.listFiles(fileFilter);
			if(children==null){
				datas=null;
			}else{
				datas=Arrays.asList(children);
			}
			if(datas!=null){
				Collections.sort(datas, sortFile);
			}			
			handler.sendEmptyMessage(HANDLER_MSG_UPDATE);
		}
	};
	private static final int HANDLER_MSG_LOADINGOPEN=0x0106;
	private static final int HANDLER_MSG_UPDATE=0x0107;
	
	private Handler handler=new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_MSG_LOADINGOPEN:
				progressDialog.show();				
				break;
			case HANDLER_MSG_UPDATE:
				adapter.notifyDataSetChanged();
				progressDialog.hide();
				break;
			default:
				break;
			}
        }
    };
	public void setOkEventListener(OkEventListener okEventListener) {
		this.okEventListener = okEventListener;
	}
	/**
	 * @param cancelEventListener the cancelEventListener to set
	 */
	public void setCancelEventListener(CancelEventListener cancelEventListener) {
		this.cancelEventListener = cancelEventListener;
	}	
}
