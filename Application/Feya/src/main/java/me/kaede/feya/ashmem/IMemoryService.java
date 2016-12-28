/*
 * Copyright (c) 2016. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 *
 */

package me.kaede.feya.ashmem;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

/**
 * Created by Kaede on 16/8/15.
 */
public interface IMemoryService extends IInterface {

    ParcelFileDescriptor getFileDescriptor() throws RemoteException;

    void setValue(int val) throws RemoteException;

    abstract class Stub extends Binder implements IMemoryService {
        private static final String DESCRIPTOR = "shy.luo.ashmem.IMemoryService";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMemoryService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }

            IInterface iin = (IInterface) obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && iin instanceof IMemoryService) {
                return (IMemoryService) iin;
            }

            return new IMemoryService.Stub.Proxy(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws android.os.RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_getFileDescriptor: {
                    data.enforceInterface(DESCRIPTOR);

                    ParcelFileDescriptor result = this.getFileDescriptor();

                    reply.writeNoException();

                    if (result != null) {
                        reply.writeInt(1);
                        result.writeToParcel(reply, 0);
                    } else {
                        reply.writeInt(0);
                    }

                    return true;
                }
                case TRANSACTION_setValue: {
                    data.enforceInterface(DESCRIPTOR);

                    int val = data.readInt();
                    setValue(val);

                    reply.writeNoException();

                    return true;
                }
            }

            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IMemoryService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                mRemote = remote;
            }

            public IBinder asBinder() {
                return mRemote;
            }

            public String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            public ParcelFileDescriptor getFileDescriptor() throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();

                ParcelFileDescriptor result;

                try {
                    data.writeInterfaceToken(DESCRIPTOR);

                    mRemote.transact(Stub.TRANSACTION_getFileDescriptor, data, reply, 0);

                    reply.readException();
                    if (0 != reply.readInt()) {
                        result = ParcelFileDescriptor.CREATOR.createFromParcel(reply);
                    } else {
                        result = null;
                    }
                } finally {
                    reply.recycle();
                    data.recycle();
                }

                return result;
            }

            public void setValue(int val) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();

                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    data.writeInt(val);

                    mRemote.transact(Stub.TRANSACTION_setValue, data, reply, 0);

                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }
        }

        static final int TRANSACTION_getFileDescriptor = IBinder.FIRST_CALL_TRANSACTION + 0;
        static final int TRANSACTION_setValue = IBinder.FIRST_CALL_TRANSACTION + 1;

    }
}