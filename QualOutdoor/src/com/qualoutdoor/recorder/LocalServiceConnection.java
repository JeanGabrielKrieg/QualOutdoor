package com.qualoutdoor.recorder;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * This ServiceConnection is used to bind a component to a service within the
 * same process.
 * 
 * The binding process impact the lifecycle of a service. The service should be
 * kept alive as long as some component are bound to it. When no more components
 * are bound to it, chances are for it to be destroyed.
 * 
 * #### Please note
 * 
 * The service you are binding to through this connection must return a
 * LocalBinder in `onBind()`. Indeed we are assuming the IBinder returned by the
 * service can be cast to a LocalBinder. Using a LocalBinder requires the
 * service to live within the same process as your component.
 * 
 * Note that this class is designed to bind a single context at a time. There
 * must be as many LocalServiceConnection as component to bind to one service.
 * That's why it is prefered to keep the number of LocalServiceConnection to the
 * minimum and share them as ServiceProvider as much as possible between the
 * components of the application.
 * 
 * @author Gaborit Nicolas
 * 
 * @param <S>
 *            The class of the Service to which to connect
 */
public class LocalServiceConnection<S extends Service> implements
        ServiceProvider<S> {

    /** The class of the Service S */
    private final Class<S> serviceClass;
    /** A reference to the connected service */
    private S service;

    /** Indicates if the service is bound or not */
    private boolean isAvailable = false;
    /** Indicates if this ServiceConnection is bound to a service or not */
    private boolean isBound = false;

    /** The reference to the context that has been bound through this connection */
    private Context context;

    /** The collection of the registered ServiceListener */
    private List<IServiceListener<S>> listeners = new ArrayList<IServiceListener<S>>();

    /** This service connection is used to monitor the state of the service */
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName serviceName) {
            // The service is no more available
            isAvailable = false;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(ComponentName serviceName, IBinder binder) {
            try {
                // Cast the IBinder to a LocalBinder
                service = ((LocalBinder<S>) binder).getService();
            } catch (ClassCastException exc) {
                throw new ClassCastException(
                        "The bound service must provide the same LocalBinder<S> as declared.");
            }
            // We have successfully bound to the service
            isAvailable = true;
            // Notifify the listeners
            notifyListeners();

        }
    };

    /**
     * Create a new LocalServiceConnection, we provide the service class for
     * later use (see `bindToService()` method).
     */
    public LocalServiceConnection(Class<S> serviceType) {
        this.serviceClass = serviceType;
    }

    /**
     * Bind the given context to a Service S through this ServiceConnection.
     * 
     * @param context
     *            The context that needs to be bound to the service through this
     *            ServiceConnection
     * @return True if the Service has bound successfully
     */
    public boolean bindToService(Context context) {
        // Check that this connection hasn't been bound already
        if (!isBound) {
            this.context = context;
            // Create an intent toward Service S. This is where we need to know
            // the class of S.
            Intent intent = new Intent(context, serviceClass);
            // Bind the context to the Service through this ServiceConnection
            isBound = context.bindService(intent, connection,
                    Context.BIND_AUTO_CREATE);
        }
        return isBound;
    }

    /** Unbind this ServiceConnection if it was bound */
    public void unbindService() {
        if (isBound) {
            // Unbind the context
            context.unbindService(connection);
            // We are no longer bound
            isBound = false;
            // The service may not be available in the future
            isAvailable = false;
        }
    }

    @Override
    public boolean isAvailable() {
        return isAvailable;
    }

    /** Indicates wether this ServiceConnection is bound to a service already */
    public boolean isBound() {
        return isBound;
    }

    @Override
    public S getService() throws ServiceNotBoundException {
        if (isAvailable) {
            // The service is available and can be safely returned
            return service;
        } else {
            // We have no current access to the service
            throw new ServiceNotBoundException();
        }
    };

    @Override
    public void register(IServiceListener<S> listener) {
        // Add the listener to the list of listeners
        listeners.add(listener);
        // If the service is available already, notify now
        if (isAvailable) {
            listener.onServiceAvailable(service);
        }
    }

    @Override
    public void unregister(IServiceListener<S> listener) {
        // Remove the listener from the list
        listeners.remove(listener);
    }

    /**
     * Notify all the listeners that the service is now available
     */
    private void notifyListeners() {
        // Read through the listeners list
        for (IServiceListener<S> listener : listeners) {
            // Notify each
            listener.onServiceAvailable(service);
        }
    }
}
