import com.habbashx.annotation.InjectPrefix;
import com.habbashx.annotation.InjectProperty;
import com.habbashx.injector.PropertyInjector;

import java.io.File;

public class Testing {

    public static void main(String[] args) {


        PropertyInjector injector = new PropertyInjector(new File("src/test/java/settings.properties"));
        Settings settings = new Settings();

        injector.inject(settings);

        System.out.println(settings.getUsername());

    }
}

class Settings {
    @InjectProperty("username")
    private String username;

    public String getUsername() {
        return username;
    }
}