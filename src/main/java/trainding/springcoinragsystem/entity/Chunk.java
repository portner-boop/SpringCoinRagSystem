package trainding.springcoinragsystem.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Chunk {

    private String id;

    private int chunkIndex;

    private String text;

    private List<Float> embedding;
}
