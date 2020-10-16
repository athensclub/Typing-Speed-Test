package athensclub.speedtype;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * Resource file handler
 */
public final class Loader {

    private Loader(){
    }

    public static String[] loadWords(){
        try(BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Loader.class.getClassLoader().getResourceAsStream("words.txt"))))){
            return br.lines().toArray(String[]::new);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
