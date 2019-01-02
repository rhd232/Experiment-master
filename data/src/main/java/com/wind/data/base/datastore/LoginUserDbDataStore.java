package com.wind.data.base.datastore;

import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;


/**
 * Created by wind on 16/5/19.
 */
public class LoginUserDbDataStore {
    public static final String TAG = "LoginUserDbDataStore";
    private final BriteDatabase mBriteDb;

    @Inject
    public LoginUserDbDataStore(BriteDatabase briteDb) {

        this.mBriteDb = briteDb;

    }

  /*  public void putLoginUser(final LoginResponse loginResponse) {
        String jsonUser = JsonParser.object2Json(loginResponse);
        final BriteDatabase.Transaction transaction = mBriteDb.newTransaction();
        try {

            LoginUserModel.Marshal marshal = LoginUser.FACTORY.marshal();
            try {
                int result = mBriteDb.getWritableDatabase().delete(LoginUser.TABLE_NAME, LoginUser._ID + "=?", new String[]{"0"});
                LogUtil.e("TABLE_NAME", "delete:" + result);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mBriteDb.insert(LoginUser.TABLE_NAME, marshal.json_user(jsonUser)._id(0).asContentValues(),
                    SQLiteDatabase.CONFLICT_REPLACE);
            transaction.markSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            transaction.end();
        }
    }

    public Observable<LoginResponse> getLoginUser() {
//LoginUser.SELECT_LOGIN_USER
        return mBriteDb.createQuery(LoginUser.TABLE_NAME, LoginUser.FACTORY.select_login_user().statement)
                .mapToOneOrDefault(new Func1<Cursor, LoginResponse>() {
                    @Override
                    public LoginResponse call(Cursor cursor) {
                        LoginResponse response = null;
                        final BriteDatabase.Transaction transaction = mBriteDb.newTransaction();
                        try {
                            //if (cursor.moveToLast()) {
                            LoginUserModel model = LoginUser.SELECT_LOGIN_USER_ROWMARPER.map(cursor);
                            String json = model.json_user();

                            response = JsonParser.parserObject(json, LoginResponse.class);

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            transaction.end();
                        }
                        return response;
                    }
                }, null);
    }


    public Observable<DelLoginUserResponse> deleleLoginUser() {
        return Observable.create(new Observable.OnSubscribe<DelLoginUserResponse>() {
            @Override
            public void call(Subscriber<? super DelLoginUserResponse> subscriber) {
                final BriteDatabase.Transaction transaction = mBriteDb.newTransaction();
                try {

                    try {
                        mBriteDb.getWritableDatabase().delete(LoginUser.TABLE_NAME, LoginUser._ID + "=?", new String[]{"0"});
                        int result = mBriteDb.delete(LoginUser.TABLE_NAME, LoginUser._ID + "=?", new String[]{"0"});
                        LogUtil.e("TABLE_NAME", "delete:" + result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    transaction.markSuccessful();
                    DelLoginUserResponse response = new DelLoginUserResponse();
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

    }*/


}
