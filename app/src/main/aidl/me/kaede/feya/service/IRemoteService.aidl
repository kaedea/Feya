// IRemoteService.aidl
package me.kaede.feya.service;

// Declare any non-default types here with import statements

interface IRemoteService {
    void toast(String msg);

    String talk(String msg);
}
