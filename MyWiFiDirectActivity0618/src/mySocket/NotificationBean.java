package mySocket;

import com.example.android.wifidirect.R;
import com.example.android.wifidirect.WiFiDirectActivity;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class NotificationBean extends Notification{
	private Context mContext ;
	
	public NotificationBean(Context context , int icon , CharSequence tickerText , long when , int id){
		super(icon , tickerText , when);
		this.mContext = context ;
		this.flags = Notification.FLAG_AUTO_CANCEL ;//设置用户点击之后自动消失
		//this.defaults = Notification.DEFAULT_SOUND;
		RemoteViews mRemoteView = new RemoteViews(mContext.getPackageName(), R.layout.notify_content);
		this.contentView = mRemoteView ;
		this.contentView.setProgressBar(R.id.pb, 100, 0, false);
	    this.contentView.setTextViewText(R.id.tv_progress, "进度" + 0 + "%");
	    this.contentView.setImageViewResource(R.id.iv_show, R.drawable.ic_launcher);
		this.contentIntent = PendingIntent.getActivity(mContext, id, new Intent(mContext , WiFiDirectActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		
	}
}
