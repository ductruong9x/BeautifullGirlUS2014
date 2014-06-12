package com.truongtvd.girl.adapter;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.truongtvd.girl.model.ItemNewFeed;
import com.truongtvd.girlus.MyApplication;
import com.truongtvd.girlus.R;
import com.truongtvd.girlus.network.MyVolley;
import com.truongtvd.girlus.network.NetworkOperator;
import com.truongtvd.girlus.view.FadeInNetworkImageView;
import com.truongtvd.girlus.view.OnCloseClickListener;
import com.truongtvd.girlus.view.OnCommentClickListener;
import com.truongtvd.girlus.view.OnDownloadClickListener;
import com.truongtvd.girlus.view.OnLikeClickListener;
import com.truongtvd.girlus.view.OnSendClickListener;
import com.truongtvd.girlus.view.OnShareClickListener;

public class DetailAdapter extends PagerAdapter implements OnClickListener {
	private Context context;
	private ImageLoader imgLoader;
	private DisplayImageOptions options;

	private LayoutInflater inflater;
	private View detailview;
	private ArrayList<ItemNewFeed> listNew = null;
	private ItemNewFeed item;
	private boolean isOpen = false;
	private NetworkOperator operator;
	private ViewHolder viewHolder;
	private ProgressDialog dialog;
	private InterstitialAd interstitial;
	private String MY_AD_UNIT_ID = "ca-app-pub-6063844612770322/5784981293";

