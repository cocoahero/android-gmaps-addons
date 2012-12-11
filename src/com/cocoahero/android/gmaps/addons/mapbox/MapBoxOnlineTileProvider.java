package com.cocoahero.android.gmaps.addons.mapbox;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import com.google.android.gms.maps.model.UrlTileProvider;

public class MapBoxOnlineTileProvider extends UrlTileProvider {

    private static final String[] FORMATS;

    static {
        String[] servers = new String[] { "a", "b", "c", "d" };
        String[] formats = new String[servers.length];
        for (int i = 0; i < servers.length; i++) {
            formats[i] = String.format("%%s://%s.tiles.mapbox.com/v3/%%s/%%d/%%d/%%d.png", servers[i]);
        }
        FORMATS = formats;
    }

    // ------------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------------

    private String mMapIdentifier;
    private boolean mUseSSL;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public MapBoxOnlineTileProvider(String mapIdentifier) {
        this(mapIdentifier, false);
    }

    public MapBoxOnlineTileProvider(String mapIdentifier, boolean useSSL) {
        super(256, 256);

        this.mMapIdentifier = mapIdentifier;
        this.mUseSSL = useSSL;
    }

    // ------------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------------
    
    public String getMapIdentifier() {
        return this.mMapIdentifier;
    }
    
    public void setMapIdentifier(String anIdentifier) {
        this.mMapIdentifier = anIdentifier;
    }
    
    public boolean isSSLEnabled() {
        return this.mUseSSL;
    }
    
    public void setSSLEnabled(boolean enableSSL) {
        this.mUseSSL = enableSSL;
    }

    @Override
    public URL getTileUrl(int x, int y, int z) {
        String f = FORMATS[new Random().nextInt(FORMATS.length)];
        String p = this.mUseSSL ? "https" : "http";
        try {
            return new URL(String.format(f, p, this.mMapIdentifier, z, x, y));
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

}
