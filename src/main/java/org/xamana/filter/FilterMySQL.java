/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.xamana.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.xamana.dhf.FOP.FragmentationOperationsMongo;
import org.xamana.dhf.FOP.FragmentationOperationsMySQL;
import org.xamana.dhf.model.Operation;

/**
 *
 * @author abryn
 */
public class FilterMySQL extends Filter implements Serializable {

    public FilterMySQL(String site) {
        super(site);
    }

    @Override
    public List<Operation> determineOperations(List<String> lines) throws Exception {
        this.operations = new ArrayList<>();
        for (String line : lines) {

            if (line.contains("select")) {
                joinOperations(select(line));
            }

            if (line.contains("update")) {
                joinOperations(update(line));
            }

            if (line.contains("delete")) {
                joinOperations(delete(line));
            }

            if (line.contains("insert")) {
                joinOperations(insert(line));
            }
        }
        this.addTuples(this.buildPredicates());
        return operations;
    }

    @Override
    protected List<Operation> select(String row) {
        return buildItems("READ", 1, row);
    }

    @Override
    protected List<Operation> update(String row) {
        return buildItems("UPDATE", 3, row);
    }

    @Override
    protected List<Operation> delete(String row) {
        return buildItems("DELETE", 2, row);
    }

    @Override
    protected List<Operation> insert(String row) {
        return buildItems("CREATE", 2, row);
    }

    private List<Operation> buildItems(String type, int value, String row) {
        List<Operation> items = new ArrayList<>();
        List<String> predicates = new ArrayList<>();
        pattern = Pattern.compile("WHERE(.*?);");
        match = pattern.matcher(row);
        if (match.find()) {
            predicates.addAll(this.buildPredicates(match.group(1)));
            tuples = 1;
            if (tuples > 0) {
                pattern = Pattern.compile(regexSite);
                match = pattern.matcher(row);
                match.find();
                predicates.forEach(p -> {
                    Operation item = new Operation(type, p, value, tuples, match.group(1), !site.contains(match.group(1)));
                    items.add(item);
                });
            }
        }
        return items;
    }

    protected List<String> buildPredicates(String predicates) {
        return Arrays.asList(predicates.split("AND"));
    }

    protected Map<String, Long> buildPredicates() {
        Map<String, Long> predicates = new HashMap<>();
        this.operations.forEach(op -> {
            predicates.put(op.getPredicate(), 0L);
        });
        return predicates;
    }

    private void addTuples(Map<String, Long> predicates) throws Exception {
        ((FragmentationOperationsMySQL) this.FO).getTuplesByPredicate(predicates);
        this.operations.forEach(item -> {
            item.setTuples(predicates.get(item.getPredicate()));
        });

    }

}
