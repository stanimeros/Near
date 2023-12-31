package com.example.socialapp;

import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Layer;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
public class Feed extends AppCompatActivity {
    private Bundle bundle;
    private String phone;
    private MyLocation myLocation;
    private ArrayList<User> myFriends;
    private LinearLayout linearLayout;
    private RelativeLayout relativeLayout;
    private View selectedView ;
    private TextView message;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        try {
            checkPermission();

            bundle = getIntent().getExtras();
            phone = bundle.getString("phone");
            String username = bundle.getString("username");
            String joinDate = bundle.getString("joinDate");
            int image = bundle.getInt("image");
            myFriends = bundle.getParcelableArrayList("list");

            long threadId = bundle.getLong("threadId");

            TextView textViewUsername = findViewById(R.id.textViewFeedUsername);
            textViewUsername.setText(username);

            ImageView profile = findViewById(R.id.imageViewFeedProfile);
            profile.setImageResource(Icons.getIcons().get(image -1));

            swipeRefreshLayout = findViewById(R.id.swipeToRefreshFeed);
            swipeRefreshLayout.setOnRefreshListener(() -> {
                new Thread(() -> {
                    checkPermission();
                    AsyncTaskCreateList createList = new AsyncTaskCreateList();
                    createList.execute();
                    System.out.println("List refreshed!");
                }).start();
            });

            TextView textViewJoinDate = findViewById(R.id.textViewFeedJoinDate);
            @SuppressLint("SimpleDateFormat")
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(joinDate);
            assert date != null;
            @SuppressLint("SimpleDateFormat")
            String join = new SimpleDateFormat(" MMMM dd, yyyy").format(date);
            textViewJoinDate.setText(getString(R.string.Member) + join);
            textViewJoinDate.setOnClickListener(v -> doRefresh());

            ImageView imageViewEdit = findViewById(R.id.imageViewFeedToEdit);
            imageViewEdit.setOnClickListener(v -> goToEditProfile());

            ImageView imageViewAdd = findViewById(R.id.imageViewFeedToFriends);
            imageViewAdd.setOnClickListener(v -> goToAddFriends());

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location loc) {
                    @SuppressWarnings("deprecation")
                    @SuppressLint("StaticFieldLeak")
                    class AsyncTaskLocationChange extends AsyncTask<String,String,String> {
                        @Override
                        @SuppressLint("SimpleDateFormat")
                        protected String doInBackground(String... strings) {
                           try {
                               Thread thread = findThreadById(threadId);
                               if (thread!=null){
                                   System.out.println("Aborted:Thread is alive!");
                                   AsyncTaskCreateList createList = new AsyncTaskCreateList();
                                   runOnUiThread(createList::execute);
                                   return null;
                               }

                               if (!canUpdate("Location")){
                                   AsyncTaskCreateList createList = new AsyncTaskCreateList();
                                   runOnUiThread(createList::execute);
                                   return null;
                               }
//                               GeoPoint pamak = new GeoPoint((float)22.96017,(float)40.625041);
//
//                               Random rand = new Random();
//                               int random = rand.nextInt(500000); //1km count
//
//                               String line = "";
//                               BufferedReader file = new BufferedReader(new InputStreamReader(getBaseContext().getAssets().open(MainActivity.unsorted_input)));
//
//                               for (int i=0;i<random;i++){
//                                   line = file.readLine();
//                               }
//                               int br = line.indexOf("-");
//                               char[] chars = line.toCharArray();
//                               char[] lon = Arrays.copyOfRange(chars, 0, br);
//                               char[] lat = Arrays.copyOfRange(chars, br+1, chars.length);
//
//                               while (new GeoPoint(String.valueOf(lon),String.valueOf(lat)).distanceTo(pamak)>MainActivity.diameter/2){
//                                   random = rand.nextInt(37800); //1km count
//
//                                   line = "";
//                                   file = new BufferedReader(new InputStreamReader(getBaseContext().getAssets().open(MainActivity.unsorted_input)));
//
//                                   for (int i=0;i<random;i++){
//                                       line = file.readLine();
//                                   }
//                                   br = line.indexOf("-");
//                                   chars = line.toCharArray();
//                                   lon = Arrays.copyOfRange(chars, 0, br);
//                                   lat = Arrays.copyOfRange(chars, br+1, chars.length);
//                               }
//
//                               loc.setLongitude(Double.parseDouble(String.valueOf(lon)));
//                               loc.setLatitude(Double.parseDouble(String.valueOf(lat)));
//
//                               System.out.println("Real location:"+loc.getLongitude()+","+loc.getLatitude());

                               myLocation = new MyLocation();
                               myLocation.setMyPointOfInterestSearch(new GeoPoint((float) loc.getLongitude(),(float)loc.getLatitude()), phone,getApplicationContext()); //set our location
                               AsyncTaskCreateList createList = new AsyncTaskCreateList();
                               runOnUiThread(createList::execute);
                           }catch (Exception e){
                               e.printStackTrace();
                           }
                            return null;
                        }
                    }
                    AsyncTaskLocationChange locationChange = new AsyncTaskLocationChange();
                    locationChange.execute();
                }
                @Override
                public void onProviderEnabled(@NonNull String provider) {}
                @Override
                public void onProviderDisabled(@NonNull String provider) {}
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}
            };
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
                System.out.println("GPS is enabled!");
            }else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, locationListener);
                System.out.println("Network location is enabled!");
            }


