package com.cocoahero.android.gmaps.addons.mapbox;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MakiFactory {
	GoogleMap map;
	double lat, lon;
	public MakiFactory(GoogleMap _map){
		this.map = _map;
	}
	void getBitmapFromURL(String urlString){
		new BitmapLoader().execute(urlString);
	}
	void drawMaki(String makiString,double _lat, double _lon){
		this.lat = _lat;
		this.lon = _lon;
		String urlString = "https://raw.github.com/mapbox/maki/gh-pages/renders/"+makiString+"-18@2x.png";
		this.getBitmapFromURL(urlString);
	}
	
	class BitmapLoader extends AsyncTask<String, Void,Bitmap>{

		@Override
		protected Bitmap doInBackground(String... src) {
			try {
		        URL url = new URL(src[0]);
		        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		        connection.setDoInput(true);
		        connection.connect();
		        InputStream input = connection.getInputStream();
		        Bitmap myBitmap = BitmapFactory.decodeStream(input);
		        return myBitmap;
		    } catch (IOException e) {
		        e.printStackTrace();
		        return null;
		    }
		}
		@Override
		protected void onPostExecute(Bitmap bitmap){
			Marker sf = map.addMarker(new MarkerOptions()
	        .position(new LatLng(lat,lon))
	        .title("Volcano campsite")
	        .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
		}
	}
}
