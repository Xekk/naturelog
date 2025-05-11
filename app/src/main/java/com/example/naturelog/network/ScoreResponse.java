package com.example.naturelog.network;

import java.util.List;

public class ScoreResponse {
    public List<Result> results;

    public static class Result {
        public Taxon taxon;
    }

    public static class Taxon {
        public String name;
        public String preferred_common_name;
        public String wikipedia_summary;
    }
}
