package me.drakeet.timemachine.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import com.google.android.agera.database.SqlDatabaseSupplier;

import static me.drakeet.timemachine.Objects.requireNonNull;

/**
 * @author drakeet
 */

final class DatabaseSupplier extends SqlDatabaseSupplier {

    static final String ID_COLUMN = "id";
    static final String CONTENT_COLUMN = "content";
    static final String CONTENT_DESC_COLUMN = "contentDesc";
    static final String FROM_USER_ID_COLUMN = "fromUserId";
    static final String TO_USER_ID_COLUMN = "toUserId";
    static final String CREATED_AT_COLUMN = "createdAt";

    static final String TABLE = "messages";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE
        + " (" + ID_COLUMN + " VARCHAR(32) PRIMARY KEY, "
        + CONTENT_COLUMN + " BLOB, "
        + CONTENT_DESC_COLUMN + " VARCHAR(128), "
        + FROM_USER_ID_COLUMN + " VARCHAR(32), "
        + TO_USER_ID_COLUMN + " VARCHAR(32), "
        + CREATED_AT_COLUMN + " INTEGER);";

    private static final String DATABASE_NAME = "Messages.db";
    private static final int VERSION = 1;


    private DatabaseSupplier(@NonNull final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }


    @NonNull
    public static DatabaseSupplier databaseSupplier(@NonNull final Context context) {
        return new DatabaseSupplier(context);
    }


    @Override
    public void onCreate(@NonNull final SQLiteDatabase sqLiteDatabase) {
        requireNonNull(sqLiteDatabase).execSQL(CREATE_TABLE);
    }


    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
