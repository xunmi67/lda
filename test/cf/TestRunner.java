package cf;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Created by found on 1/27/16.
 */
public class TestRunner {
    public static void main(String[]a){
        Result res = JUnitCore.runClasses(TestItemcf.class);
        for(Failure fail : res.getFailures()){
            System.out.println(fail.toString());
        }
    }
}
