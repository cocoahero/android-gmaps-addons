package com.cocoahero.android.gmaps.addons.mapbox;

import java.io.Closeable;
import java.io.File;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

public class MapBoxOfflineTileProvider implements TileProvider, Closeable {

    // ------------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------------

    private int mMinimumZoom = Integer.MIN_VALUE;
    private int mMaximumZoom = Integer.MAX_VALUE;
    private SQLiteDatabase mDatabase;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public MapBoxOfflineTileProvider(File file) {
        this(file.getAbsolutePath());
    }
    
    public MapBoxOfflineTileProvider(String pathToFile) {
        int flags = SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS;
        this.mDatabase = SQLiteDatabase.openDatabase(pathToFile, null, flags);
        this.calculateZoomConstraints();
    }

    // ------------------------------------------------------------------------
    // TileProvider Interface
    // ------------------------------------------------------------------------

    @Override
    public Tile getTile(int x, int y, int z) {
        Tile tile = NO_TILE;
        if (this.isZoomLevelAvailable(z) && this.isDatabaseAvailable()) {
            int row = (int) (Math.pow(2, z) - y);
            String[] projection = { "tile_data" };
            String predicate = "tile_row = ? AND tile_column = ? AND zoom_level = ?";
            String[] values = { String.valueOf(row), String.valueOf(x), String.valueOf(z) };
            Cursor c = this.mDatabase.query("tiles", projection, predicate, values, null, null, null);
            if (c != null) {
                c.moveToFirst();
                if (!c.isAfterLast()) {
                    tile = new Tile(256, 256, c.getBlob(0));
                }
                c.close();
            }
        }
        return tile;
    }

    // ------------------------------------------------------------------------
    // Closeable Interface
    // ------------------------------------------------------------------------

    /**
     * Closes the provider, cleaning up any background resources.
     * 
     * <p>
     * You must call {@link #close()} when you are finished using an instance of
     * this provider. Failing to do so may leak resources, such as the backing
     * SQLiteDatabase.
     * </p>
     */
    @Override
    public void close() {
        if (this.mDatabase != null) {
            this.mDatabase.close();
            this.mDatabase = null;
        }
    }

    // ------------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------------

    /**
     * The minimum zoom level supported by this provider.
     * 
     * @return the minimum zoom level supported or {@link Integer.MIN_VALUE} if
     *         it could not be determined.
     */
    public int getMinimumZoom() {
        return this.mMinimumZoom;
    }

    /**
     * The maximum zoom level supported by this provider.
     * 
     * @return the maximum zoom level supported or {@link Integer.MAX_VALUE} if
     *         it could not be determined.
     */
    public int getMaximumZoom() {
        return this.mMaximumZoom;
    }

    /**
     * Determines if the requested zoom level is supported by this provider.
     * 
     * @param zoom The requested zoom level.
     * @return {@code true} if the requested zoom level is supported by this
     *         provider.
     */
    public boolean isZoomLevelAvailable(int zoom) {
        return (zoom >= this.mMinimumZoom) && (zoom <= this.mMaximumZoom);
    }

    // ------------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------------

    private void calculateZoomConstraints() {
        if (this.isDatabaseAvailable()) {
            String[] projection = { "min(zoom_level)", "max(zoom_level)" };
            Cursor c = this.mDatabase.query("tiles", projection, null, null, null, null, null);
            c.moveToFirst();
            if (!c.isAfterLast()) {
                this.mMinimumZoom = c.getInt(0);
                this.mMaximumZoom = c.getInt(1);
            }
            c.close();
        }
    }

    private boolean isDatabaseAvailable() {
        return (this.mDatabase != null) && (this.mDatabase.isOpen());
    }

}
