package me.drakeet.transformer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import com.google.android.agera.database.SqlDatabaseSupplier;

/**
 * @author drakeet
 */

final class SimpleMessagesSqlDatabaseSupplier extends SqlDatabaseSupplier {

    static final String ID_COLUMN = "id";
    static final String CONTENT_COLUMN = "content";
    static final String FROM_USER_ID_COLUMN = "fromUserId";
    static final String TO_USER_ID_COLUMN = "toUserId";
    static final String CREATED_AT_COLUMN = "createdAt";

    static final String TABLE = "messages";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE
        + " (" + ID_COLUMN + " VARCHAR(32) PRIMARY KEY, "
        + CONTENT_COLUMN + " VARCHAR(255), "
        + FROM_USER_ID_COLUMN + " VARCHAR(32), "
        + TO_USER_ID_COLUMN + " VARCHAR(32), "
        + CREATED_AT_COLUMN + " INTEGER); ";

    private static final String DATABASE_NAME = "SimpleMessages";
    private static final int VERSION = 1;


    public SimpleMessagesSqlDatabaseSupplier(@NonNull final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }


    @NonNull
    public static SimpleMessagesSqlDatabaseSupplier databaseSupplier(@NonNull final Context context) {
        return new SimpleMessagesSqlDatabaseSupplier(context);
    }


    @Override
    public void onCreate(@NonNull final SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }


    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
