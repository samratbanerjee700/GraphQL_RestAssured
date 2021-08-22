package com.queries;

import lombok.Data;

@Data
public class GrahpQLQueries {
    private String query;
    private Object variables;

    public Object getVariables() {
        return variables;
    }

    public void setVariables(Object variables) {
        this.variables = variables;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
