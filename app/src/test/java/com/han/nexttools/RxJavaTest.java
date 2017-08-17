package com.han.nexttools;

import org.junit.Test;

import java.io.File;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Han on 2017/8/14.
 */

public class RxJavaTest {


    @Test
    public void test() {
        File[] files = new File[2];
        files[0] = new File("D:\\Download");
        files[1] = new File("D:\\DecodeAPK");
        Observable.from(new File("D:\\").listFiles())
                .flatMap(new Func1<File, Observable<File>>() {
                    @Override
                    public Observable<File> call(File file) {
                        if (file.isDirectory()) {
                            return Observable.from(file.listFiles());
                        }
                        return Observable.from(new File[]{file});
                    }
                }).retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Throwable> observable) {
                return null;
            }
        }).subscribe(new Action1<File>() {
            @Override
            public void call(File file) {
                System.out.println(file);
            }
        });
//
//
//        Observable.from(new File("D:\\").listFiles())
//                .flatMap(new Func1<File, Observable<File>>() {
//                    @Override
//                    public Observable<File> call(File file) {
//                        System.out.println("flatmap >" + file.getName());
//                        return Observable.from(file.listFiles());
//                    }
//                })
//                .filter(new Func1<File, Boolean>() {
//                    @Override
//                    public Boolean call(File file) {
//                        System.out.println("filter >" + file.getName());
//                        return file.getName().endsWith(".crx");
//                    }
//                })
//                .map(new Func1<File, String>() {
//                    @Override
//                    public String call(File file) {
//                        return file.getName();
//                    }
//                })
//                .subscribe(new Action1<String>() {
//                    @Override
//                    public void call(String bitmap) {
//                        System.out.println("Subscribe > " + bitmap);
//                    }
//                });
    }

    @Test
    public void retry() {
        Observable<Object> obs = Observable
                .create(sub -> {
                    for (int i = 0; i < 10; i++) {
                        if (i == 1) {
                            sub.onError(new RuntimeException("error"));
                        }
                        sub.onNext(i);
                    }
                });
        obs.retry((time, ex) -> {
            if (time == 2 && ex instanceof RuntimeException) {
                return false;
            }
            return true;
        })
                .subscribe(obj -> System.out.println(obj));
    }
}