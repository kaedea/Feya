package me.kaede.feya.aysnc;

import android.util.Log;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func0;

/**
 * RxAndroid api demo
 * An API for asynchronous programming with observable streams.
 * {@link "https://github.com/ReactiveX/RxAndroid"}
 *
 * @author kaede
 * @version date 16/9/7
 */
public class RxApiTest extends TestCase {

    public static final String TAG = "rx";

    public void testCreate() {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("10086");
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "[onCompleted]");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                assertEquals(s, "10086");
            }
        });
    }

    public void testCreate2() {
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("10086");
            }
        });

        observable.subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "[onCompleted.1]");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.i(TAG, "[onNext.1] s = " + s);
                assertEquals(s, "10086");
            }
        });

        observable.subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "[onCompleted.2]");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.i(TAG, "[onNext.2] s = " + s);
                assertEquals(s, "10086");
            }
        });
    }


    public void testJust() {
        Observable<String> observable = Observable.just("10086", "10010");
        observable.subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.i(TAG, "[onNext] s = " + s);
            }
        });
    }


    public void testFrom() {
        List<String> list = new ArrayList<>();
        list.add("from1");
        list.add("from2");
        list.add("from3");

        Observable.from(list).subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.i(TAG, "[onNext] s = " + s);
            }
        });
    }

    public void testDefer() {
        Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                return Observable.just("10086");
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.i(TAG, "[onNext] s = " + s);
            }
        });
    }

    public void testInterval() {
        Observable.interval(1, TimeUnit.SECONDS).subscribe(new Observer<Long>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Long aLong) {
                Log.i(TAG, "[onNext] aLong = " + aLong);
            }
        });
    }
}
