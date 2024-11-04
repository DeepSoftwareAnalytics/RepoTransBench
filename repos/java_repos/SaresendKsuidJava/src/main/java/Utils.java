import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class Utils {
    public static List<Ksuid> sortKSUID(List<Ksuid> ksuidList) {
        ksuidList.sort(Comparator.comparingLong(Ksuid::getTimestamp));
        return ksuidList;
    }
}
