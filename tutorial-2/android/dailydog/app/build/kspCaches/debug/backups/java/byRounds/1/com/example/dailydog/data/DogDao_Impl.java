package com.example.dailydog.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DogDao_Impl implements DogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DogItem> __insertionAdapterOfDogItem;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldestFavorite;

  private final SharedSQLiteStatement __preparedStmtOfRemoveFavorite;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldestCache;

  public DogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDogItem = new EntityInsertionAdapter<DogItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `dogs` (`imageUrl`,`breed`,`isFavorite`,`timestamp`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DogItem entity) {
        statement.bindString(1, entity.getImageUrl());
        statement.bindString(2, entity.getBreed());
        final int _tmp = entity.isFavorite() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindLong(4, entity.getTimestamp());
      }
    };
    this.__preparedStmtOfDeleteOldestFavorite = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM dogs WHERE imageUrl IN (SELECT imageUrl FROM dogs WHERE isFavorite = 1 ORDER BY timestamp ASC LIMIT 1)";
        return _query;
      }
    };
    this.__preparedStmtOfRemoveFavorite = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE dogs SET isFavorite = 0 WHERE imageUrl = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOldestCache = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM dogs WHERE imageUrl IN (SELECT imageUrl FROM dogs WHERE isFavorite = 0 ORDER BY timestamp ASC LIMIT ?)";
        return _query;
      }
    };
  }

  @Override
  public Object insertDog(final DogItem dog, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDogItem.insert(dog);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object addFavoriteFIFO(final DogItem dog, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> DogDao.DefaultImpls.addFavoriteFIFO(DogDao_Impl.this, dog, __cont), $completion);
  }

  @Override
  public Object insertCacheDogs(final List<DogItem> dogs,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> DogDao.DefaultImpls.insertCacheDogs(DogDao_Impl.this, dogs, __cont), $completion);
  }

  @Override
  public Object deleteOldestFavorite(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldestFavorite.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteOldestFavorite.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object removeFavorite(final String url, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRemoveFavorite.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, url);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfRemoveFavorite.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldestCache(final int amount, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldestCache.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, amount);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteOldestCache.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getFavorites(final Continuation<? super List<DogItem>> $completion) {
    final String _sql = "SELECT * FROM dogs WHERE isFavorite = 1 ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DogItem>>() {
      @Override
      @NonNull
      public List<DogItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfBreed = CursorUtil.getColumnIndexOrThrow(_cursor, "breed");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<DogItem> _result = new ArrayList<DogItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DogItem _item;
            final String _tmpImageUrl;
            _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            final String _tmpBreed;
            _tmpBreed = _cursor.getString(_cursorIndexOfBreed);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new DogItem(_tmpImageUrl,_tmpBreed,_tmpIsFavorite,_tmpTimestamp);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getFavoriteCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM dogs WHERE isFavorite = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDogByUrl(final String url, final Continuation<? super DogItem> $completion) {
    final String _sql = "SELECT * FROM dogs WHERE imageUrl = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, url);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DogItem>() {
      @Override
      @Nullable
      public DogItem call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfBreed = CursorUtil.getColumnIndexOrThrow(_cursor, "breed");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final DogItem _result;
          if (_cursor.moveToFirst()) {
            final String _tmpImageUrl;
            _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            final String _tmpBreed;
            _tmpBreed = _cursor.getString(_cursorIndexOfBreed);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _result = new DogItem(_tmpImageUrl,_tmpBreed,_tmpIsFavorite,_tmpTimestamp);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCacheDogs(final Continuation<? super List<DogItem>> $completion) {
    final String _sql = "SELECT * FROM dogs WHERE isFavorite = 0 ORDER BY timestamp DESC LIMIT 50";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DogItem>>() {
      @Override
      @NonNull
      public List<DogItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfBreed = CursorUtil.getColumnIndexOrThrow(_cursor, "breed");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<DogItem> _result = new ArrayList<DogItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DogItem _item;
            final String _tmpImageUrl;
            _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            final String _tmpBreed;
            _tmpBreed = _cursor.getString(_cursorIndexOfBreed);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new DogItem(_tmpImageUrl,_tmpBreed,_tmpIsFavorite,_tmpTimestamp);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getCacheCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM dogs WHERE isFavorite = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
