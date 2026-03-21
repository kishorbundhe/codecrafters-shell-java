import org.jline.reader.impl.DefaultParser;

public class DisableEscapingChars extends DefaultParser {
    @Override
    public boolean isEscapeChar(char ch) {
        // Don't treat backslash as an escape character
        return false;
    }
}