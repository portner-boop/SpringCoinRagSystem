package trainding.springcoinragsystem.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Article {

    private String title;

    private String link;

    private String text;

    private List<Chunk> chunks;
}
