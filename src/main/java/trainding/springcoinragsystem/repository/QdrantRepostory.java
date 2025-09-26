package trainding.springcoinragsystem.repository;


import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Repository;
import trainding.springcoinragsystem.entity.Article;
import trainding.springcoinragsystem.entity.Chunk;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class QdrantRepostory {

    private final VectorStore vectorStore;




}
