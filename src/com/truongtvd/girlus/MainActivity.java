package com.truongtvd.girlus;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.truongtvd.girl.adapter.DetailAdapter;
import com.truongtvd.girl.model.ItemNewFeed;
import com.truongtvd.girlus.network.NetworkOperator;
import com.truongtvd.girlus.util.JsonUtils;

public class MainActivity extends SherlockActivity {
	private ActionBar actionBar;
	private ViewPager vpMain;
	private DetailAdapter adapter;
	private ArrayList<ItemNewFeed> listNew = new ArrayList<ItemNewFeed>();
	private NetworkOperator operator;
	private Session session;
	private ProgressBar loading;
	private String id, avatar, nameUser;
	private AdView adView;
	private ImageButton btnInvate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		session = Session.getActiveSession();
		operator = NetworkOperator.getInstance().init(this);
		vpMain = (ViewPager) findViewById(R.id.vpMain);
		btnInvate = (ImageButton) findViewById(R.id.btnInvate);
		loading = (ProgressBar) findViewById(R.id.loading);
		adView = (AdView) findViewById(R.id.adFragment);
//		adView.setAdSize(AdSize.SMART_BANNER);
//		adView.setAdUnitId("ca-app-pub-6063844612770322/5548295694");
		adView.loadAd(new AdRequest.Builder().build());
		getIDUser();
		btnInvate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendRequestDialog();
			}
		});
	}

	private void sendRequestDialog() {
		Bundle params = new Bundle();
		params.putString("message", "Use Beatifull girl with me");

		WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(
				MainActivity.this, Session.getActiveSession(), params))
				.setOnCompleteListener(new OnCompleteListener() {

					@Override
					public void onComplete(Bundle values,
							FacebookException error) {
						if (error != null) {
							if (error instanceof FacebookOperationCanceledException) {
								Toast.makeText(
										MainActivity.this
												.getApplicationContext(),
										"Request cancelled", Toast.LENGTH_SHORT)
										.show();
							} else {
								Toast.makeText(
										MainActivity.this
												.getApplicationContext(),
										"Network Error", Toast.LENGTH_SHORT)
										.show();
							}
						} else {
							final String requestId = values
									.getString("request");
							if (requestId != null) {
								Toast.makeText(
										MainActivity.this
												.getApplicationContext(),
										"Request sent", Toast.LENGTH_SHORT)
										.show();
							} else {
								Toast.makeText(
										MainActivity.this
												.getApplicationContext(),
										"Request cancelled", Toast.LENGTH_SHORT)
										.show();
							}
						}
					}

				}).build();
		requestsDialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	private void getIDUser() {
		Request request = Request.newMeRequest(session,
				new GraphUserCallback() {

					@Override
					public void onCompleted(GraphUser user, Response response) {
						// TODO Auto-generated method stub
						try {
							id = user.getId();
							getUserInfo(id);
						} catch (Exception e) {

						}
					}
				});
		Request.executeBatchAsync(request);
	}

	private void getUserInfo(String id) {
		String fqlQuery = "SELECT name,pic FROM user WHERE uid='" + id + "'";
		Bundle params = new Bundle();
		params.putString("q", fqlQuery);

		// session = Session.getActiveSession();
		Request request = new Request(session, "/fql", params, HttpMethod.GET,
				new Request.Callback() {
					public void onCompleted(Response response) {
						JSONObject jso = JsonUtils.parseResponToJson(response);
						try {
							JSONArray data = jso.getJSONArray("data");
							if (data.length() > 0) {
								JSONObject info = data.getJSONObject(0);
								avatar = info.getString("pic");
								nameUser = info.getString("name");
								MyApplication.setAvater(avatar);
								MyApplication.setName(nameUser);
								Log.e("AVATAR", MyApplication.getAvater() + "");
								Log.e("USERNAME", MyApplication.getName() + "");
								getNewFeed();
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// Log.e("USER_INFO", jso.toString());

					}
				});
		Request.executeBatchAsync(request);

	}

	private void getNewFeed() {
		operator.getNewFeed(500, getSuccess(), getError());
	}

	private Listener<JSONObject> getSuccess() {
		// TODO Auto-generated method stub
		return new Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response, String extraData) {
				// TODO Auto-generated method stub
				loading.setVisibility(View.GONE);
				Log.e("NEW", response.toString());
				listNew = JsonUtils.getListItem(response, listNew);
				adapter = new DetailAdapter(MainActivity.this, listNew);
				vpMain.setAdapter(adapter);
			}
		};
	}

	private ErrorListener getError() {
		// TODO Auto-generated method stub
		return new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error, String extraData) {
				// TODO Auto-generated method stub
				loading.setVisibility(View.GONE);
				MyApplication.showToast(MainActivity.this, "f you can not download content. Please go to the Play Store to update the latest version");
				error.printStackTrace();
			}
		};
	}

	public void danhGia() {
		SharedPreferences getPre = getSharedPreferences("SAVE", MODE_PRIVATE);
		int i = getPre.getInt("VOTE", 0);
		SharedPreferences pre;
		SharedPreferences.Editor edit;
		switch (i) {
		case 0:
			pre = getSharedPreferences("SAVE", MODE_PRIVATE);
			edit = pre.edit();
			edit.putInt("VOTE", 1);
			edit.commit();
			break;
		case 1:
			pre = getSharedPreferences("SAVE", MODE_PRIVATE);
			edit = pre.edit();
			edit.putInt("VOTE", i + 1);
			edit.commit();
			break;
		case 2:
			pre = getSharedPreferences("SAVE", MODE_PRIVATE);
			edit = pre.edit();
			edit.putInt("VOTE", i + 1);
			edit.commit();
			break;
		case 3:
			pre = getSharedPreferences("SAVE", MODE_PRIVATE);
			edit = pre.edit();
			edit.putInt("VOTE", i + 1);
			edit.commit();
			break;
		case 4:
			pre = getSharedPreferences("SAVE", MODE_PRIVATE);
			edit = pre.edit();
			edit.putInt("VOTE", i + 1);
			edit.commit();
			break;
		case 5:
			DialogVote dialog = new DialogVote(MainActivity.this);
			dialog.show();
			pre = getSharedPreferences("SAVE", MODE_PRIVATE);
			edit = pre.edit();
			edit.putInt("VOTE", 5);
			edit.commit();
			break;
		}
	}

	private boolean backPressedToExitOnce = false;

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (backPressedToExitOnce) {
			super.onBackPressed();
		} else {
			this.backPressedToExitOnce = true;
			Toast.makeText(MainActivity.this, getString(R.string.backclick),
					Toast.LENGTH_SHORT).show();
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					backPressedToExitOnce = false;
				}
			}, 3000);
		}
	}

}
