package trainding.springcoinragsystem.embedding;

public class StringPreparing {

    public static String cleanText(String text) {
        return text.replaceAll("[^а-яА-ЯA-Za-z0-9\\s.!?;:]", "")
                .replaceAll("\\s+", " ")
                .toLowerCase()
                .trim();
    }
}
