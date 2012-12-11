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

    private int mMinimumZoom = -1;
    private int mMaximumZoom = -1;
    private SQLiteDatabase mDatabase;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public MapBoxOfflineTileProvider(File file) {
        int flags = SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS;
        this.mDatabase = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, flags);
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

    public int getMinimumZoom() {
        return this.mMinimumZoom;
    }

    public int getMaximumZoom() {
        return this.mMaximumZoom;
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

    private boolean isZoomLevelAvailable(int zoom) {
        return (this.mMinimumZoom <= zoom) && (this.mMaximumZoom >= zoom);
    }

    private boolean isDatabaseAvailable() {
        return (this.mDatabase != null) && (this.mDatabase.isOpen());
    }

}
