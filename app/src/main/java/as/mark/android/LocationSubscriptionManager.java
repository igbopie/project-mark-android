package as.mark.android;

import com.google.android.gms.location.LocationListener;

/**
 * Created by igbopie on 10/10/14.
 */
public interface LocationSubscriptionManager {


    public void subscribeLocationUpdates(LocationListener ll);

    public void removeSubscriptionLocationUpdates(LocationListener ll);
}
