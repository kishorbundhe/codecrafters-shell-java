import org.jline.reader.impl.DefaultParser;

public class WindowsPathAwareParser extends DefaultParser {
    @Override
    public boolean isEscapeChar(char ch) {
        // Don't treat backslash as an escape character
        return false;
    }
}