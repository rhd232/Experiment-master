package com.wind.data.base.datastore;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqldelight.SqlDelightStatement;
import com.wind.data.base.bean.User;
import com.wind.data.base.bean.UserModel;
import com.wind.data.base.request.FindUserRequest;
import com.wind.data.base.request.InsertUserRequest;
import com.wind.data.base.request.UpdateUserRequest;
import com.wind.data.base.response.FindUserResponse;
import com.wind.data.base.response.InsertUserResponse;
import com.wind.data.base.response.UpdateUserResponse;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class UserDataStore {

    public static final String TAG = "UserDataStore";
    private final BriteDatabase mBriteDb;

    private static UserDataStore sInstance=null;
    public static UserDataStore getInstance(BriteDatabase briteDb){
        if (sInstance==null){
            synchronized (UserDataStore.class){
                if (sInstance==null){
                    sInstance=new UserDataStore(briteDb);
                }
            }
        }

        return sInstance;
    }
    @Inject
    public UserDataStore(BriteDatabase briteDb) {
        this.mBriteDb = briteDb;
    }

    public Observable<InsertUserResponse> insertUser(final InsertUserRequest request) {
        return Observable.create(new Observable.OnSubscribe<InsertUserResponse>() {

            @Override
            public void call(Subscriber<? super InsertUserResponse> subscriber) {
                InsertUserResponse response=new InsertUserResponse();
                response.setErr(-1);
                final BriteDatabase.Transaction transaction = mBriteDb.newTransaction();
                try {

                    UserModel.Marshal marshal = User.FACTORY.marshal();

                    long rowId=mBriteDb.insert(User.TABLE_NAME, marshal.username(request.getUsername())
                                    .password(request.getPwd()).asContentValues(),
                            SQLiteDatabase.CONFLICT_REPLACE);
                    transaction.markSuccessful();
                    response.setErr(0);
                    subscriber.onNext(response);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                } finally {
                    transaction.end();
                }
            }
        });

    }

    public Observable<FindUserResponse> findUserByUsernameAndPwd(FindUserRequest request) {
        final FindUserResponse response = new FindUserResponse();
        response.setErr(-1);
       SqlDelightStatement statement= User.FACTORY.find_user_by_username_and_pwd(request.getUsername(),
                request.getPwd());
        return mBriteDb.createQuery(User.TABLE_NAME,
                statement.statement,statement.args)
                .mapToOneOrDefault(new Func1<Cursor, FindUserResponse>() {
                    @Override
                    public FindUserResponse call(Cursor cursor) {
                        final BriteDatabase.Transaction transaction = mBriteDb.newTransaction();
                        try {
                            //if (cursor.moveToLast()) {
                            User user=User.FACTORY.find_user_by_username_and_pwdMapper().map(cursor);
                            response.setUser(user);
                            response.setErr(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            transaction.end();
                        }
                        return response;
                    }
                }, response);
    }
    public Observable<FindUserResponse> findUserByUsername(FindUserRequest request) {
        final FindUserResponse response = new FindUserResponse();
        response.setErr(-1);

        return mBriteDb.createQuery(User.TABLE_NAME,
                User.FACTORY.find_user_by_username(request.getUsername()).statement,
                request.getUsername())
                .mapToOneOrDefault(new Func1<Cursor, FindUserResponse>() {
                    @Override
                    public FindUserResponse call(Cursor cursor) {

                        final BriteDatabase.Transaction transaction = mBriteDb.newTransaction();
                        try {
                            //if (cursor.moveToLast()) {
                            User user=User.FACTORY.find_user_by_usernameMapper().map(cursor);
                            response.setUser(user);
                            response.setErr(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            transaction.end();
                        }
                        return response;
                    }
                }, response);
    }
    public Observable<UpdateUserResponse> updateUserByUsername(final UpdateUserRequest request) {
        final UpdateUserResponse response = new UpdateUserResponse();
        response.setErr(-1);

        return Observable.create(new Observable.OnSubscribe<UpdateUserResponse>() {
            @Override
            public void call(Subscriber<? super UpdateUserResponse> subscriber) {
                UserModel.Marshal marshal = User.FACTORY.marshal();
                ContentValues values=marshal.password(request.getPwd()).asContentValues();

                int rowId=mBriteDb.update(User.TABLE_NAME,values,User.USERNAME +" = ?",request.getUsername());
                response.setErr(0);
                subscriber.onNext(response);
                subscriber.onCompleted();
            }
        });
    }
}