//            Random rand = new Random();
//            int random = rand.nextInt(6000);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    doRefresh();
//                }
//            }, random+6000);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void addView(User friend){
        try {
            @SuppressLint("InflateParams")
            View friendView = getLayoutInflater().inflate(R.layout.friend, null, false);
            friendView.setId(View.generateViewId());

            RelativeLayout basicLayout = friendView.findViewById(R.id.basicLayout);
            basicLayout.setOnClickListener(v -> {
                Layer layer = friendView.findViewById(R.id.layer);
                RelativeLayout background = friendView.findViewById(R.id.infoLayout);
                ImageView callButton = friendView.findViewById(R.id.imageViewCallButton);
                ImageView removeFriend = friendView.findViewById(R.id.imageViewDeleteFriend);
                TextView phoneNumber = friendView.findViewById(R.id.textViewFriendPhone);
                background.setBackgroundColor(getResources().getColor(R.color.base2));
                phoneNumber.setText(friend.getPhone());

                removeFriend.setOnClickListener(v12 -> {
                    linearLayout.removeView(friendView);
                    if (linearLayout.getChildCount()==0){
                        message.setText(R.string.noFriends);
                        message.setVisibility(View.VISIBLE);
                    }
                    new Thread(() -> ServerSQL.removeFriendOrRequest(phone,friend.getName())).start();
                });

                callButton.setOnClickListener(v1 -> {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" +friend.getPhone()));
                    startActivity(intent);
                });

                if (layer.getVisibility()==View.VISIBLE){
                    layer.setVisibility(View.GONE);
                }else{
                    if (selectedView!=null){ //Delete last selected
                        Layer selectedLayer = selectedView.findViewById(R.id.layer);
                        selectedLayer.setVisibility(View.GONE);
                    }
                    layer.setVisibility(View.VISIBLE);
                    selectedView = friendView;
                }
            });

            TextView username = friendView.findViewById(R.id.textViewFriendUsername);
            TextView kmAway = friendView.findViewById(R.id.textViewFriendKmAway);
            TextView lastUpdated = friendView.findViewById(R.id.textViewFriendLastUpdated);
            ImageView imageView = friendView.findViewById(R.id.imageViewFriendProfile);
            imageView.setImageResource(Icons.getIcons().get(friend.getImage()-1));

            username.setText(friend.getName());
            kmAway.setText(friend.getStringMetersAway());
            lastUpdated.setText(friend.getUpdateDateTime());

            linearLayout.addView(friendView);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void goToEditProfile()
    {
        Intent intent = new Intent(this, EditProfile.class);
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        finish();
    }

    private void goToAddFriends()
    {
        Intent intent = new Intent(this, Friends.class);
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        finish();
    }

    private void doRefresh()
    {
        Intent intent = new Intent(this, Feed.class);
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        finish();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskCreateList extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
           try {
               runOnUiThread(() -> {
                   linearLayout = findViewById(R.id.linearLayoutFeed);
                   message  = findViewById(R.id.textViewFeedMessage);
                   relativeLayout = findViewById(R.id.loadingLayoutFeed);
               });

               if (!canUpdate("Friends") && myFriends!=null){
                   runOnUiThread(() -> {
                       message.setVisibility(View.INVISIBLE);
                       linearLayout.removeAllViews();
                       relativeLayout.setVisibility(View.INVISIBLE);
                       for (int i = 0; i < myFriends.size(); i++) {
                           addView(myFriends.get(i));
                       }
                   });
                   runOnUiThread(() -> {
                       swipeRefreshLayout.setRefreshing(false);
                   });
                   return null;
               }

               ArrayList<User> temp = ServerSQL.getFriends(phone);
//               ArrayList<User> real = ServerSQL.getFriendsRealLocations(phone);
//               GeoPoint realLoc = ServerSQL.getRealLocation(phone);
               //ServerSQL.uploadExperimentRealPositions(realLoc,temp,real);

               if (temp.isEmpty()){
                   if (ServerSQL.isExecuted()){
                       System.out.println("No friends!");
                       runOnUiThread(() -> {
                           message.setText(R.string.noFriends);
                           message.setVisibility(View.VISIBLE);
                           relativeLayout.setVisibility(View.INVISIBLE);
                           linearLayout.removeAllViews();
                       });
                   }else{
                       System.out.println("No connection!");
                       runOnUiThread(() -> {
                           message.setText(getString(R.string.Error));
                           message.setVisibility(View.VISIBLE);
                           relativeLayout.setVisibility(View.INVISIBLE);
                           linearLayout.removeAllViews();
                       });
                   }
                   runOnUiThread(() -> {
                       swipeRefreshLayout.setRefreshing(false);
                   });
                   return null;
               }

               myFriends = temp;
               runOnUiThread(() -> {
                   message.setVisibility(View.INVISIBLE);
                   linearLayout.removeAllViews();
                   relativeLayout.setVisibility(View.INVISIBLE);
                   for (int i = 0; i < myFriends.size(); i++) {
                       addView(myFriends.get(i));
                   }
                   bundle.putParcelableArrayList("list",myFriends);
               });

               runOnUiThread(() -> {
                   swipeRefreshLayout.setRefreshing(false);
               });
           }catch (Exception e){
               e.printStackTrace();
           }
            return null;
        }
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            System.out.println("Permission granted!");
            myLocation = ServerSQL.getMyLocation(phone);
            AsyncTaskCreateList createList = new AsyncTaskCreateList();
            createList.execute();
        } else {
            checkPermission();
        }
    }

    private Thread findThreadById(long threadId) {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        Thread[] threads = new Thread[threadGroup.activeCount()];
        threadGroup.enumerate(threads);

        for (Thread thread : threads) {
            if (thread != null && thread.getId() == threadId) {
                return thread;
            }
        }

        return null;
    }

    private boolean canUpdate(String key){
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String temp = prefs.getString(key, "");
            if (!temp.isEmpty()){
                Date lastTimeUpdated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(temp);
                Date now = new Date();
                long diffInMillies = now.getTime() - Objects.requireNonNull(lastTimeUpdated).getTime();
                long diffInSeconds = TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                if (diffInSeconds<6){ //SECONDS PROTECTION
                    System.out.println("Aborted:"+key+" updated "+diffInSeconds+" seconds ago!");
                    return false;
                }
            }
            temp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putString(key,temp);
            prefsEditor.apply();
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onBackPressed() {}
}