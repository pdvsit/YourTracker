package com.app.interfaces;

public interface WebServiceInterface<T> {
     void requestCompleted(T obj, int serviceCode);
}