	public DetailAdapter(Context context, ArrayList<ItemNewFeed> listNew) {
		this.context = context;
		this.listNew = listNew;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imgLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.ic_default)
				.showImageOnFail(R.drawable.ic_default).cacheInMemory(true)
				.cacheOnDisc(false).displayer(new RoundedBitmapDisplayer(50))
				.build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		imgLoader.init(config);
		operator = NetworkOperator.getInstance().init(context);
		dialog = new ProgressDialog(context);
		dialog.setMessage("Sharing...");
		interstitial = new InterstitialAd((Activity) context);
		interstitial.setAdUnitId(MY_AD_UNIT_ID);
		AdRequest request = new AdRequest.Builder().build();
		interstitial.loadAd(request);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listNew.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object arg1) {
		// TODO Auto-generated method stub
		return view == (arg1);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// TODO Auto-generated method stub
		viewHolder = new ViewHolder();
		detailview = inflater.inflate(R.layout.layout_detail, container, false);
		item = listNew.get(position);
		viewHolder.btnAvatar = (ImageView) detailview
				.findViewById(R.id.btnAvatar);
		viewHolder.btnShare = (RelativeLayout) detailview
				.findViewById(R.id.btnShare);
		viewHolder.btnComment = (RelativeLayout) detailview
				.findViewById(R.id.btnComment);
		viewHolder.btnLike = (RelativeLayout) detailview
				.findViewById(R.id.btnLike);
		viewHolder.tvCountComment = (TextView) detailview
				.findViewById(R.id.tvCountComment);
		viewHolder.tvCountLike = (TextView) detailview
				.findViewById(R.id.tvCountLike);
		viewHolder.comment_detail = (RelativeLayout) detailview
				.findViewById(R.id.comment_detail);
		viewHolder.btnClose = (ImageButton) detailview
				.findViewById(R.id.btnCloseComment);
		viewHolder.load = (ProgressBar) detailview.findViewById(R.id.load);
		viewHolder.lvListComment = (ListView) detailview
				.findViewById(R.id.lvListComment);
		viewHolder.imgMyAvatar = (ImageView) detailview
				.findViewById(R.id.imgMyAvatar);
		viewHolder.edComment = (EditText) detailview
				.findViewById(R.id.edComment);
		viewHolder.btnSend = (Button) detailview.findViewById(R.id.btnSend);
		viewHolder.btnDownload = (ImageButton) detailview
				.findViewById(R.id.btnDownload);

		if (position % 10 == 0) {
			interstitial.show();
			AdRequest request = new AdRequest.Builder().build();
			interstitial.loadAd(request);
			
		}
		TextView tvDes = (TextView) detailview.findViewById(R.id.tvDes);
		FadeInNetworkImageView imgDetail = (FadeInNetworkImageView) detailview
				.findViewById(R.id.imgDetial);

		imgDetail.setImageUrl(item.getImage(), MyVolley.getImageLoader());
		imgLoader.displayImage(MyApplication.getAvater(), viewHolder.btnAvatar,
				options, new ImageLoadingListener() {

					@Override
					public void onLoadingStarted(String imageUri, View view) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {
						// TODO Auto-generated method stub

					}
				});
		viewHolder.tvCountComment.setText(item.getComment_count() + "");
		viewHolder.tvCountLike.setText(item.getLike_count() + "");

		tvDes.setText(item.getMessage() + "");
		viewHolder.btnAvatar.setOnClickListener(new OnCommentClickListener(
				context, viewHolder, item, operator, imgLoader, options));
		viewHolder.btnShare.setOnClickListener(new OnShareClickListener(
				context, item));
		viewHolder.btnComment.setOnClickListener(new OnCommentClickListener(
				context, viewHolder, item, operator, imgLoader, options));
		viewHolder.btnLike.setOnClickListener(new OnLikeClickListener(context,
				viewHolder, item));
		viewHolder.btnClose.setOnClickListener(new OnCloseClickListener(
				viewHolder));
		viewHolder.btnSend.setOnClickListener(new OnSendClickListener(context,
				viewHolder, item));
		viewHolder.btnDownload.setOnClickListener(new OnDownloadClickListener(
				context, item));
		((ViewPager) container).addView(detailview, 0);
		return detailview;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnAvatar:
			MyApplication.showToast(context, "asasd");
			if (isOpen == false) {

				viewHolder.btnComment.setVisibility(View.VISIBLE);
				viewHolder.btnLike.setVisibility(View.VISIBLE);
				viewHolder.btnShare.setVisibility(View.VISIBLE);
				isOpen = true;
			} else {
				viewHolder.btnComment.setVisibility(View.GONE);
				viewHolder.btnLike.setVisibility(View.GONE);
				viewHolder.btnShare.setVisibility(View.GONE);
				isOpen = false;
			}

			break;
		case R.id.btnComment:

			break;
		case R.id.btnLike:

			break;
		case R.id.btnShare:
			try {
				if (!Session.getActiveSession().getPermissions()
						.contains("publish_actions")) {
					NewPermissionsRequest request = new NewPermissionsRequest(
							(Activity) context,
							Arrays.asList("publish_actions"));

					Session.getActiveSession().requestNewPublishPermissions(
							request);
					return;
				}
			} catch (Exception e) {

			}
			dialog.show();
			Bundle postParams = new Bundle();
			postParams.putString("name", "Ảnh vui");
			postParams.putString("message", item.getImage());
			postParams.putString("description", item.getMessage());
			postParams
					.putString("link",
							"https://play.google.com/store/apps/details?id=com.truongtvd.anhvui");
			postParams.putString("picture", item.getImage());

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					dialog.dismiss();
					Toast.makeText(context, "Share successfuly	",
							Toast.LENGTH_SHORT).show();
				}
			};

			Request request = new Request(Session.getActiveSession(),
					"me/feed", postParams, HttpMethod.POST, callback);

			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();

			break;

		}
	}

	public class ViewHolder {
		public ImageView btnAvatar;
		public RelativeLayout btnShare, btnLike, comment_detail;
		public TextView tvCountLike, tvCountComment;
		public RelativeLayout btnComment;
		public ImageButton btnClose;
		public ProgressBar load;
		public ListView lvListComment;
		public ImageView imgMyAvatar;
		public EditText edComment;
		public Button btnSend;
		public ImageButton btnDownload;
	}

}
