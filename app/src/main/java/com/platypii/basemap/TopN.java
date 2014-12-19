package com.platypii.basemap;

import java.util.ArrayList;

// A class to store topN results
class TopN {
    private int n = 1;
    ArrayList<ASRRecord> records = new ArrayList<>();

    public TopN(int n) {
        this.n = n;
    }
    public void add(ASRRecord record) {
        int index = indexOf(record.height);
        if(index < n) {
            records.add(index, record);
        }
        while(records.size() > n) {
            records.remove(records.size() - 1);
        }
    }
    private int indexOf(double score) {
        for(int i = 0; i < records.size(); i++) {
            if(score > records.get(i).height) {
                return i;
            }
        }
        return records.size();
    }
    public int size() {
        return records.size();
    }
    public String toString() {
        String output = "{";
        for(int i = 0; i < records.size(); i++) {
            output += records.get(i) + ", ";
        }
        output += "}";
        return output;
    }
}
