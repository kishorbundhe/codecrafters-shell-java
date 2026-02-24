package commands;

public class Test123 {

    public static void main(String[] args) {
        String options = "'   hello world   '";
        if (options.startsWith("'")&& options.endsWith("'")) {
            options = options.replaceAll("^'|'$", "");
            System.out.println("options: " + options);
        } else {
            options = options.replace("\s+", " ").trim();
            System.out.println(options);
        }
    }
}
