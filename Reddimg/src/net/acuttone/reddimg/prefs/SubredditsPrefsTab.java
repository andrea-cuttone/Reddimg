package net.acuttone.reddimg.prefs;
import net.acuttone.reddimg.R;
import net.acuttone.reddimg.R.layout;
import net.acuttone.reddimg.core.ReddimgApp;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;

public class SubredditsPrefsTab extends TabActivity {
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

	    setContentView(R.layout.subreddits_prefs_tabs);

	    TabHost tabHost = getTabHost(); 
	    TabHost.TabSpec spec;  
	    Intent intent; 
	    intent = new Intent().setClass(this, SubredditsPickerActivity.class);
	    spec = tabHost.newTabSpec("Selected").setIndicator("Selected").setContent(intent);
	    tabHost.addTab(spec);
	    intent = new Intent().setClass(this, UserSubredditsActivity.class);
	    spec = tabHost.newTabSpec("Subscribed").setIndicator("Subscribed").setContent(intent);
	    tabHost.addTab(spec);
	    intent = new Intent().setClass(this, PopularSubredditsActivity.class);
	    spec = tabHost.newTabSpec("Popular").setIndicator("Popular").setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	}
	
	@Override
	protected void onPause() {
		ReddimgApp.instance().getLinksQueue().initSubreddits();
		super.onPause();
	}
}